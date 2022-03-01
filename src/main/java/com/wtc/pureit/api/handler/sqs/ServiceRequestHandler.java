package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.data.dto.EventDetail;
import com.wtc.pureit.data.dto.request.ComplaintCreationRequest;
import com.wtc.pureit.data.dto.request.ComplaintCreationRequest.UpdateNetsuiteComplaintDetailsToMagentoReq;
import com.wtc.pureit.data.dto.response.ComplaintCreationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ServiceRequestHandler {

    private final NetSuiteCaller netSuiteCaller;

    private final MagentoCaller magentoCaller;

    private final ObjectMapper objectMapper;

    public ServiceRequestHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
        this.magentoCaller = new MagentoCaller();
        this.objectMapper = new ObjectMapper();
    }

    public String process(JsonNode messageBody) throws Exception {
        log.info("Message body: {}", messageBody.textValue());

        String resp = "";
        try {
            JsonNode complaintIdNode = messageBody.get("complaintId");

            Integer complaint = Integer.parseInt(complaintIdNode.textValue());
            log.info("Complaint-id: {}", complaint);

            EventDetail eventDetail = fetchComplaint(complaint);
            log.info("Complaint details: {}", eventDetail);

            EventRetry eventRetry = new EventRetry();
            RetryTemplate retryTemplate = eventRetry.retryTemplate();

            resp = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
                ComplaintCreationResponse complaintStatusResponse = null;
                if (Objects.nonNull(eventDetail)) {
                    JsonNode complaintDetailNode = eventDetail.getBodyAsNode();
                    if (Objects.isNull(complaintDetailNode)) {
                        log.error("Complaint data is null");
                        throw new IllegalArgumentException("Complaint data is null");
                    }
                    log.info("Complaint node: {}", complaintDetailNode);

                    ComplaintCreationRequest complaintCreationRequest = objectMapper.treeToValue(complaintDetailNode,
                            ComplaintCreationRequest.class);
                    log.info("Complaint creation req body: {}", objectMapper.writeValueAsString(complaintCreationRequest));

                    complaintStatusResponse = netSuiteCaller.registerComplaint(complaintCreationRequest);
                }

                String response = null;
                if (Objects.nonNull(complaintStatusResponse)) {
                    UpdateNetsuiteComplaintDetailsToMagentoReq complaintDetailsToMagentoReq =
                            UpdateNetsuiteComplaintDetailsToMagentoReq.builder()
                                    .netsuiteComplaintId(complaintStatusResponse.getData())
                                    .build();

                    String requestBody = objectMapper.writer().withRootName("serviceRequest")
                            .writeValueAsString(complaintDetailsToMagentoReq);

                    response = magentoCaller.call(magentoCaller.getMagentoConfig().getUpdateComplaintIdToMagentoUrl(),
                            requestBody, ApiMapper.POST, null);
                }
                return response;
            });
        } catch (Exception e) {
            log.error("Exception: ", e);
        }

        return resp;
    }

    public EventDetail fetchComplaint(Integer magentoComplaintId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", magentoComplaintId);

            String complaintDetail = magentoCaller.call(magentoCaller.getMagentoConfig().getFetchComplaintDetailUrl(),
                    null, ApiMapper.GET, paramMap);

            return EventDetail.builder()
                    .bodyAsNode(objectMapper.readTree(complaintDetail)).build();
        } catch (Exception e) {
            throw new RuntimeException("Error file fetching the order details: " + e);
        }
    }


}
