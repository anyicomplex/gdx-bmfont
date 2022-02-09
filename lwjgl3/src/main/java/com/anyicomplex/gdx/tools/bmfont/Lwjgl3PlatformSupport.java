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

import com.badlogic.gdx.utils.GdxRuntimeException;
import picocli.CommandLine;

import static com.anyicomplex.gdx.tools.bmfont.BitmapFontPacker.Utils.stringNotEmpty;

class Lwjgl3PlatformSupport implements BitmapFontPacker.PlatformSupport {

    @Override
    public void verbose(String tag, String message) {
        if (tag == null) {
            if (stringNotEmpty(message)) System.out.println(message);
        }
        else System.out.println("[" + tag + "] " + message);
    }

    @Override
    public void error(String tag, String message) {
        if (tag == null) {
            if (stringNotEmpty(message)) System.err.println(errorColor(message));
        }
        else System.err.println(errorColor("[" + tag + "] " + message));
    }

    @Override
    public void exception(String message) {
        if (!stringNotEmpty(message)) throw new GdxRuntimeException((Throwable) null);
        throw new GdxRuntimeException(errorColor(message));
    }

    private String errorColor(String string) {
        return CommandLine.Help.Ansi.AUTO.string("@|bold,fg_red " + string + " |@");
    }

}
