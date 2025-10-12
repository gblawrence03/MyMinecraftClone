import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import java.nio.*;
import java.lang.Math;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

record Direction(int dx, int dy, int dz) { }

record ChunkDirection(int dx, int dz) { } 

public class Chunk {
	public static final int CHUNKSIZE = 16;
	public static final int CHUNKHEIGHT = WorldGenerator.worldHeight;
	
	public Block[][][] blocks;
	public int cx;
	public int cz;
	
	public int cOffsetX;
	public int cOffsetZ;
	
	public ArrayList<Float> opaqueVertices;
	public FloatBuffer opaqueVerticesBuffer;
	
	public ArrayList<Integer> opaqueLight;
	public IntBuffer opaqueLightBuffer;
	
	public ArrayList<Float> transparentVertices;
	public FloatBuffer transparentVerticesBuffer;
	
	public ArrayList<Integer> transparentLight;
	public IntBuffer transparentLightBuffer;
	 
	public HashMap<ChunkDirection, Chunk> neighbourMap; // Holds neighbouring chunks. 
								 // If null, the neighbouring chunk has not been loaded
	
	public int opaqueVAO;
	public int opaqueVBO;
	public int opaqueLightVBO;
	public int opaqueVertexCount;
	
	public int transparentVAO;
	public int transparentVBO;
	public int transparentLightVBO;
	public int transparentVertexCount;
	
	public Chunk(int cx, int cz, Block[][][] blocks) {
		this.neighbourMap = new HashMap<ChunkDirection, Chunk>();
		neighbourMap.put(new ChunkDirection(0, 0), this); // Add self to neighbour map
		this.blocks = blocks;
		this.cx = cx;
		this.cz = cz;
		
		cOffsetX = cx * CHUNKSIZE;
		cOffsetZ = cz * CHUNKSIZE;
	}
	
	public void SetNeighbour(int dx, int dz, Chunk chunk) {
		neighbourMap.put(new ChunkDirection(dx, dz), chunk);
	}
	
	public void Update() {
		CalculateLightLevels();
		BuildMesh();
		uploadToGPU();
	}
	
	// x, y, z is positions in our chunk's reference frame - 
	// e.g. -1 x or z means the block beyond our left/south border (in the next chunk)
	// does not work if more than 1 chunk away
	// currently does not work for diagonal chunks (more neighbours needed)
	private Block getBlockAt(int x, int y, int z) {
		if (y >= CHUNKHEIGHT || y < 0) return null; // no block above or below y limit
		
		int chunkdx = 0;
		int chunkdz = 0;
		
		int xInChunk = x;
		int zInChunk = z;
		
		if (x < 0) {
			chunkdx = -1;
			xInChunk = CHUNKSIZE + x;
		}
			
		if (x >= CHUNKSIZE) {
			chunkdx = 1;
			xInChunk = x - CHUNKSIZE;
		}
		
		if (z < 0) {
			chunkdz = -1;
			zInChunk = CHUNKSIZE + z;
		}
			
		if (z >= CHUNKSIZE) {
			chunkdz = 1;
			zInChunk = z - CHUNKSIZE;
		}
		
		Chunk chunk = neighbourMap.get(new ChunkDirection(chunkdx, chunkdz));
		if (chunk == null) return null;
		return chunk.blocks[xInChunk][y][zInChunk];
	}
	
	// x, y, z: Position of the block in our chunk. 
	// dir: direction of the block we care about.
	private Block getBlockInDirection(int x, int y, int z, Direction dir) {
		int nx = x + dir.dx();
		int ny = y + dir.dy();
		int nz = z + dir.dz();
		
		return getBlockAt(nx, ny, nz);
	}
	
