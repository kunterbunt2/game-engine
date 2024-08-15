//#version 330 core
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
#define varying in
out vec4 out_FragColor;
#define textureCube texture
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

in vec3 vPosition;
in vec3 v_normal;
//uniform sampler2D u_sourceTexture;
varying vec2 v_texCoords;


uniform sampler2D u_depthTexture;
uniform sampler2D uNoiseTexture;
uniform mat4 uProjMatrix;

const int kernelSize = 16;
uniform vec3 samples[kernelSize];

uniform vec2 uScreenSize;
uniform float uRadius;
uniform float uBias;
uniform float uIntensity;

float getLinearDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * 1.0 * 300.0) / (300.0 + 1.0 - z * (300.0 - 1.0));
}

void main() {
    //    vec2 texCoord = v_texCoords;
    //    vec3 sceneColor = texture(u_sourceTexture, v_texCoords).rgb;
    //    float depth = texture(u_depthTexture, texCoord).r;
    //    vec3 position = vPosition;
    vec3 normal = normalize(v_normal);
    //
    //    float occlusion = 0.0;
    //    vec3 randomVec = texture(uNoiseTexture, texCoord * uScreenSize / 4.0).xyz;
    //
    //    mat3 TBN = mat3(normalize(cross(randomVec, normal)), normalize(cross(normal, normalize(cross(randomVec, normal)))), normal);
    //
    //    for (int i = 0; i < kernelSize; ++i) {
    //        vec3 samplePos = TBN * samples[i];
    //        samplePos = position + samplePos * uRadius;
    //
    //        vec4 offset = uProjMatrix * vec4(samplePos, 1.0);
    //        offset.xy /= offset.w;
    //        offset.xy = offset.xy * 0.5 + 0.5;
    //
    //        float sampleDepth = texture(u_depthTexture, offset.xy).r;
    //        float rangeCheck = smoothstep(0.0, 1.0, uRadius / abs(position.z - getLinearDepth(sampleDepth)));
    //        occlusion += (sampleDepth >= getLinearDepth(sampleDepth) + uBias ? 1.0 : 0.0) * rangeCheck;
    //    }
    //
    //    occlusion = 1.0 - (occlusion / kernelSize) * uIntensity;
    //    //    occlusion = 1.0;
    //    vec3 finalColor = sceneColor * occlusion;
    //    out_FragColor = vec4(finalColor, 1.0);
    out_FragColor = vec4(normal, 1.0);
    out_FragColor.rgb = v_normal;
}
