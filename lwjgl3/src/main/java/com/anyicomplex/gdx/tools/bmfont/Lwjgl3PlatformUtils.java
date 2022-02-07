package com.anyicomplex.gdx.tools.bmfont;

import picocli.CommandLine;

public class Lwjgl3PlatformUtils implements PlatformUtils {

    @Override
    public void verbose(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(CommandLine.Help.Ansi.AUTO.string("@|bold,fg_red " + message + " |@"));
    }

}
