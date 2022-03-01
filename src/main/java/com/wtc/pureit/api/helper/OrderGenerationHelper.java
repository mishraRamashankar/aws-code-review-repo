package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.data.dto.OrderDetail;
import com.wtc.pureit.data.dto.request.*;
import com.wtc.pureit.data.dto.response.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class OrderGenerationHelper {

    private final ObjectMapper objectMapper;

    private final NetSuiteCaller netSuiteCaller;

    private final MagentoCaller magentoCaller;

    public OrderGenerationHelper() {
        this.netSuiteCaller = new NetSuiteCaller();
        this.magentoCaller = new MagentoCaller();
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    /**
     * send {@link CustomerCreationResponse} to the m2
     *
     * @param customerCreationResponse {@link CustomerCreationResponse}
     * @return
     */
    public String sendCustomerRespToMagento(CustomerCreationResponse customerCreationResponse) {
        try {
            log.info("Generated customer details: {}", customerCreationResponse);

            String sendCustomerResponseUrl = magentoCaller.getMagentoConfig().getSendCustomerResponseUrl();
            sendCustomerResponseUrl = sendCustomerResponseUrl.replaceAll("/id", "/" + customerCreationResponse.getCustomerId().toString());

            String requestBody = objectMapper.writeValueAsString(customerCreationResponse);
            log.info("Update customer netsuite id at magento | Request body : {}", requestBody);

            return magentoCaller.call(sendCustomerResponseUrl, requestBody, ApiMapper.PUT, null);
        } catch (Exception e) {
            log.error("Unable to send customer creation response to Magento", e);
            return null;
        }
    }

    /**
     * Create Customer from the incoming param
     *
     * @param m2OrderDetailNode Order details which is saved at M2 end
     * @return
     * @throws Exception
     */
    public CustomerCreationResponse createCustomer(JsonNode m2OrderDetailNode) {
        try {
            CustomerCreationRequest customerCreationRequest = CustomerCreationRequest.createCustomer(m2OrderDetailNode);
            log.info("CustomerCreationRequest {}", objectMapper.writeValueAsString(customerCreationRequest));

            return netSuiteCaller.createCustomer(customerCreationRequest);
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param orderDetailNode {@link JsonNode}
     * @return CustomerDetailResponse
     */
    public CustomerDetailResponse isExistingCustomer(JsonNode orderDetailNode) {
        JsonNode shippingAssignmentsNode = orderDetailNode.path("extension_attributes").path("shipping_assignments");
        if (Objects.isNull(shippingAssignmentsNode)) {
            throw new IllegalArgumentException("Shipping Assignment not present for order id" + orderDetailNode.get("entity_id"));
        }

        JsonNode shippingAddressNode = shippingAssignmentsNode.get(0).path("shipping").path("address");

        Map<String, String> param = new HashMap<>();
        param.put("phoneNumber", shippingAddressNode.get("telephone").textValue());
        return netSuiteCaller.isExistingCustomer(param);
    }

    /**
     * fetch specific order details
     *
     * @param orderId Order Id
     * @return
     */
    public OrderDetail fetchOrderDetails(String orderId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", orderId);

            String orderDetail = magentoCaller.call(magentoCaller.getMagentoConfig().getFetchOrderUrl(), null,
                    ApiMapper.GET, paramMap);

            return OrderDetail.builder()
                    .bodyAsNode(objectMapper.readTree(orderDetail)).build();
        } catch (Exception e) {
            throw new RuntimeException("Error file fetching the order details: " + e);
        }
    }

    /**
     * create rfs order record
     *
     * @param orderCreationRequest {@link OrderCreationRequest}
     * @return
     */
    public RfsCreationResponse generateRfsOrder(OrderCreationRequest orderCreationRequest) {
        return netSuiteCaller.createOrder(orderCreationRequest);
    }

    /**
     * create sales order for the generated rfs
     *
     * @param salesOrderCreationRequest {@link SalesOrderCreationRequest}
     * @return
     */
    public SalesOrderCreationResponse generateSalesOrder(SalesOrderCreationRequest salesOrderCreationRequest) {
        return netSuiteCaller.createSales(salesOrderCreationRequest);
    }

    /**
     * create invoice for the generated rfs
     *
     * @param invoiceCreationRequest {@link InvoiceCreationRequest}
     * @return
     */
    public InvoiceCreationResponse generateInvoice(InvoiceCreationRequest invoiceCreationRequest) {
        return netSuiteCaller.createInvoice(invoiceCreationRequest);
    }

    /**
     * Register Billing & Shipping address.
     *
     * @param orderDetailNode    Order Detail
     * @param netsuiteCustomerId Netsuite Customer Id
     * @return
     */
    public UpdateCustomerAddressResponse registerAddress(JsonNode orderDetailNode, String netsuiteCustomerId) {
        UpdateCustomerMasterAddressRequest updateCustomerMasterAddressRequest = UpdateCustomerMasterAddressRequest.updateCustomer(orderDetailNode,
                netsuiteCustomerId);
        try {
            log.info("Update customer master adr req: {}", objectMapper.writeValueAsString(updateCustomerMasterAddressRequest));
        } catch (JsonProcessingException e) {
            log.error("Error while logging update master req: ", e);
        }

        return netSuiteCaller.updateCustomerMasterAddress(updateCustomerMasterAddressRequest);
    }

    /**
     * @param updateOrderToMagento {@link UpdateOrderToMagento}
     * @return
     */
    public String sendRfsToMagento(UpdateOrderToMagento updateOrderToMagento) {
        try {
            String sendOrderUpdateStatusUrl = magentoCaller.getMagentoConfig().getOrderUpdateStatusUrl();

            String requestBody = objectMapper.writeValueAsString(updateOrderToMagento);
            log.info("Update order to magento : {}", updateOrderToMagento);

            return magentoCaller.call(sendOrderUpdateStatusUrl, requestBody, ApiMapper.POST, null);
        } catch (Exception e) {
            log.error("Unable to send customer creation response to Magento", e);
            return null;
        }
    }

    /**
     * get current order status
     *
     * @param orderId
     * @return
     * @throws InterruptedException
     */
    public boolean getOrderCurrentStatus(String orderId) throws InterruptedException {
        boolean isOrderPending = false;
        int orderRetryVal = Integer.parseInt(magentoCaller.getMagentoConfig().getOrderStatusRetryCount());
        int waitTime = Integer.parseInt(magentoCaller.getMagentoConfig().getThreadWaitTime());
        int counter = 0;

        log.info("Going to fetch the order status of {}", orderId);

        do {
            OrderDetail orderDetail = fetchOrderDetails(orderId);
            JsonNode m2OrderDetailNode = orderDetail.getBodyAsNode();
            String status = m2OrderDetailNode.get("status").textValue();
            isOrderPending = "pending".equalsIgnoreCase(status);

            if (counter == orderRetryVal) {
                break;
            } else {
                Thread.sleep(waitTime);
                counter++;
            }
            log.info(">>>>>>>>>>-------{}-------<<<<<<<<<<<", counter);
        } while (isOrderPending);

        return !isOrderPending;
    }
}
