//mirror.fs.glsl
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

#ifdef diffuseColorFlag
 uniform vec4 u_diffuseColor;
#endif
 uniform sampler2D u_reflectionTexture;
uniform vec2 u_cameraNearFar;
uniform float u_reflectivity;
varying vec4 v_clipSpace;
varying vec2 v_texCoord0;
varying vec3 v_toCamera;
const float shineDamper = 20.0;
const float reflectivity = 0.2;

#if numDirectionalLights > 0
 struct DirectionalLight
{
vec3 color;
vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights

void main(void) {
vec2 ndc = (v_clipSpace.xy / v_clipSpace.w) / 2.0 + 0.5;
vec2 reflectionCoords = vec2(ndc.x, 1.0 - ndc.y);
reflectionCoords.x = clamp(reflectionCoords.x, 0.001, 0.999);
reflectionCoords.y = clamp(reflectionCoords.y, 0.001, 0.999);

vec4 reflectColor = texture(u_reflectionTexture, reflectionCoords);

vec3 normal = vec3(0.0, 1.0, 0.0);

vec3 viewVector = normalize(v_toCamera);
float refractiveFactor = dot(viewVector, normal);

vec4 diffuse = mix(u_diffuseColor,reflectColor, u_reflectivity);
fragColor = diffuse;
}
