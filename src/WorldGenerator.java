import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;

public class WorldGenerator {
	public static final int worldHeight = 50;

	int seaLevel;
	
	int yScale = 80;
	int baseHeight = 13;
	
	int layer1Seed, layer2Seed, layer3Seed, layer4Seed;
	int continentalSeed, peaksSeed, continentalDampenSeed, variationSeed;
	float continentalWavelength, peaksWavelength, continentalDampenWavelength, variationWavelength;
	int layer1Wavelength, layer2Wavelength, layer3Wavelength, layer4Wavelength;
	float continentalStrength, layer1Amplitude, layer2Amplitude, layer3Amplitude, layer4Amplitude;
	float[] continentalIndexes = {0, 0.2f, 0.4f, 0.41f, 0.6f, 0.8f, 1.0f};
	float[] continentalValues = {0, 0.2f, 0.2f, 0.4f, 0.4f, 0.3f, 0.1f};
	
	public WorldGenerator(int seed) {
		this.seaLevel = (int) (yScale / 4);
		
		// Generate seeds
		Random ran = new Random(seed);
		
		layer1Seed = ran.nextInt(Integer.MAX_VALUE);
		layer2Seed = ran.nextInt(Integer.MAX_VALUE);
		layer3Seed = ran.nextInt(Integer.MAX_VALUE);
		layer4Seed = ran.nextInt(Integer.MAX_VALUE);
		continentalSeed = ran.nextInt(Integer.MAX_VALUE);
		peaksSeed = ran.nextInt(Integer.MAX_VALUE);
		continentalDampenSeed = ran.nextInt(Integer.MAX_VALUE);
		variationSeed = ran.nextInt(Integer.MAX_VALUE);
		
		continentalWavelength = 200f; // Controls another base height layer, for land/water/cliffs
		peaksWavelength = 200f;
		continentalDampenWavelength = 100f; // Used to dampen the continental factor to prevent long cliffs
		variationWavelength = 300f;
		layer1Wavelength = 240; // Base height layer 
		layer2Wavelength = 60; // Second base height layer
		layer3Wavelength = 30; // To add small variations regardless of location
		layer4Wavelength = 10; // Small peaks in high volalility areas
		
		continentalStrength = 6f;
		layer1Amplitude = 4f;
		layer2Amplitude = 2f;
		layer3Amplitude = 1f;
		layer4Amplitude = 0.4f;
	}
	
	public Chunk GenerateChunk(int cx, int cz) {
		Block[][][] blocks = new Block[Chunk.CHUNKSIZE][worldHeight][Chunk.CHUNKSIZE];
		
		int worldX, worldZ;
		
		// Populate block array
		for(int x = 0; x < Chunk.CHUNKSIZE; x++) {
			for(int z = 0; z < Chunk.CHUNKSIZE; z++) {
						
				worldX = cx * Chunk.CHUNKSIZE + x;
				worldZ = cz * Chunk.CHUNKSIZE + z;
				
				// Get noise values
				float continentalVal = OpenSimplex2.noise2(continentalSeed, worldX / continentalWavelength, worldZ / continentalWavelength) * 0.5f + 0.5f;
				float continentalDampen = OpenSimplex2.noise2(continentalDampenSeed, worldX / continentalDampenWavelength, worldZ / continentalDampenWavelength) * 0.5f + 0.5f;
				float continental = lerpSpline(continentalIndexes, continentalValues, continentalVal);
				float peaks = OpenSimplex2.noise2(peaksSeed, worldX / peaksWavelength, worldZ / peaksWavelength) * 0.5f + 0.5f;
				float variation = OpenSimplex2.noise2(variationSeed, worldX / variationWavelength, worldZ / variationWavelength) * 0.5f + 0.5f;
				float layer1 = OpenSimplex2.noise2(layer1Seed, (float) worldX / layer1Wavelength, (float) worldZ / layer1Wavelength) * 0.5f + 0.5f;
				float layer2 = OpenSimplex2.noise2(layer2Seed, (float) worldX / layer2Wavelength, (float) worldZ / layer2Wavelength) * 0.5f + 0.5f;
				float layer3 = OpenSimplex2.noise2(layer3Seed, (float) worldX / layer3Wavelength, (float) worldZ / layer3Wavelength) * 0.5f + 0.5f;
				float layer4 = OpenSimplex2.noise2(layer4Seed, (float) worldX / layer4Wavelength, (float) worldZ / layer4Wavelength) * 0.5f + 0.5f;
						
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
				yPos = Math.min(worldHeight - 1, yPos);
						
				int i = 0;
				// Fill with stone till height
				for (; i < yPos - 2; i++) {
					blocks[x][i][z] = new Block(Block.BlockType.STONE);
				}
				// then dirt
				for (; i < yPos; i++) {
					blocks[x][i][z] = new Block(Block.BlockType.DIRT);
				}
						
				// then grass, or sand if below sea level
				if (yPos >= seaLevel) blocks[x][yPos][z] = new Block(Block.BlockType.GRASS);
				else blocks[x][i][z] = new Block(Block.BlockType.SAND);
						
				i++;
				// then water up to sea level
				for (; i <= seaLevel; i++) {
					blocks[x][i][z] = new Block(Block.BlockType.WATER);
				}
				// then air
				for (; i < worldHeight; i++) {
					blocks[x][i][z] = new Block(Block.BlockType.AIR);
				}
			}
		}
		
		int circx = Chunk.CHUNKSIZE/2;
		int circz = Chunk.CHUNKSIZE/2; 
		int circy = 16; 
		int r = 12;
		
		for (int x = 0; x < Chunk.CHUNKSIZE; x++) {
			for (int z = 0; z < Chunk.CHUNKSIZE; z++) {
				for (int i = 0; i < worldHeight; i++) {
					if ((x + (Chunk.CHUNKSIZE * cx) - circx)*(x + (Chunk.CHUNKSIZE * cx) - circx) + (i - circy)*(i - circy) + (z + (Chunk.CHUNKSIZE * cz) - circz)*(z + (Chunk.CHUNKSIZE * cz) - circz) < r*r) {
						blocks[x][i][z] = new Block(Block.BlockType.AIR);
					}
				}
			}
		}
		
		circx = Chunk.CHUNKSIZE/2 + 13;
		circz = Chunk.CHUNKSIZE/2 + 7; 
		circy = 11; 
		r = 9;
		
		for (int x = 0; x < Chunk.CHUNKSIZE; x++) {
			for (int z = 0; z < Chunk.CHUNKSIZE; z++) {
				for (int i = 0; i < worldHeight; i++) {
					if ((x + (Chunk.CHUNKSIZE * cx) - circx)*(x + (Chunk.CHUNKSIZE * cx) - circx) + (i - circy)*(i - circy) + (z + (Chunk.CHUNKSIZE * cz) - circz)*(z + (Chunk.CHUNKSIZE * cz) - circz) < r*r) {
						blocks[x][i][z] = new Block(Block.BlockType.AIR);
					}
				}
			}
		}
		
		return new Chunk(cx, cz, blocks);
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


