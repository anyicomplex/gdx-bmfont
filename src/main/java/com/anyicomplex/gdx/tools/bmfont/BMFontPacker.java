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
import com.badlogic.gdx.utils.GdxRuntimeException;

public class BMFontPacker {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FILE_EXISTS = 1;

    public static class Settings {
        public String name = null;
        public int pageWidth = -1;
        public int pageHeight = -1;
        public String fntFormat = "txt";

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

    public static int generate(FileHandle srcFile, FileHandle dstDir, Settings settings) {
        return generate(srcFile, dstDir, settings, true);
    }

    public static int generate(FileHandle srcFile, FileHandle dstDir, Settings settings, boolean override) {
        if (settings == null) error("Settings cannot be null.");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(srcFile);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = parameter(settings);
        // .png file(s)
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
        BitmapFont bitmapFont = generator.generateFont(parameter);
        String fileName;
        if (settings.name == null || settings.name.length() < 1) {
            fileName = srcFile.nameWithoutExtension();
        }
        else {
            fileName = settings.name;
        }
        // check whether file(s) exists
        for (int i = 0; i < bitmapFont.getRegions().size; i ++) {
            FileHandle image = Gdx.files.absolute(dstDir.path() + "/" + fileName + i + ".png");
            if (image.exists() && !override) {
                bitmapFont.dispose();
                return 1;
            }
        }
        for (int i = 0; i < bitmapFont.getRegions().size; i ++) {
            Pixmap pixmap = bitmapFont.getRegion(i).getTexture().getTextureData().consumePixmap();
            FileHandle image = Gdx.files.absolute(dstDir.path() + "/" + fileName + i + ".png");
            PixmapIO.writePNG(image, pixmap);
        }
        bitmapFont.dispose();
        // .fnt file
        return 0;
    }

    public static void error(String message) {
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

}
