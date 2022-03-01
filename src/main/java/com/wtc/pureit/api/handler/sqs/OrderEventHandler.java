package com.wtc.pureit.api.handler.sqs;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.wtc.pureit.api.helper.SqsMessageExtractor;
import com.wtc.pureit.data.dto.M2EventCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class OrderEventHandler implements RequestHandler<SQSEvent, String> {

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("SQS event handler: Processing the message: {}", sqsEvent);

        for (SQSEvent.SQSMessage sqsMessage : sqsEvent.getRecords()) {
            JsonNode messageBody = SqsMessageExtractor.extract(sqsMessage);

            JsonNode eventShortCode = messageBody.get("eventShortcode");
            if (Objects.isNull(eventShortCode)) {
                log.error("Event short node not present so returning back.... :(");
                return null;
            }

            log.info("Event code: {}", eventShortCode);
            M2EventCode m2EventCode = M2EventCode.valueOf(eventShortCode.textValue());

            try {
                switch (m2EventCode) {
                    case MCME_ORC_036:
                        OrderCreationEventHandler orderFunctionHandler = new OrderCreationEventHandler();
                        return orderFunctionHandler.process(messageBody);
                    case MCME_ORC_037:
                        OrderStatusEventHandler orderStatusEventHandler = new OrderStatusEventHandler();
                        return orderStatusEventHandler.process(messageBody);
                    case MCME_ORC_038:
                        OrderCancellationEventHandler orderCancellationEventHandler = new OrderCancellationEventHandler();
                        return orderCancellationEventHandler.process(messageBody);
                }
            } catch (Exception e) {
                log.error("Exception observed: ", e);
            }
        }
        return "Event Node not found";
    }
}
