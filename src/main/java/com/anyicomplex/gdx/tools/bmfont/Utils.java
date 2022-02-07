package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

class Utils {

    static PlatformUtils platformUtils;

    static boolean stringNotEmpty(String s) {
        return s != null && s.length() > 0;
    }

    static String quote(Object param) {
        return quote(true, param);
    }

    static String quote(boolean xml, Object param) {
        return quote(xml, param, false);
    }

    static String quote(boolean xml, Object param, boolean spaceAfter) {
        if (xml) return "\"" + param.toString().trim() + "\"" + (spaceAfter ? " " : "");
        return param.toString();
    }

    static String readCharsFromFiles(FileHandle... files) {
        return readCharsFromFiles("UTF-8", files);
    }

    static String readCharsFromFiles(String charset, FileHandle... files) {
        StringBuilder builder = new StringBuilder();
        for (FileHandle file : files) {
            if (file.isDirectory()) builder.append(readCharsFromFiles(charset, file.list()));
            else builder.append(file.readString(charset));
        }
        return removeDuplicateChars(builder.toString());
    }

    static String removeDuplicateChars(String string) {
        return Arrays.stream(string.split("")).distinct().collect(Collectors.joining());
    }

    static void verbose(String message) {
        platformUtils.verbose(message);
    }

    static void error(String message) {
        platformUtils.error(message);
    }

}
