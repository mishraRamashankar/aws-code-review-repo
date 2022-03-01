package com.wtc.pureit.api.helper;

import com.wtc.pureit.config.MagentoConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
public class MagentoCaller {

    @Getter
    private final MagentoConfig magentoConfig;

    public MagentoCaller() {
        this.magentoConfig = new MagentoConfig();
    }

    /**
     * @param requestBody RequestBody
     * @param externalUrl M2 url
     * @param verb        GET/POST/....
     * @return Response
     */
    public String call(String externalUrl, final String requestBody,
                       final String verb, final Map<String, Object> params) {
        try {
            StringBuilder externalUrlBuilder = new StringBuilder(externalUrl);

            if (Objects.nonNull(params)) {
                log.info("Preparing url for GET function.....");

                params.forEach((key, value) -> {
                    if (externalUrlBuilder.toString().contains(key)) {
                        String externalUri = externalUrlBuilder.toString().replaceAll(key, value.toString());

                        externalUrlBuilder.setLength(0);
                        externalUrlBuilder.append(externalUri);

                        log.info("External url: {}", externalUrlBuilder);
                    }
                });
                log.info("Get url: {}", externalUrlBuilder);
            }

            HttpURLConnection urlConnection = setUpConnection(externalUrlBuilder.toString(), verb);

            //<editor-fold desc="Post content">
            if (Objects.isNull(params)) {
                log.info("Going to post/put data.....");

                final byte[] bodyRequest = requestBody.getBytes(StandardCharsets.UTF_8);
                log.info("Output stream is enabled: {}", urlConnection.getOutputStream());
                DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                outputStream.write(bodyRequest);

                outputStream.flush();
                outputStream.close();
            }
            //</editor-fold>

            int responseCode = urlConnection.getResponseCode();
            log.info("Response Code: {}", responseCode);

            if (isResponseSuccess(responseCode)) {
                //<editor-fold desc="Success response fetch">
                try {
                    final String encoding = urlConnection.getContentEncoding();
                    final InputStream in;
                    if ("gzip".equals(encoding)) {
                        in = new GZIPInputStream(urlConnection.getInputStream());
                    } else {
                        in = urlConnection.getInputStream();
                    }
                    return textFromInputStream(in);
                } finally {
                    urlConnection.getInputStream().close();
                    urlConnection.disconnect();

                    log.info("Done extracting response");
                }
                //</editor-fold>
            } else {
                //<editor-fold desc="Failure response">
                String msg = "Something went wrong... :( status code: " +
                        responseCode + " " + urlConnection.getResponseMessage();

                urlConnection.disconnect();

                log.error(msg);
                throw new RuntimeException(msg);
                //</editor-fold>
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * validate success family response code
     *
     * @param responseCode Response code value
     * @return Status
     */
    private boolean isResponseSuccess(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_OK ||
                responseCode == HttpURLConnection.HTTP_ACCEPTED ||
                responseCode == HttpURLConnection.HTTP_CREATED ||
                responseCode == HttpURLConnection.HTTP_NO_CONTENT ||
                responseCode == HttpURLConnection.HTTP_PARTIAL ||
                responseCode == HttpURLConnection.HTTP_RESET;
    }

    /**
     * Set-up HTTP connection
     *
     * @param externalUrl External connection url
     * @param verb        Http Verb GET/POST/PUT/PATCH
     * @return HttpURLConnection
     * @throws IOException
     */
    @NotNull
    private HttpURLConnection setUpConnection(String externalUrl, String verb) throws IOException {
        URL obj = new URL(externalUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();
        urlConnection.setRequestMethod(verb);
        urlConnection.addRequestProperty("accept", "application/json, text/plain, */*");
        urlConnection.addRequestProperty("accept-encoding", "gzip, deflate, br");

        log.info("Media type: {}", magentoConfig.getMediaType());
        log.info("Authorization type: {}", magentoConfig.getMagnetoAccessToken());

        urlConnection.addRequestProperty("content-type", magentoConfig.getMediaType());
        urlConnection.addRequestProperty("Authorization", magentoConfig.getMagnetoAccessToken());

        if ("get".equalsIgnoreCase(verb)) {
            urlConnection.setDoInput(true);
        } else {
//            if (!urlConnection.getDoOutput()) {
            urlConnection.addRequestProperty("x-requested-with", "XMLHttpRequest");
            urlConnection.setDoOutput(true);
//            }
        }

        if (urlConnection instanceof HttpsURLConnection) {
            SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(socketFactory);
        }

        log.info("HttpURLConnection setup done.");
        return urlConnection;
    }

    /**
     * Extract response message from the input stream
     *
     * @param inputStream InputStream
     * @return Response
     */
    private String textFromInputStream(final InputStream inputStream) {
        log.info("Extracting response from stream!!");

        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
    }
}
