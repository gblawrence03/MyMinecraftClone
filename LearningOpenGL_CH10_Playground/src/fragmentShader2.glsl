#version 330 core
in vec4 vertexColor;
out vec4 FragColor;
uniform float ourColorBrightness;
void main()
{
	// FragColor = ourColor;
	float vx = vertexColor.x;
	float vy = vertexColor.y;
	float vz = vertexColor.z;
	FragColor = vec4(vx * ourColorBrightness, vy * ourColorBrightness, vz * ourColorBrightness, vertexColor.w);
}
