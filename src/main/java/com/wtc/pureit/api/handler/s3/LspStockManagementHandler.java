package com.wtc.pureit.api.handler.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.EventRetry;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.S3Reader;
import com.wtc.pureit.data.dto.request.LspStockMapperRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LspStockManagementHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        log.info("LspStockManagementHandler | S3 event got trigger: {}", s3Event);

        log.info("Lambda function is invoked: Processing the uploads.........{}", s3Event);

        String bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String fileName = s3Event.getRecords().get(0).getS3().getObject().getKey();

        fileName = fileName.replaceAll("\\+", " ");

        if (s3Event.getRecords().isEmpty()) {
            log.info("{} records present in the {} which is uploaded in bucket {}", s3Event.getRecords().size(), fileName, bucketName);
            return null;
        }

        log.info("File - {} uploaded into {} bucket at {}", fileName, bucketName, s3Event.getRecords().get(0).getEventTime());

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);

        try {
            S3Reader s3Reader = new S3Reader(bucketName, fileName);
            String csvJson = s3Reader.getS3Records();
            log.info("CSV Json: {}", objectMapper.writeValueAsString(csvJson));

            MagentoCaller magentoCaller = new MagentoCaller();

            if (Objects.nonNull(csvJson) && !csvJson.isEmpty()) {
                log.info("Going to persist LspStockMapperRequest at PureIt");

                List<LspStockMapperRequest> lspStockMapperRequests = Objects.requireNonNull(LspStockMapperRequest.prepareReq(csvJson));
                log.info("LSP Stock, without filter size: {}", lspStockMapperRequests.size());

                List<LspStockMapperRequest> inventoryMapperRequests = lspStockMapperRequests.stream()
                        .filter(lspStockMapperRequest -> !lspStockMapperRequest.getSourceCode().isEmpty()
                                && "LSP Direct".equalsIgnoreCase(lspStockMapperRequest.getSwzType())
                                && lspStockMapperRequest.getIsActive())
                        .collect(Collectors.toList());
                log.info("LSP Stock, with filter-1 size: {}", inventoryMapperRequests.size());

                final List<String> bundleLspStockMapperReqBody = new ArrayList<>();
                List<LspStockMapperRequest> interimBundleItemCollectors = new ArrayList<>();

                int counter = 1;
                int lspStockBulkLimitVal = Integer.parseInt(magentoCaller.getMagentoConfig().getLspStockBulkLimitVal());

                Iterator<LspStockMapperRequest> iterator = inventoryMapperRequests.iterator();

                while (iterator.hasNext()) {
                    interimBundleItemCollectors.add(iterator.next());

                    if (counter == lspStockBulkLimitVal) {
                        bundleLspStockMapperReqBody.add(objectMapper.writer().withRootName("sourceItems")
                                .writeValueAsString(interimBundleItemCollectors.toArray()));

                        interimBundleItemCollectors = new ArrayList<>();
                        counter = 1;
                    } else {
                        counter++;
                    }

                    iterator.remove();
                }
                if (!interimBundleItemCollectors.isEmpty()) {
                    bundleLspStockMapperReqBody.add(objectMapper.writer().withRootName("sourceItems")
                            .writeValueAsString(interimBundleItemCollectors.toArray()));
                }

                log.info("LSP Stock, in bundle size:: {}", bundleLspStockMapperReqBody.size());

                String requestBody = Arrays.toString(bundleLspStockMapperReqBody.toArray());
                log.info("Lsp-stock mapper request body: {}", requestBody);

                //retry logic
                EventRetry eventRetry = new EventRetry();
                RetryTemplate retryTemplate = eventRetry.retryTemplate();

                return retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
                    String response = magentoCaller.call(magentoCaller.getMagentoConfig().getLspStockMapperUrl(), requestBody, ApiMapper.POST, null);
                    log.info("Lsp-stock mapper response: {}", response);

                    return response.contains("Error")
                            ? "Successfully persisted LSP-Inventory information"
                            : "Either for all/some LSP-Inventory couldn't persist at m2";
                });
            } else {
                throw new IllegalArgumentException("There is no information present in the incoming file or unable to convert csv to json.");
            }
        } catch (Exception e) {
            log.error("", e);
            return "Error reading contents of the file";
        }
    }
}
