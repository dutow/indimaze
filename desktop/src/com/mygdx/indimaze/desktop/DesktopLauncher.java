package com.mygdx.indimaze.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.mygdx.indimaze.IndiscriminateMazeGame;

public class DesktopLauncher {
  public static void main(final String[] arg) {
    final TexturePacker.Settings settings = new TexturePacker.Settings();
    TexturePacker.process(settings, "../../atlases/grid/", ".", "grid");

    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    if (false) {
      config.fullscreen  = true;
      config.width = 1920;
      config.height  = 1080;
    } else {
      config.width = 1280;
      config.height  = 720;
    }
    new LwjglApplication(new IndiscriminateMazeGame(null), config);
  }
}
