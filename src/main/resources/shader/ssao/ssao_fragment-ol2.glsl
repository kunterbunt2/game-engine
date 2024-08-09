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

in vec3 v_position;
in vec3 v_normal;
varying vec2 v_texCoord0;

//uniform sampler2D u_sourceTexture;
uniform sampler2D u_depthTexture;
uniform sampler2D uNoiseTexture;
uniform mat4 uProjMatrix;
uniform mat4 u_projection_inverse;

const int kernelSize = 16;
uniform vec3 samples[kernelSize];

uniform vec2 uScreenSize;
uniform float uRadius;
uniform float uBias;
uniform float uIntensity;
uniform float znear = 0.1;//camera clipping start
uniform float zfar = 100.0;//camera clipping end

float getLinearDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (2.0 * 1.0 * 300.0) / (300.0 + 1.0 - z * (300.0 - 1.0));
}
float unpackVec3ToFloat(vec3 packedValue, float near, float far) {
    float packScale = far - near;
    float depth = dot(packedValue, 1.0 / vec3(1.0, 256.0, 256.0 * 256.0));
    float ndc = depth * 2.0 - 1.0;
    depth = (2.0 * near * far) / (far + near - ndc * (far - near));
    //	depth = near + packScale * depth * (256.0 * 256.0 * 256.0)	/ (256.0 * 256.0 * 256.0 - 1.0);
    return depth;
}
// this function calculates view position of a fragment from its depth value
// sampled from the depth texture.
vec3 calcViewPosition(vec2 coords) {
    float fragmentDepth = unpackVec3ToFloat(texture(u_depthTexture, coords).rgb, znear, zfar);

    // Convert coords and fragmentDepth to
    // normalized device coordinates (clip space)
    vec4 ndc = vec4(
    coords.x * 2.0 - 1.0,
    coords.y * 2.0 - 1.0,
    fragmentDepth * 2.0 - 1.0,
    1.0
    );

    // Transform to view space using inverse camera projection matrix.
    vec4 vs_pos = u_projection_inverse * ndc;

    // since we used a projection transformation (even if it was in inverse)
    // we need to convert our homogeneous coordinates using the perspective divide.
    vs_pos.xyz = vs_pos.xyz / vs_pos.w;

    return vs_pos.xyz;
}
void main() {
    vec2 texCoord = v_texCoord0;
    //    vec2 screenSize = uScreenSize;
    vec2 screenSize = vec2(1000, 1000);
    //    vec3 sceneColor = texture(u_sourceTexture, texCoord).rgb;
    float depth = texture(u_depthTexture, texCoord).r;
    vec3 position = v_position;
    vec3 normal = normalize(v_normal);

    float occlusion = 0.0;
    vec3 randomVec = texture(uNoiseTexture, texCoord * screenSize / 4.0).xyz;

    //    mat3 TBN = mat3(normalize(cross(randomVec, normal)), normalize(cross(normal, normalize(cross(randomVec, normal)))), normal);
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 viewPos = calcViewPosition(texCoord);
    for (int i = 0; i < kernelSize; ++i) {
        vec3 samplePos = TBN * samples[i];
        samplePos = position + samplePos * uRadius;

        vec4 offset = uProjMatrix * vec4(samplePos, 1.0);
        offset.xy /= offset.w;
        offset.xy = offset.xy * 0.5 + 0.5;

        float geometryDepth = calcViewPosition(offset.xy).z;
        float rangeCheck = smoothstep(0.0, 1.0, uRadius / abs(viewPos.z - geometryDepth));

        // samplePos.z is the sample's depth i.e. the view_space_sampling_position depth
        // this value is negative in my coordinate system
        // for occlusion to be true the geometry's depth should be greater or equal (equal or less negative and consequently closer to the camera) than the sample's depth
        occlusion += float(geometryDepth >= samplePos.z + 0.0001) * rangeCheck;

        //        float sampleDepth = texture(u_depthTexture, offset.xy).r;
        //        float rangeCheck = smoothstep(0.0, 1.0, uRadius / abs(position.z - unpackVec3ToFloat(texture(u_depthTexture, offset.xy).rgb, znear, zfar)));
        //        float rangeCheck = smoothstep(0.0, 1.0, uRadius / abs(position.z - getLinearDepth(sampleDepth)));
        //        occlusion += (sampleDepth >= unpackVec3ToFloat(texture(u_depthTexture, offset.xy).rgb, znear, zfar) + uBias ? 1.0 : 0.0) * rangeCheck;
        //        occlusion += (sampleDepth >= getLinearDepth(sampleDepth) + uBias ? 1.0 : 0.0) * rangeCheck;
    }

    occlusion = 1.0 - (occlusion / kernelSize) * uIntensity;
    //    occlusion = 1.0;
    //    vec3 finalColor = sceneColor * occlusion;
    //    out_FragColor = vec4(finalColor, 1.0);
    //    out_FragColor = vec4(normal, 1.0);

    out_FragColor = vec4(vec3(occlusion), 1.0);

    //    out_FragColor = vec4(v_texCoord0, 1.0, 1.0);
    //    out_FragColor = vec4(normal, 1.0);
    //    out_FragColor = vec4(sceneColor, 1.0);
    //    float depth1 = unpackVec3ToFloat(texture(u_depthTexture, v_texCoord0.xy).rgb, znear, zfar);
    //    out_FragColor = vec4(depth1, 0, 0, 1.0);
}
