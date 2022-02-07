package com.anyicomplex.gdx.tools.bmfont;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

import static com.anyicomplex.gdx.tools.bmfont.Utils.*;

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
    @CommandLine.Option(names = {"-H", "--hinting"}, paramLabel = "<none|slight|medium|full|autoSlight|autoMedium|autoFull>",
            description = "Strength of hinting.")
    private FreeTypeFontGenerator.Hinting hinting;
    @CommandLine.Option(names = {"-C", "--color"}, description = "Foreground color (required for non-black borders). Should be hex string, eg. 0xFFFFFF #FF000000 FFFFFF.")
    private Color color;
    @CommandLine.Option(names = {"-g", "--gamma"}, defaultValue = "1.8f", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Glyph gamma. Values > 1 reduce antialiasing.")
    private float gamma;
    @CommandLine.Option(names = {"-r", "--render-count"}, defaultValue = "2", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Number of times to render the glyph. Useful with a shadow or border, so it doesn't show through the glyph.")
    private int renderCount;
    @CommandLine.Option(names = {"-b", "--border-width"}, defaultValue = "0", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Border width in pixels, 0 to disable.")
    private float borderWidth;
    @CommandLine.Option(names = "--border-color", description = "Border color; only used if borderWidth > 0. Should be hex string, eg. 0xFFFFFF #FF000000 FFFFFF.")
    public Color borderColor;
    @CommandLine.Option(names = "--border-straight", defaultValue = "false", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "true for straight (mitered), false for rounded borders.")
    private boolean borderStraight;
    @CommandLine.Option(names = "--border-gamma", defaultValue = "1.8f", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Values < 1 increase the border size.")
    private float borderGamma;
    @CommandLine.Option(names = {"-s", "--shadow-offsets"}, paramLabel = "<shadowOffsetX,shadowOffsetY>",
            description = "Offsets of text shadow on X and Y axis in pixels, 0 to disable.")
    private IntIntWrapper shadowOffsets;
    @CommandLine.Option(names = "--shadow-color",
            description = "Shadow color; only used if shadowOffset > 0. If alpha component is 0, " +
                    "no shadow is drawn but characters are still offset by shadowOffset. Should be hex string, eg. 0xFFFFFF #FF000000 FFFFFF.")
    private Color shadowColor;
    @CommandLine.Option(names = {"-S", "--spacing"}, paramLabel = "<spaceX,spaceY>",
            description = "Pixels to add to glyph spacing when text is rendered.")
    private IntIntWrapper spacing;
    @CommandLine.Option(names = {"-p", "--paddings"}, paramLabel = "<padTop,padLeft,padBottom,padRight>",
            description = "Pixels to add to the glyph in the texture.")
    private IntIntIntIntWrapper paddings;
    @CommandLine.Option(names = {"-c", "--characters"}, description = "The characters the font should contain.")
    private String characters;
    @CommandLine.Option(names = "--characters-files", paramLabel = "<charactersFile|charactersDir>", arity = "1..*", description = "The characters files and dirs will be read recursively.")
    private File[] charactersFiles;
    @CommandLine.Option(names = {"-k", "--kerning"}, defaultValue = "true", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Whether the font should include kerning.")
    private boolean kerning;
    @CommandLine.Option(names = "--flip", defaultValue = "false", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Whether to flip the font vertically.")
    private boolean flip;
    @CommandLine.Option(names = {"-M", "--gen-mipmaps"}, defaultValue = "false", showDefaultValue = CommandLine.Help.Visibility.NEVER,
            description = "Whether to generate mip maps for the resulting texture.")
    private boolean genMipMaps;
    @CommandLine.Option(names = "--min-filter", paramLabel = "<nearest|linear|mipMap|mipMapNearestNearest|mipMapLinearNearest|" +
            "mipMapNearestLinear|mipMapLinearLinear>", description = "Minification filter.")
    private Texture.TextureFilter minFilter;
    @CommandLine.Option(names = "--mag-filter", paramLabel = "<nearest|linear|mipMap|mipMapNearestNearest|mipMapLinearNearest|" +
            "mipMapNearestLinear|mipMapLinearLinear>", description = "Magnification filter.")
    private Texture.TextureFilter magFilter;
    @CommandLine.Option(names = {"-i", "--incremental"}, description = "When true, glyphs are rendered on the fly to the font's glyph page textures as they are needed.")
    private boolean incremental;
    @CommandLine.Option(names = {"-o", "--override"}, defaultValue = "false",
            description = "Whether override exist file")
    private boolean override;

    @CommandLine.Option(names = {"-v", "--verbose"}, defaultValue = "false", description = "Enable verbose output.")
    private volatile static boolean VERBOSE;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setInitialVisible(false);
        configuration.setDecorated(false);
        configuration.setWindowedMode(1, 1);
        configuration.disableAudio(true);
        platformUtils = new Lwjgl3PlatformUtils();
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create() {
                super.create();
                int exitCode = new CommandLine(new GdxBMFont())
                        .registerConverter(IntIntWrapper.class, new IntIntWrapperConverter())
                        .registerConverter(IntIntIntIntWrapper.class, new IntIntIntIntWrapperConverter())
                        .registerConverter(FntFormatWrapper.class, new FntFormatConverter())
                        .registerConverter(Color.class, new ColorConverter())
                        .registerConverter(FreeTypeFontGenerator.Hinting.class, new HintingConverter())
                        .registerConverter(Texture.TextureFilter.class, new TextureFilterConverter())
                        .execute(args);
                System.exit(exitCode);
            }
        }, configuration);
    }

    @Override
    public Integer call() throws Exception {
        verbose("Generating BitmapFontPacker config...");
        BitmapFontPacker.Configuration config = new BitmapFontPacker.Configuration();
        if (pageSize != null) {
            config.pageWidth = pageSize.arg0;
            config.pageHeight = pageSize.arg1;
        }
        config.borderWidth = borderWidth;
        config.borderGamma = borderGamma;
        config.borderStraight = borderStraight;
        if (stringNotEmpty(characters)) config.characters = characters;
        if (charactersFiles != null) {
            verbose("Parsing characters files...");
            FileHandle[] files = new FileHandle[charactersFiles.length];
            for (int i = 0; i < files.length; i ++) {
                files[i] = Gdx.files.absolute(charactersFiles[i].getAbsolutePath());
            }
            config.characters = removeDuplicateChars(config.characters + readCharsFromFiles(files));
            verbose("Characters files parsed successfully.");
        }
        if (fntFormat != null) config.fntFormat = fntFormat.format;
        config.flip = flip;
        config.gamma = gamma;
        config.incremental = incremental;
        config.kerning = kerning;
        config.mono = mono;
        if (stringNotEmpty(name)) config.name = name;
        if (paddings != null) {
            config.padTop = paddings.arg0;
            config.padLeft = paddings.arg1;
            config.padBottom = paddings.arg2;
            config.padRight = paddings.arg3;
        }
        config.renderCount = renderCount;
        if (spacing != null) {
            config.spaceX = spacing.arg0;
            config.spaceY = spacing.arg1;
        }
        if (shadowOffsets != null) {
            config.shadowOffsetX = shadowOffsets.arg0;
            config.shadowOffsetY = shadowOffsets.arg1;
        }
        config.size = size;
        if (color != null) config.color = color;
        if (borderColor != null) config.borderColor = borderColor;
        if (shadowColor != null) config.shadowColor = shadowColor;
        if (hinting != null) config.hinting = hinting;
        if (minFilter != null) config.minFilter = minFilter;
        if (magFilter != null) config.magFilter = magFilter;
        config.genMipMaps = genMipMaps;
        BitmapFontPacker.VERBOSE = VERBOSE;
        verbose("BitmapFontPacker config generated successfully.");
        verbose("Processing BitmapFontPacker...");
        int result = BitmapFontPacker.process(Gdx.files.absolute(srcFile.getAbsolutePath()), Gdx.files.absolute(dstDir.getAbsolutePath()), config, override);
        if (result != BitmapFontPacker.CODE_SUCCESS) {
            error("BitmapFontPacker processed failed with exit code " + result + ".");
            return result;
        }
        verbose("BitmapFontPacker processed successfully.");
        return result;
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
            allowed.addAll("txt", "xml");
            if (allowed.contains(value.toLowerCase(), false)) return new FntFormatWrapper(value);
            throw new CommandLine.TypeConversionException("Parameter type mismatch!");
        }
    }

    private static class ColorConverter implements CommandLine.ITypeConverter<Color> {
        @Override
        public Color convert(String value) throws Exception {
            value = value.replace("#", "").replace("0x", "");
            return new Color(Integer.parseUnsignedInt(value, 16));
        }
    }

    private static class HintingConverter implements CommandLine.ITypeConverter<FreeTypeFontGenerator.Hinting> {
        @Override
        public FreeTypeFontGenerator.Hinting convert(String value) throws Exception {
            value = value.toLowerCase();
            for (FreeTypeFontGenerator.Hinting hinting : FreeTypeFontGenerator.Hinting.values()) {
                if (hinting.name().toLowerCase().equals(value)) return hinting;
            }
            throw new CommandLine.TypeConversionException("Parameter type mismatch!");
        }
    }

    private static class TextureFilterConverter implements CommandLine.ITypeConverter<Texture.TextureFilter> {
        @Override
        public Texture.TextureFilter convert(String value) throws Exception {
            value = value.toLowerCase();
            for (Texture.TextureFilter filter : Texture.TextureFilter.values()) {
                if (filter.name().toLowerCase().equals(value)) return filter;
            }
            throw new CommandLine.TypeConversionException("Parameter type mismatch!");
        }
    }

    private static void verbose(String message) {
        if (stringNotEmpty(message) && VERBOSE) {
            Utils.verbose("[GdxBMFont] " + message);
        }
    }

    private static void error(String message) {
        if (stringNotEmpty(message) && VERBOSE) {
            Utils.error("[GdxBMFont] " + message);
        }
    }

}
