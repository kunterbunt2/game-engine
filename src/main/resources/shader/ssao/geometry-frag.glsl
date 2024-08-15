//#version 330
layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec4 gColor;

in vec2 v_texCoords;
in vec3 v_normal;
in vec3 v_position;

void main()
{
    gColor = vec4(1.0);//color map
    gPosition = v_position;//depth map
    //    gPosition = (v_position + 1.0) * 0.5;

    gNormal = normalize(v_normal);//normal map
    //    gNormal = normalize((v_normal + 1.0) * 0.5);


    //    gPosition = vec3(1f, 0f, 0f);
    //    gNormal = vec3(0f, 1f, 0f);
    //    gColor = vec4(0f, 0f, 1f, 1f);
}
