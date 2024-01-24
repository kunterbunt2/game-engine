//mirror.vs.glsl
//@author kunterbunt
#ifdef GL_ES
	#extension GL_APPLE_clip_distance : require
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
#else
	#if __VERSION__ >= 130
		out vec4 fragColor;
	#else
		#define fragColor gl_FragColor
	#endif
#endif

attribute vec3 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_clippingPlane; //clipping plane for water refraction and reflections
varying float gl_ClipDistance[4];
uniform vec4 u_cameraPosition;
varying vec2 v_texCoord0;
varying vec4 v_clipSpace;
varying vec3 v_toCamera;

void main() {
	vec4 worldPosition = u_worldTrans * vec4(a_position, 1.0);
//	#ifdef u_clippingPlane
		gl_ClipDistance[0] = dot(worldPosition, u_clippingPlane);
//	#endif
	//coords
	v_clipSpace = u_projViewTrans * worldPosition;
	gl_Position = v_clipSpace;
	//DUDV
	v_texCoord0 = a_texCoord0 /** u_tiling*/;
	v_toCamera = u_cameraPosition.xyz - worldPosition.xyz;
}
