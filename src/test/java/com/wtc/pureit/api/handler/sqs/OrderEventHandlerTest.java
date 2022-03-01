package com.wtc.pureit.api.handler.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtc.pureit.api.handler.gateway.OrderUpdateStatusHandler;
import com.wtc.pureit.api.helper.MagentoCaller;
import com.wtc.pureit.api.helper.NetSuiteCaller;
import com.wtc.pureit.config.AwsConfig;
import com.wtc.pureit.config.MagentoConfig;
import com.wtc.pureit.config.NetSuiteConfig;
import com.wtc.pureit.data.dto.request.InvoiceCreationRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class OrderEventHandlerTest {

    @InjectMocks
    NetSuiteCaller netSuiteCaller;

    @Spy
    NetSuiteConfig netSuiteConfig;

    @Spy
    MagentoConfig magentoConfig;

    @Spy
    AwsConfig awsConfig;

    @Spy
    MagentoCaller magentoCaller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void loadConfig() {
        Mockito.when(netSuiteConfig.getNetSuiteUri()).thenReturn("https://3667364-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl");
        Mockito.when(netSuiteConfig.getDeploy()).thenReturn("1");
        Mockito.when(netSuiteConfig.getCookie()).thenReturn("NS_ROUTING_VERSION=LAGGING");
        Mockito.when(netSuiteConfig.getMediaType()).thenReturn("application/json");
        Mockito.when(netSuiteConfig.getCustomerDetailScript()).thenReturn("2112");
        Mockito.when(netSuiteConfig.getCustomerAddressUpdateScript()).thenReturn("2080");
        Mockito.when(netSuiteConfig.getComplaintCreationScript()).thenReturn("2090");
        Mockito.when(netSuiteConfig.getInvoiceCreationScript()).thenReturn("2073");
        Mockito.when(netSuiteConfig.getSalesOrderCreationScript()).thenReturn("2071");
        Mockito.when(netSuiteConfig.getOrderCancellationScript()).thenReturn("2078");
        Mockito.when(netSuiteConfig.getOrderStatusScript()).thenReturn("2079");
        Mockito.when(netSuiteConfig.getOrderCreationScript()).thenReturn("2049");
        Mockito.when(netSuiteConfig.getComplaintStatusScript()).thenReturn("2092");
        Mockito.when(netSuiteConfig.getCustomerCreationScript()).thenReturn("2059");
        Mockito.when(netSuiteConfig.getLeadCreationScript()).thenReturn("2042");
        Mockito.when(netSuiteConfig.getConsumerKey()).thenReturn("3d579d4b66c8bcf4c86e4fb06f258adf2c67e9631a7fc0c17b676a3491081c54");
        Mockito.when(netSuiteConfig.getConsumerSecret()).thenReturn("677175d5d35ed51458c4518c709d44cdd10d16971e72c1887da5e9cc93614cac");
        Mockito.when(netSuiteConfig.getToken()).thenReturn("856926f942b20a07d30e6fb02c06887532fe9c8f118425156d65676fe0b47066");
        Mockito.when(netSuiteConfig.getTokenSecret()).thenReturn("8045e4be2ae953731ac0679b9db2d000ced82c7f1870d9b6aac1fb6800fc1ad9");
        Mockito.when(netSuiteConfig.getSignatureMethod()).thenReturn("HMAC-SHA256");
        Mockito.when(netSuiteConfig.getVersion()).thenReturn("1.0");
        Mockito.when(netSuiteConfig.getRealm()).thenReturn("3667364_SB1");

        Mockito.when(magentoConfig.getMediaType()).thenReturn("application/json");
        Mockito.when(magentoConfig.getMagnetoAccessToken()).thenReturn("1");
        Mockito.when(magentoConfig.getFetchOrderUrl()).thenReturn("https://integration-5ojmyuq-wvlvohz72l6hm.ap-3.magentosite.cloud/rest/default/V1/orders/id");
        Mockito.when(magentoConfig.getSendCustomerResponseUrl()).thenReturn("https://integration-5ojmyuq-wvlvohz72l6hm.ap-3.magentosite.cloud/rest/default/V1/customers/id");
        Mockito.when(magentoConfig.getOrderUpdateStatusUrl()).thenReturn("https://integration-5ojmyuq-wvlvohz72l6hm.ap-3.magentosite.cloud/rest/V1/order/netsuiteorderupdate");

        Mockito.when(awsConfig.getMaxRetry()).thenReturn("3");
        Mockito.when(awsConfig.getBackOffPeriod()).thenReturn("1000l");

        Mockito.when(magentoCaller.getMagentoConfig()).thenReturn(magentoConfig);
        Mockito.when(magentoCaller.getMagentoConfig().getFetchOrderUrl()).thenReturn("https://integration-5ojmyuq-wvlvohz72l6hm.ap-3.magentosite.cloud/rest/default/V1/orders/id");
    }

    @Test
    public void testExistingCustomer() {
        loadConfig();

        Map<String, String> param = new HashMap<>();
        param.put("phoneNumber", "2025550136");
        netSuiteCaller.isExistingCustomer(param);
    }

    @Test
    public void testCreateInvoice() {
        loadConfig();
        netSuiteCaller.createInvoice(InvoiceCreationRequest.builder().rfsId("RFS/5029631").build());
    }

    @Test
    public void orderCreationRetry() throws Exception {
        loadConfig();

        OrderCreationEventHandler eventHandler = new OrderCreationEventHandler();
        String process = eventHandler.process(null);
    }

    @Test
    public void orderUpdateStatus() {
        loadConfig();

        String statusUpdate = "{\n" +
                "    \"rfsId\":\"RFS/5031932\",\n" +
                "    \"orderStatus\":\"Inprogress\"\n" +
                "}";

        InputStream stream = new ByteArrayInputStream(statusUpdate.getBytes(StandardCharsets.UTF_8));

        OrderUpdateStatusHandler orderUpdateStatusHandler = new OrderUpdateStatusHandler();
        orderUpdateStatusHandler.handleRequest(stream, null);
    }

    @Test
    public void testConnection() throws IOException {
        String externalUrl = "https://integration2-hohc4oi-wvlvohz72l6hm.ap-3.magentosite.cloud/rest/default/V1/orders/507";

        setupConnection(externalUrl, "POST");
    }

    public void setupConnection(String externalUrl, String verb) throws IOException {
        URL obj = new URL(externalUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) obj.openConnection();
        urlConnection.setRequestMethod(verb);
        urlConnection.addRequestProperty("accept", "application/json, text/plain, */*");
        urlConnection.addRequestProperty("accept-encoding", "gzip, deflate, br");

//        log.info("Media type: {}", magentoConfig.getMediaType());
//        log.info("Authorization type: {}", magentoConfig.getMagnetoAccessToken());

        urlConnection.addRequestProperty("content-type", "application/json");
        urlConnection.addRequestProperty("Authorization", "Bearer 60fuuu97oklv9kwj3q8oi4n49qjblie5");

        if ("get".equalsIgnoreCase(verb)) {
            urlConnection.setDoInput(true);
        } else {
//            if (!urlConnection.getDoOutput()) {
            urlConnection.addRequestProperty("x-requested-with", "XMLHttpRequest");
            urlConnection.setDoOutput(true);
//            }
        }

        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//        ((HttpsURLConnection) urlConnection).setSSLSocketFactory(socketFactory);

        if (urlConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
            httpsConnection.setSSLSocketFactory(socketFactory);
//            httpsConnection.setHostnameVerifier(hostnameVerifier);
        }

        System.out.println("HttpURLConnection setup done.");
//        return urlConnection;
    }

    @Test
    public void test() {
        String date = "29/02/2022";
        System.out.println(date.replaceAll("\\/", "-"));
    }

    private JsonNode getOrder() {
        try {
            String jsonString = "{\"applied_rule_ids\":\"2\",\"base_currency_code\":\"INR\",\"base_discount_amount\":0,\"base_grand_total\":28990,\"base_discount_tax_compensation_amount\":0,\"base_shipping_amount\":0,\"base_shipping_discount_amount\":0,\"base_shipping_discount_tax_compensation_amnt\":0,\"base_shipping_incl_tax\":0,\"base_shipping_tax_amount\":0,\"base_subtotal\":24567.8,\"base_subtotal_incl_tax\":28990,\"base_tax_amount\":4422.2,\"base_total_due\":28990,\"base_to_global_rate\":1,\"base_to_order_rate\":1,\"billing_address_id\":18,\"created_at\":\"2022-02-25 15:20:45\",\"customer_email\":\"ved1585@gmail.com\",\"customer_firstname\":\"vedendra\",\"customer_group_id\":1,\"customer_id\":8,\"customer_is_guest\":0,\"customer_lastname\":\"singh\",\"customer_note_notify\":1,\"discount_amount\":0,\"entity_id\":9,\"global_currency_code\":\"INR\",\"grand_total\":28990,\"discount_tax_compensation_amount\":0,\"increment_id\":\"000000009\",\"is_virtual\":0,\"order_currency_code\":\"INR\",\"protect_code\":\"ecf8b66c0688fe45e6e9be97ec257ece\",\"quote_id\":24,\"remote_ip\":\"47.8.63.219\",\"shipping_amount\":0,\"shipping_description\":\"Free Shipping - Shipping Free\",\"shipping_discount_amount\":0,\"shipping_discount_tax_compensation_amount\":0,\"shipping_incl_tax\":0,\"shipping_tax_amount\":0,\"state\":\"pending\",\"status\":\"pending\",\"store_currency_code\":\"INR\",\"store_id\":1,\"store_name\":\"Main Website\\nMain Website Store\\nDefault Store View\",\"store_to_base_rate\":0,\"store_to_order_rate\":0,\"subtotal\":24567.8,\"subtotal_incl_tax\":28990,\"tax_amount\":4422.2,\"total_due\":28990,\"total_item_count\":1,\"total_qty_ordered\":1,\"updated_at\":\"2022-02-25 15:20:47\",\"weight\":0,\"x_forwarded_for\":\"47.8.63.219\",\"items\":[{\"amount_refunded\":0,\"applied_rule_ids\":\"2\",\"base_amount_refunded\":0,\"base_discount_amount\":0,\"base_discount_invoiced\":0,\"base_discount_tax_compensation_amount\":0,\"base_original_price\":28990,\"base_price\":24567.8,\"base_price_incl_tax\":28990,\"base_row_invoiced\":0,\"base_row_total\":24567.8,\"base_row_total_incl_tax\":28990,\"base_tax_amount\":4422.2,\"base_tax_invoiced\":0,\"created_at\":\"2022-02-25 15:20:46\",\"discount_amount\":0,\"discount_invoiced\":0,\"discount_percent\":0,\"free_shipping\":0,\"discount_tax_compensation_amount\":0,\"is_qty_decimal\":0,\"is_virtual\":0,\"item_id\":9,\"name\":\"Pureit Copper\",\"no_discount\":0,\"order_id\":9,\"original_price\":28990,\"price\":24567.8,\"price_incl_tax\":28990,\"product_id\":3,\"product_type\":\"simple\",\"qty_canceled\":0,\"qty_invoiced\":0,\"qty_ordered\":1,\"qty_refunded\":0,\"qty_returned\":0,\"qty_shipped\":0,\"quote_item_id\":12,\"row_invoiced\":0,\"row_total\":24567.8,\"row_total_incl_tax\":28990,\"row_weight\":0,\"sku\":\"PC009\",\"store_id\":1,\"tax_amount\":4422.2,\"tax_invoiced\":0,\"tax_percent\":18,\"updated_at\":\"2022-02-25 15:20:46\",\"extension_attributes\":{\"item_device_category\":\"Blueair -Pure 121\",\"item_device_category_internal_id\":\"215\",\"item_internal_id\":\"7054\",\"item_category\":\"Device\",\"serial_number\":\"PC009\"}}],\"billing_address\":{\"address_type\":\"billing\",\"city\":\"ff\",\"country_id\":\"IN\",\"email\":\"ved1585@gmail.com\",\"entity_id\":18,\"firstname\":\"vedendra\",\"lastname\":\"singh\",\"parent_id\":9,\"postcode\":\"201306\",\"region\":\"Delhi\",\"region_code\":\"DL\",\"region_id\":578,\"street\":[\"test\",\"test\"],\"telephone\":\"9898989898\"},\"payment\":{\"account_status\":null,\"additional_information\":[\"CCAvenue Payment\"],\"amount_authorized\":28990,\"amount_ordered\":28990,\"base_amount_authorized\":28990,\"base_amount_ordered\":28990,\"base_shipping_amount\":0,\"cc_exp_year\":\"0\",\"cc_last4\":null,\"cc_ss_start_month\":\"0\",\"cc_ss_start_year\":\"0\",\"entity_id\":9,\"method\":\"ccavenue\",\"parent_id\":9,\"shipping_amount\":0},\"status_histories\":[{\"comment\":\"Customer earned promotion extra 5000 Reward points.\",\"created_at\":\"2022-02-25 15:20:47\",\"entity_id\":20,\"entity_name\":\"order\",\"is_customer_notified\":null,\"is_visible_on_front\":0,\"parent_id\":9,\"status\":\"processing\"},{\"comment\":\"Authorized amount of ₹28,990.00.\",\"created_at\":\"2022-02-25 15:20:46\",\"entity_id\":19,\"entity_name\":\"order\",\"is_customer_notified\":null,\"is_visible_on_front\":0,\"parent_id\":9,\"status\":\"processing\"}],\"extension_attributes\":{\"shipping_assignments\":[{\"shipping\":{\"address\":{\"address_type\":\"shipping\",\"city\":\"ff\",\"country_id\":\"IN\",\"customer_address_id\":103,\"email\":\"ved1585@gmail.com\",\"entity_id\":17,\"firstname\":\"vedendra\",\"lastname\":\"singh\",\"parent_id\":9,\"postcode\":\"201306\",\"region\":\"Delhi\",\"region_code\":\"DL\",\"region_id\":578,\"street\":[\"test\",\"test\"],\"telephone\":\"9898989898\"},\"method\":\"freeshipping_freeshipping\",\"total\":{\"base_shipping_amount\":0,\"base_shipping_discount_amount\":0,\"base_shipping_discount_tax_compensation_amnt\":0,\"base_shipping_incl_tax\":0,\"base_shipping_tax_amount\":0,\"shipping_amount\":0,\"shipping_discount_amount\":0,\"shipping_discount_tax_compensation_amount\":0,\"shipping_incl_tax\":0,\"shipping_tax_amount\":0}},\"items\":[{\"amount_refunded\":0,\"applied_rule_ids\":\"2\",\"base_amount_refunded\":0,\"base_discount_amount\":0,\"base_discount_invoiced\":0,\"base_discount_tax_compensation_amount\":0,\"base_original_price\":28990,\"base_price\":24567.8,\"base_price_incl_tax\":28990,\"base_row_invoiced\":0,\"base_row_total\":24567.8,\"base_row_total_incl_tax\":28990,\"base_tax_amount\":4422.2,\"base_tax_invoiced\":0,\"created_at\":\"2022-02-25 15:20:46\",\"discount_amount\":0,\"discount_invoiced\":0,\"discount_percent\":0,\"free_shipping\":0,\"discount_tax_compensation_amount\":0,\"is_qty_decimal\":0,\"is_virtual\":0,\"item_id\":9,\"name\":\"Pureit Copper\",\"no_discount\":0,\"order_id\":9,\"original_price\":28990,\"price\":24567.8,\"price_incl_tax\":28990,\"product_id\":3,\"product_type\":\"simple\",\"qty_canceled\":0,\"qty_invoiced\":0,\"qty_ordered\":1,\"qty_refunded\":0,\"qty_returned\":0,\"qty_shipped\":0,\"quote_item_id\":12,\"row_invoiced\":0,\"row_total\":24567.8,\"row_total_incl_tax\":28990,\"row_weight\":0,\"sku\":\"PC009\",\"store_id\":1,\"tax_amount\":4422.2,\"tax_invoiced\":0,\"tax_percent\":18,\"updated_at\":\"2022-02-25 15:20:46\"}]}],\"payment_additional_info\":[{\"key\":\"method_title\",\"value\":\"CCAvenue Payment\"}],\"applied_taxes\":[{\"code\":\"GST\",\"title\":\"GST\",\"percent\":18,\"amount\":4422.2,\"base_amount\":4422.2}],\"item_applied_taxes\":[{\"type\":\"product\",\"item_id\":9,\"applied_taxes\":[{\"code\":\"GST\",\"title\":\"GST\",\"percent\":18,\"amount\":4422.2,\"base_amount\":4422.2}]}],\"converting_from_quote\":true,\"gift_cards\":[],\"base_gift_cards_amount\":0,\"gift_cards_amount\":0,\"gw_base_price\":\"0.0000\",\"gw_price\":\"0.0000\",\"gw_items_base_price\":\"0.0000\",\"gw_items_price\":\"0.0000\",\"gw_card_base_price\":\"0.0000\",\"gw_card_price\":\"0.0000\",\"netsuite_customer_id\":\"0\",\"expected_delivery_date\":\"\",\"rfs_id\":\"RFS/102541\",\"netsuite_invoice_url\":\"\"}}";

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


    @Test
    public void testCancelOrder() throws JsonProcessingException {
        JsonNode rfsIdNode = getOrder().path("extension_attributes").path("rfs_id");

        String rfsId = Objects.nonNull(rfsIdNode) ?
                rfsIdNode.textValue() : rfsIdNode.toString();

        System.out.println(rfsId);
    }

    @Test
    public void testOrderStatusRetryLogic() throws InterruptedException {
        boolean canProceed = false;
        int orderRetryVal = 5;
        int counter = 0;
        do {
            String status = getOrder().get("status").textValue();
            canProceed = "pending".equalsIgnoreCase(status);

            if (counter == orderRetryVal) {
                break;
            } else {
                counter++;
                Thread.sleep(1000);
            }
        } while (canProceed);


        System.out.println(canProceed);
    }
}