	public void BuildMesh() {
		opaqueVertices = new ArrayList<Float>();
		opaqueLight = new ArrayList<Integer>();
		
		transparentVertices = new ArrayList<Float>();
		transparentLight = new ArrayList<Integer>();
		
		for (int x = 0; x < CHUNKSIZE; x++) {
			for (int y = 0; y < CHUNKHEIGHT; y++) {
				for (int z = 0; z < CHUNKSIZE; z++) {
					Block block = blocks[x][y][z];
					
					// Don't draw air
					if (block.type == Block.BlockType.AIR) continue;
					
					for (int d = 0; d < 6; d++) {
						Direction dir = Block.directions[d];
						
						Block nextBlock = getBlockInDirection(x, y, z, dir);
						
						boolean drawFace = false;
						int lightLevel = 15;
						
						// The light level of the face will be the light level of the next block
						
						if (nextBlock == null) {
							// The next block is either above the height limit
							// or below 0, regardless, draw the face
							if (dir.dy() != 1) {
								drawFace = true;
								lightLevel = 15;
							} else {
								// Otherwise we're at the border of the generated world
								// In this case, we don't want to draw a water side face
								// Also assume light level of 1 (for caves etc)
								//TODO: Fix water side faces
								if (block.type != Block.BlockType.WATER) {
									drawFace = true;
									lightLevel = 1;
								}
							}
						} else {
							System.out.println("Hello");
							
							// Not drawing water if it's not next to air
							if (block.type == Block.BlockType.WATER && nextBlock.type != Block.BlockType.AIR) continue;
							
							// Otherwise, we draw the face if the next block is transparent
							if (nextBlock.IsTransparent()) {
								drawFace = true;
								lightLevel = nextBlock.lightLevel;
							}
						}
						
						if (drawFace == false) continue;
						
						// Add to either transparent or opaque buffers
						if (block.IsTransparent()) {
							Block.addFaceToMesh(transparentVertices, block.type, d, x + cOffsetX, y, z + cOffsetZ);
							transparentLight.add(lightLevel);
							transparentLight.add(lightLevel);
							transparentLight.add(lightLevel);
							transparentLight.add(lightLevel);
							transparentLight.add(lightLevel);
							transparentLight.add(lightLevel);
						} else {
							Block.addFaceToMesh(opaqueVertices, block.type, d, x + cOffsetX, y, z + cOffsetZ);
							opaqueLight.add(lightLevel);
							opaqueLight.add(lightLevel);
							opaqueLight.add(lightLevel);
							opaqueLight.add(lightLevel);
							opaqueLight.add(lightLevel);
							opaqueLight.add(lightLevel);
						}
					}
				}
			}
		}
		
		opaqueVertexCount = opaqueVertices.size() / 8;
		
		opaqueVerticesBuffer = BufferUtils.createFloatBuffer(opaqueVertices.size());
		for (float f : opaqueVertices) opaqueVerticesBuffer.put(f);
		opaqueVerticesBuffer.flip();
		
		opaqueLightBuffer = BufferUtils.createIntBuffer(opaqueVertices.size());
		for (int i : opaqueLight) opaqueLightBuffer.put(i);
		opaqueLightBuffer.flip();
		
		transparentVertexCount = transparentVertices.size() / 8;
		
		transparentVerticesBuffer = BufferUtils.createFloatBuffer(transparentVertices.size());
		for (float f : transparentVertices) transparentVerticesBuffer.put(f);
		transparentVerticesBuffer.flip();
		
		transparentLightBuffer = BufferUtils.createIntBuffer(transparentVertices.size());
		for (int i : transparentLight) transparentLightBuffer.put(i);
		transparentLightBuffer.flip();
	}
	
	private void uploadToGPU() {
		
		// Opaque
		if (opaqueVAO != 0) glDeleteVertexArrays(opaqueVAO);
		if (opaqueVBO != 0) glDeleteBuffers(opaqueVBO);
		if (opaqueLightVBO != 0) glDeleteBuffers(opaqueLightVBO);
		
		opaqueVAO = glGenVertexArrays();
		glBindVertexArray(opaqueVAO);
		
		opaqueVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, opaqueVBO);
		glBufferData(GL_ARRAY_BUFFER, opaqueVerticesBuffer, GL_STATIC_DRAW);
		
		int stride = 8 * Float.BYTES;
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
		glEnableVertexAttribArray(2);
		
		opaqueLightVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, opaqueLightVBO);
		glBufferData(GL_ARRAY_BUFFER, opaqueLightBuffer, GL_STATIC_DRAW);
		
		glVertexAttribIPointer(3, 1, GL_INT, Integer.BYTES, 0);
		glEnableVertexAttribArray(3);
		
		// Transparent
		if (transparentVAO != 0) glDeleteVertexArrays(transparentVAO);
		if (transparentVBO != 0) glDeleteBuffers(transparentVBO);
		if (transparentLightVBO != 0) glDeleteBuffers(transparentLightVBO);
		
		transparentVAO = glGenVertexArrays();
		glBindVertexArray(transparentVAO);
		
		transparentVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, transparentVBO);
		glBufferData(GL_ARRAY_BUFFER, transparentVerticesBuffer, GL_STATIC_DRAW);
		
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
		glEnableVertexAttribArray(2);
		
		transparentLightVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, transparentLightVBO);
		glBufferData(GL_ARRAY_BUFFER, transparentLightBuffer, GL_STATIC_DRAW);
		
		glVertexAttribIPointer(3, 1, GL_INT, Integer.BYTES, 0);
		glEnableVertexAttribArray(3);
	}
	
	private byte lightLevelFrom(int x, int y, int z) {
		// Light level from sides of the chunk is 1
		// TODO: Update this to use neighbouring chunks instead
		// if (x < 0 || x >= CHUNKSIZE || z < 0 || z >= CHUNKSIZE) return 1;
		
		// Light level from the top of the world is 15
		if (y >= CHUNKHEIGHT) return 15;
		
		// Light level from bottom of the world is 1
		if (y < 0) return 1;
		
		Block block = getBlockAt(x, y, z);
		
		if (block == null) return 1;
		
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
