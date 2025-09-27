#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in int aVertexID;
layout (location = 1) in int aBlockID;

uniform mat4 model;
uniform mat4 view;
uniform mat4 perspective;
uniform vec3 cameraPos;
uniform vec3 cameraDir;

uniform isamplerBuffer atlasIndices;

uniform int atlasWidth;
uniform int atlasHeight;

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

vec2 texCoords[36] = vec2[] (
	vec2(1.0, 1.0),
	vec2(0.0, 1.0),
	vec2(0.0, 0.0),
	vec2(0.0, 0.0),
	vec2(1.0, 0.0),
	vec2(1.0, 1.0),

	vec2(0.0, 1.0),
	vec2(1.0, 1.0),
	vec2(1.0, 0.0),
	vec2(1.0, 0.0),
	vec2(0.0, 0.0),
	vec2(0.0, 1.0),

	vec2(1.0, 0.0),
	vec2(0.0, 0.0),
	vec2(0.0, 1.0),
	vec2(0.0, 1.0),
	vec2(1.0, 1.0),
	vec2(1.0, 0.0),

	vec2(0.0, 0.0),
	vec2(1.0, 0.0),
	vec2(1.0, 1.0),
	vec2(1.0, 1.0),
	vec2(0.0, 1.0),
	vec2(0.0, 0.0),

	vec2(0.0, 1.0),
	vec2(1.0, 1.0),
	vec2(1.0, 0.0),
	vec2(1.0, 0.0),
	vec2(0.0, 0.0),
	vec2(0.0, 1.0),

	vec2(0.0, 1.0),
	vec2(1.0, 1.0),
	vec2(1.0, 0.0),
	vec2(1.0, 0.0),
	vec2(0.0, 0.0),
	vec2(0.0, 1.0)
);

void main()
{
	int faceID = aVertexID / 6;
	gl_Position = perspective * view * vec4(aPos, 1.0);

	// Look up tile position (two ints = atlasX, atlasY)
	int lookupIndex = aBlockID * 12 + faceID * 2; // 12 = 6 faces * 2 ints
	ivec2 atlasTile = texelFetch(atlasIndices, lookupIndex).rg;

	// Convert to float UV by adding the cube-local offset
	vec2 tileUV = (vec2(atlasTile) + texCoords[aVertexID]) / vec2(atlasWidth, atlasHeight);

	texCoord = vec2(tileUV.x, 1.0 - tileUV.y);
	normal = faceNormals[faceID];
}
