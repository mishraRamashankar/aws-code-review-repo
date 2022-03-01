package com.wtc.pureit.api.handler.gateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.data.dto.response.ComplaintStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.io.InputStream;

@Slf4j
public class ComplaintStatusEventHandler implements RequestHandler<InputStream, String> {

    private final NetSuiteCaller netSuiteCaller;

    public ComplaintStatusEventHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
    }

    @Override
    public String handleRequest(InputStream inputStream, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode node = objectMapper.readTree(inputStream);
            String requestBody = objectMapper.writeValueAsString(node);
            log.info("InputStream: {}", requestBody);

            EventRetry eventRetry = new EventRetry();
            RetryTemplate retryTemplate = eventRetry.retryTemplate();

            ComplaintStatusResponse complaintStatusResponse = retryTemplate.execute((RetryCallback<ComplaintStatusResponse, Exception>) retryContext -> {
                ComplaintStatusResponse interimComplaintStatusUpdateRes = new ComplaintStatusResponse();

                MagentoCaller magentoCaller = new MagentoCaller();
                String response = magentoCaller.call(magentoCaller.getMagentoConfig().getComplaintStatusUpdateUrl(),
                        requestBody, ApiMapper.POST, null);

                interimComplaintStatusUpdateRes.setMessage(response);

                log.info("Complaint Update status response: {}", response);

                return interimComplaintStatusUpdateRes;
            });

            String formattedResponse = objectMapper.writeValueAsString(complaintStatusResponse);
            log.info("OrderUpdateStatusResponse: {}", formattedResponse);

            return formattedResponse;
        } catch (Exception e) {
            log.info("Error: ", e);
            return e.getMessage();
        }
    }
}
