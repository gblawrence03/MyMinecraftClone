#version 330 core
out vec4 FragColor;
in vec2 texCoord;
flat in float lightLevel;
flat in vec3 normal;
in vec3 fPosition;

uniform sampler2D texture1;
uniform vec3 globalLightDir;
uniform int chunkRenderDistance;

float fog_maxdist = (chunkRenderDistance - 1) * 16.0;
float fog_mindist = (chunkRenderDistance - 3) * 16.0;

vec4 fog_color = vec4(0.55, 0.7, 0.9, 1.0);

// float fog_maxdist = 24.0;
// float fog_mindist = 6.0;

// vec4 fog_color = vec4(0.05, 0.3, 0.7, 1.0);

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

	vec4 shadedColor = vec4(color * max(0, lightLevel), alpha);

	float dist = length(fPosition.xyz);
	float fog_factor = (fog_maxdist - dist) / (fog_maxdist - fog_mindist);

	fog_factor = clamp(fog_factor, 0.0, 1.0);

	FragColor = mix(fog_color, shadedColor, fog_factor);
}
