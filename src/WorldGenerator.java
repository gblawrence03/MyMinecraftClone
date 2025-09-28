import org.joml.*;
import java.lang.Math;
import java.util.ArrayList;

public class WorldGenerator {
	public Block[][][] positions;
	
	int length, width, height, seaLevel;
	
	int yScale = 80;
	int baseHeight = 15;
	
	public WorldGenerator(int seed, int length, int width, int height) {
		this.length = length;
		this.width = width;
		this.height = height;
		this.seaLevel = (int) (yScale / 4);
		
		// Generate seeds
		
		Random ran = new Random(seed);
		
		int layer1Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer2Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer3Seed = ran.nextInt(Integer.MAX_VALUE);
		int layer4Seed = ran.nextInt(Integer.MAX_VALUE);
		int continentalSeed = ran.nextInt(Integer.MAX_VALUE);
		int peaksSeed = ran.nextInt(Integer.MAX_VALUE);
		int continentalDampenSeed = ran.nextInt(Integer.MAX_VALUE);
		int variationSeed = ran.nextInt(Integer.MAX_VALUE);
		
		float continentalWavelength = 200f; // Controls another base height layer, for land/water/cliffs
		float peaksWavelength = 200f;
		float continentalDampenWavelength = 100f; // Used to dampen the continental factor to prevent long cliffs
		float variationWavelength = 300f;
		int layer1Wavelength = 240; // Base height layer 
		int layer2Wavelength = 60; // Second base height layer
		int layer3Wavelength = 30; // To add small variations regardless of location
		int layer4Wavelength = 10; // Small peaks in high volalility areas
		
		float continentalStrength = 6f;
		float layer1Amplitude = 4f;
		float layer2Amplitude = 2f;
		float layer3Amplitude = 1f;
		float layer4Amplitude = 0.4f;
		
		// Should include 0 and 1 and be in order
		float[] continentalIndexes = {0, 0.2f, 0.4f, 0.41f, 0.6f, 0.8f, 1};
		float[] continentalValues = {0, 0.2f, 0.2f, 0.4f, 0.4f, 0.3f, 0.1f};
		
		positions = new Block[length][height][width];
		
		// Populate block array
		for(int x = 0; x < length; x++) {
			for(int z = 0; z < width; z++) {
				
				// Get noise values
				float continentalVal = OpenSimplex2.noise2(continentalSeed, x / continentalWavelength, z / continentalWavelength) * 0.5f + 0.5f;
				float continentalDampen = OpenSimplex2.noise2(continentalDampenSeed, x / continentalDampenWavelength, z / continentalDampenWavelength) * 0.5f + 0.5f;
				float continental = lerpSpline(continentalIndexes, continentalValues, continentalVal);
				float peaks = OpenSimplex2.noise2(peaksSeed, x / peaksWavelength, z / peaksWavelength) * 0.5f + 0.5f;
				float variation = OpenSimplex2.noise2(variationSeed, x / variationWavelength, z / variationWavelength) * 0.5f + 0.5f;
				float layer1 = OpenSimplex2.noise2(layer1Seed, (float) x / layer1Wavelength, (float) z / layer1Wavelength) * 0.5f + 0.5f;
				float layer2 = OpenSimplex2.noise2(layer2Seed, (float) x / layer2Wavelength, (float) z / layer2Wavelength) * 0.5f + 0.5f;
				float layer3 = OpenSimplex2.noise2(layer3Seed, (float) x / layer3Wavelength, (float) z / layer3Wavelength) * 0.5f + 0.5f;
				float layer4 = OpenSimplex2.noise2(layer4Seed, (float) x / layer4Wavelength, (float) z / layer4Wavelength) * 0.5f + 0.5f;
				
				// Generate height, round to int
				double y = layer1Amplitude * layer1 
						+  layer2Amplitude * layer2 * variation * peaks
						+  layer3Amplitude * layer3 * variation * peaks
						+ layer4Amplitude * layer4 * variation * variation
						+ continental * continentalStrength * continentalDampen * continentalDampen * continentalDampen; 
				y = y / (layer1Amplitude + layer2Amplitude + layer3Amplitude + layer4Amplitude + continentalStrength); // Scale to between 0 and 1
				y = Math.pow(y, 1.5f);
				y *= yScale;
				y += baseHeight;
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
				
				if (yPos >= seaLevel) positions[x][yPos][z] = new Block(Block.BlockType.GRASS);
				else positions[x][i][z] = new Block(Block.BlockType.SAND);
				
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
	public boolean blockVisible(int x, int y, int z) {
		Block.BlockType myType = positions[x][y][z].type;
		
		if (myType == Block.BlockType.WATER) {
			// We only want to render water when it's adjacent to air
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
		
		// Check adjacent blocks
		if (positions[x - 1][y][z].type == Block.BlockType.WATER) return true;
		if (positions[x + 1][y][z].type == Block.BlockType.WATER) return true;
		if (y > 0)
			if (positions[x][y - 1][z].type == Block.BlockType.WATER) return true;
		if (positions[x][y + 1][z].type == Block.BlockType.WATER) return true;
		if (positions[x][y][z - 1].type == Block.BlockType.WATER) return true;
		if (positions[x][y][z + 1].type == Block.BlockType.WATER) return true;
		
		return false;
	}
	
	// Splines for terrain shaping
	private float lerpSpline(float[] indexes, float[] values, float index) {
		int i;
		for (i = 0; i < indexes.length; i++) {
			if (index == indexes[i]) return values[i];
			if (index <= indexes[i]) break;
		}

		// i - 1 is now the index of the first value we will interpolate from, i is the second
		float distToIndex1 = index - indexes[i - 1];
		float distBetween = indexes[i] - indexes[i - 1];
		return values[i - 1] + (values[i] - values[i - 1])/distBetween * distToIndex1;
	}
}


