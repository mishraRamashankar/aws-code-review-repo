package com.wtc.pureit.data.dto.helper;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class NetSuiteUtil {

    /**
     * @param ts
     * @return
     */
    public static String convertDate(Timestamp ts) {
        String date = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Date expectedDate = format.parse(ts.toString());
            format = new SimpleDateFormat("dd/MM/YYY");

            date = format.format(expectedDate);
        } catch (ParseException e) {
            log.error("Date parsing exception: ", e);
        }
        return date;
    }
}
