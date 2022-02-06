package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import static com.anyicomplex.gdx.tools.bmfont.Utils.quote;

public class BMFontPacker {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FILE_EXISTS = 1;

    public static class Settings {
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

    public static int process(FileHandle srcFile, FileHandle dstDir, Settings settings) {
        return process(srcFile, dstDir, settings, true);
    }

    public static int process(FileHandle srcFile, FileHandle dstDir, Settings settings, boolean override) {
        if (settings == null) error("Settings cannot be null.");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(srcFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = parameter(settings);
        // page(s)
        if (settings.pageWidth != -1 && settings.pageHeight != -1) {
            if (parameter.packer == null) {
                parameter.packer = new PixmapPacker(settings.pageWidth, settings.pageHeight, Pixmap.Format.RGBA8888,
                        1, false, new PixmapPacker.SkylineStrategy());
                parameter.packer.setTransparentColor(settings.color);
                parameter.packer.getTransparentColor().a = 0;
                if (settings.borderWidth > 0) {
                    parameter.packer.setTransparentColor(settings.borderColor);
                    parameter.packer.getTransparentColor().a = 0;
                }
            }
        }
        FreeTypeFontGenerator.FreeTypeBitmapFontData data = new FreeTypeFontGenerator.FreeTypeBitmapFontData();
        BitmapFont bitmapFont = generator.generateFont(parameter, data);
        String fileName;
        if (settings.name == null || settings.name.length() < 1) {
            fileName = srcFile.nameWithoutExtension();
        }
        else {
            fileName = settings.name;
        }
        // check whether file(s) exists
        for (int i = 0; i < bitmapFont.getRegions().size; i ++) {
            FileHandle pageFile = Gdx.files.absolute(dstDir.path() + "/" + fileName +
                    (bitmapFont.getRegions().size == 1 ? ".png" : "_" + i + ".png"));
            if (pageFile.exists() && !override) {
                bitmapFont.dispose();
                return CODE_FILE_EXISTS;
            }
        }
        FileHandle fntFile = Gdx.files.absolute(dstDir.path() + "/" + fileName + ".fnt");
        if (fntFile.exists() && !override) {
            bitmapFont.dispose();
            return CODE_FILE_EXISTS;
        }
        FileHandle[] pageFiles = new FileHandle[bitmapFont.getRegions().size];
        for (int i = 0; i < bitmapFont.getRegions().size; i ++) {
            Pixmap pixmap = bitmapFont.getRegion(i).getTexture().getTextureData().consumePixmap();
            FileHandle pageFile = Gdx.files.absolute(dstDir.path() + "/" + fileName +
                    (bitmapFont.getRegions().size == 1 ? ".png" : "_" + i + ".png"));
            pageFiles[i] = pageFile;
            PixmapIO.writePNG(pageFile, pixmap);
        }
        // .fnt
        processFnt(data, pageFiles, fntFile, settings);
        bitmapFont.dispose();
        return CODE_SUCCESS;
    }

    private static void error(String message) {
        if (message == null) throw new GdxRuntimeException((String) null);
        throw new GdxRuntimeException("[BMFontPacker] " + message);
    }

    public static FreeTypeFontGenerator.FreeTypeFontParameter parameter(Settings settings) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = settings.size;
        parameter.mono = settings.mono;
        parameter.hinting = settings.hinting;
        parameter.color = new Color(settings.color);
        parameter.gamma = settings.gamma;
        parameter.renderCount = settings.renderCount;
        parameter.borderWidth = settings.borderWidth;
        parameter.borderColor = new Color(settings.borderColor);
        parameter.borderStraight = settings.borderStraight;
        parameter.borderGamma = settings.borderGamma;
        parameter.shadowOffsetX = settings.shadowOffsetX;
        parameter.shadowOffsetY = settings.shadowOffsetY;
        parameter.shadowColor = new Color(settings.shadowColor);
        parameter.spaceX = settings.spaceX;
        parameter.spaceY = settings.spaceY;
        parameter.padTop = settings.padTop;
        parameter.padLeft = settings.padLeft;
        parameter.padBottom = settings.padBottom;
        parameter.padRight = settings.padRight;
        parameter.characters = settings.characters;
        parameter.kerning = settings.kerning;
        parameter.packer = settings.packer;
        parameter.flip = settings.flip;
        parameter.genMipMaps = settings.genMipMaps;
        parameter.minFilter = settings.minFilter;
        parameter.magFilter = settings.magFilter;
        parameter.incremental = settings.incremental;
        return parameter;
    }

