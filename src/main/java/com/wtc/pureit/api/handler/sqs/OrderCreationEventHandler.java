package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.LambdaInMemoryCache;
import com.wtc.pureit.api.helper.OrderGenerationHelper;
import com.wtc.pureit.data.dto.OrderDetail;
import com.wtc.pureit.data.dto.request.InvoiceCreationRequest;
import com.wtc.pureit.data.dto.request.OrderCreationRequest;
import com.wtc.pureit.data.dto.request.SalesOrderCreationRequest;
import com.wtc.pureit.data.dto.request.UpdateOrderToMagento;
import com.wtc.pureit.data.dto.response.*;
import com.wtc.pureit.data.dto.response.CustomerCreationResponse.CustomerCustomAttributes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class handles the process to create the order and
 * provide details to both M2 & NetSuite
 */
@Slf4j
public class OrderCreationEventHandler extends LambdaInMemoryCache {

    @Setter
    @Getter
    private final OrderGenerationHelper orderGenerationHelper;

    private final ObjectMapper objectMapper;

    public OrderCreationEventHandler() {
        this.orderGenerationHelper = new OrderGenerationHelper();
        this.objectMapper = new ObjectMapper();
    }

    public String process(JsonNode messageBody) throws Exception {
        log.info("Message body: {}", messageBody.textValue());

        JsonNode orderIdNode = messageBody.get("orderId");
        if (Objects.isNull(orderIdNode)) {
            return "Order Id is not present in the sqs msg body";
        }

        String orderId = orderIdNode.textValue();
        log.info("Order-id: {}", orderId);

        EventRetry eventRetry = new EventRetry();
        RetryTemplate retryTemplate = eventRetry.retryTemplate();

        String message = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
            // step-1: fetch order information
            try {
                String magentoOrderKey = "magentoOrderDetail:" + orderId;
                OrderDetail orderDetail = (OrderDetail) retrieve(magentoOrderKey);
                if (Objects.isNull(orderDetail)) {
                    orderDetail = orderGenerationHelper.fetchOrderDetails(orderId);
                    log.info("Order details: {}", orderDetail);
                }

                if (Objects.nonNull(orderDetail)) {
                    save(magentoOrderKey, orderDetail);

                    JsonNode m2OrderDetailNode = orderDetail.getBodyAsNode();
                    if (Objects.isNull(m2OrderDetailNode)) {
                        log.error("Order data is null");
                        throw new IllegalArgumentException("Order data is null");
                    }
                    log.info("M2 order node: {}", m2OrderDetailNode);

                    JsonNode orderStatusNode = m2OrderDetailNode.get("status");
                    log.info("Order id {} status is {}, which is not proper, so waiting for sometime and will the check the status !!!",
                            orderId, orderStatusNode.textValue());
                    if ("pending".equalsIgnoreCase(orderStatusNode.textValue())) {
                        boolean canProceed = orderGenerationHelper.getOrderCurrentStatus(orderId);

                        if (!canProceed) {
                            log.info("Order id {} status is still {}, and we have tried to wait but still {}, so reverting back !!!",
                                    orderId, orderStatusNode.textValue(), orderStatusNode.textValue());
                            return null;
                        }
                    }

                    String netsuiteCustomerId = Optional.ofNullable(m2OrderDetailNode.get("netsuite_customer_id")).isPresent()
                            ? m2OrderDetailNode.get("netsuite_customer_id").toString() : null;
                    log.info("Netsuite customer-id: {}", netsuiteCustomerId);

                    if (Objects.isNull(m2OrderDetailNode.get("customer_id"))) {
                        String errorMsg = "Magento customer-id is null for order id " + orderId;
                        log.info(errorMsg);
                        throw new IllegalStateException(errorMsg);
                    }

                    Integer magentoCustomerId = Integer.parseInt(m2OrderDetailNode.get("customer_id").toString());
                    log.info("Magento customer-id: {}", magentoCustomerId);

                    // step-2: validate customer exist in NetSuite or not. If not then crete customer and send response to Magento
                    boolean isCustomerRegAtNetsuiteEnd = Objects.nonNull(netsuiteCustomerId);

                    if (!isCustomerRegAtNetsuiteEnd) {
//                        CustomerDetailResponse customerDetailResponse = orderGenerationHelper.isExistingCustomer(m2OrderDetailNode);
//                        if (Objects.isNull(customerDetailResponse.getData()) && !"Success".equalsIgnoreCase(customerDetailResponse.getStatus())) {
                        try {
                            CustomerCreationResponse customerCreationResponse = orderGenerationHelper.createCustomer(m2OrderDetailNode);
                            if (!Objects.nonNull(customerCreationResponse)) {
                                throw new IllegalArgumentException("Customer creation response is not proper/null");
                            }
                            customerCreationResponse.setCustomerId(magentoCustomerId);

                            String customerCreationResp = orderGenerationHelper.sendCustomerRespToMagento(customerCreationResponse);
                            isCustomerRegAtNetsuiteEnd = !customerCreationResp.isEmpty();

                            List<CustomerCustomAttributes> customerCustomAttributes = customerCreationResponse.getCustomerCustomAttributes();
                            if (!customerCustomAttributes.isEmpty()) {
                                netsuiteCustomerId = customerCustomAttributes.get(0).getValue();
                            }
                        } catch (Exception e) {
                            log.error("Exception: ", e);
                            throw new RuntimeException(e);
                        }
//                        }
                    }

                    if (isCustomerRegAtNetsuiteEnd) {
                        UpdateCustomerAddressResponse updateCustomerAddressResponse = (UpdateCustomerAddressResponse) retrieve(magentoCustomerId);
                        if (Objects.isNull(updateCustomerAddressResponse)) {
                            updateCustomerAddressResponse = orderGenerationHelper.registerAddress(m2OrderDetailNode,
                                    netsuiteCustomerId);
                            log.info("Updated customer multiple address: {}", objectMapper.writeValueAsString(updateCustomerAddressResponse));

                            save(magentoCustomerId, updateCustomerAddressResponse);
                        }

                        UpdateOrderToMagento updateOrderToMagento = proceedOrderCreation(m2OrderDetailNode, netsuiteCustomerId,
                                updateCustomerAddressResponse.getBillingAddressId(),
                                updateCustomerAddressResponse.getShippingAddressId(),
                                orderId);
                        if (Objects.nonNull(updateOrderToMagento)) {
                            updateOrderToMagento.setEntityId(orderId);
                            return orderGenerationHelper.sendRfsToMagento(updateOrderToMagento);
                        }
                    } else {
                        log.error("Customer not present, so not creating order");
                    }
                }
            } catch (Exception e) {
                log.error("Exception: ", e);
                throw new RuntimeException(e);
            }
            return null;
        });

        // clear in-memory
        clearInMemory();

        return message;
    }

    /**
     * proceed for order creation based on the incoming customer details.
     *
     * @param m2OrderDetailNode  JsonNode containing order details received from m2
     * @param netsuiteCustomerId NetSuite Customer Id
     */
    protected UpdateOrderToMagento proceedOrderCreation(JsonNode m2OrderDetailNode, String netsuiteCustomerId,
                                                        String billingAddressId, String shippingAddressId,
                                                        String orderId) {
        try {
            //<editor-fold desc="Step-3: create rfs-id">
            OrderCreationRequest orderCreationRequest = OrderCreationRequest.createOrder(m2OrderDetailNode,
                    netsuiteCustomerId, shippingAddressId);
            log.info("OrderCreationRequest: {}", objectMapper.writeValueAsString(orderCreationRequest));

            RfsCreationResponse rfsCreationResponse = (RfsCreationResponse) retrieve(orderId);
            if (Objects.isNull(rfsCreationResponse)) {
                rfsCreationResponse = orderGenerationHelper.generateRfsOrder(orderCreationRequest);
                log.info("Rfs creation response: {}", objectMapper.writeValueAsString(rfsCreationResponse));
            }
            //</editor-fold>

            if (Objects.nonNull(rfsCreationResponse)
                    && Objects.nonNull(rfsCreationResponse.getTransactionId())
                    && !rfsCreationResponse.getTransactionId().isEmpty()) {
                //save data in-memory
                save(orderId, rfsCreationResponse);

                //<editor-fold desc="Step-4: create sales order">
                SalesOrderCreationRequest salesOrderCreationRequest = SalesOrderCreationRequest.createReq(
                        rfsCreationResponse.getTransactionId(), shippingAddressId, billingAddressId);
                log.info("SalesOrderCreationRequest: {}", objectMapper.writeValueAsString(salesOrderCreationRequest));

                SalesOrderCreationResponse salesOrderCreationResponse = (SalesOrderCreationResponse) retrieve("sales:" + rfsCreationResponse.getTransactionId());
                if (Objects.isNull(salesOrderCreationResponse)) {
                    salesOrderCreationResponse = orderGenerationHelper.generateSalesOrder(salesOrderCreationRequest);
                    log.info("SalesOrderCreationResponse: {}", objectMapper.writeValueAsString(salesOrderCreationResponse));
                }
                //</editor-fold>

                //<editor-fold desc="Step-5: create invoice">
                if ("Success".equalsIgnoreCase(salesOrderCreationResponse.getStatus())) {
                    //save data in-memory
                    save("sales:" + rfsCreationResponse.getTransactionId(), salesOrderCreationResponse);

                    UpdateOrderToMagento updateOrderToMagento = (UpdateOrderToMagento) retrieve("invoice:" + rfsCreationResponse.getTransactionId());
                    if (Objects.isNull(updateOrderToMagento)) {
                        updateOrderToMagento = createInvoice(rfsCreationResponse);
                        log.info("Update Order to magento: {}", objectMapper.writeValueAsString(updateOrderToMagento));

                        updateOrderToMagento.setNetsuiteCustomerId(netsuiteCustomerId);

                        //save data in-memory
                        save("invoice:" + rfsCreationResponse.getTransactionId(), updateOrderToMagento);
                    }
                    return updateOrderToMagento.getIsInvoiceCreationSuccess() ? updateOrderToMagento : null;
                } else {
                    throw new IllegalStateException(salesOrderCreationResponse.getMessage());
                }
                //</editor-fold>
            }
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }
        return null;
    }

    /**
     * @param rfsCreationResponse {@link RfsCreationResponse}
     * @return
     */
    private UpdateOrderToMagento createInvoice(RfsCreationResponse rfsCreationResponse) {
        InvoiceCreationRequest invoiceCreationRequest = InvoiceCreationRequest.creationRequest(rfsCreationResponse.getTransactionId());
        try {
            log.info("InvoiceCreationRequest: {}", objectMapper.writeValueAsString(invoiceCreationRequest));
        } catch (JsonProcessingException e) {
            log.error("Error: ", e);
        }

        InvoiceCreationResponse invoiceCreationResponse = (InvoiceCreationResponse) retrieve("invoice:" + rfsCreationResponse.getTransactionId());
        if (Objects.isNull(invoiceCreationResponse)) {
            invoiceCreationResponse = orderGenerationHelper.generateInvoice(invoiceCreationRequest);
            log.info("InvoiceCreationResponse: {}", invoiceCreationResponse);
        }

        UpdateOrderToMagento updateOrderToMagento = UpdateOrderToMagento.builder().build();
        boolean isInvoiceCreated = "Success".equalsIgnoreCase(invoiceCreationResponse.getStatus());

        updateOrderToMagento.setIsInvoiceCreationSuccess(isInvoiceCreated);

        if (isInvoiceCreated) {
            //save data in-memory
            save("invoice:" + rfsCreationResponse.getTransactionId(), invoiceCreationResponse);

            updateOrderToMagento.setRfsId(rfsCreationResponse.getTransactionId());
            updateOrderToMagento.setInvoiceUrl(invoiceCreationResponse.getData());
        } else {
            throw new IllegalStateException(invoiceCreationResponse.getMessage());
        }
        return updateOrderToMagento;
    }

}
