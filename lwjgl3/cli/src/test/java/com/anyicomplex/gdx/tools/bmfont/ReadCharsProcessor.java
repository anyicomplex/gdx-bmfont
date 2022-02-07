package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import static com.anyicomplex.gdx.tools.bmfont.Utils.readCharsFromFiles;

public class ReadCharsProcessor {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setInitialVisible(false);
        configuration.setDecorated(false);
        configuration.setWindowedMode(1, 1);
        configuration.disableAudio(true);
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create() {
                super.create();
                System.out.println(readCharsFromFiles(Gdx.files.absolute(System.getProperty("user.home") + "/test")));
                Gdx.app.exit();
            }
        }, configuration);
    }

}
