//depthOfField.fs.glsl
//@author kunterbunt
#if __VERSION__ >= 130
out vec4 fragColor;
#define varying in
#define texture2D texture
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


/*
DoF with bokeh GLSL shader v2.4
by Martins Upitis (martinsh) (devlog-martinsh.blogspot.com)

----------------------
The shader is Blender Game Engine ready, but it should be quite simple to adapt for your engine.

This work is licensed under a Creative Commons Attribution 3.0 Unported License.
So you are free to share, modify and adapt it for your needs, and even use it for commercial use.
I would also love to hear about a project you are using it.

Have fun,
Martins
----------------------

changelog:

2.4:
- physically accurate DoF simulation calculated from "focalDepth" ,"focalLength", "f-stop" and "CoC" parameters.
- option for artist controlled DoF simulation calculated only from "focalDepth" and individual controls for near and far blur
- added "circe of confusion" (CoC) parameter in mm to accurately simulate DoF with different camera sensor or film sizes
- cleaned up the code
- some optimization

2.3:
- new and physically little more accurate DoF
- two extra input variables - focal length and aperture iris diameter
- added a debug visualization of focus point and focal range

2.1:
- added an option for pentagonal bokeh shape
- minor fixes

2.0:
- variable sample count to increase quality/performance
- option to blur depth buffer to reduce hard edges
- option to dither the samples with noise or pattern
- bokeh chromatic aberration/fringing
- bokeh bias to bring out bokeh edges
- image thresholding to bring out highlights when image is out of focus

*/

uniform sampler2D u_sourceTexture;
uniform sampler2D u_depthTexture;
varying vec2 v_texCoords;

#define PI  3.14159265

ivec2 textureSize2d = textureSize(u_sourceTexture, 0);
float width = float(textureSize2d.x);
float height = float(textureSize2d.y);
vec2 texel = vec2(1.0/width, 1.0/height);

//uniform variables - made consistent
uniform float focalDepth = 10.0;// Changed from 1.5 to 10.0 - focus point much further away
uniform float focalLength = 12.0;
uniform float fstop = 2.0;
uniform bool showFocus = false;

//make sure that these two values are the same for your camera, otherwise distances will be wrong.
uniform float znear = 0.1;
uniform float zfar = 100.0;

//------------------------------------------
//user variables

uniform int samples = 5;// - samples on the first ring
uniform int rings= 5;// - ring count

uniform bool manualdof = true;// - manual dof calculation
uniform float ndofstart = 5.0;// Changed from 1.0 to 5.0 - near blur starts much further
uniform float ndofdist = 32.0;// - near dof blur falloff distance
uniform float fdofstart = 15.0;// Changed from 32.0 to 15.0 - far blur starts closer to focal point
uniform float fdofdist = 300.0;// - far dof blur falloff distance

uniform float CoC = 0.03;// - circle of confusion size in mm

uniform bool vignetting = false;// - use optical lens vignetting?
uniform float vignout = 1.3;// - vignetting outer border
uniform float vignin = 0.0;// - vignetting inner border
uniform float vignfade = 22.0;// - f-stops till vignette fades

uniform float maxblur = 1.0;// - clamp value of max blur
uniform float threshold = 0.9;// - highlight threshold
uniform float gain = 100.0;// - highlight gain

uniform float bias = 0.5;// - bokeh edge bias
uniform float fringe = 0.7;// - bokeh chromatic aberration/fringing

uniform bool noise = true;// - use noise instead of pattern for sample dithering
uniform float namount = 0.0001;// - dither amount

uniform bool pentagon = false;// - use pentagon as bokeh shape?
uniform float feather = 0.4;// - pentagon shape feather
//------------------------------------------

float unpackVec3ToFloat(vec3 packedValue, float near, float far) {
    float packScale = far - near;
    float depth = dot(packedValue, 1.0 / vec3(1.0, 256.0, 256.0 * 256.0));
    float ndc = depth * 2.0 - 1.0;
    depth = (2.0 * near * far) / (far + near - ndc * (far - near));
    //	depth = near + packScale * depth * (256.0 * 256.0 * 256.0)	/ (256.0 * 256.0 * 256.0 - 1.0);
    return depth;
}

