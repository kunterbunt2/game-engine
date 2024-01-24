//depthOfField.fs.glsl
//@author kunterbunt
#if __VERSION__ >= 130
out vec4 fragColor;
#define varying in
#else
#define fragColor gl_FragColor
#endif

#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
	precision PRECISION int;
#else
#define PRECISION
#endif

//uniform sampler2D u_sourceTexture;
//varying vec2 v_texCoords;
//
//void main() {
//	gl_FragColor = texture2D(u_sourceTexture, v_texCoords);
//}

uniform sampler2D u_sourceTexture;
uniform sampler2D u_depthTexture;

uniform vec2 u_pixelSize;
uniform int u_vertical;
uniform vec2 u_cameraClipping;
uniform vec2 u_focusDistance;
uniform float u_nearDistanceBlur;
uniform float u_farDistanceBlur;
varying vec2 v_texCoords;

const float PI = 3.14159265358979323846;
const float SIGMA_HELPER = 3.5676;

float kernel[MAX_BLUR];

//float unpackVec3ToFloat(vec3 packedValue, float minValue, float maxValue) {
//    vec3 scaleVector = vec3(1.0, 256.0, 256.0 * 256.0) / (256.0 * 256.0 * 256.0 - 1.0) * 255.0;
//    float zeroToOne = dot(packedValue, scaleVector);
//    return zeroToOne * (maxValue - minValue) + minValue;
//}

float unpackVec3ToFloat(vec3 packedValue, float near, float far) {
	float packScale = far - near;
	float depth = dot(packedValue, 1.0 / vec3(1.0, 256.0, 256.0 * 256.0));
	float ndc = depth * 2.0 - 1.0;
	depth = (2.0 * near * far) / (far + near - ndc * (far - near));
//	depth = near + packScale * depth * (256.0 * 256.0 * 256.0)	/ (256.0 * 256.0 * 256.0 - 1.0);
	return depth;
}

void initializeKernel(int blur) {
	for (int i = 0; i < /*=*/MAX_BLUR; i++) {
		kernel[i] = 0.0;
	}

	float sigma = float(blur) / SIGMA_HELPER;
	float norm = 1.0 / (sqrt(2.0 * PI) * sigma);
	float coeff = 2.0 * sigma * sigma;
	float total = 0.0;
	for (int i = 0; i < /*=*/MAX_BLUR; i++) {
		if (i > blur) {
			break;
		}
		float value = norm * exp(-float(i) * float(i) / coeff);
		kernel[i] = value;
		total += value;
		if (i > 0) {
			total += value;
		}
	}
	for (int i = 0; i < /*=*/MAX_BLUR; i++) {
		if (i > blur) {
			break;
		}
		kernel[i] = kernel[i] / total;
	}
}

float getDepth() {
	vec4 depthColor = texture(u_depthTexture, v_texCoords);
	return unpackVec3ToFloat(depthColor.rgb, u_cameraClipping.x,
			u_cameraClipping.y);
//	float depth = texture(u_depthTexture, v_texCoords).r;
//	float floorDistance = 2.0 * u_cameraClipping.x * u_cameraClipping.y	/ (u_cameraClipping.y + u_cameraClipping.x	- (2.0 * depth - 1.0)* (u_cameraClipping.y - u_cameraClipping.x));
//	return floorDistance;
}

float getDeclaredBlur() {
	float depth = getDepth();
	if (!BLUR_BACKGROUND) {
		if ((depth) == u_cameraClipping.y){
//		if (depth + 0.00001 > u_cameraClipping.y) {
			return 0.0;
		}
	}
	if (u_focusDistance.x <= depth && depth <= u_focusDistance.y) {
		return 0.0;
	} else if (depth < u_focusDistance.x) {
		// It's too close
		return u_nearDistanceBlur * (u_focusDistance.x - depth)
				/ (u_focusDistance.x - u_cameraClipping.x);
	} else {
		// It's too far
		return u_farDistanceBlur * (depth - u_focusDistance.y)
				/ (u_cameraClipping.y - u_focusDistance.y);
	}
}

int getBlur() {
	return int(min(getDeclaredBlur(), float(MAX_BLUR)));
//	return int(getDeclaredBlur());
}
float LinearizeDepth(float depth) {
	float z = depth * 2.0 - 1.0; // back to NDC
	return (2.0 * u_cameraClipping.x * u_cameraClipping.y)
			/ (u_cameraClipping.y + u_cameraClipping.x
					- z * (u_cameraClipping.y - u_cameraClipping.x));
}

void main() {
	vec4 sampleAccum = vec4(0.0, 0.0, 0.0, 0.0);
	int blur = getBlur();
	float depth = getDepth();
	if (blur == 0) {
		sampleAccum = texture(u_sourceTexture, v_texCoords);
	} else {
		initializeKernel(blur);

		for (int i = 0; i < MAX_BLUR; i++) {
			if (i > blur) {
				break;
			}
			float kernelValue = kernel[i];
			if (u_vertical == 1) {
				sampleAccum += texture(u_sourceTexture,
						v_texCoords + u_pixelSize * vec2(0, i)) * kernelValue;
				if (i > 0) {
					sampleAccum += texture(u_sourceTexture,
							v_texCoords - u_pixelSize * vec2(0, i))
							* kernelValue;
				}
			} else {
				sampleAccum += texture(u_sourceTexture,
						v_texCoords + u_pixelSize * vec2(i, 0)) * kernelValue;
				if (i > 0) {
					sampleAccum += texture(u_sourceTexture,
							v_texCoords - u_pixelSize * vec2(i, 0))
							* kernelValue;
				}
			}
		}
	}

	fragColor = sampleAccum;
//	if ((depth) == u_cameraClipping.y)
//				gl_FragColor = vec4(1, 0, 0, 1.0);

//	vec4 c = texture(u_sourceTexture, v_texCoords);
	//show depth information
//	float b = float(depth - u_cameraClipping.x)	/ float(u_cameraClipping.y - u_cameraClipping.x);
//	float b = float(depth);
//	gl_FragColor = vec4(b, b, b, 1.0);
	//show blur information
//	if( depth > u_focusDistance.y)
//		gl_FragColor = vec4(1, 0, 0, 1.0);
//	else if( depth < u_focusDistance.x)
//		gl_FragColor = vec4(0, 0, 1, 1.0);
//	float b = float(blur) / float(MAX_BLUR);
//	gl_FragColor = vec4(b, b, b, 1.0);
//	float b = float( u_focusDistance.y) / float(256);
//	gl_FragColor = vec4(b, b, b, 1.0);

	//	if (v_texCoords.x == 0.0 || v_texCoords.y == 0.0)//
//	float depthr = texture(u_depthTexture, v_texCoords).r;//
//	float depthg = texture(u_depthTexture, v_texCoords).g;//
//	float depthb = texture(u_depthTexture, v_texCoords).b;//
//	gl_FragColor = vec4(depth1);//
//	if (depth1 == 1.0)//
//	float depth2 = LinearizeDepth(depthr/*gl_FragCoord.z*/) / u_cameraClipping.y;//
//		gl_FragColor = vec4(depth2, depth2, depth2, 1.0);//
//	else//
//		gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);//
//		gl_FragColor = vec4(float(depth)/2000.0, 0.0, 0.0, 1.0);//
//	gl_FragColor = mix(texture(u_sourceTexture, v_texCoords),texture(u_depthTexture, v_texCoords), 0.5);//

}
