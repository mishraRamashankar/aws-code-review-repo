package com.wtc.pureit.api.helper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.wtc.pureit.config.AwsConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class S3Reader {

    private final AwsConfig awsConfig;
    private final String bucketName;
    private final String fileName;

    public S3Reader(String bucketName, String fileName) {
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.awsConfig = new AwsConfig();
    }

    public String _getS3Records() throws IOException {
        S3Object object = getS3().getObject(bucketName, fileName);
        S3ObjectInputStream s3Stream = object.getObjectContent();

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        MappingIterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class).with(csvSchema).readValues(new InputStreamReader(s3Stream, StringUtils.UTF8));

        // We extract a list from the mapping Iterator
        List<Map<String, String>> readObjectsFromCsv = iterator.readAll();

        return new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(readObjectsFromCsv);
    }

    public String getS3Records() throws IOException, CsvValidationException {
        log.info("aws access key: {}", awsConfig.getAccessKeyId());
        log.info("aws secret access key: {}", awsConfig.getSecretAccessKey());
        log.info("aws region: {}", awsConfig.getRegion());

        List<Map<?, ?>> records = new ArrayList<>();
        try (CSVReaderHeaderAware reader = getReader()) {
            Map<?, ?> values;
            while ((values = reader.readMap()) != null) {
                records.add(values);
            }
        }

        log.info("Records size: {}", records.size());

        // Convert object to JSON string and pretty print
        return new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(records);
    }

    private CSVReaderHeaderAware getReader() {
        CSVParser parser = new CSVParserBuilder().build();
        S3Object object = getS3().getObject(bucketName, fileName);
        InputStreamReader br = new InputStreamReader(object.getObjectContent());

        return (CSVReaderHeaderAware) new CSVReaderHeaderAwareBuilder(br)
                .withSkipLines(0)
                .withCSVParser(parser)
                .withVerifyReader(true)
                .withMultilineLimit(0)
                .build();
    }

    public InputStreamReader getInputStream(){
        S3Object object = getS3().getObject(bucketName, fileName);
        return new InputStreamReader(object.getObjectContent());
    }

    private AmazonS3 getS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsConfig.getAccessKeyId(),
                        awsConfig.getSecretAccessKey())))
                .withRegion(Regions.fromName(awsConfig.getRegion()))
                .build();
    }
}
