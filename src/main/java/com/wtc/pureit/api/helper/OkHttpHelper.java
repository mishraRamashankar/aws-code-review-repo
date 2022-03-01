package com.wtc.pureit.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpHelper {

    private final OkHttpClient okHttpClient;

    private final String magnetoAccessToken = System.getenv("magnetoAccessToken");
    private final String mediaType = System.getenv("mediaType");

    public OkHttpHelper() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * @param object
     * @param externalUrl
     * @return
     * @throws IOException
     */
    public Response getResponse(Object object, String externalUrl) throws IOException {
        log.info("magento access token: {}", magnetoAccessToken);
        log.info("mediaType: {}", mediaType);

        RequestBody requestBody = createRequestBody(object);

        Request request = new Request.Builder()
                .url(externalUrl)
                .addHeader("Authorization", magnetoAccessToken)
                .addHeader("Content-Type", mediaType)
                .post(requestBody)
                .build();

        log.info(request.toString());
        Response execute = okHttpClient.newCall(request).execute();

        log.info(execute.body().string());
        return execute;
    }

    /**
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    private okhttp3.RequestBody createRequestBody(Object object) throws JsonProcessingException {
        return okhttp3.RequestBody
                .create(new ObjectMapper().writeValueAsString(object),
                        MediaType.parse(mediaType));
    }

}
