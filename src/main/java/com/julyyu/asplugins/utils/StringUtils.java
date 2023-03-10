package com.julyyu.asplugins.utils;

import org.apache.http.util.TextUtils;

public class StringUtils {

    public enum IndexType {
        First,
        Last,
    }

    public static int findLine(String completeText, String text, IndexType indexType) {
        final int index;
        switch (indexType) {
            case Last:
                index = completeText.lastIndexOf(text);
                break;
            case First:
                index = completeText.indexOf(text);
                break;
            default:
                return -1;
        }

        final String targetStr = completeText.substring(0, index).replace("\n\n", "\n-\n");
        final String[] lines = targetStr.split("\n");
        return lines.length;
    }




    public  static String firstLetterUpper(String value) {
        if (value == null || TextUtils.isEmpty(value)) {
            return "";
        }
        return value.substring(0,1).toUpperCase() + value.substring(1);
    }

    public static String firstLetterLower(String value) {
        if (value == null || TextUtils.isEmpty(value)) {
            return "";
        }
        return value.substring(0,1).toLowerCase() + value.substring(1);
    }



}
