//#version 330

in vec3 a_position;
//in vec3 in_Position;
in vec2 a_texCoord0;
//in vec2 in_UV;

out vec2 var_UV;

void main()
{
    var_UV = a_texCoord0;
    gl_Position = vec4(a_position, 1.0);
}
