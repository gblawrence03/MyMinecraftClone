#version 330 core
out vec4 FragColor;
in vec2 texCoord;
in vec3 normal;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform float lerp;
uniform vec3 globalLightDir;

void main()
{
	vec3 lightDir = normalize(globalLightDir);
	float ambientStrength = 0.3;
	float minimumStrength = 0.7;
	float diff = max(dot(normalize(normal), -lightDir), 0.0);
	FragColor = max(minimumStrength, ambientStrength + diff) * mix(texture(texture1, texCoord),
	 				texture(texture2, vec2(texCoord.x, texCoord.y)), lerp);
}
