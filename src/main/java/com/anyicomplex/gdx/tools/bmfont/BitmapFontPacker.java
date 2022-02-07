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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import static com.anyicomplex.gdx.tools.bmfont.Utils.quote;
import static com.anyicomplex.gdx.tools.bmfont.Utils.stringNotEmpty;

public class BitmapFontPacker {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FILE_EXISTS = 1;

    public static volatile boolean VERBOSE = false;

    public static class Configuration {
        public String name = null;
        public int pageWidth = -1;
        public int pageHeight = -1;
        public String fntFormat = "txt";
        public boolean bold = false;
        public boolean italic = false;
        public boolean unicode = true;
        public int stretchH = 100;
        public String charset;

        public int size = 16;
        public boolean mono;
        public FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.AutoMedium;
        public Color color = Color.WHITE;
        public float gamma = 1.8f;
        public int renderCount = 2;
        public float borderWidth = 0;
        public Color borderColor = Color.BLACK;
        public boolean borderStraight = false;
        public float borderGamma = 1.8f;
        public int shadowOffsetX = 0;
        public int shadowOffsetY = 0;
        public Color shadowColor = new Color(0, 0, 0, 0.75f);
        public int spaceX, spaceY;
        public int padTop, padLeft, padBottom, padRight;
        public String characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        public boolean kerning = true;
        public PixmapPacker packer = null;
        public boolean flip = false;
        public boolean genMipMaps = false;
        public Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
        public Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;
        public boolean incremental;
    }

    public static int process(FileHandle srcFile, FileHandle dstDir, Configuration config) {
        return process(srcFile, dstDir, config, true);
    }

    public static int process(FileHandle srcFile, FileHandle dstDir, Configuration config, boolean override) {
        if (config == null) exception("Configuration cannot be null.");
        verbose("Process begin.");
        verbose("Generating FreeType config...");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(srcFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = parameter(config);
        if (config.pageWidth != -1 && config.pageHeight != -1) {
            if (parameter.packer == null) {
                parameter.packer = new PixmapPacker(config.pageWidth, config.pageHeight, Pixmap.Format.RGBA8888,
                        1, false, new PixmapPacker.SkylineStrategy());
                parameter.packer.setTransparentColor(config.color);
                parameter.packer.getTransparentColor().a = 0;
                if (config.borderWidth > 0) {
                    parameter.packer.setTransparentColor(config.borderColor);
                    parameter.packer.getTransparentColor().a = 0;
                }
            }
        }
        verbose("FreeType config generated successfully.");
        verbose("Generating BitmapFont data...");
        FreeTypeFontGenerator.FreeTypeBitmapFontData data = new FreeTypeFontGenerator.FreeTypeBitmapFontData();
        BitmapFont bitmapFont = generator.generateFont(parameter, data);
        verbose("BitmapFont data generated successfully.");
        String fileName = stringNotEmpty(config.name) ? config.name : srcFile.nameWithoutExtension();
        FileHandle[] pageFiles = new FileHandle[bitmapFont.getRegions().size];
        verbose("Glyph page amount: " + pageFiles.length);
        for (int i = 0; i < bitmapFont.getRegions().size; i ++) {
            FileHandle pageFile = dstDir.child(fileName + (bitmapFont.getRegions().size == 1 ? ".png" : "_" + i + ".png"));
            pageFiles[i] = pageFile;
        }
        FileHandle fntFile = dstDir.child(fileName + ".fnt");
        if (!override) {
            verbose("Checking whether files exists...");
            for (FileHandle pageFile : pageFiles) {
                if (pageFile.exists()) {
                    try {
                        error("BitmapFont files already exists.");
                        return CODE_FILE_EXISTS;
                    }
                    finally {
                        verbose("Cleaning up...");
                        bitmapFont.dispose();
                        verbose("Done.");
                    }
                }
                verbose(pageFile.path() + " does not exist, pass.");
            }
            if (fntFile.exists()) {
                try {
                    error("BitmapFont files already exists.");
                    return CODE_FILE_EXISTS;
                }
                finally {
                    verbose("Cleaning up...");
                    bitmapFont.dispose();
                    verbose("Done.");
                }
            }
            verbose(fntFile.path() + " does not exist, pass.");
        }
        verbose("Generating glyph page files...");
        for (int i = 0; i < pageFiles.length; i ++) {
            verbose("Rendering page [" + (i + 1) + "/" + bitmapFont.getRegions().size + "]");
            Pixmap pixmap = bitmapFont.getRegion(i).getTexture().getTextureData().consumePixmap();
            PixmapIO.writePNG(pageFiles[i], pixmap);
            verbose("File generated successfully at: " + pageFiles[i].path());
        }
        verbose("Generating .fnt file...");
        processFnt(data, pageFiles, fntFile, config);
        verbose("File generated successfully at: " + fntFile.path());
        verbose("Cleaning up...");
        bitmapFont.dispose();
        verbose("Done.");
        return CODE_SUCCESS;
    }

    private static void exception(String message) {
        if (message == null) throw new GdxRuntimeException((String) null);
        throw new GdxRuntimeException("[BitmapFontPacker] " + message);
    }

    private static void verbose(String message) {
        if (stringNotEmpty(message) && VERBOSE) {
            Utils.verbose("[BitmapFontPacker] " + message);
        }
    }

    private static void error(String message) {
        if (stringNotEmpty(message)) {
            Utils.error("[BitmapFontPacker] " + message);
        }
    }

    private static FreeTypeFontGenerator.FreeTypeFontParameter parameter(Configuration config) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = config.size;
        parameter.mono = config.mono;
        parameter.hinting = config.hinting;
        parameter.color = new Color(config.color);
        parameter.gamma = config.gamma;
        parameter.renderCount = config.renderCount;
        parameter.borderWidth = config.borderWidth;
        parameter.borderColor = new Color(config.borderColor);
        parameter.borderStraight = config.borderStraight;
        parameter.borderGamma = config.borderGamma;
        parameter.shadowOffsetX = config.shadowOffsetX;
        parameter.shadowOffsetY = config.shadowOffsetY;
        parameter.shadowColor = new Color(config.shadowColor);
        parameter.spaceX = config.spaceX;
        parameter.spaceY = config.spaceY;
        parameter.padTop = config.padTop;
        parameter.padLeft = config.padLeft;
        parameter.padBottom = config.padBottom;
        parameter.padRight = config.padRight;
        parameter.characters = config.characters;
        parameter.kerning = config.kerning;
        parameter.packer = config.packer;
        parameter.flip = config.flip;
        parameter.genMipMaps = config.genMipMaps;
        parameter.minFilter = config.minFilter;
        parameter.magFilter = config.magFilter;
        parameter.incremental = config.incremental;
        return parameter;
    }

