# gdx-bmfont [![Java CI with Gradle](https://github.com/anyicomplex/gdx-bmfont/actions/workflows/gradle.yml/badge.svg)](https://github.com/anyicomplex/gdx-bmfont/actions/workflows/gradle.yml) [![License](https://img.shields.io/github/license/anyicomplex/gdx-bmfont)](https://github.com/anyicomplex/gdx-bmfont/blob/master/LICENSE)

CLI BitmapFont packer tool powered by Java, libGDX &amp; Picocli.

```sh
sh-5.1$ java -jar gdx-bmfont-1.0.4.jar --help
Usage: gdx-bmfont [-hikmMovV] [--border-straight] [--flip] [-b=<borderWidth>]
                  [--border-color=<borderColor>] [--border-gamma=<borderGamma>]
                  [-c=<characters>] [-C=<color>] [--charset=<charset>]
                  [-f=<size>] [-F=<txt|xml>] [-g=<gamma>]
                  [-H=<none|slight|medium|full|autoSlight|autoMedium|autoFull>]
                  [--mag-filter=<nearest|linear|mipMap|mipMapNearestNearest|mipM
                  apLinearNearest|mipMapNearestLinear|mipMapLinearLinear>]
                  [--min-filter=<nearest|linear|mipMap|mipMapNearestNearest|mipM
                  apLinearNearest|mipMapNearestLinear|mipMapLinearLinear>]
                  [-n=<name>] [-p=<padTop,padLeft,padBottom,padRight>]
                  [-P=<pageWidth,pageHeight>] [-r=<renderCount>]
                  [-s=<shadowOffsetX,shadowOffsetY>] [-S=<spaceX,spaceY>]
                  [--shadow-color=<shadowColor>]
                  [--characters-files=<charactersFile|charactersDir>...]...
                  <inputFile> <outputDir>
Generate BitmapFont from FreeType supported font file.
      <inputFile>           The FreeType supported font file.
      <outputDir>           The BitmapFont output directory.
  -b, --border-width=<borderWidth>
                            Border width in pixels, 0 to disable.
      --border-color=<borderColor>
                            Border color; only used if borderWidth > 0. Should
                              be hex string, eg. 0xFFFFFF #FF000000 FFFFFF.
      --border-gamma=<borderGamma>
                            Values < 1 increase the border size.
      --border-straight     true for straight (mitered), false for rounded
                              borders.
  -c, --characters=<characters>
                            The characters the font should contain.
  -C, --color=<color>       Foreground color (required for non-black borders).
                              Should be hex string, eg. 0xFFFFFF #FF000000
                              FFFFFF.
      --characters-files=<charactersFile|charactersDir>...
                            The characters files and dirs will be read
                              recursively.
      --charset=<charset>   The charset will be used to parse characters files.
  -f, --font-size=<size>    The BitmapFont size in pixels.
  -F, --fnt-format=<txt|xml>
                            The output .fnt file format.
      --flip                Whether to flip the font vertically.
  -g, --gamma=<gamma>       Glyph gamma. Values > 1 reduce antialiasing.
  -h, --help                Show this help message and exit.
  -H, --hinting=<none|slight|medium|full|autoSlight|autoMedium|autoFull>
                            Strength of hinting.
  -i, --incremental         When true, glyphs are rendered on the fly to the
                              font\'s glyph page textures as they are needed.
  -k, --kerning             Whether the font should include kerning.
  -m, --mono                If true, font smoothing is disabled.
  -M, --gen-mipmaps         Whether to generate mip maps for the resulting
                              texture.
      --mag-filter=<nearest|linear|mipMap|mipMapNearestNearest|mipMapLinearNeare
        st|mipMapNearestLinear|mipMapLinearLinear>
                            Magnification filter.
      --min-filter=<nearest|linear|mipMap|mipMapNearestNearest|mipMapLinearNeare
        st|mipMapNearestLinear|mipMapLinearLinear>
                            Minification filter.
  -n, --name=<name>         The output file base name.
  -o, --override            Whether override exist file
  -p, --paddings=<padTop,padLeft,padBottom,padRight>
                            Pixels to add to the glyph in the texture.
  -P, --page-size=<pageWidth,pageHeight>
                            The size of each output image.
  -r, --render-count=<renderCount>
                            Number of times to render the glyph. Useful with a
                              shadow or border, so it doesn\'t show through the
                              glyph.
  -s, --shadow-offsets=<shadowOffsetX,shadowOffsetY>
                            Offsets of text shadow on X and Y axis in pixels, 0
                              to disable.
  -S, --spacing=<spaceX,spaceY>
                            Pixels to add to glyph spacing when text is
                              rendered.
      --shadow-color=<shadowColor>
                            Shadow color; only used if shadowOffset > 0. If
                              alpha component is 0, no shadow is drawn but
                              characters are still offset by shadowOffset.
                              Should be hex string, eg. 0xFFFFFF #FF000000
                              FFFFFF.
  -v, --verbose             Enable verbose output.
  -V, --version             Print version information and exit.
sh-5.1$
```
