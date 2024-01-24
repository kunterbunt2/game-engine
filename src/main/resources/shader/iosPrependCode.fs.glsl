//@author kunterbunt
#version 300 es
//#define GLSL3
#ifdef GLSL3
	#define textureCube texture
	#define texture2D texture
	#define varying in
	precision highp float
#endif
