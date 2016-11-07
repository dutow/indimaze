attribute vec4 a_position;

uniform mat4 u_projTrans;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;

void main()
{
    v_texCoords = a_texCoord0;
    gl_Position =  u_projTrans * a_position;
}