float penta(vec2 coords)//pentagonal shape
{
    float scale = float(rings) - 1.3;
    vec4  HS0 = vec4(1.0, 0.0, 0.0, 1.0);
    vec4  HS1 = vec4(0.309016994, 0.951056516, 0.0, 1.0);
    vec4  HS2 = vec4(-0.809016994, 0.587785252, 0.0, 1.0);
    vec4  HS3 = vec4(-0.809016994, -0.587785252, 0.0, 1.0);
    vec4  HS4 = vec4(0.309016994, -0.951056516, 0.0, 1.0);
    vec4  HS5 = vec4(0.0, 0.0, 1.0, 1.0);

    vec4  one = vec4(1.0);

    vec4 P = vec4((coords), vec2(scale, scale));

    vec4 dist = vec4(0.0);
    float inorout = -4.0;

    dist.x = dot(P, HS0);
    dist.y = dot(P, HS1);
    dist.z = dot(P, HS2);
    dist.w = dot(P, HS3);

    dist = smoothstep(-feather, feather, dist);

    inorout += dot(dist, one);

    dist.x = dot(P, HS4);
    dist.y = HS5.w - abs(P.z);

    dist = smoothstep(-feather, feather, dist);
    inorout += dist.x;

    return clamp(inorout, 0.0, 1.0);
}

//float bdepth(vec2 coords)//blurring depth
//{
//    float d = 0.0;
//    float kernel[9];
//    vec2 offset[9];
//
//    vec2 wh = vec2(texel.x, texel.y) * dbsize;
//
//    offset[0] = vec2(-wh.x, -wh.y);
//    offset[1] = vec2(0.0, -wh.y);
//    offset[2] = vec2(wh.x -wh.y);
//
//    offset[3] = vec2(-wh.x, 0.0);
//    offset[4] = vec2(0.0, 0.0);
//    offset[5] = vec2(wh.x, 0.0);
//
//    offset[6] = vec2(-wh.x, wh.y);
//    offset[7] = vec2(0.0, wh.y);
//    offset[8] = vec2(wh.x, wh.y);
//
//    kernel[0] = 1.0/16.0;   kernel[1] = 2.0/16.0;   kernel[2] = 1.0/16.0;
//    kernel[3] = 2.0/16.0;   kernel[4] = 4.0/16.0;   kernel[5] = 2.0/16.0;
//    kernel[6] = 1.0/16.0;   kernel[7] = 2.0/16.0;   kernel[8] = 1.0/16.0;
//
//
//    for (int i=0; i<9; i++)
//    {
//        float tmp = texture2D(u_depthTexture, coords + offset[i]).r;
//        d += tmp * kernel[i];
//    }
//
//    return d;
//}


vec3 color(vec2 coords, float blur)//processing the sample
{
    vec3 col = vec3(0.0);

    col.r = texture2D(u_sourceTexture, coords + vec2(0.0, 1.0)*texel*fringe*blur).r;
    col.g = texture2D(u_sourceTexture, coords + vec2(-0.866, -0.5)*texel*fringe*blur).g;
    col.b = texture2D(u_sourceTexture, coords + vec2(0.866, -0.5)*texel*fringe*blur).b;

    vec3 lumcoeff = vec3(0.299, 0.587, 0.114);
    float lum = dot(col.rgb, lumcoeff);
    float thresh = max((lum-threshold)*gain, 0.0);
    return col+mix(vec3(0.0), col, thresh*blur);
}

vec2 rand(vec2 coord)//generating noise/pattern texture for dithering
{
    float noiseX = ((fract(1.0-coord.s*(width/2.0))*0.25)+(fract(coord.t*(height/2.0))*0.75))*2.0-1.0;
    float noiseY = ((fract(1.0-coord.s*(width/2.0))*0.75)+(fract(coord.t*(height/2.0))*0.25))*2.0-1.0;

    if (noise)
    {
        noiseX = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233))) * 43758.5453), 0.0, 1.0)*2.0-1.0;
        noiseY = clamp(fract(sin(dot(coord, vec2(12.9898, 78.233)*2.0)) * 43758.5453), 0.0, 1.0)*2.0-1.0;
    }
    return vec2(noiseX, noiseY);
}

vec3 debugFocus(vec3 col, float blur, float depth)
{
    //    if (blur < 0.02f)
    //    return vec3(0.0, 1.0, 1.0);

    //    if (depth < focalDepth)
    //    return vec3(0.0, clamp(depth/focalDepth, 0f, 1f), 0.0);

    float edge = 0.002*depth;//distance based edge smoothing
    float m = clamp(smoothstep(0.0, edge, blur), 0.0, 1.0);
    float e = clamp(smoothstep(1.0-edge, 1.0, blur), 0.0, 1.0);

    col = mix(col, vec3(1.0, 0.0, 0.0), (1.0-m)*0.6);
    col = mix(col, vec3(0.0, 0.0, 1.0), ((1.0-e)-(1.0-m))*0.2);
    col = vec3(blur, 0.0, 0.0);
    return col;
}

