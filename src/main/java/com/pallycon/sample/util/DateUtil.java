package com.pallycon.sample.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Brown on 2019-12-11.
 */
public class DateUtil {
    public static String getGMTTimeStampString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        TimeZone z = TimeZone.getTimeZone("GMT");
        formatter.setTimeZone(z);
        return formatter.format(new Date());
    }
}
