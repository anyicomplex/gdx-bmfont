package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

import static com.anyicomplex.gdx.tools.bmfont.Utils.stringNotEmpty;

@CommandLine.Command(name = "gdx-bmfont", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Generate BitmapFont from FreeType supported font file.")
public class GdxBMFont implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "The FreeType font file.")
    private File srcFile;

    @CommandLine.Parameters(index = "1", description = "The output directory.")
    private File dstDir;

    @CommandLine.Option(names = {"-n", "--name"}, description = "The output file base name.")
    private String name;
    @CommandLine.Option(names = {"-P", "--page-size"}, paramLabel = "<pageWidth,pageHeight>", description = "The size of each output image.")
    private IntIntWrapper pageSize;
    @CommandLine.Option(names = {"-F", "--fnt-format"}, paramLabel = "<txt|xml>", description = "The output .fnt file format.")
    private FntFormatWrapper fntFormat;
    @CommandLine.Option(names = {"-f", "--font-size"}, defaultValue = "16", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "The BitmapFont size in pixels.")
    private int size;
    @CommandLine.Option(names = {"-m", "--mono"}, description = "If true, font smoothing is disabled.")
    private boolean mono;

    public FreeTypeFontGenerator.Hinting hinting = FreeTypeFontGenerator.Hinting.AutoMedium;
    public Color color = Color.WHITE;

    @CommandLine.Option(names = {"-g", "--gamma"}, defaultValue = "1.8f", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Glyph gamma. Values > 1 reduce antialiasing.")
    private float gamma;
    @CommandLine.Option(names = {"-r", "--render-count"}, defaultValue = "2", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph.")
    private int renderCount;
    @CommandLine.Option(names = {"-b", "--border-width"}, defaultValue = "0", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Border width in pixels, 0 to disable.")
    private float borderWidth;

    public Color borderColor = Color.BLACK;

    @CommandLine.Option(names = "--border-straight", defaultValue = "false", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "true for straight (mitered), false for rounded borders.")
    private boolean borderStraight;
    @CommandLine.Option(names = "--border-gamma", defaultValue = "1.8f", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Values < 1 increase the border size.")
    private float borderGamma;
    @CommandLine.Option(names = {"-s", "--shadow-offsets"}, paramLabel = "<shadowOffsetX,shadowOffsetY>",
            description = "Offsets of text shadow on X and Y axis in pixels, 0 to disable.")
    private IntIntWrapper shadowOffsets;

    public Color shadowColor = new Color(0, 0, 0, 0.75f);

    @CommandLine.Option(names = {"-S", "--spacing"}, paramLabel = "<spaceX,spaceY>",
            description = "Pixels to add to glyph spacing when text is rendered.")
    private IntIntWrapper spacing;
    @CommandLine.Option(names = {"-p", "--paddings"}, paramLabel = "<padTop,padLeft,padBottom,padRight>",
            description = "Pixels to add to the glyph in the texture.")
    private IntIntIntIntWrapper paddings;
    @CommandLine.Option(names = {"-c", "--characters"}, description = "The characters the font should contain. If '\\0' is not included then BitmapFont.BitmapFontData.missingGlyph is not set.")
    private String characters;
    @CommandLine.Option(names = {"-k", "--kerning"}, defaultValue = "true", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Whether the font should include kerning.")
    private boolean kerning;

    public PixmapPacker packer = null;

    @CommandLine.Option(names = "--flip", defaultValue = "false", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Whether to flip the font vertically.")
    private boolean flip;

    public boolean genMipMaps = false;
    public Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
    public Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;

    @CommandLine.Option(names = {"-i", "--incremental"}, description = "When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed.")
    private boolean incremental;

    @CommandLine.Option(names = {"-o", "--override"}, defaultValue = "false",
            description = "Whether override exist file")
    private boolean override;

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
                int exitCode = new CommandLine(new GdxBMFont())
                        .registerConverter(IntIntWrapper.class, new IntIntWrapperConverter())
                        .registerConverter(IntIntIntIntWrapper.class, new IntIntIntIntWrapperConverter())
                        .registerConverter(FntFormatWrapper.class, new FntFormatConverter())
                        .execute(args);
                System.exit(exitCode);
            }
        }, configuration);
    }

    @Override
    public Integer call() throws Exception {
        BMFontPacker.Settings settings = new BMFontPacker.Settings();
        if (pageSize != null) {
            settings.pageWidth = pageSize.arg0;
            settings.pageHeight = pageSize.arg1;
        }
        settings.borderWidth = borderWidth;
        settings.borderGamma = borderGamma;
        settings.borderStraight = borderStraight;
        if (stringNotEmpty(characters)) settings.characters = characters;
        if (fntFormat != null) settings.fntFormat = fntFormat.format;
        settings.flip = flip;
        settings.gamma = gamma;
        settings.incremental = incremental;
        settings.kerning = kerning;
        settings.mono = mono;
        if (stringNotEmpty(name)) settings.name = name;
        if (paddings != null) {
            settings.padTop = paddings.arg0;
            settings.padLeft = paddings.arg1;
            settings.padBottom = paddings.arg2;
            settings.padRight = paddings.arg3;
        }
        settings.renderCount = renderCount;
        if (spacing != null) {
            settings.spaceX = spacing.arg0;
            settings.spaceY = spacing.arg1;
        }
        if (shadowOffsets != null) {
            settings.shadowOffsetX = shadowOffsets.arg0;
            settings.shadowOffsetY = shadowOffsets.arg1;
        }
        settings.size = size;
        int result = BMFontPacker.process(Gdx.files.absolute(srcFile.getAbsolutePath()), Gdx.files.absolute(dstDir.getAbsolutePath()), settings, override);
        if (result == BMFontPacker.CODE_FILE_EXISTS) {
            System.err.println("BitmapFont file(s) already exists.");
            return 1;
        }
        return 0;
    }

    private static class IntIntWrapper {
        int arg0, arg1;
        public IntIntWrapper(int arg0, int arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }
    }
    
    private static class IntIntIntIntWrapper {
        int arg0, arg1, arg2, arg3;
        public IntIntIntIntWrapper(int arg0, int arg1, int arg2, int arg3) {
            this.arg0 = arg0;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
        }
    }

    private static class FntFormatWrapper {
        String format;
        public FntFormatWrapper(String format) {
            this.format = format;
        }
    }

    private static class IntIntWrapperConverter implements CommandLine.ITypeConverter<IntIntWrapper> {
        @Override
        public IntIntWrapper convert(String value) throws Exception {
            String[] values = value.replaceAll(" ", "").split(",");
            if (values.length != 2) throw new CommandLine.TypeConversionException("Parameter count mismatch!");
            return new IntIntWrapper(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
        }
    }

    private static class IntIntIntIntWrapperConverter implements CommandLine.ITypeConverter<IntIntIntIntWrapper> {
        @Override
        public IntIntIntIntWrapper convert(String value) throws Exception {
            String[] values = value.replaceAll(" ", "").split(",");
            if (values.length != 4) throw new CommandLine.TypeConversionException("Parameter count mismatch!");
            return new IntIntIntIntWrapper(Integer.parseInt(values[0]), Integer.parseInt(values[1]), 
                    Integer.parseInt(values[2]), Integer.parseInt(values[3]));
        }
    }

    private static class FntFormatConverter implements CommandLine.ITypeConverter<FntFormatWrapper> {
        @Override
        public FntFormatWrapper convert(String value) throws Exception {
            Array<String> allowed = new Array<>(2);
            allowed.addAll("txt, xml");
            if (!allowed.contains(value.toLowerCase(), false)) return new FntFormatWrapper(value);
            else throw new CommandLine.TypeConversionException("Parameter type mismatch!");
        }
    }

}
