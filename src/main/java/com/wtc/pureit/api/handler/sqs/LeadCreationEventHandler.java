package com.wtc.pureit.api.handler.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.api.helper.SqsMessageExtractor;
import com.wtc.pureit.data.dto.LeadDetail;
import com.wtc.pureit.data.dto.request.LeadCreationRequest;
import com.wtc.pureit.data.dto.response.LeadCreationResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class LeadCreationEventHandler implements RequestHandler<SQSEvent, String> {

    private final NetSuiteCaller netSuiteCaller;

    private final MagentoCaller magentoCaller;

    private final ObjectMapper objectMapper;

    public LeadCreationEventHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
        this.magentoCaller = new MagentoCaller();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("SQS event handler: {}", sqsEvent);
        String resp = "";

        for (SQSEvent.SQSMessage sqsMessage : sqsEvent.getRecords()) {
            JsonNode messageBody = SqsMessageExtractor.extract(sqsMessage);

            log.info("Message body: {}", messageBody.textValue());

            try {
                JsonNode leadIdNode = messageBody.get("leadId");
                if (Objects.isNull(leadIdNode)) {
                    String msg = "lead-id is not present !!";
                    log.error(msg);
                    return msg;
                }

                Integer leadId = Integer.parseInt(leadIdNode.textValue());
                log.info("Lead-id: {}", leadId);

                LeadDetail leadDetail = fetchLead(leadId);
                log.info("Lead details: {}", leadDetail);
                LeadCreationResponse leadCreationResponse = null;
                if (Objects.nonNull(leadDetail)) {
                    JsonNode m2LeadDetailNode = leadDetail.getBodyAsNode();
                    if (Objects.isNull(m2LeadDetailNode)) {
                        log.error("Lead data is null");
                        throw new IllegalArgumentException("Lead data is null");
                    }
                    log.info("M2 lead node: {}", m2LeadDetailNode);

                    LeadCreationRequest leadCreationRequest = LeadCreationRequest.createLead(m2LeadDetailNode);
                    log.info("Lead creation req body: {}", objectMapper.writeValueAsString(leadCreationRequest));

                    leadCreationResponse = netSuiteCaller.createLead(leadCreationRequest);
                }

                resp = Objects.nonNull(leadCreationResponse) ? (leadCreationResponse.getMessage().equalsIgnoreCase("Record added") ?
                        leadCreationResponse.getMessage() : null) : null;

                if(Objects.isNull(resp)){

                }
            } catch (Exception e) {
                log.error("Exception: ", e);
            }
        }
        return resp;
    }

    public LeadDetail fetchLead(Integer leadId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", leadId);

            String leadDetail = magentoCaller.call(magentoCaller.getMagentoConfig().getFetchLeadDetailUrl(),
                    null, ApiMapper.GET, paramMap);

            return LeadDetail.builder()
                    .bodyAsNode(objectMapper.readTree(leadDetail)).build();
        } catch (Exception e) {
            throw new RuntimeException("Error file fetching the order details: " + e);
        }
    }

}
