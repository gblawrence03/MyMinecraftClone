import org.joml.*;
import java.lang.Math;
import java.util.ArrayList;

public class WorldGenerator {
	public Block[][][] positions;
	
	int length, width, height, seaLevel;
	
	int yScale = 50;
	
	public WorldGenerator(int seed, int length, int width, int height) {
		this.length = length;
		this.width = width;
		this.height = height;
		this.seaLevel = (int) (yScale / 5);
		
		// Generate seeds
		
		Random ran = new Random(seed);
		
		int layer1Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer2Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer3Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer4Seed = ran.nextInt(Integer.MAX_VALUE);
		int controlSeed = ran.nextInt(Integer.MAX_VALUE);
		
		float controlWavelength = 300f; // Controls how volatile the terrain in a given area is 
		int layer1Wavelength = 160; // Base height layer 
		int layer2Wavelength = 40; // Second base height layer
		int layer3Wavelength = 5; // To add small variations regardless of location
		int layer4Wavelength = 10; // Small peaks in high volalility areas
		
		float controlAmplitude = 0.5f;
		float layer1Amplitude = 0.6f;
		float layer2Amplitude = 0.4f;
		float layer3Amplitude = 0.005f;
		float layer4Amplitude = 0.15f;
		
		positions = new Block[length][height][width];
		
		// Populate block array
		for(int x = 0; x < length; x++) {
			for(int z = 0; z < width; z++) {
				
				// Get noise values
				float control = OpenSimplex2.noise2(controlSeed, x / controlWavelength, z / controlWavelength) * controlAmplitude + 0.5f;
				float layer1 = OpenSimplex2.noise2(layer1Seed, (float) x / layer1Wavelength, (float) z / layer1Wavelength) * 0.5f + 0.5f;
				float layer2 = OpenSimplex2.noise2(layer2Seed, (float) x / layer2Wavelength, (float) z / layer2Wavelength) * 0.5f + 0.5f;
				float layer3 = OpenSimplex2.noise2(layer3Seed, (float) x / layer3Wavelength, (float) z / layer3Wavelength) * 0.5f + 0.5f;
				float layer4 = OpenSimplex2.noise2(layer4Seed, (float) x / layer4Wavelength, (float) z / layer4Wavelength) * 0.5f + 0.5f;
				
				// Generate height, round to int
				double y = layer1Amplitude * layer1
						+  layer2Amplitude * layer2 * Math.pow(control, 2)
						+  layer3Amplitude * layer3 
						+ layer4Amplitude * layer4 * Math.pow(control, 10); 
				y = y / (layer1Amplitude + layer2Amplitude + layer3Amplitude + layer4Amplitude); // Scale to between 0 and 1
				y = Math.pow(y, 1.5);
				y *= yScale;
				int yPos = (int) Math.floor(y);
				
				// Ensure it doesn't exceed the height limit 
				yPos = Math.min(height - 1, yPos);
				
				int i = 0;
				// Fill with stone till height
				for (; i < yPos - 2; i++) {
					positions[x][i][z] = new Block(Block.BlockType.STONE);
				}
				// then dirt
				for (; i < yPos; i++) {
					positions[x][i][z] = new Block(Block.BlockType.DIRT);
				}
				// then grass
				positions[x][yPos][z] = new Block(Block.BlockType.GRASS);
				i++;
				// then water up to sea level
				for (; i <= seaLevel; i++) {
					positions[x][i][z] = new Block(Block.BlockType.WATER);
				}
				// then air
				for (; i < height; i++) {
					positions[x][i][z] = new Block(Block.BlockType.AIR);
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


