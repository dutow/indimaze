package com.mygdx.indimaze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AfterEffect {

    private GameGrid grid;
    private GameState gs;
    private SpriteBatch postprocessBatch = new SpriteBatch();
    private SpriteBatch finalBatch = new SpriteBatch();
    private FrameBuffer baseFbo = new FrameBuffer(Pixmap.Format.RGBA8888, 64, 64, false);
    private FrameBuffer postprocessFbo = new FrameBuffer(Pixmap.Format.RGBA8888, 64, 64, false);
    private ShaderProgram postprocessShader = new ShaderProgram(Gdx.files.internal("grid/vertex.glsl"), Gdx.files.internal("grid/pixel.glsl"));

    public AfterEffect(GameState gameState, GameGrid gameGrid) {
        this.grid = gameGrid;
        this.gs = gameState;
        baseFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        postprocessFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        if (!postprocessShader.isCompiled()) throw new GdxRuntimeException("Couldn't compile postprocessShader: " + postprocessShader.getLog());
        this.postprocessBatch.setShader(postprocessShader);
    }

    void updateFbo() {
        baseFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        grid.render();
        baseFbo.end();
    }

    void postprocessFbo() {
        postprocessFbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.postprocessBatch.begin();

        float hppercent = gs.hp / gs.maxhp;
        float vis = hppercent / 1.7f + 0.1f;
        if (vis > 0.45) vis = 0.45f;

        float hue = hppercent / 5f;
        float saturation = (100 + (1-hppercent) * 150.f) / 255.f;

        postprocessShader.setUniformf("visibility",vis);
        postprocessShader.setUniformf("hue",hue);
        postprocessShader.setUniformf("saturation",saturation);
        this.postprocessBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 64, 64));
        this.postprocessBatch.draw(baseFbo.getColorBufferTexture(), 0, 0);
        this.postprocessBatch.end();
        postprocessFbo.end();
    }

    void render() {
        updateFbo();
        postprocessFbo();
        finalBatch.begin();
        this.finalBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 64, 64));
        this.finalBatch.draw(postprocessFbo.getColorBufferTexture(), 0, 0);
        finalBatch.end();
    }
    void dispose() {
        baseFbo.dispose();
        grid.dispose();
    }

}
