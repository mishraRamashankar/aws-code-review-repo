package com.wtc.pureit.api.helper;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;

@Slf4j
public class SqsMessageExtractor {

    public static JsonNode extract(SQSMessage sqsMessage) {
        String sqsMessageBody = sqsMessage.getBody();
        log.info("Processing the message: {}", sqsMessageBody);

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
        JsonNode messageBody = null;
        try {
            //<editor-fold desc="masking sqs message body">
            // mask double quote at starting
            if (sqsMessageBody.charAt(0) == '\"') {
                log.info("Removing double quote from message body at index 0: {}", sqsMessageBody);
                sqsMessageBody = sqsMessageBody.substring(1, sqsMessageBody.length() - 1);
                log.info("Removed double quote from message body: {}", sqsMessageBody);
            }
            // mask double quote at bottom
            if (sqsMessageBody.charAt(sqsMessageBody.length() - 1) == '\"') {
                log.info("Removing double quote from message body at index {}: {}", sqsMessageBody.length() - 1, sqsMessageBody);
                sqsMessageBody = sqsMessageBody.substring(0, sqsMessageBody.length() - 1);
                log.info("Removed double quote from message body: {}", sqsMessageBody);
            }
            //</editor-fold>

            messageBody = objectMapper.readTree(StringEscapeUtils.unescapeJson(sqsMessageBody));
            log.info("SQS body: {}", messageBody);
        } catch (Exception e) {
            log.error("Exception: ", e);
        }
        return messageBody;
    }
}
