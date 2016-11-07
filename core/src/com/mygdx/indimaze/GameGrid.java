package com.mygdx.indimaze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Matrix4;
import hu.cancellar.indimaze.map.*;

public class GameGrid {

  static final int CENTER_X = 26;
  static final int CENTER_Y = 26;

  TextureAtlas atlas;

  private SpriteBatch batch;

  private Sprite theOne;
  private Sprite wall;
  private Sprite fairy;
  private Sprite farmer;
  private Sprite cow;
  private final Sprite target;
  private final Sprite progress;
  private GameState gameState;

  public GameGrid(final GameState gameState) {
    this.gameState = gameState;
    this.batch = new SpriteBatch();
    this.batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 64, 64));

    this.atlas = new TextureAtlas(Gdx.files.internal("grid.atlas"));
    this.theOne = this.atlas.createSprite("the_one");
    this.wall = this.atlas.createSprite("wall");
    this.fairy = this.atlas.createSprite("fairy");
    this.farmer = this.atlas.createSprite("farmer");
    this.cow = this.atlas.createSprite("tehen");
    this.target = this.atlas.createSprite("target");
    this.progress = this.atlas.createSprite("progress");
  }

  public void render() {
    this.batch.begin();
    renderGrid();
    renderNextAction();
    renderProgress();
    this.batch.end();
  }

  public void renderGrid() {

    final Level lev = this.gameState.currentLevelAround;

    if (lev == null) return;


    for (int iy = -3; iy <= 3; ++iy) {
      for (int ix = -3; ix <= 3; ++ix) {
        final MapObject mo = lev.at(new Coordinate(ix + 3, iy + 3));
        Sprite spr = null;
        if (mo != null) {
          spr = spriteFor(mo);
        }
        if (ix == 0 && iy == 0) spr = this.theOne;
        if (spr != null) this.batch.draw(spr, CENTER_X + 12 * ix, CENTER_Y + 12 * iy);
      }
    }


  }


  private void renderNextAction() {
    if(this.gameState.nextAction != null && this.gameState.nextAction != Player.ActionType.NOPE && this.gameState.nextAction != Player.ActionType.NPC) {
      int x = CENTER_X;
      int y = CENTER_Y;
      switch(gameState.nextAction) {
        case UP: y += 12; break;
        case DOWN: y -= 12; break;
        case LEFT: x -=12; break;
        case RIGHT: x +=12; break;
      }
      this.batch.draw(target, x, y);
    }
  }

  private void renderProgress() {
    int pg = gameState.onepercent;
    int atx = 64-pg;
    this.batch.draw(progress, atx, 0, pg, 1);
  }


  private Sprite spriteFor(final MapObject mo) {
    Sprite spr = null;
    if (mo instanceof Wall) {
      spr = this.wall;
      return spr;
    }
    if (mo instanceof Player) {
      switch (((Player) mo).getCharacterType()) {
        case COW:
          spr = this.cow;
          break;
        case FAIRY:
          spr = this.fairy;
          break;
        case FARMER:
          spr = this.farmer;
          break;
      }
      return spr;
    }
    return null;
  }

  void dispose() {
    this.batch.dispose();
  }

}
