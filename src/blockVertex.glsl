#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in int aFaceID;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 perspective;
uniform vec3 cameraPos;
uniform vec3 cameraDir;

out vec2 texCoord;
out vec3 normal;

vec3 faceNormals[6] = vec3[] (
	vec3(0.0, 0.0, -1),
	vec3(0.0, 0.0, 1),
	vec3(-1, 0.0, 0.0),
	vec3(1, 0.0, 0.0),
	vec3(0.0, -1, 0.0),
	vec3(0.0, 1, 0.0)
);

void main()
{
	gl_Position = perspective * view * vec4(aPos, 1.0);
	texCoord = aTexCoord;
	normal = faceNormals[aFaceID];
}
