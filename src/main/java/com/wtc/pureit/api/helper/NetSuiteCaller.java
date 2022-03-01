package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.config.NetSuiteConfig;
import com.wtc.pureit.data.dto.helper.NetSuiteOauthHeaderBuilder;
import com.wtc.pureit.data.dto.request.*;
import com.wtc.pureit.data.dto.response.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Validated
@Slf4j
public class NetSuiteCaller {

    private final NetSuiteConfig netSuiteConfig;
    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient;

    public NetSuiteCaller() {
        this.netSuiteConfig = new NetSuiteConfig();
        this.objectMapper = new ObjectMapper();
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();

        log.info("Netsuite config: {}", netSuiteConfig);
    }

    @NotNull
    private Response getResponse(Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    /**
     * validate success family response code
     *
     * @param responseCode Response code value
     * @return Status
     */
    private boolean isResponseSuccess(int responseCode) {
        return (Objects.equals(responseCode, HttpStatus.SC_OK) ||
                Objects.equals(responseCode, HttpStatus.SC_ACCEPTED) ||
                Objects.equals(responseCode, HttpStatus.SC_CREATED) ||
                Objects.equals(responseCode, HttpStatus.SC_NO_CONTENT) ||
                Objects.equals(responseCode, HttpStatus.SC_PARTIAL_CONTENT) ||
                Objects.equals(responseCode, HttpStatus.SC_RESET_CONTENT));
    }

    /**
     * @return
     */
    @NotNull
    private String getOauthHeader(String script, String verb, Map<String, String> param) {
        return new NetSuiteOauthHeaderBuilder()
                .withMethod(verb)
                .withUri(netSuiteConfig.getNetSuiteUri())
                .withRealm(netSuiteConfig.getRealm())
                .withDeploy(netSuiteConfig.getDeploy())
                .withScript(script)
                .withTokenSecret(netSuiteConfig.getTokenSecret())
                .withConsumerSecret(netSuiteConfig.getConsumerSecret())
                .withSignatureMethod(netSuiteConfig.getSignatureMethod())
                .withParameter("oauth_consumer_key", netSuiteConfig.getConsumerKey())
                .withParameter("oauth_token", netSuiteConfig.getToken())
                .withParameter("oauth_signature_method", netSuiteConfig.getSignatureMethod())
                .withParameter("oauth_version", netSuiteConfig.getVersion())
                .build();
    }

    /**
     * Build M2 request
     *
     * @param netSuiteScriptType {@link NetSuiteScriptType}
     * @param postBody           Request body
     * @param verb               Verb
     * @param param              Parameter map
     * @return
     * @throws JsonProcessingException
     */
    @NotNull
    private Request requestBuilder(NetSuiteScriptType netSuiteScriptType, Object postBody, String verb,
                                   Map<String, String> param) throws JsonProcessingException {

        //<editor-fold desc="prepare Netsuite auth header and script">
        log.info("Netsuite Script type: {}", netSuiteScriptType);

        String authHeader = "";
        String script = "";
        switch (netSuiteScriptType) {
            case LEAD:
                script = netSuiteConfig.getLeadCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case CUSTOMER_CREATE:
                script = netSuiteConfig.getCustomerCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case CUSTOMER_STATUS:
                script = netSuiteConfig.getComplaintStatusScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case ORDER_CREATION:
                script = netSuiteConfig.getOrderCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case ORDER_STATUS:
                script = netSuiteConfig.getOrderStatusScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case ORDER_CANCELLATION:
                script = netSuiteConfig.getOrderCancellationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case SALES:
                script = netSuiteConfig.getSalesOrderCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case INVOICE:
                script = netSuiteConfig.getInvoiceCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case COMPLAINT_CREATE:
                script = netSuiteConfig.getComplaintCreationScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case UPDATE_CUSTOMER_ADDRESS:
                script = netSuiteConfig.getCustomerAddressUpdateScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
            case EXISTING_CUSTOMER:
                script = netSuiteConfig.getCustomerDetailScript();
                authHeader = getOauthHeader(script, verb, param);
                break;
        }
        //</editor-fold>

        //<editor-fold desc="prepare request urlBuilder">
        StringBuilder urlBuilder = new StringBuilder(netSuiteConfig.getNetSuiteUri()
                .concat("?script=").concat(script)
                .concat("&deploy=").concat(netSuiteConfig.getDeploy()));

        if (Objects.nonNull(param)) {
            for (Map.Entry<String, String> entry : param.entrySet()) {
                urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        log.info("Netsuite url for {} is {}", netSuiteScriptType.name(), urlBuilder.toString());
        //</editor-fold>

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Authorization", authHeader)
                .addHeader("cookie", netSuiteConfig.getCookie())
                .addHeader("mediaType", netSuiteConfig.getMediaType());

        if (Objects.nonNull(postBody)) {
            RequestBody requestBody = RequestBody
                    .create(objectMapper.writeValueAsString(postBody),
                            MediaType.parse(netSuiteConfig.getMediaType()));

            if (ApiMapper.POST.equalsIgnoreCase(verb)) {
                requestBuilder.post(requestBody);
            } else if (ApiMapper.PUT.equalsIgnoreCase(verb)) {
                requestBuilder.put(requestBody);
            }
        }
        //</editor-fold>

        return requestBuilder.build();
    }

    private void throwErrorResponse(Response response) {
        String msg = "Something went wrong... :( \n status code: " +
                response.code() + " " + response.message();
        log.info(msg);
        throw new RuntimeException(msg);
    }

    /**
     * @param leadCreationReq {@link LeadCreationRequest}
     * @return LeadCreationResponse
     */
    public LeadCreationResponse createLead(LeadCreationRequest leadCreationReq) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.LEAD, leadCreationReq, ApiMapper.POST, null);

            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Ok-http response code: {}", response.code());
            log.info("Ok-http response message: {}", response.message());

            LeadCreationResponse leadCreationResponse = new LeadCreationResponse();
            if (isResponseSuccess(response.code())) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                JsonNode data = responseBody.get("data");
                if (Objects.nonNull(data) && data.get(0).size() != 0) {
                    JsonNode netSuiteCustomer = data.get(0).get("customer_id");
                    if (Objects.nonNull(netSuiteCustomer) && !netSuiteCustomer.textValue().isEmpty()) {
                        String[] arr = netSuiteCustomer.textValue().split(" ");

                        leadCreationResponse.setCustomerId(arr[0]);
                        leadCreationResponse.setDeviceCategory(data.get(0).get("deviceCategory").textValue());
                        leadCreationResponse.setResponseCode(responseBody.get("responseCode").textValue());
                    }
                }
                leadCreationResponse.setMessage(responseBody.get("message").textValue());
            }
            return leadCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param orderCreationReq {@link OrderCreationRequest}
     * @return RfsCreationResponse
     */
    public RfsCreationResponse createOrder(OrderCreationRequest orderCreationReq) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.ORDER_CREATION, orderCreationReq, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);

