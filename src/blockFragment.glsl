#version 330 core
out vec4 FragColor;
in vec2 texCoord;
flat in vec3 normal;

uniform sampler2D texture1;
uniform vec3 globalLightDir;

void main()
{
	vec3 lightDir = normalize(globalLightDir);
	float ambientStrength = 0.3;
	float minimumStrength = 0.7;
	float diff = max(dot(normalize(normal), -lightDir), 0.0);
	FragColor = max(minimumStrength, ambientStrength + diff) *
	 				texture(texture1, vec2(texCoord.x, texCoord.y));
}
