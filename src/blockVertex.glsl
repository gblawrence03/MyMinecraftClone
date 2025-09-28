#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in float aFaceID;
layout (location = 4) in ivec3 aOffset; // World translation
layout (location = 5) in int aBlockID; // Block type

uniform mat4 model;
uniform mat4 view;
uniform mat4 perspective;
uniform vec3 cameraPos;
uniform vec3 cameraDir;

uniform isamplerBuffer atlasIndices;

uniform int atlasWidth;
uniform int atlasHeight;

out vec2 texCoord;
flat out vec3 normal;

void main()
{
	gl_Position = perspective * view * vec4(aPos + aOffset, 1.0);

	// Look up tile position (two ints = atlasX, atlasY)
	int lookupIndex = aBlockID * 12 + int(aFaceID) * 2; // 12 = 6 faces * 2 ints
	ivec2 atlasTile = texelFetch(atlasIndices, lookupIndex).rg;

	// Convert to float UV by adding the cube-local offset
	vec2 tileUV = (vec2(atlasTile) + aTexCoord) / vec2(atlasWidth, atlasHeight);
	texCoord = vec2(tileUV.x, 1.0 - tileUV.y);

	normal = aNormal;
}