            RfsCreationResponse rfsCreationResponse = new RfsCreationResponse();
            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                log.info("Response body: {}", responseBody);

                rfsCreationResponse.setMessage(responseBody.get("message").textValue());
                rfsCreationResponse.setResponseCode(responseBody.get("responseCode").textValue());

                JsonNode data = responseBody.get("data");
                if (Objects.nonNull(data) && data.get(0).size() != 0) {
                    JsonNode transactionId = data.get(0).get("transaction_id");
                    if (Objects.nonNull(transactionId) && !(transactionId.textValue().isEmpty())) {
                        rfsCreationResponse.setTransactionId(transactionId.textValue());
                        log.info("RfsCreationResponse: {}", rfsCreationResponse);
                    } else {
                        throw new IllegalArgumentException(responseBody.get("message").toString());
                    }
                } else {
                    throw new IllegalArgumentException(responseBody.get("message").toString());
                }
            }
            return rfsCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param customerCreationReq {@link CustomerCreationRequest}
     * @return CustomerCreationResponse
     */
    public CustomerCreationResponse createCustomer(CustomerCreationRequest customerCreationReq) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.CUSTOMER_CREATE, customerCreationReq, ApiMapper.POST, null);
            log.info("Request: {}", request);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);

            JsonNode responseBody = objectMapper.readTree(response.body().string());
            log.info("Response body: {}", responseBody);

            CustomerCreationResponse customerCreationResponse = CustomerCreationResponse.builder().build();
            customerCreationResponse.setMessage(responseBody.get("message").textValue());

            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode dataNode = responseBody.get("data");

                if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                    JsonNode netSuiteCustomer = dataNode.get(0).get("customer_id");
                    if (Objects.nonNull(netSuiteCustomer) && !netSuiteCustomer.textValue().isEmpty()) {
                        String[] customerInfoArr = netSuiteCustomer.textValue().split(" ");
                        customerCreationResponse.setCustomerCustomAttributes(CustomerCreationResponse
                                .customerCustomAttributes(customerInfoArr[0]));

                        if (Objects.nonNull(dataNode.get(1).get("addressArray"))) {
                            JsonNode addressArrayNode = dataNode.get(1).get("addressArray");
                            customerCreationResponse.setShippingAddressId(addressArrayNode.get(0).get("id").textValue());
                        }
                    }
                }
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            log.info("Customer creation response: {}", customerCreationResponse);
            return customerCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            log.error("Exception: ", e);
            return null;
        }
    }

    /**
     * @param salesOrderCreationRequest {@link SalesOrderCreationRequest}
     * @return SalesOrderCreationResponse
     */
    public SalesOrderCreationResponse createSales(SalesOrderCreationRequest salesOrderCreationRequest) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.SALES, salesOrderCreationRequest, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            SalesOrderCreationResponse salesOrderCreationResponse = new SalesOrderCreationResponse();

            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                JsonNode statusNode = responseBody.get("status");
                JsonNode messageNode = responseBody.get("message");

                salesOrderCreationResponse.setStatus(statusNode.textValue());
                salesOrderCreationResponse.setMessage(messageNode.textValue());

                if ("Success".equalsIgnoreCase(statusNode.textValue())) {
                    JsonNode dataNode = responseBody.get("dataNode");
                    if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                        if (!dataNode.textValue().isEmpty()) {
                            salesOrderCreationResponse.setData(dataNode.textValue());
                        }
                    }
                }
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            return salesOrderCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param invoiceCreationRequest {@link InvoiceCreationRequest}
     * @return InvoiceCreationResponse
     */
    public InvoiceCreationResponse createInvoice(InvoiceCreationRequest invoiceCreationRequest) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.INVOICE, invoiceCreationRequest, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            InvoiceCreationResponse invoiceCreationResponse = new InvoiceCreationResponse();
            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());

                invoiceCreationResponse.setStatus(responseBody.get("status").textValue());
                invoiceCreationResponse.setMessage(responseBody.get("message").textValue());

                if ("Success".equalsIgnoreCase(invoiceCreationResponse.getStatus())) {
                    JsonNode dataNode = responseBody.get("data");
                    if (Objects.nonNull(dataNode) && !dataNode.textValue().isEmpty()) {
                        log.info("Invoice url: {}", dataNode);
                        invoiceCreationResponse.setData(dataNode.textValue());
                    }
                }
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            return invoiceCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param complaintCreationRequest {@link ComplaintCreationRequest}
     * @return ComplaintCreationResponse
     */
    public ComplaintCreationResponse registerComplaint(ComplaintCreationRequest complaintCreationRequest) {
        try {
            //<editor-fold desc="prepare request">
            log.info("ComplaintCreationRequest: {}", objectMapper.writeValueAsString(complaintCreationRequest));
            Request request = requestBuilder(NetSuiteScriptType.COMPLAINT_CREATE, complaintCreationRequest, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Ok-Http response: {}", response);

            ComplaintCreationResponse complaintCreationResponse = new ComplaintCreationResponse();
            if (isResponseSuccess(response.code())) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                log.info("Response body: {}", response);

                complaintCreationResponse.setMessage(responseBody.get("message").textValue());

                JsonNode dataNode = responseBody.get("data");
                if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                    JsonNode complaintId = dataNode.get(0).get("complaint_id");
                    if (Objects.nonNull(complaintId) && !(complaintId.textValue().isEmpty())) {
                        complaintCreationResponse.setData(complaintId.textValue());
                        log.info("ComplaintCreationResponse: {}", complaintCreationResponse);
                    } else {
                        throw new IllegalArgumentException(responseBody.get("message").toString());
                    }
                }
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            return complaintCreationResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return ComplaintStatusResponse
     */
    public ComplaintStatusResponse getComplaintStatus(Map<String, String> param) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.COMPLAINT_STATUS, null, ApiMapper.GET, param);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            ComplaintStatusResponse complaintStatusResponse = new ComplaintStatusResponse();
            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());

                complaintStatusResponse.setStatus(responseBody.get("status").textValue());
                complaintStatusResponse.setMessage(responseBody.get("message").textValue());

                if ("Success".equalsIgnoreCase(complaintStatusResponse.getStatus())) {
                    JsonNode dataNode = responseBody.get("dataNode");
                    if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                        if (!dataNode.textValue().isEmpty()) {
                            complaintStatusResponse.setData(dataNode.textValue());
                        }
                    }
                }
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            return complaintStatusResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return OrderStatusResponse
     */
    public OrderStatusResponse getOrderStatus(Map<String, String> param) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.ORDER_STATUS, null, ApiMapper.GET, param);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            OrderStatusResponse orderStatusResponse = new OrderStatusResponse();
            if (Objects.equals(response.code(), HttpStatus.SC_OK)) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());

                orderStatusResponse.setStatus(responseBody.get("status").textValue());
                orderStatusResponse.setMessage(responseBody.get("message").textValue());

                if ("Success".equalsIgnoreCase(orderStatusResponse.getStatus())) {
                    JsonNode dataNode = responseBody.get("dataNode");
                    if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                        if (!dataNode.textValue().isEmpty()) {
                            orderStatusResponse.setData(dataNode.textValue());
                        }
                    }
                }
            }
            return orderStatusResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param orderCancellationRequest {@link String}
     * @return OrderCancellationResponse
     */
    public OrderCancellationResponse cancelOrder(OrderCancellationRequest orderCancellationRequest) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.ORDER_CANCELLATION, orderCancellationRequest, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            OrderCancellationResponse orderCancellationResponse = new OrderCancellationResponse();
            if (isResponseSuccess(response.code())) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());

