package ru.akirakozov.sd.refactoring.utils;

public class HTMLResponseMaker {
    private HTMLResponseMaker() {
        // Utility class
    }

    public static String withHTMLWrapper(String src) {
        return "<html><body>\n" + src + "</body></html>";
    }

    public static String makeRow(String key, String value) {
        return key + "\t" + value + "</br>";
    }

    public static String makeHeader(String str) {
        return "<h1>" + str + "</h1>";
    }
}