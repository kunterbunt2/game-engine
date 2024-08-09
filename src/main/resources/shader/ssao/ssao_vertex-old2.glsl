#line 1
// required to have same precision in both shader for light structure
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#ifdef GLSL3
#define attribute in
#define varying out
#endif

varying vec3 v_position;
attribute vec3 a_position;
uniform mat4 u_projViewTrans;

attribute vec2 a_texCoord0;
varying vec2 texCoords;
uniform mat3 u_texCoord0Transform;

//#if defined(colorFlag)
//varying vec4 v_color;
//attribute vec4 a_color;
//#endif// colorFlag

attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;


uniform mat4 u_worldTrans;

void main() {
    vec3 morph_pos = a_position;


    texCoords = (u_texCoord0Transform * vec3(a_texCoord0, 1.0)).xy;

    //    #if defined(colorFlag)
    //    v_color = a_color;
    //    #endif// colorFlag

    vec4 pos = u_worldTrans * vec4(morph_pos, 1.0);
    v_position = vec3(pos.xyz) / pos.w;
    gl_Position = u_projViewTrans * pos;

    v_normal = normalize(vec3(u_normalMatrix * a_normal.xyz));
    //    v_normal = a_normal;

}
