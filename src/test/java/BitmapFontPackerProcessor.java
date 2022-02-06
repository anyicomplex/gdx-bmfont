import com.anyicomplex.gdx.tools.bmfont.BitmapFontPacker;
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
                int exitCode = BitmapFontPacker.process(
                        Gdx.files.absolute(System.getProperty("user.home") + "/test.ttf"),
                        fntDir, new BitmapFontPacker.Configuration(), true);
                if (exitCode != BitmapFontPacker.CODE_SUCCESS) System.exit(exitCode);
                font = new BitmapFont(Gdx.files.absolute(fntDir + "/test.fnt"));
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
