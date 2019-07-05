package com.fullsecurity.common;

import java.util.Calendar;
import java.util.Locale;

public class Utilities {

    public static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) byte2hex(block[i], buf);
        return buf.toString();
    }

    public static String toHexStringWithSeparator(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) buf.append(":");
        }
        return buf.toString();
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    //Adds two byte arrays together.
    public static byte[] concatenateByteArrays(byte[] first, byte[] second) {
        byte[]result=new byte[first.length+second.length];
        System.arraycopy(first,0,result,0,first.length);
        System.arraycopy(second,0,result,first.length,second.length);
        return result;
    }

    // one byte array is the parameter, consisting of three smaller byte arrays, array[0], array[1] and array[2]
    // array[0] is always 32 bytes
    // array[1] and array[2] are the same length
    // ndx = 0, 1, or 2
    // returns array[ndx]
    public static byte[] splitArray(byte[] array, int ndx) {
        byte[] result;
        if (ndx == 0) {
            result = new byte[32];
            System.arraycopy(array, 0, result, 0, 32);
        } else {
            int offset = 32;
            int halfLength = (array.length - offset) / 2;
            result = new byte[halfLength];
            System.arraycopy(array, offset + halfLength*(ndx-1), result, 0, halfLength);
        }
        return result;
    }

    // Compares two byte arrays for equality
    // return true if the arrays have identical contents
    public static boolean compareArrays(byte[] a, byte[] b) {
        int aLength = a.length;
        if (aLength != b.length) return false;
        for (int i = 0; i < aLength; i++)
            if (a[i] != b[i])
                return false;
        return true;
    }

    public static String displayablePrice(int price) {
        String dollarString;
        int cents = price % 100;
        int dollars = price / 100;
        if (dollars < 1000)
            dollarString = Integer.toString(dollars);
        else
            dollarString = Integer.toString(dollars / 1000) + "," + String.format("%03d", dollars % 1000);
        return "$" + dollarString + (cents < 10 ? ".0"+cents : "." + cents);
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return convertCalendarDateToString(calendar);
    }

    public static String convertCalendarDateToString(Calendar calendar) {
        return String.format(Locale.US,
                "%4d" + "%02d" + "%02d" + "%02d" + "%02d" + "%02d",
               calendar.get(Calendar.YEAR),
              (calendar.get(Calendar.MONTH) + 1),
               calendar.get(Calendar.DAY_OF_MONTH),
               calendar.get(Calendar.HOUR_OF_DAY),
               calendar.get(Calendar.MINUTE),
               calendar.get(Calendar.SECOND));
    }

    public static int decodeTimeYear(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(0,4));
    }

    public static int decodeTimeMonth(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(4,6));
    }

    public static int decodeTimeDay(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(6,8));
    }

    public static int decodeTimeHour(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(8,10));
    }

    public static int decodeTimeMinutes(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(10,12));
    }

    public static int decodeTimeSeconds(String time) {
        if (time == null)
            return -1;
        else if (time.length() < 14)
            return -1;
        else
            return Integer.parseInt(time.substring(12,14));
    }

    public static String formattedDate(String time) {
        return String.format(Locale.US,"%02d",decodeTimeMonth(time)) + "/" + String.format(Locale.US,"%02d",decodeTimeDay(time)) + "/" + decodeTimeYear(time);
    }

    public static String formattedTime(String time) {
        String minutes = String.format(Locale.US,"%02d", decodeTimeMinutes(time));
        int decodedHour = decodeTimeHour(time);
        String pm = (decodedHour < 12)?("AM"):("PM");
        String hours = (decodedHour % 12 == 0)?("12"):(String.format(Locale.US,"%02d",decodedHour % 12));
        return String.format("%s:%s %s", hours, minutes, pm);
    }

    public static String formattedDateFromInternalDate(String date) {
        String month = date.substring(4,6);
        month = (month.charAt(0) == '0' ? month.substring(1,2) : month);
        String day = date.substring(6,8);
        day = (day.charAt(0) == '0' ? day.substring(1,2) : day);
        return month + "/" + day + "/" + date.substring(0,4);
    }

}
