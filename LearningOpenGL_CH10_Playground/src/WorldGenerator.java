import org.joml.*;
import java.lang.Math;
import java.util.ArrayList;

public class WorldGenerator {
	public Block[][][] positions;
	
	int length, width, height, seaLevel;
	
	public WorldGenerator(int seed, int length, int width, int height) {
		this.length = length;
		this.width = width;
		this.height = height;
		this.seaLevel = (int) (height / 2.5);
		
		// Generate seeds
		int layer1Seed = seed;
		Random ran = new Random(seed);
		int layer2Seed = ran.nextInt(10000);
		int layer3Seed = ran.nextInt(10000);
		
		int layer1Wavelength = 40;
		int layer2Wavelength = 20;
		int layer3Wavelength = 10;
		
		float layer1Amplitude = 0.4f;
		float layer2Amplitude = 0.2f;
		float layer3Amplitude = 0.1f;
		
		positions = new Block[length][height][width];
		
		// Populate block array
		for(int x = 0; x < length; x++) {
			for(int z = 0; z < width; z++) {
				
				// Get noise values
				float layer1 = OpenSimplex2.noise2(layer1Seed, (float) x / layer1Wavelength, (float) z / layer1Wavelength) + 1;
				float layer2 = OpenSimplex2.noise2(layer2Seed, (float) x / layer2Wavelength, (float) z / layer2Wavelength) + 1;
				float layer3 = OpenSimplex2.noise2(layer3Seed, (float) x / layer3Wavelength, (float) z / layer3Wavelength) + 1;
				
				// Generate height, round to int
				double y = layer1Amplitude * layer1
						+  layer2Amplitude * layer2
						+  layer3Amplitude * layer3;
				y = y / (2 * (layer1Amplitude + layer2Amplitude + layer3Amplitude)); // halve because y value will be between 0 and 2 
				y = Math.pow(y, 1.5);
				y *= height;
				int yPos = (int) Math.floor(y);
				
				// Fill with stone till height, air for the rest
				for (int i = 0; i < yPos; i++) {
					positions[x][i][z] = new Block(Block.BlockType.STONE);
				}
				positions[x][yPos][z] = new Block(Block.BlockType.GRASS);
				for (int i = yPos + 1; i < height; i++) {
					positions[x][i][z] = new Block(Block.BlockType.AIR);
				}
				for (int i = yPos + 1; i <= seaLevel; i++) {
					positions[x][i][z] = new Block(Block.BlockType.WATER);
				}
			}
		}
	}
	
	// Block culling
	public boolean blockAdjacentToAir(int x, int y, int z) {
		// If it is at the edge of the world, consider it adjacent to the air 
		// except when below the world
		if (x <= 0 || x >= length - 1) return true;
		if (		  y >= height - 1) return true;
		if (z <= 0 || z >= width - 1) return true;
		
		// Check adjacent blocks
		if (positions[x - 1][y][z].type == Block.BlockType.AIR) return true;
		if (positions[x + 1][y][z].type == Block.BlockType.AIR) return true;
		if (y > 0)
			if (positions[x][y - 1][z].type == Block.BlockType.AIR) return true;
		if (positions[x][y + 1][z].type == Block.BlockType.AIR) return true;
		if (positions[x][y][z - 1].type == Block.BlockType.AIR) return true;
		if (positions[x][y][z + 1].type == Block.BlockType.AIR) return true;
		
		return false;
	}
}


