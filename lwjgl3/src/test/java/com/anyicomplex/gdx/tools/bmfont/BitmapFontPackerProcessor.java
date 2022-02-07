/*
 * Copyright 2022 Yi An
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BitmapFontPackerProcessor {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.disableAudio(true);
        new Lwjgl3Application(new ApplicationAdapter() {
            private SpriteBatch batch;
            private BitmapFont font;
            private FileHandle fntDir;
            @Override
            public void create() {
                super.create();
                fntDir = Gdx.files.absolute(System.getProperty("java.io.tmpdir") + "/" + BitmapFontPackerProcessor.class.getCanonicalName() + "_" +
                        System.getProperty("user.name"));
                BitmapFontPacker.Configuration configuration1 = new BitmapFontPacker.Configuration();
                configuration1.size = 64;
                int exitCode = BitmapFontPacker.process(
                        Gdx.files.absolute(System.getProperty("user.home") + "/test.ttf"),
                        fntDir, configuration1, true);
                if (exitCode != BitmapFontPacker.CODE_SUCCESS) System.exit(exitCode);
                font = new BitmapFont(fntDir.child("test.fnt"));
                batch = new SpriteBatch();
            }

            @Override
            public void render() {
                super.render();
                batch.begin();
                font.draw(batch, "Test", Gdx.graphics.getWidth() / 2.f, Gdx.graphics.getHeight() / 2.f);
                batch.end();
            }

            @Override
            public void dispose() {
                batch.dispose();
                font.dispose();
                super.dispose();
                if (fntDir.exists()) fntDir.deleteDirectory();
            }

        }, configuration);
    }

}
