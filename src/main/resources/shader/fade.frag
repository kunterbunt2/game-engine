#ifdef GL_ES
#define PRECISION mediump
precision PRECISION float;
precision PRECISION int;
#else
#define PRECISION
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform float u_intensity;

#ifdef CONTROL_SATURATION
const vec3 grayscale = vec3(0.3, 0.59, 0.11);

uniform float u_saturation;
uniform float u_saturationMul;

// 0 = totally desaturated
// 1 = saturation unchanged
// higher = increase saturation
vec3 adjustSaturation(vec3 color, float saturation) {
    vec3 grey = vec3(dot(color, grayscale));
    return mix(grey, color, saturation);// correct
}
#endif

void main() {
    vec3 rgb = texture2D(u_texture0, v_texCoords).xyz;
    rgb = rgb * u_intensity;

    #ifdef CONTROL_SATURATION
    rgb = adjustSaturation(rgb, u_saturation) * u_saturationMul;
    #endif

    gl_FragColor = vec4(rgb, 1);
}