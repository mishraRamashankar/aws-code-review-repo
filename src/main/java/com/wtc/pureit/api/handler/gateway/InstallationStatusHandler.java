package com.wtc.pureit.api.handler.gateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.data.dto.request.InstallationStatusUpdateRequest;
import com.wtc.pureit.data.dto.response.InstallationStatusResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class InstallationStatusHandler implements RequestHandler<InputStream, String> {


    @SneakyThrows
    @Override
    public String handleRequest(InputStream inputStream, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();

        String message = "Success";
        String requestBody = "";
        JsonNode node = null;
        try {
            node = objectMapper.readTree(inputStream);
            requestBody = objectMapper.writeValueAsString(node);
            log.info("InputStream: {}", requestBody);
        } catch (JsonProcessingException e) {
            log.info("Error: ", e);
            message = e.getMessage();
            return message;
        }

        InstallationStatusUpdateRequest updateRequest = InstallationStatusUpdateRequest.builder()
                .rfsId(node.get("rfsId").textValue())
                .itemInternalId(getValue(node, "itemInternalId"))
                .complaintTitle(getValue(node, "complaintTitle"))
                .installationId(getValue(node, "installationId"))
                .netsuiteCustomerId(getValue(node, "netsuiteCustomerId"))
                .installationStatus(getValue(node, "installationStatus"))
                .installationDate(getValue(node, "installationDate").replaceAll("\\/", "-"))
                .installationTime(getValue(node, "installationTime"))
                .serialNo(getValue(node, "serialNo"))
                .qtyPos(getValue(node, "qtyPos"))
                .build();

        MagentoCaller magentoCaller = new MagentoCaller();

        EventRetry eventRetry = new EventRetry();
        RetryTemplate retryTemplate = eventRetry.retryTemplate();

        InstallationStatusResponse installationStatusResponse = retryTemplate.execute((RetryCallback<InstallationStatusResponse, Exception>) retryContext -> {
            String installationUpdateRequestBody = objectMapper.writer().withRootName("serviceRequest")
                    .writeValueAsString(updateRequest);
            log.info("Input update request: {}", installationUpdateRequestBody);

            String response = magentoCaller.call(magentoCaller.getMagentoConfig().getInstallationStatusUrl(),
                    installationUpdateRequestBody, ApiMapper.POST, null);
            log.info("M2 Response: {}", response);

            return InstallationStatusResponse.builder()
                    .message(response)
                    .build();
        });

        String formattedResponse = objectMapper.writeValueAsString(installationStatusResponse);
        log.info("InstallationStatusResponse: {}", formattedResponse);

        return message;
    }

    private static String getValue(JsonNode jsonNode, String key) {
        return Objects.nonNull(jsonNode.get(key)) ? jsonNode.get(key).textValue() : "";
    }
}
