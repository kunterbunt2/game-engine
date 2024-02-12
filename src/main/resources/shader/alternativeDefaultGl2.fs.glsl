//alternativeDefaultGl2.fs.glsl
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
 out vec4 fragColor;
#else
    #if __VERSION__ >= 130
        out vec4 fragColor;
#else
        #define fragColor gl_FragColor
    #endif
#endif

 varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
fragColor = v_color * texture(u_texture, v_texCoords);
}
