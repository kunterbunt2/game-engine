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

in vec3 in_Position;
in vec2 in_UV;

out vec2 var_UV;

void main() {
    var_UV = in_UV;
    gl_Position = vec4(in_Position, 1.0);
}
