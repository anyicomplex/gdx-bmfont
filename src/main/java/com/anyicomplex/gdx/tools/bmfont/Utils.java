package com.anyicomplex.gdx.tools.bmfont;

class Utils {

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

}
