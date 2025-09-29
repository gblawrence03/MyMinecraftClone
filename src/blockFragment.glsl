#version 330 core
out vec4 FragColor;
in vec2 texCoord;
flat in int lightLevel;
flat in vec3 normal;

uniform sampler2D texture1;
uniform vec3 globalLightDir;

void main()
{
	vec4 texColor = texture(texture1, texCoord);

	float alpha = texColor.a;

	vec3 lightDir = normalize(globalLightDir);
	float ambientStrength = 0.3;
	float minimumStrength = 0.7;
	float diff = max(dot(normalize(normal), -lightDir), 0.0);


	vec3 color = max(minimumStrength, ambientStrength + diff) *
	 				texColor.rgb;

	FragColor = vec4(color * max(0, float(lightLevel) / 15.0), alpha);
}
