//alternativeDefaultGl2.vs.glsl
//@author kunterbunt
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

#ifdef GL_ES
#else
    #if __VERSION__ >= 130
 out vec4 fragColor;
#else
        #define fragColor gl_FragColor
    #endif
#endif

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
v_color = a_color;
v_color.a = v_color.a * (255.0 / 254.0);
v_texCoords = a_texCoord0;
gl_Position = u_projTrans * a_position;
}