    /**
     * source https://github.com/mattdesl/gdx-fontpack/blob/master/gdx-fontpack/src/mdesl/font/BitmapFontWriter.java
     */
    private static void processFnt (BitmapFont.BitmapFontData data, FileHandle[] pageFiles, FileHandle fntFile, Settings settings) {
        boolean xml = settings.fntFormat.equalsIgnoreCase("xml");
        StringBuilder builder = new StringBuilder();
        if (xml) {
            builder.append("<font>\n");
        }
        String xmlTab = xml ? "\t" : "";
        String xmlOpen = xml ? "\t<" : "";
        String xmlClose = xml ? ">" : "";
        String xmlCloseSelf = xml ? "/>" : "";
        String xmlQuote = xml ? "\"" : "";
        String chnlParams = xml ?
                " alphaChnl=\"0\" redChnl=\"0\" greenChnl=\"0\" blueChnl=\"0\"" : " alphaChnl=0 redChnl=0 greenChnl=0 blueChnl=0";

        builder.append(xmlOpen)
                .append("info face=\"")
                .append(fntFile.nameWithoutExtension() == null ? "" : fntFile.nameWithoutExtension().replaceAll("\"", "'"))
                .append("\" size=").append(quote(xml, settings.size))
                .append(" bold=").append(quote(xml, settings.bold ? 1 : 0))
                .append(" italic=").append(quote(xml, settings.italic ? 1 : 0))
                .append(" charset=\"").append(settings.charset == null ? "" : settings.charset)
                .append("\" unicode=").append(quote(xml, settings.unicode ? 1 : 0))
                .append(" stretchH=").append(quote(xml, settings.stretchH))
                .append(" smooth=").append(quote(xml, !settings.mono ? 1 : 0))
                .append(" aa=").append(quote(xml, settings.renderCount))
                .append(" padding=")
                .append(xmlQuote)
                .append(settings.padTop).append(",")
                .append(settings.padBottom).append(",")
                .append(settings.padLeft).append(",")
                .append(settings.padRight)
                .append(xmlQuote)
                .append(" spacing=")
                .append(xmlQuote)
                .append(settings.spaceX).append(",")
                .append(settings.spaceY)
                .append(xmlQuote)
                .append(xmlCloseSelf)
                .append("\n");
        builder.append(xmlOpen)
                .append("common lineHeight=").append(quote(xml, data.lineHeight))
                .append(" base=").append(quote(xml, data.capHeight + (data.flipped ? - data.ascent : data.ascent)))
                .append(" scaleW=").append(quote(xml, settings.pageWidth))
                .append(" scaleH=").append(quote(xml, settings.pageHeight))
                .append(" pages=").append(quote(xml, pageFiles.length))
                .append(" packed=").append(quote(xml, 0))
                .append(chnlParams)
                .append(xmlCloseSelf)
                .append("\n");
        if (xml) builder.append("\t<pages>\n");
        for (int i = 0; i < pageFiles.length; i ++) {
            builder.append(xmlTab)
                    .append(xmlOpen)
                    .append("page id=")
                    .append(quote(xml, i))
                    .append(" file=\"")
                    .append(pageFiles[i].name())
                    .append("\"")
                    .append(xmlCloseSelf)
                    .append("\n");
        }
        if (xml) builder.append("\t</pages>\n");
        Array<BitmapFont.Glyph> glyphs = new Array<>(settings.characters.length());
        for (int i = 0; i < data.glyphs.length; i ++) {
            if (data.glyphs[i] == null) continue;
            for (int j = 0; j < data.glyphs[i].length; j ++) {
                if (data.glyphs[i][j] != null) {
                    glyphs.add(data.glyphs[i][j]);
                }
            }
        }
        builder.append(xmlOpen)
                .append("chars count=").append(quote(xml, glyphs.size))
                .append(xmlClose)
                .append("\n");
        for (int i = 0; i < glyphs.size; i ++) {
            BitmapFont.Glyph g = glyphs.get(i);
            builder.append(xmlTab)
                    .append(xmlOpen)
                    .append("char id=")
                    .append(quote(xml, String.format("%-5s", g.id), true))
                    .append("x=").append(quote(xml, String.format("%-5s", g.srcX), true))
                    .append("y=").append(quote(xml, String.format("%-5s", g.srcY), true))
                    .append("width=").append(quote(xml, String.format("%-5s", g.width), true))
                    .append("height=").append(quote(xml, String.format("%-5s", g.height), true))
                    .append("xoffset=").append(quote(xml, String.format("%-5s", g.xoffset), true))
                    .append("yoffset=").append(quote(xml, String.format("%-5s", data.flipped ? g.yoffset : - (g.height + g.yoffset)), true))
                    .append("xadvance=").append(quote(xml, String.format("%-5s", g.xadvance), true))
                    .append("page=").append(quote(xml, String.format("%-5s", g.page), true))
                    .append("chnl=").append(quote(xml, 0, true))
                    .append(xmlCloseSelf)
                    .append("\n");
        }
        if (xml) builder.append("\t</chars>\n");
        
        if (settings.kerning) {
            int kernCount = 0;
            StringBuilder kernings = new StringBuilder();
            for (int i = 0; i < glyphs.size; i ++) {
                for (int j = 0; j < glyphs.size; j ++) {
                    BitmapFont.Glyph first = glyphs.get(i);
                    BitmapFont.Glyph second = glyphs.get(j);
                    int kern = first.getKerning((char)second.id);
                    if (kern!=0) {
                        kernCount ++;
                        kernings.append(xmlTab)
                                .append(xmlOpen)
                                .append("kerning first=").append(quote(xml, first.id))
                                .append(" second=").append(quote(xml, second.id))
                                .append(" amount=").append(quote(xml, kern, true))
                                .append(xmlCloseSelf)
                                .append("\n");
                    }
                }
            }
            builder.append(xmlOpen)
                    .append("kernings count=").append(quote(xml, kernCount))
                    .append(xmlClose)
                    .append("\n");
            builder.append(kernings);
            if (xml) {
                builder.append("\t</kernings>\n");
                builder.append("</font>");
            }
        }
        fntFile.writeString(builder.toString(), false);
    }

}