    /*******************************************************************************
     * Source https://github.com/libgdx/libgdx/blob/gdx-parent-1.10.0/extensions/gdx-tools/src/com/badlogic/gdx/tools/bmfont/BitmapFontWriter#WriteFont
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
    private static void processFnt (BitmapFont.BitmapFontData data, FileHandle[] pageFiles, FileHandle fntFile, Configuration config) {

        String face = fntFile.nameWithoutExtension();
        int lineHeight = MathUtils.round(data.lineHeight);
        int base = MathUtils.round(data.capHeight + (data.flipped ? - data.ascent : data.ascent));
        boolean xml = config.fntFormat.equalsIgnoreCase("xml");
        int aa;
        switch (config.hinting) {
            case None:
            default:
                aa = 0;
                break;
            case Slight:
            case AutoSlight:
                aa = 1;
                break;
            case Medium:
            case AutoMedium:
                aa = 2;
                break;
            case Full:
            case AutoFull:
                aa = 3;
                break;
        }

        StringBuilder buf = new StringBuilder();

        if (xml) buf.append("<font>\n");

        String xmlOpen = xml ? "\t<" : "";
        String xmlCloseSelf = xml ? "/>" : "";
        String xmlTab = xml ? "\t" : "";
        String xmlClose = xml ? ">" : "";

        String xmlQuote = xml ? "\"" : "";
        String alphaChnlParams = xml ? " alphaChnl=\"0\" redChnl=\"0\" greenChnl=\"0\" blueChnl=\"0\""
                : " alphaChnl=0 redChnl=0 greenChnl=0 blueChnl=0";

        // INFO LINE
        buf.append(xmlOpen).append("info face=\"").append(face == null ? "" : face.replaceAll("\"", "'"))
                .append("\" size=").append(quote(xml, config.size)).append(" bold=").append(quote(xml, config.bold ? 1 : 0)).append(" italic=")
                .append(quote(xml, config.italic ? 1 : 0)).append(" charset=\"").append(config.charset == null ? "" : config.charset)
                .append("\" unicode=").append(quote(xml, config.unicode ? 1 : 0)).append(" stretchH=").append(quote(xml, config.stretchH))
                .append(" smooth=").append(quote(xml, !config.mono ? 1 : 0)).append(" aa=").append(quote(xml, aa)).append(" padding=")
                .append(xmlQuote).append(config.padTop).append(",").append(config.padRight).append(",").append(config.padBottom)
                .append(",").append(config.padLeft).append(xmlQuote).append(" spacing=").append(xmlQuote)
                .append(config.spaceX).append(",").append(config.spaceY).append(xmlQuote).append(xmlCloseSelf)
                .append("\n");

        // COMMON line
        buf.append(xmlOpen).append("common lineHeight=").append(quote(xml, lineHeight)).append(" base=").append(quote(xml, base))
                .append(" scaleW=").append(quote(xml, config.pageWidth)).append(" scaleH=").append(quote(xml, config.pageHeight))
                .append(" pages=").append(quote(xml, pageFiles.length)).append(" packed=").append(quote(xml, 0))
                .append(alphaChnlParams).append(xmlCloseSelf).append("\n");

        if (xml) buf.append("\t<pages>\n");

        // PAGES
        for (int i = 0; i < pageFiles.length; i ++) {
            buf.append(xmlTab).append(xmlOpen).append("page id=").append(quote(xml, i)).append(" file=\"").append(pageFiles[i].name())
                    .append("\"").append(xmlCloseSelf).append("\n");
        }

        if (xml) buf.append("\t</pages>\n");

        // CHARS
        Array<BitmapFont.Glyph> glyphs = new Array<>(config.characters.length());
        for (int i = 0; i < data.glyphs.length; i ++) {
            if (data.glyphs[i] == null) continue;

            for (int j = 0; j < data.glyphs[i].length; j ++) {
                if (data.glyphs[i][j] != null) {
                    glyphs.add(data.glyphs[i][j]);
                }
            }
        }

        buf.append(xmlOpen).append("chars count=").append(quote(xml, glyphs.size)).append(xmlClose).append("\n");

        // CHAR definitions
        for (int i = 0; i < glyphs.size; i++) {
            BitmapFont.Glyph g = glyphs.get(i);
            boolean empty = g.width == 0 || g.height == 0;
            buf.append(xmlTab).append(xmlOpen).append("char id=").append(quote(xml, String.format("%-6s", g.id), true)).append("x=")
                    .append(quote(xml, String.format("%-5s", empty ? 0 : g.srcX), true)).append("y=")
                    .append(quote(xml, String.format("%-5s", empty ? 0 : g.srcY), true)).append("width=")
                    .append(quote(xml, String.format("%-5s", empty ? 0 : g.width), true)).append("height=")
                    .append(quote(xml, String.format("%-5s", empty ? 0 : g.height), true)).append("xoffset=")
                    .append(quote(xml, String.format("%-5s", g.xoffset - config.padLeft), true)).append("yoffset=")
                    .append(quote(xml, String.format("%-5s", data.flipped ? g.yoffset + config.padTop : -(g.height + (g.yoffset + config.padTop))), true))
                    .append("xadvance=").append(quote(xml, String.format("%-5s", g.xadvance), true)).append("page=")
                    .append(quote(xml, String.format("%-5s", g.page), true)).append("chnl=").append(quote(xml, 0, true)).append(xmlCloseSelf)
                    .append("\n");
        }

        if (xml) buf.append("\t</chars>\n");

        // KERNINGS
        if (config.kerning) {
            int kernCount = 0;
            StringBuilder kernBuf = new StringBuilder();
            for (int i = 0; i < glyphs.size; i ++) {
                for (int j = 0; j < glyphs.size; j ++) {
                    BitmapFont.Glyph first = glyphs.get(i);
                    BitmapFont.Glyph second = glyphs.get(j);
                    int kern = first.getKerning((char)second.id);
                    if (kern != 0) {
                        kernCount ++;
                        kernBuf.append(xmlTab).append(xmlOpen).append("kerning first=").append(quote(xml, first.id)).append(" second=")
                                .append(quote(xml, second.id)).append(" amount=").append(quote(xml, kern, true)).append(xmlCloseSelf).append("\n");
                    }
                }
            }

            // KERN info
            buf.append(xmlOpen).append("kernings count=").append(quote(xml, kernCount)).append(xmlClose).append("\n");
            buf.append(kernBuf);

            if (xml) buf.append("\t</kernings>\n");
        }

        // Override metrics
        if (xml) buf.append("\t<metrics>\n");

        buf.append(xmlTab).append(xmlOpen)
                .append("metrics ascent=").append(quote(xml, data.ascent, true))
                .append(" descent=").append(quote(xml, data.descent, true))
                .append(" down=").append(quote(xml, data.down, true))
                .append(" capHeight=").append(quote(xml, data.capHeight, true))
                .append(" lineHeight=").append(quote(xml, data.lineHeight, true))
                .append(" spaceXAdvance=").append(quote(xml, data.spaceXadvance, true))
                .append(" xHeight=").append(quote(xml, data.xHeight, true))
                .append(xmlCloseSelf).append("\n");

        if (xml) buf.append("\t</metrics>\n");

        if (xml) buf.append("</font>");

        String charset = config.charset;
        charset = charset == null ? null : (charset.length() == 0 ? null : charset);

        fntFile.writeString(buf.toString(), false, charset);
    }

}
