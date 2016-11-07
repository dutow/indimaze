package com.mygdx.indimaze;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class RetroGridMesh {

  private final static short gridWidth = 64;
  private final static short gridHeight = 64;
  private final static short gridSize = gridWidth * gridHeight;

  private final static float[] baseVertices = {
      0, 0, 0, // bottom left
      0, 1, 0, // top left
      1, 1, 0, // top right
      1, 0, 0 // bottom right
  };

  private final static short[] baseIndices = {
      0, 1, 2, // top left triangle
      0, 2, 3  // bottom right triangle
  };
  private final static float[] vertices = generateVertices();
  private static final short[] indices = generateIndices();
  private final Mesh gridMesh;
  private final ShaderProgram gridShader;
  private Matrix4 projection;

  public RetroGridMesh(final Matrix4 projection) {
    this.projection = projection;
    this.gridMesh = new Mesh(
        Mesh.VertexDataType.VertexBufferObject,
        true,
        vertices.length / 3,
        indices.length,
        new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE)
    );
    this.gridMesh.setVertices(vertices);
    this.gridMesh.setIndices(indices);

    this.gridShader = new ShaderProgram(Gdx.files.internal("grid/vertex.glsl"), Gdx.files.internal("grid/pixel.glsl"));
    if (!this.gridShader.isCompiled()) {
      throw new RuntimeException("Couldn't compile shader: " + this.gridShader.getLog());
    }
  }

  private static float[] generateVertices() {
    final float[] vert = new float[gridSize * baseVertices.length];
    for (short y = 0; y < gridHeight; ++y) {
      for (short x = 0; x < gridWidth; ++x) {
        for (short j = 0; j < baseVertices.length; ++j) {
          float pos = baseVertices[j];
          switch (j % 3) {
            case 0:
              pos += x;
              break;
            case 1:
              pos += y;
              break;
            case 2:
              pos = y * gridWidth + x;
              break;
          }
          vert[(y * gridWidth + x) * baseVertices.length + j] = pos;
        }
      }
    }
    return vert;
  }

  private static short[] generateIndices() {
    final short[] ind = new short[gridSize * baseIndices.length];
    for (short i = 0; i < gridSize; ++i) {
      for (short j = 0; j < baseIndices.length; ++j) {
        ind[i * baseIndices.length + j] = (short) (baseIndices[j] + i * 4);
      }
    }
    return ind;
  }

  public void setProjection(final Matrix4 projection) {
    this.projection = projection;
  }

  public void render() {
    this.gridShader.begin();
    this.gridShader.setUniformMatrix("projection", this.projection);
    this.gridMesh.render(this.gridShader, GL20.GL_TRIANGLES);
    this.gridShader.end();
  }

  public void dispose() {
    this.gridMesh.dispose();
    this.gridShader.dispose();
  }
}
