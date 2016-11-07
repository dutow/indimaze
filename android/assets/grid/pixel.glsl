#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float visibility;
uniform float hue;
uniform float saturation;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main()
{
   vec3 color = texture2D(u_texture, v_texCoords).rgb;

   vec3 hsv = rgb2hsv(color);

   float goodUntil = visibility;
   float remaining = 0.5f - goodUntil;

   float distX = 1.f + clamp(abs(0.5 - v_texCoords.x) - goodUntil, 0.f, remaining) * 25.f;
   float distY = 1.f + clamp(abs(0.5 - v_texCoords.y) - goodUntil, 0.f, remaining) * 25.f;

   float distance = sqrt(distX * distX + distY * distY);

   hsv.x = hue;
   hsv.y = saturation;
   hsv.z /= distance;

   gl_FragColor = vec4(hsv2rgb(hsv), 1.0);
}
