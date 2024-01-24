//water.fs.glsl
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

uniform sampler2D u_refractionTexture;
uniform sampler2D u_reflectionTexture;
uniform sampler2D u_dudvMapTexture;
uniform sampler2D u_normalMap;
uniform sampler2D u_depthMap;
uniform vec2 u_cameraNearFar;
uniform float u_moveFactor;
uniform float u_refractiveMultiplicator;

varying vec4 v_clipSpace;
varying vec2 v_texCoord0;
varying vec3 v_toCamera;
uniform float u_waveStrength;
const float shineDamper = 20.0;
const float reflectivity = 0.3;

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
	vec2 refractionCoords = vec2(ndc.x, ndc.y);
	vec2 reflectionCoords = vec2(ndc.x, 1.0 - ndc.y);
	#ifdef u_depthMap
		float depth = texture( u_depthMap, refractionCoords).r;
	#else
		float depth = 0.0;
	#endif

	float floorDistance = 2.0 * u_cameraNearFar.x * u_cameraNearFar.y / (u_cameraNearFar.y + u_cameraNearFar.x -(2.0 * depth - 1.0) * (u_cameraNearFar.y - u_cameraNearFar.x));
	depth = gl_FragCoord.z;
	float waterDistance = 2.0 * u_cameraNearFar.x * u_cameraNearFar.y / (u_cameraNearFar.y + u_cameraNearFar.x -(2.0 * depth - 1.0) * (u_cameraNearFar.y - u_cameraNearFar.x));
	float waterDepth = floorDistance - waterDistance;

	vec2 distortedTexCoords = texture(u_dudvMapTexture,
			vec2(v_texCoord0.x + u_moveFactor, v_texCoord0.y)).rg * 0.1;
	distortedTexCoords = v_texCoord0
			+ vec2(distortedTexCoords.x, distortedTexCoords.y + u_moveFactor);
	vec2 totalDistortion = (texture(u_dudvMapTexture, distortedTexCoords).rg
			* 2.0 - 1.0) * u_waveStrength /** clamp(waterDepth/100.0, 0.0, 1.0)*/;

	refractionCoords += totalDistortion;
	refractionCoords = clamp(refractionCoords, 0.001, 0.999);
	reflectionCoords += totalDistortion;
	reflectionCoords.x = clamp(reflectionCoords.x, 0.001, 0.999);
	reflectionCoords.y = clamp(reflectionCoords.y, 0.001, 0.999);

	vec4 refractColor = texture(u_refractionTexture, refractionCoords);
	vec4 reflectColor = texture(u_reflectionTexture, reflectionCoords);

	vec4 normalMapColor = texture(u_normalMap, distortedTexCoords);
	vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0, normalMapColor.b * 3.0,
			normalMapColor.g * 2.0 - 1.0);
	normal = normalize(normal);

	vec3 viewVector = normalize(v_toCamera);
	float refractiveFactor = dot(viewVector, normal);


	vec3 specularHighlights;
	#if (numDirectionalLights > 0)
		// Directional lights calculation
		for(int i=0 ; i<1/*numDirectionalLights*/ ; i++){
			vec3 reflectedLight = reflect(u_dirLights[i].direction, normal);
			float specular = max( dot( reflectedLight, viewVector), 0.0);
			specular = pow(specular, shineDamper);
			specularHighlights = u_dirLights[i].color * 10.0*u_waveStrength*specular * reflectivity/**clamp(waterDepth/100.0, 0.0, 1.0)*/;
		}
	#else
	specularHighlights = vec3(0.0, 0.0, 0.0);
	fragColor = vec4(1.0,0.0,0.0,1.0);
	#endif

	refractiveFactor = pow(refractiveFactor, 2.0);
	vec4 diffuse = mix(reflectColor, refractColor, refractiveFactor*u_refractiveMultiplicator) + vec4(specularHighlights, 0.0);
	fragColor = mix( diffuse, vec4(0.1, 0.4, 0.3, 1.0), 0.3);
//	fragColor = vec4(waterDepth/1000.0);
//	fragColor.a = clamp(waterDepth/100.0, 0.0, 1.0);
}
