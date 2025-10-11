import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import java.nio.*;
import java.lang.Math;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

record Direction(int dx, int dy, int dz) { }

public class Chunk {
	public static final int CHUNKSIZE = 1000;
	public static final int CHUNKHEIGHT = WorldGenerator.worldHeight;
	
	public Block[][][] blocks;
	public int cx;
	public int cz;
	
	public int cOffsetX;
	public int cOffsetZ;
	
	public ArrayList<Float> vertices;
	public FloatBuffer verticesBuffer;
	
	public ArrayList<Integer> lightLevels;
	public IntBuffer lightLevelsBuffer;
	
	public Chunk[][] neighbours; // Holds neighbouring chunks. 
								 // If null, the neighbouring chunk has not been loaded
	
	public int vao;
	public int vbo;
	public int lightVBO;
	public int vertexCount;
	
	public Chunk(int cx, int cz, Block[][][] blocks) {
		this.blocks = blocks;
		this.cx = cx;
		this.cz = cz;
		
		cOffsetX = cx * CHUNKSIZE;
		cOffsetZ = cz * CHUNKSIZE;
	}
	
	public void Update() {
		CalculateLightLevels();
		BuildMesh();
		uploadToGPU();
	}
	
	public void BuildMesh() {
		vertices = new ArrayList<Float>();
		lightLevels = new ArrayList<Integer>();
		
		for (int x = 0; x < CHUNKSIZE; x++) {
			for (int y = 0; y < CHUNKHEIGHT; y++) {
				for (int z = 0; z < CHUNKSIZE; z++) {
					Block block = blocks[x][y][z];
					
					// Don't draw air
					if (block.type == Block.BlockType.AIR) continue;
					
					for (int d = 0; d < 6; d++) {
						int nx = x + Block.directions[d].dx();
						int ny = y + Block.directions[d].dy();
						int nz = z + Block.directions[d].dz();
						
						// The light level of the face will be the light level of the next block
						
						
						if ((nx < 0 || nx >= CHUNKSIZE || nz < 0 || nz >= CHUNKSIZE) && block.type == Block.BlockType.WATER) {
							continue;
						} 
						
						// For now, we'll just draw all faces on chunk borders
						//TODO: Update to account for neighbouring chunks
						if (nx < 0 || nx >= CHUNKSIZE || ny < 0 || ny >= CHUNKHEIGHT || nz < 0 || nz >= CHUNKSIZE) {
							Block.addFaceToMesh(vertices, block.type, d, x + cOffsetX, y, z + cOffsetZ);
							lightLevels.add(15);
							lightLevels.add(15);
							lightLevels.add(15);
							lightLevels.add(15);
							lightLevels.add(15);
							lightLevels.add(15);
							continue;
						}
						
						Block nextBlock = blocks[nx][ny][nz];
						
						if (block.type == Block.BlockType.WATER && nextBlock.type != Block.BlockType.AIR) continue;
						
						// If the current block is water, we only want to draw the face 
						// if the next block is air
						if (block.type == Block.BlockType.WATER && nextBlock.type == Block.BlockType.AIR) {
							Block.addFaceToMesh(vertices, block.type, d, x + cOffsetX, y, z + cOffsetZ);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);

							continue;
						}
						
						// Otherwise, we draw the face if the next block is transparent
						if (nextBlock.IsTransparent()) {
							Block.addFaceToMesh(vertices, block.type, d, x + cOffsetX, y, z + cOffsetZ);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
							lightLevels.add(nextBlock.lightLevel);
						}
					}
				}
			}
		}
		
		vertexCount = vertices.size() / 8;
		
		verticesBuffer = BufferUtils.createFloatBuffer(vertices.size());
		for (float f : vertices) verticesBuffer.put(f);
		verticesBuffer.flip();
		
		lightLevelsBuffer = BufferUtils.createIntBuffer(vertices.size());
		for (int i : lightLevels) lightLevelsBuffer.put(i);
		lightLevelsBuffer.flip();
	}
	
	private void uploadToGPU() {
		if (vao != 0) glDeleteVertexArrays(vao);
		if (vbo != 0) glDeleteBuffers(vbo);
		if (lightVBO != 0) glDeleteBuffers(lightVBO);
		
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		
		int stride = 8 * Float.BYTES;
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
		glEnableVertexAttribArray(2);
		
		lightVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, lightVBO);
		glBufferData(GL_ARRAY_BUFFER, lightLevelsBuffer, GL_STATIC_DRAW);
		
		glVertexAttribIPointer(3, 1, GL_INT, Integer.BYTES, 0);
		glEnableVertexAttribArray(3);
		
	}
	
	private byte lightLevelFrom(int x, int y, int z) {
		// Light level from sides of the chunk is 1
		// TODO: Update this to use neighbouring chunks instead
		if (x < 0 || x >= CHUNKSIZE || z < 0 || z >= CHUNKSIZE || y >= CHUNKHEIGHT) return 1;
		
		// Light level from bottom of the world is 1
		if (y < 0) return 1;
		
		Block block = blocks[x][y][z];
		
		if (block.type == Block.BlockType.WATER) {
			return (byte) Math.max(1, block.lightLevel - 3);
		} else if (block.type == Block.BlockType.AIR) {
			return (byte) Math.max(1, block.lightLevel);
		} else {
			return 1;
		}
	}
	
	public void CalculateLightLevels() {
		// All blocks at the top of the world have a light level of 15
		for (int x = 0; x < CHUNKSIZE; x++) {
			for (int z = 0; z < CHUNKSIZE; z++) {
				Block block = blocks[x][CHUNKHEIGHT - 1][z];
				block.lightLevel = 15;
				block.isExposedToSunlight = true;
			}
		}
		
		// First pass: Trivial cases where light level must equal 15
		for (int x = 0; x < CHUNKSIZE; x++) {
			for (int y = CHUNKHEIGHT - 2; y >= 0; y--) {
				for (int z = 0; z < CHUNKSIZE; z++) {
					Block block = blocks[x][y][z];
					
					if (blocks[x][y + 1][z].type == Block.BlockType.AIR && blocks[x][y + 1][z].lightLevel == 15) {
						block.lightLevel = 15;
						block.isExposedToSunlight = true;
					}
				}
			}
		}
		
		
		// Continue making passes, calculating the new light levels, until no changes are made
		boolean changeMade = true;
		
		while (changeMade == true) {
			changeMade = false;
			for (int x = 0; x < CHUNKSIZE; x++) {
				for (int y = CHUNKHEIGHT - 2; y >= 0; y--) {
					for (int z = 0; z < CHUNKSIZE; z++) {
						Block block = blocks[x][y][z];
						
						if (block.isExposedToSunlight) {
							if (block.type == Block.BlockType.WATER) block.lightLevel = 12;
							else block.lightLevel = 15;
							continue;
						}
						
						int newLightLevel;
						newLightLevel = Math.max(
							lightLevelFrom(x - 1, y, z), Math.max(lightLevelFrom(x + 1, y, z),
							Math.max(lightLevelFrom(x, y - 1, z), Math.max(lightLevelFrom(x, y + 1, z),
							Math.max(lightLevelFrom(x, y, z - 1), lightLevelFrom(x, y, z + 1))))));
						
						if (block.type == Block.BlockType.AIR) {
							newLightLevel = Math.max(newLightLevel - 1, 1);
						}
						
						if (newLightLevel != block.lightLevel) {
							block.lightLevel = newLightLevel;
							changeMade = true;
						}
					}
				}
			}
		}	
	}
}
