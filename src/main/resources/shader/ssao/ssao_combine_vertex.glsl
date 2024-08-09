//depthOfField.vs.glsl
//@author kunterbunt
#if __VERSION__ >= 130
out vec4 fragColor;
#define attribute in
#define varying out
#else
#define fragColor gl_FragColor
#endif
#ifdef GL_ES
#extension GL_APPLE_clip_distance: require
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;
varying vec2 v_texCoord0;

void main() {
    v_texCoord0 = a_texCoord0;
    gl_Position = a_position;
}
