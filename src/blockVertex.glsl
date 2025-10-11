#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in int aLightLevel;

uniform mat4 view;
uniform mat4 perspective;

out vec2 texCoord;
flat out vec3 normal;
flat out float lightLevel;

void main()
{
	gl_Position = perspective * view * vec4(aPos, 1.0);
	texCoord = aTexCoord;
	normal = aNormal;
	if (aLightLevel == 15) {
		lightLevel = 1;
	}
	else {
		lightLevel = pow(float(aLightLevel + 3) / 18.0, 1.5);
	}

}
