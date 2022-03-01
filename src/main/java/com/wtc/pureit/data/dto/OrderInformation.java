package com.wtc.pureit.data.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class OrderInformation {
    public String base_currency_code;
    public int base_discount_amount;
    public double base_grand_total;
    public int base_discount_tax_compensation_amount;
    public int base_shipping_amount;
    public int base_shipping_discount_amount;
    public int base_shipping_discount_tax_compensation_amnt;
    public int base_shipping_incl_tax;
    public int base_shipping_tax_amount;
    public double base_subtotal;
    public double base_subtotal_incl_tax;
    public int base_tax_amount;
    public double base_total_due;
    public int base_to_global_rate;
    public int base_to_order_rate;
    public int billing_address_id;
    public String created_at;
    public String customer_email;
    public String customer_firstname;
    public int customer_group_id;
    public int customer_is_guest;
    public String customer_lastname;
    public int customer_note_notify;
    public int discount_amount;
    public int email_sent;
    public int entity_id;
    public String global_currency_code;
    public double grand_total;
    public int discount_tax_compensation_amount;
    public String increment_id;
    public int is_virtual;
    public String order_currency_code;
    public String protect_code;
    public int quote_id;
    public String remote_ip;
    public int shipping_amount;
    public String shipping_description;
    public int shipping_discount_amount;
    public int shipping_discount_tax_compensation_amount;
    public int shipping_incl_tax;
    public int shipping_tax_amount;
    public String state;
    public String status;
    public String store_currency_code;
    public int store_id;
    public String store_name;
    public int store_to_base_rate;
    public int store_to_order_rate;
    public double subtotal;
    public double subtotal_incl_tax;
    public int tax_amount;
    public double total_due;
    public int total_item_count;
    public int total_qty_ordered;
    public String updated_at;
    public int weight;
    public List<Item> items;
    public BillingAddress billing_address;
    public Payment payment;
    public List<Object> status_histories;
    public ExtensionAttributes extension_attributes;

    static class Item {
        public int amount_refunded;
        public int base_amount_refunded;
        public int base_discount_amount;
        public int base_discount_invoiced;
        public int base_discount_tax_compensation_amount;
        public double base_original_price;
        public double base_price;
        public double base_price_incl_tax;
        public int base_row_invoiced;
        public double base_row_total;
        public double base_row_total_incl_tax;
        public int base_tax_amount;
        public int base_tax_invoiced;
        public String created_at;
        public int discount_amount;
        public int discount_invoiced;
        public int discount_percent;
        public int free_shipping;
        public int discount_tax_compensation_amount;
        public int is_qty_decimal;
        public int is_virtual;
        public int item_id;
        public String name;
        public int no_discount;
        public int order_id;
        public double original_price;
        public double price;
        public double price_incl_tax;
        public int product_id;
        public String product_type;
        public int qty_canceled;
        public int qty_invoiced;
        public int qty_ordered;
        public int qty_refunded;
        public int qty_returned;
        public int qty_shipped;
        public int quote_item_id;
        public int row_invoiced;
        public double row_total;
        public double row_total_incl_tax;
        public int row_weight;
        public String sku;
        public int store_id;
        public int tax_amount;
        public int tax_invoiced;
        public int tax_percent;
        public String updated_at;
    }

    static class BillingAddress {
        public String address_type;
        public String city;
        public String country_id;
        public String email;
        public int entity_id;
        public String firstname;
        public String lastname;
        public int parent_id;
        public String postcode;
        public String region;
        public String region_code;
        public int region_id;
        public List<String> street;
        public String telephone;
    }

    static class Payment {
        public Object account_status;
        public List<String> additional_information;
        public double amount_ordered;
        public double base_amount_ordered;
        public int base_shipping_amount;
        public String cc_exp_year;
        public Object cc_last4;
        public String cc_ss_start_month;
        public String cc_ss_start_year;
        public int entity_id;
        public String method;
        public int parent_id;
        public int shipping_amount;
    }

    static class Address {
        public String address_type;
        public String city;
        public String country_id;
        public String email;
        public int entity_id;
        public String firstname;
        public String lastname;
        public int parent_id;
        public String postcode;
        public String region;
        public String region_code;
        public int region_id;
        public List<String> street;
        public String telephone;
    }

    static class Total {
        public int base_shipping_amount;
        public int base_shipping_discount_amount;
        public int base_shipping_discount_tax_compensation_amnt;
        public int base_shipping_incl_tax;
        public int base_shipping_tax_amount;
        public int shipping_amount;
        public int shipping_discount_amount;
        public int shipping_discount_tax_compensation_amount;
        public int shipping_incl_tax;
        public int shipping_tax_amount;
    }

    static class Shipping {
        public Address address;
        public String method;
        public Total total;
    }

    static class ShippingAssignment {
        public Shipping shipping;
        public List<Item> items;
    }

    static class PaymentAdditionalInfo {
        public String key;
        public String value;
    }

    static class ExtensionAttributes {
        public List<ShippingAssignment> shipping_assignments;
        public List<PaymentAdditionalInfo> payment_additional_info;
        public List<Object> applied_taxes;
        public List<Object> item_applied_taxes;
        public List<Object> gift_cards;
        public int base_gift_cards_amount;
        public int gift_cards_amount;
        public String gw_base_price;
        public String gw_price;
        public String gw_items_base_price;
        public String gw_items_price;
        public String gw_card_base_price;
        public String gw_card_price;
        public String netsuite_customer_id;
    }

}