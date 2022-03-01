package com.wtc.pureit.data.dto.helper;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ram M
 */
public class NetSuiteOauthHeaderBuilder {
    private String tokenSecret, uri, script, deploy, signatureMethod,
            consumerSecret, method, realm;

    private final String AMPERSAND = "&";
    private final String EQUAL = "=";
    private final String QUOTE = "\"";

    private final Map<String, String> parameters = new LinkedHashMap<>();
    private final Map<String, String> paramMap = new LinkedHashMap<>();

    /**
     * build auth token
     *
     * @return
     */
    public String build() {
        if (!parameters.containsKey("oauth_timestamp")) {
            parameters.put("oauth_timestamp", "" + Encoder.getCurrentTimestamp());
        }
        if (!parameters.containsKey("oauth_nonce")) {
            parameters.put("oauth_nonce", "" + getNonce());
        }

        // Build the parameter string after sorting the keys in lexicographic order per the OAuth v1 spec.
        String staticValues = new StringBuilder("deploy=").append(deploy).append(AMPERSAND)
                .append(parameters.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> e.getKey() + EQUAL + e.getValue() + AMPERSAND)
                        .collect(Collectors.joining()))
                .append("script=").append(script).toString();

        try {
            // If the signing key was not provided, build it by encoding the consumer secret + the token secret
            String signingKey = consumerSecret + AMPERSAND + tokenSecret;
            String data = method.toUpperCase() + AMPERSAND + Encoder.urlEncoding(uri) + AMPERSAND + Encoder.urlEncoding(staticValues);

            // Build the signature base string
            byte[] sha256Encoding = Encoder.sha256Encoding(signingKey, data);
            String base64Encoded = Encoder.base64Encoding(sha256Encoding);

            // Add the signature to be included in the header
            parameters.put("oauth_signature", Encoder.urlEncoding(base64Encoded));

            if (!parameters.containsKey("realm")) {
                parameters.put("realm", realm);
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return "OAuth " + parameters.entrySet().stream()
                .map(e -> e.getKey() + EQUAL + QUOTE + e.getValue() + QUOTE)
                .collect(Collectors.joining(", "));
    }

    /**
     * generate random nonce
     *
     * @return
     */
    public static String getNonce() {
        return Long.toHexString(Math.abs(new SecureRandom().nextLong())).substring(0, 11);
    }
    //<editor-fold desc="input parameters of builder pattern">

    /**
     * Set the Consumer Secret
     *
     * @param consumerSecret the Consumer Secret
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
        return this;
    }

    /**
     * Set the requested HTTP method
     *
     * @param method the HTTP method you are requesting
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Add a parameter to the be included when building the signature.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withParameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * Set the OAuth Token Secret
     *
     * @param tokenSecret the OAuth Token Secret
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
        return this;
    }

    /**
     * Set the requested URL in the builder.
     *
     * @param uri the URL you are requesting
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withUri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Set the requested URL in the builder.
     *
     * @param script the code/mechanism you are requesting
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withScript(String script) {
        this.script = script;
        return this;
    }

    /**
     * Set the requested URL in the builder.
     * s
     *
     * @param deploy the deploy you are requesting
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withDeploy(String deploy) {
        this.deploy = deploy;
        return this;
    }

    /**
     * Set the requested URL in the builder.
     * s
     *
     * @param signatureMethod the algorithm key which will be used to generate the signature.
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withSignatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
        return this;
    }

    /**
     * Set the requested URL in the builder.
     * s
     *
     * @param realm
     * @return this
     */
    public NetSuiteOauthHeaderBuilder withRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public NetSuiteOauthHeaderBuilder withParam(Map<String, String> param) {
        if (Objects.nonNull(param)) {
            paramMap.putAll(param);
        }
        return this;
    }
    //</editor-fold>
}