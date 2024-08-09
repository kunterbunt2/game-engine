//#version 330 core

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
#define varying in
out vec4 out_FragColor;
#define textureCube texture
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

in vec2 v_texCoord0;

uniform sampler2D u_colorTexture;
uniform sampler2D u_ssaoTexture;

void main() {
    vec3 sceneColor = texture(u_colorTexture, v_texCoord0).rgb;
    float ssaoFactor = texture(u_ssaoTexture, v_texCoord0).r;
    vec3 ssaoColor = texture(u_ssaoTexture, v_texCoord0).rgb;

    // Combine the SSAO with the scene's color
    vec3 finalColor = sceneColor * ssaoFactor;

    out_FragColor = vec4(finalColor, 1.0);
    //    out_FragColor = vec4(ssaoFactor, ssaoFactor, ssaoFactor, 1.0);
    //    out_FragColor = vec4(sceneColor, 1.0);
    //    out_FragColor = vec4(ssaoColor, 1.0);
    //    out_FragColor = vec4(v_texCoord0, 1.0, 1.0);
}
