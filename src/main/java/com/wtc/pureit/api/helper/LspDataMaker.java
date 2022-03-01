package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.wtc.pureit.data.dto.helper.LspInventoryLinker;
import com.wtc.pureit.data.dto.request.LspCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LspDataMaker {

    /**
     * convert incoming json into  list of {@link LspCreationRequest } object
     *
     * @param json
     * @return
     */
    public List<LspCreationRequest> prepareLspReq(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<LspCreationRequest> lspCreationRequests = objectMapper.readValue(json,
                    TypeFactory.defaultInstance()
                            .constructCollectionType(List.class, LspCreationRequest.class));

            return lspCreationRequests.stream()
                    .filter(distinctByKeys(LspCreationRequest::getSourceCode,
                            LspCreationRequest::getZipCode,
                            LspCreationRequest::getAddress1))
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * distinct filtering process
     *
     * @param keyExtractors
     * @param <T>
     * @return
     */
    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());

            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

    /**
     * @param lspCreationRequests {@link List}
     * @return
     */
    public List<LspInventoryLinker> lspLinkageBuilder(List<LspCreationRequest> lspCreationRequests) {
        List<LspInventoryLinker> lspInventoryLinkers = new ArrayList<>();
        int counter = 1;

        for (LspCreationRequest lspCreationRequest : lspCreationRequests) {
            lspInventoryLinkers.add(LspInventoryLinker.builder()
                    .sourceCode(lspCreationRequest.getSourceCode())
                    .stockId("2")
                    .priority(counter)
                    .build());
            counter++;
        }

        return lspInventoryLinkers;
    }

}