//float linearize(float depth)
//{
//    return -zfar * znear / (depth * (zfar - znear) - zfar);
//}
float linearize(float d)
{
    float z_n = 2.0 * d - 1.0;
    return 2.0 * znear * zfar / (zfar + znear - z_n * (zfar - znear));
}

float vignette()
{
    float dist = distance(v_texCoords.xy, vec2(0.5, 0.5));
    dist = smoothstep(vignout+(fstop/vignfade), vignin+(fstop/vignfade), dist);
    return clamp(dist, 0.0, 1.0);
}

void main()
{
    //scene depth calculation
    float depth = unpackVec3ToFloat(texture2D(u_depthTexture, v_texCoords.xy).rgb, znear, zfar);

    //focal plane calculation
    float fDepth = focalDepth;

    //dof blur factor calculation
    float blur = 0.0;

    if (manualdof)
    {
        if (depth < fDepth+fdofstart && depth > fDepth-ndofstart)
        {
            blur = 0.0;//sharp
        }
        else
        {
            float a = depth-fDepth;//focal plane
            float b = (a-fdofstart)/fdofdist;//far DoF
            float c = (-a-ndofstart)/ndofdist;//near Dof
            blur = (a>0.0)?b:c;
        }
    }
    else
    {
        float f = focalLength;//focal length in mm
        float d = fDepth*1000.0;//focal plane in mm
        float o = depth*1000.0;//depth in mm

        // Add safety check to prevent division by zero
        if (abs(o-f) < 0.001) {
            blur = 0.0;
        } else {
            float a = (o*f)/(o-f);
            float b = (d*f)/(d-f);
            float c = (d-f)/(d*fstop*CoC);
            blur = abs(a-b)*c;
        }
    }

    blur = clamp(blur, 0.0, 1.0);

    // calculation of pattern for dithering
    vec2 noiseOffset = rand(v_texCoords.xy)*namount*blur;

    // getting blur x and y step factor
    float w = (1.0/width)*blur*maxblur+noiseOffset.x;
    float h = (1.0/height)*blur*maxblur+noiseOffset.y;

    // calculation of final color
    vec3 col = vec3(0.0);

    if (blur < 0.05)
    {
        //sharp - early exit for performance
        col = texture2D(u_sourceTexture, v_texCoords.xy).rgb;
    }
    else
    {
        //blurry - optimized sampling
        col = texture2D(u_sourceTexture, v_texCoords.xy).rgb;
        float s = 1.0;

        // Use constant loop bounds for better GPU performance
        const int maxRings = 8;// Adjust based on your quality needs
        const int maxSamples = 8;// Adjust based on your quality needs

        int actualRings = min(rings, maxRings);
        int actualSamples = min(samples, maxSamples);

        for (int i = 1; i <= maxRings; i++)
        {
            if (i > actualRings) break;

            int ringsamples = i * actualSamples;
            float ringWeight = mix(1.0, float(i)/float(actualRings), bias);

            for (int j = 0; j < maxSamples * maxRings; j++)
            {
                if (j >= ringsamples) break;

                float step = PI*2.0 / float(ringsamples);
                float pw = cos(float(j)*step)*float(i);
                float ph = sin(float(j)*step)*float(i);
                float p = 1.0;

                if (pentagon)
                {
                    p = penta(vec2(pw, ph));
                }

                col += color(v_texCoords.xy + vec2(pw*w, ph*h), blur) * ringWeight * p;
                s += ringWeight * p;
            }
        }
        col /= s;//divide by sample count
    }

    if (showFocus)
    {
        col = debugFocus(col, blur, depth);
    }

    if (vignetting)
    {
        col *= vignette();
    }

        fragColor.rgb = col;
fragColor.a = 1.0;
}

/*
uniform sampler2D u_sourceTexture;
uniform sampler2D u_depthTexture;

out vec4 color;

smooth in vec2 texcoord;

void main()
{
	vec4 c	=	texture(u_sourceTexture, v_texCoords);
	// grabbing values out of the depth buffer causes program to fail.
	float z	=	texture(u_depthTexture, v_texCoords).x;
	color	=	texture(u_sourceTexture, v_texCoords) + (z * 0.000001);
}
*/