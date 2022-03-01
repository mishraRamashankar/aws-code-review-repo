package com.wtc.pureit.api.handler.s3;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wtc.pureit.api.helper.*;
import com.wtc.pureit.config.MagentoConfig;
import com.wtc.pureit.data.dto.ItemCategory;
import com.wtc.pureit.data.dto.request.ItemCreationRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ItemCreationHandler extends LambdaInMemoryCache
        implements RequestHandler<S3Event, String> {

    private String bucketName;
    private String fileName;
    private boolean isFileRead;

    @SneakyThrows
    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        log.info("ItemCreationHandler S3 event got trigger: {}", s3Event);

        MagentoConfig magentoConfig = new MagentoConfig();
        log.info("Magento Configuration: {}", magentoConfig);

        log.info("Lambda function is invoked: Processing the uploads.........{}", s3Event);

        bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
        fileName = s3Event.getRecords().get(0).getS3().getObject().getKey();

        fileName = fileName.replaceAll("\\+", " ");

        if (s3Event.getRecords().isEmpty()) {
            log.info("{} records present in the {} which is uploaded in bucket {}", s3Event.getRecords().size(), fileName, bucketName);
            return null;
        }

        log.info("File - {} uploaded into {} bucket at {}", fileName, bucketName, s3Event.getRecords().get(0).getEventTime());

        S3Reader s3Reader = new S3Reader(bucketName, fileName);
        String csvJson = s3Reader.getS3Records();

        log.info("CSV Json: {}", csvJson);

        EventRetry eventRetry = new EventRetry();
        RetryTemplate retryTemplate = eventRetry.retryTemplate();

        String message = retryTemplate.execute((RetryCallback<String, Exception>) retryContext -> {
            try {
                if (Objects.nonNull(csvJson) && !csvJson.isEmpty()) {
                    List<ItemCreationRequest> itemCreationRequests = (List<ItemCreationRequest>) retrieve(fileName);

                    if (Objects.isNull(itemCreationRequests)) {
                        itemCreationRequests = ItemCreationRequest.prepareItemReq(csvJson);
                    }

                    if (itemCreationRequests.isEmpty()) {
                        throw new IllegalStateException("Unable to read the file: " + fileName);
                    } else {
                        save(fileName, itemCreationRequests);
                    }

                    itemCreationRequests = filterItemAsPerUL(itemCreationRequests, null);

                    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);

                    List<String> request = new ArrayList<>();
                    itemCreationRequests.forEach(itemCreationRequest -> {
                        try {
                            request.add(mapper.writeValueAsString(itemCreationRequest));
                        } catch (JsonProcessingException e) {
                            log.error("Item master parsing exception: ", e);
                        }
                    });

                    log.info("ItemCreationRequests size: {}", itemCreationRequests.size());
                    log.info("Request body size: {}", request.size());

                    String requestBody = Arrays.toString(request.toArray());
                    log.info("Request body: {}", requestBody);

                    log.info("Going to persist Item/Product at PureIt");

                    MagentoCaller magentoCaller = new MagentoCaller();
                    String response = magentoCaller.call(magentoConfig.getBulkItemCreationMagentoUrl(), requestBody,
                            ApiMapper.POST, null);

                    boolean isItemCreationSuccess = !StringUtils.isEmpty(response);
                    log.info(response);

                    return isItemCreationSuccess
                            ? "Successfully persisted item/product information"
                            : "Either for all/some item/product couldn't persist at m2";
                } else {
                    throw new IllegalArgumentException("There is no information present in the incoming file or unable to convert csv to json.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error reading contents of the file";
            }
        });

        clearInMemory();

        return message;
    }

    public List<ItemCreationRequest> filterItemAsPerUL(List<ItemCreationRequest> itemCreationRequests, String ulRequiredCbuCode) {
        List<ItemCreationRequest> deviceItems = itemCreationRequests.stream().filter(itemCreationRequest ->
                        itemCreationRequest.getItemCategory().equalsIgnoreCase(ItemCategory.DEVICE.name()))
                .collect(Collectors.toList());

        deviceItems = deviceItems.stream().filter(itemCreationRequest -> (
                        itemCreationRequest.getCbuCode().equalsIgnoreCase("WUCU100")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WUCU2R1")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WDRJ200")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WDRJ4R1")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WPNT6R1")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WPNT7R1")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WPNT5R1")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCUX400")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCOR500")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCUX200")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCUR500")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCRX200")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCRO600")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCRA500")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCRO500")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WPUV500")
                                || itemCreationRequest.getCbuCode().equalsIgnoreCase("WCUV400")))
                .collect(Collectors.toList());

        List<ItemCreationRequest> finalizedItemCreationReq = new ArrayList<>(deviceItems);

        // components
        List<ItemCreationRequest> componentsItems = itemCreationRequests.stream().filter(itemCreationRequest ->
                        itemCreationRequest.getItemCategory().equalsIgnoreCase(ItemCategory.COMPONENTS.name()))
                .collect(Collectors.toList());
//        finalizedItemCreationReq.addAll(getDeviceRelatedProduct(deviceItems, componentsItems));

        // consumable
        List<ItemCreationRequest> consumableItems = itemCreationRequests.stream().filter(itemCreationRequest ->
                        itemCreationRequest.getItemCategory().equalsIgnoreCase(ItemCategory.CONSUMABLE.name()))
                .collect(Collectors.toList());
        finalizedItemCreationReq.addAll(getDeviceRelatedProduct(deviceItems, consumableItems));

        // contracts
        List<ItemCreationRequest> contractsItems = itemCreationRequests.stream().filter(itemCreationRequest ->
                        itemCreationRequest.getItemCategory().equalsIgnoreCase(ItemCategory.CONTRACTS.name()))
                .collect(Collectors.toList());
        finalizedItemCreationReq.addAll(getDeviceRelatedProduct(deviceItems, contractsItems));


        return finalizedItemCreationReq;
    }

    private List<ItemCreationRequest> getDeviceRelatedProduct(List<ItemCreationRequest> deviceItems, List<ItemCreationRequest> deviceRelatedItems) {
        List<ItemCreationRequest> finalizedItemCreationReq = new ArrayList<>();
        List<String> uniqueSku = new ArrayList<>();
        for (ItemCreationRequest deviceRelatedItem : deviceRelatedItems) {
            for (ItemCreationRequest deviceItem : deviceItems) {
                String deviceCategoryName = deviceItem.getDeviceCategoryName();
                if (!deviceCategoryName.isEmpty()) {
                    if (Objects.nonNull(deviceRelatedItem.getDeviceCategoryName()) && !deviceRelatedItem.getDeviceCategoryName().isEmpty()) {
                        String[] itemDeviceCategoryArr = deviceRelatedItem.getItemDeviceCategory().split(",");
                        for (int i = 0; i < itemDeviceCategoryArr.length; i++) {
                            if (itemDeviceCategoryArr[i].trim().equalsIgnoreCase(deviceCategoryName.trim())) {
                                if (!uniqueSku.contains(deviceRelatedItem.getSku())) {
                                    finalizedItemCreationReq.add(deviceRelatedItem);
                                    uniqueSku.add(deviceRelatedItem.getSku());
                                }
                            }
                        }
                    }
                }
//                log.info("Device {} has related product as {}", deviceItem.getSku(),);
            }
        }

        return finalizedItemCreationReq;
    }
}
