package com.wtc.pureit.data.convertor;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelToJsonConvertor {

    public static String convert(File inputFile) throws IOException {
        List<Map<?, ?>> data = readObjectsFromCsv(inputFile);
        return writeAsJson(data);
    }

    public static String convert(InputStream inputStream) throws IOException {
        List<Map<?, ?>> data = readObjectsFromCsv(inputStream);
        return writeAsJson(data);
    }

    public static List<Map<?, ?>> readObjectsFromCsv(File file) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(file);

        return mappingIterator.readAll();
    }

    public static List<Map<?, ?>> readObjectsFromCsv(InputStream inputStream) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(inputStream);

        return mappingIterator.readAll();
    }

    public static String writeAsJson(List<Map<?, ?>> data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Convert object to JSON string and pretty print
        return mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(data);
    }

}
