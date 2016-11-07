package com.mygdx.indimaze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;

public class IndiscriminateMazeGame extends ApplicationAdapter {
  NetworkCommander commander;
  private AfterEffect grid;

  String clientId = "desktop-demo";

  public IndiscriminateMazeGame(String android_id) {
    if (android_id != null) {
      clientId = android_id;
    }
  }

  @Override
  public void create() {
    final Matrix4 pixelPerfectProjection = new Matrix4();
    pixelPerfectProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    if (Gdx.graphics.getHeight() > Gdx.graphics.getWidth()) {
      throw new RuntimeException("Game requires a landscape orientation!");
    }

    final int halfDifference = (Gdx.graphics.getWidth() - Gdx.graphics.getHeight()) / 2;

    final float gridSize = 64;

    final float simulatorZoom = Gdx.graphics.getHeight() / gridSize;

    this.commander = new NetworkCommander(clientId);
    this.grid = new AfterEffect(this.commander.getGameState(), new GameGrid(this.commander.getGameState()));


    Gdx.input.setInputProcessor(new GestureDetectorWithKeyboard(new GameGestures()));
  }

  @Override
  public void render() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    this.grid.render();

  }

  @Override
  public void dispose() {
    this.grid.dispose();
    this.commander.dispose();
  }

  private class GameGestures extends GestureDetector.GestureAdapter {
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
      if (Math.abs(velocityX) > Math.abs(velocityY) * 2) {
        // left / right
        if (velocityX > 0) {
          IndiscriminateMazeGame.this.commander.right();
        } else {
          IndiscriminateMazeGame.this.commander.left();
        }
        return true;
      } else if (Math.abs(velocityY) > Math.abs(velocityX) * 2){
        // up / down
        if (velocityY < 0) {
          IndiscriminateMazeGame.this.commander.up();
        } else {
          IndiscriminateMazeGame.this.commander.down();
        }
        return true;
      }
      return super.fling(velocityX, velocityY, button);
    }
  }


  class GestureDetectorWithKeyboard extends GestureDetector {

    public GestureDetectorWithKeyboard(GestureListener listener) {
      super(listener);
    }

    @Override
    public boolean keyDown(final int keycode) {
      switch (keycode) {
        case Input.Keys.UP:
          IndiscriminateMazeGame.this.commander.up();
          return true;
        case Input.Keys.DOWN:
          IndiscriminateMazeGame.this.commander.down();
          return true;
        case Input.Keys.LEFT:
          IndiscriminateMazeGame.this.commander.left();
          return true;
        case Input.Keys.RIGHT:
          IndiscriminateMazeGame.this.commander.right();
          return true;
      }
      return false;
    }
  }

}
