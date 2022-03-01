package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.wtc.pureit.api.handler.s3.ItemCreationHandler;
import com.wtc.pureit.data.dto.request.ItemCreationRequest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Ignore
public class ItemCreationTest {

    public String loadFile() {
        try {
            List<Map<?, ?>> records = new ArrayList<>();
            try (CSVReaderHeaderAware reader = getReader()) {
                Map<?, ?> values;
                while ((values = reader.readMap()) != null) {
                    records.add(values);
                }
            }

            // Convert object to JSON string and pretty print
            return new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(records);
        } catch (Exception e) {
            return null;
        }

    }

    private CSVReaderHeaderAware getReader() {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream inputStream = classloader.getResourceAsStream("ItemMasterSupportTeamResults510.csv");

            return (CSVReaderHeaderAware) new CSVReaderHeaderAwareBuilder(
                    new InputStreamReader(inputStream))
                    .withSkipLines(0)
                    .withCSVParser(new CSVParserBuilder().build())
                    .withVerifyReader(true)
                    .withMultilineLimit(0)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testFileFilterMechanism() throws JsonProcessingException, FileNotFoundException {
        ItemCreationHandler itemCreationHandler = new ItemCreationHandler();
        List<ItemCreationRequest> itemCreationRequests = ItemCreationRequest.prepareItemReq(loadFile());

        itemCreationRequests = itemCreationHandler.filterItemAsPerUL(itemCreationRequests, null);

        PrintWriter writer = new PrintWriter(
                ResourceUtils.getFile("src/test/resources/output"));
        writer.println(new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(itemCreationRequests));
        writer.close();
    }
}
