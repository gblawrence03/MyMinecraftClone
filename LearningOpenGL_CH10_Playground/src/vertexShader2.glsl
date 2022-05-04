#version 330 core
layout (location = 0) in vec3 aPos;
out vec4 vertexColor;
uniform float xPositionOffset;

void main()
{
	gl_Position = vec4(aPos.x + xPositionOffset, aPos.y, aPos.z, 1.0);
	vertexColor = vec4(aPos.x + 0.5, gl_Position.y + 0.5, gl_Position.zw);
}
