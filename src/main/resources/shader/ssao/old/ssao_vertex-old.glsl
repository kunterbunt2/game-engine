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


attribute vec4 a_position;
//layout(location = 0) in vec3 aPosition;
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;

//layout(location = 1) in vec3 aNormal;
attribute vec2 a_texCoord0;
//layout(location = 2) in vec2 aTexCoord;

uniform mat4 uWorldTrans;
uniform mat4 uProjViewTrans;
//uniform mat3 u_normalMatrix;

out vec3 vPosition;
varying vec3 v_normal;

out vec2 v_texCoords;

void main() {
    //    vNormal = normalize(mat3(uWorldTrans) * aNormal);
    //    vNormal =  normalize(a_normal);
    //    v_normal = normalize(vec3(u_normalMatrix * a_normal.xyz));
    v_normal = a_normal;
    v_texCoords = a_texCoord0;
    //    vPosition = a_position.rgb;
    gl_Position = a_position;
}