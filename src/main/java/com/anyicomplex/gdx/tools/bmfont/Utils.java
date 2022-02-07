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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Arrays;
import java.util.stream.Collectors;

class Utils {

    static PlatformUtils platformUtils;

    static boolean stringNotEmpty(String s) {
        return s != null && s.length() > 0;
    }

    /*******************************************************************************
     * Source: https://github.com/libgdx/libgdx/blob/gdx-parent-1.10.0/extensions/gdx-tools/src/com/badlogic/gdx/tools/bmfont/BitmapFontWriter#quote
     * AUTHORS file: https://github.com/libgdx/libgdx/blob/gdx-parent-1.10.0/AUTHORS
     *
     * Copyright 2011 See AUTHORS file.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     ******************************************************************************/
    static String quote(boolean xml, Object param) {
        return quote(xml, param, false);
    }
    static String quote(boolean xml, Object param, boolean spaceAfter) {
        if (xml) return "\"" + param.toString().trim() + "\"" + (spaceAfter ? " " : "");
        return param.toString();
    }

    static String readCharsFromFiles(FileHandle... files) {
        return readCharsFromFiles(null, files);
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
