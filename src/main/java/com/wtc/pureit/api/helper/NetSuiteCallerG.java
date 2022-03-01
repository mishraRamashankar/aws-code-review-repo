package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.config.NetSuiteConfig;
import com.wtc.pureit.data.dto.helper.NetSuiteOauthHeaderBuilder;
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
public class NetSuiteCallerG {

    private NetSuiteConfig netSuiteConfig;
    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient;

    public NetSuiteCallerG() {
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

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Authorization", authHeader)
                .addHeader("Cookie", netSuiteConfig.getCookie())
                .addHeader("Content-Type", netSuiteConfig.getMediaType());

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
     * @param requestBody
     * @param netSuiteScriptType
     * @param verb
     * @param param
     * @return
     */
    public JsonNode call(Object requestBody, NetSuiteScriptType netSuiteScriptType, String verb, Map<String, String> param) {
        try {
            //<editor-fold desc="prepare request">
            Request request = requestBuilder(netSuiteScriptType, requestBody, verb, param);

            //</editor-fold>

            //<editor-fold desc="return response">
            Response response = getResponse(request);
            log.info("Ok-http response code: {}", response.code());
            log.info("Ok-http response message: {}", response.message());

            if (isResponseSuccess(response.code())) {
                return objectMapper.readTree(response.body().string());
            } else {
                //<editor-fold desc="Failure response">
                throwErrorResponse(response);
                //</editor-fold>
            }
            //</editor-fold>
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