//                orderCancellationResponse.setStatus(responseBody.get("status").textValue());
//                orderCancellationResponse.setMessage(responseBody.get("message").textValue());
            }
            return orderCancellationResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param updateCustomerMasterAddressRequest {@link UpdateCustomerMasterAddressRequest}
     * @return UpdateCustomerAddressResponse
     */
    public UpdateCustomerAddressResponse updateCustomerMasterAddress(UpdateCustomerMasterAddressRequest updateCustomerMasterAddressRequest) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.UPDATE_CUSTOMER_ADDRESS, updateCustomerMasterAddressRequest, ApiMapper.POST, null);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response: {}", response);

            JsonNode responseBody = objectMapper.readTree(response.body().string());
            log.info("Response body: {}", responseBody);

            UpdateCustomerAddressResponse updateCustomerAddressResponse = new UpdateCustomerAddressResponse();
            updateCustomerAddressResponse.setStatus(responseBody.get("status").textValue());
            updateCustomerAddressResponse.setMessage(responseBody.get("message").textValue());

            if (isResponseSuccess(response.code())) {
                if ("Success".equalsIgnoreCase(updateCustomerAddressResponse.getStatus())) {
                    JsonNode dataNode = responseBody.get("data");
                    if (Objects.nonNull(dataNode) && dataNode.get(0).size() != 0) {
                        updateCustomerAddressResponse.setShippingAddressId(dataNode.get(dataNode.size() - 2).get("id").textValue());
                        updateCustomerAddressResponse.setBillingAddressId(dataNode.get(dataNode.size() - 1).get("id").textValue());
                    }
                }
            }
            return updateCustomerAddressResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param param Parameter map
     * @return
     */
    public CustomerDetailResponse isExistingCustomer(Map<String, String> param) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(NetSuiteScriptType.EXISTING_CUSTOMER, null, ApiMapper.GET, param);
            log.info("Request: {}", request);
            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Response body: {}", response);

            CustomerDetailResponse customerDetailResponse = new CustomerDetailResponse();

            if (isResponseSuccess(response.code())) {
                JsonNode responseBody = objectMapper.readTree(response.body().string());
                log.info("Response body as string: {}", responseBody.textValue());

                customerDetailResponse.setStatus(getValue(responseBody, "status"));
                customerDetailResponse.setMessage(getValue(responseBody, "message"));

                if ("Success".equalsIgnoreCase(customerDetailResponse.getStatus())) {
                    JsonNode dataNode = responseBody.get("data");
                    if (Objects.nonNull(dataNode) && !dataNode.textValue().isEmpty()) {
                        customerDetailResponse.setData(dataNode.textValue());
                    }
                }
            }
            return customerDetailResponse;
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue() : "";
    }

}
