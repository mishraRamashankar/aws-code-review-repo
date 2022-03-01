package com.wtc.pureit.api.handler.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.api.helper.ApiMapper;
import com.wtc.pureit.api.helper.LspDataMaker;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.S3Reader;
import com.wtc.pureit.data.dto.helper.LspInventoryLinker;
import com.wtc.pureit.data.dto.request.LspCreationRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class LspManagementHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        log.info("LspManagementHandler | S3 event got trigger: {}", s3Event);

        log.info("Lambda function is invoked: Processing the uploads.........{}", s3Event);

        String bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String fileName = s3Event.getRecords().get(0).getS3().getObject().getKey();

        fileName = fileName.replaceAll("\\+", " ");

        if (s3Event.getRecords().isEmpty()) {
            log.info("{} records present in the {} which is uploaded in bucket {}", s3Event.getRecords().size(), fileName, bucketName);
            return null;
        }

        log.info("File - {} uploaded into {} bucket at {}", fileName, bucketName, s3Event.getRecords().get(0).getEventTime());

        boolean isLspCreationSuccess = false;
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
        LspDataMaker lspDataMaker = new LspDataMaker();

        try {
            S3Reader s3Reader = new S3Reader(bucketName, fileName);
            String csvJson = s3Reader.getS3Records();
            log.info("CSV Json: {}", objectMapper.writeValueAsString(csvJson));

            MagentoCaller magentoCaller = new MagentoCaller();

            if (Objects.nonNull(csvJson) && !csvJson.isEmpty()) {
                log.info("Going to persist LspCreationRequest at PureIt");

                //<editor-fold desc="prepare and send LSP zipcode wise information to Magento">
                List<LspCreationRequest> lspCreationRequests = lspDataMaker.prepareLspReq(csvJson);
                log.info("LspCreationRequest size: {}", lspCreationRequests.size());

                final List<String> lspCreationReqBody = new ArrayList<>();
                lspCreationRequests.forEach(lspCreationRequest -> {
                    try {
                        lspCreationReqBody.add(objectMapper.writeValueAsString(lspCreationRequest));
                    } catch (JsonProcessingException e) {
                        log.error("LspCreationRequest master parsing exception: ", e);
                    }
                });
                log.info("Lsp request body: {}", Arrays.toString(lspCreationReqBody.toArray()));
                String response = magentoCaller.call(magentoCaller.getMagentoConfig().getLspCreationUrl(),
                        Arrays.toString(lspCreationReqBody.toArray()), ApiMapper.POST, null);
                log.info("Lsp creation response: {}", response);

                isLspCreationSuccess = response.contains("Error");
                //</editor-fold>

                //<editor-fold desc="build linkage b/w lsp and item">
                List<LspInventoryLinker> lspInventoryLinkers = lspDataMaker.lspLinkageBuilder(lspCreationRequests);
                String lspInventoryBody = objectMapper.writer()
                        .withRootName("links")
                        .writeValueAsString(lspInventoryLinkers);
                String lspInventoryReqBody = "[".concat(lspInventoryBody).concat("]");
                log.info("LspInventory linkage: {}", lspInventoryBody);

                response = magentoCaller.call(magentoCaller.getMagentoConfig().getLspStockLinkageUrl(),
                        lspInventoryReqBody, ApiMapper.POST, null);
                log.info("Lsp Inventory linkage response: {}", response);
                //</editor-fold>

                isLspCreationSuccess = response.contains("Error");
            } else {
                throw new IllegalArgumentException("There is no information present in the incoming file or unable to convert csv to json.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading contents of the file";
        }

        return isLspCreationSuccess
                ? "Successfully persisted LSP information"
                : "Either for all/some LSP couldn't persist at m2";
    }

    private String createLsp(MagentoCaller magentoCaller, List<String> request, String url) {
        String requestBody = Arrays.toString(request.toArray());
        log.info("Request body: {}", requestBody);

        return magentoCaller.call(url, requestBody, ApiMapper.POST, null);
    }

}
