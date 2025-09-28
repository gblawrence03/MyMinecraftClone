#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in ivec3 aOffset; // World translation

uniform mat4 view;
uniform mat4 perspective;

out vec2 texCoord;
flat out vec3 normal;

void main()
{
	gl_Position = perspective * view * vec4(aPos + aOffset, 1.0);
	texCoord = aTexCoord;
	normal = aNormal;
}
