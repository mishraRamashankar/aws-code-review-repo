package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.LambdaInMemoryCache;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.api.helper.OrderGenerationHelper;
import com.wtc.pureit.data.dto.OrderDetail;
import com.wtc.pureit.data.dto.request.OrderCancellationRequest;
import com.wtc.pureit.data.dto.response.OrderCancellationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.Objects;

/**
 * This class handles the process to cancel the order and
 * provide details to both M2 & NetSuite
 */
@Slf4j
public class OrderCancellationEventHandler extends LambdaInMemoryCache {

    private final NetSuiteCaller netSuiteCaller;

    private final OrderGenerationHelper orderGenerationHelper;

    public OrderCancellationEventHandler() {
        this.netSuiteCaller = new NetSuiteCaller();
        this.orderGenerationHelper = new OrderGenerationHelper();
    }

    public String process(JsonNode messageBody) throws Exception {
        log.info("Message body: {}", messageBody.textValue());

        EventRetry eventRetry = new EventRetry();
        RetryTemplate retryTemplate = eventRetry.retryTemplate();

        String message = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);

            String orderId = Objects.nonNull(messageBody.get("orderId")) ?
                    messageBody.get("orderId").textValue() : null;

            String rfsId = Objects.nonNull(messageBody.get("rfsId")) ?
                    messageBody.get("rfsId").textValue() : null;

            if (rfsId.isEmpty()) {
                rfsId = getRfsIdBasedOnOrderId(orderId);
            }

            if (Objects.isNull(rfsId)) {
                throw new NoSuchFieldException("Rfs-id is not present in the message");
            }

            OrderCancellationRequest orderCancellationRequest = OrderCancellationRequest
                    .builder().rfsId(rfsId).build();
            log.info("Order cancel req: {}", objectMapper.writeValueAsString(orderCancellationRequest));
            OrderCancellationResponse orderCancellationResponse = netSuiteCaller.cancelOrder(orderCancellationRequest);

            return objectMapper.writeValueAsString(orderCancellationResponse);
        });

        clearInMemory();

        return message;
    }

    /**
     * Get RFS-ID based on Order-Id
     *
     * @param orderId
     * @return
     */
    public String getRfsIdBasedOnOrderId(String orderId) {
        OrderDetail orderDetail = orderGenerationHelper.fetchOrderDetails(orderId);
        JsonNode m2OrderDetailNode = orderDetail.getBodyAsNode();
        if (Objects.isNull(m2OrderDetailNode)) {
            log.error("Order data is null");
            throw new IllegalArgumentException("Order data is null");
        }
        log.info("M2 order node: {}", m2OrderDetailNode);

        JsonNode rfsIdNode = m2OrderDetailNode.path("extension_attributes").path("rfs_id");
        log.info("Rfs-Id node: {}", rfsIdNode);

        return Objects.nonNull(rfsIdNode) ?
                rfsIdNode.textValue() : rfsIdNode.toString();
    }
}
