import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Block {
	public enum BlockType {
		AIR,
		GRASS,
		STONE,
		WATER,
		DIRT
	}
	
	public enum BlockFace {
		SOUTH,
		NORTH,
		WEST,
		EAST,
		DOWN,
		UP
	}
	
	static private class TilePos {
		public int x;
		public int y;
		TilePos(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public static float[] vertices = {
		// positions 		 // normals 	   // texcoords // FaceID
		-0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 1.0f, 1.0f, 0f,
		 0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 0.0f, 1.0f, 0f,
		 0.5f, 	0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 0.0f, 0.0f, 0f,
		 0.5f, 	0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 0.0f, 0.0f, 0f,
		-0.5f, 	0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 1.0f, 0.0f, 0f,
		-0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f, 1.0f, 1.0f, 0f,
			
		-0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 0.0f, 1.0f, 1f,
		 0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 1.0f, 1.0f, 1f,
   	 	 0.5f, 	0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 1.0f, 0.0f, 1f,
 		 0.5f, 	0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 1.0f, 0.0f, 1f,
		-0.5f, 	0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 0.0f, 0.0f, 1f,
		-0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f, 0.0f, 1.0f, 1f,
			
		-0.5f, 	0.5f,  0.5f, -1.0f,  0.0f,  0.0f, 1.0f, 0.0f, 2f,
		-0.5f, 	0.5f, -0.5f, -1.0f,  0.0f,  0.0f, 0.0f, 0.0f, 2f,
		-0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f, 0.0f, 1.0f, 2f,
		-0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f, 0.0f, 1.0f, 2f,
		-0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f, 1.0f, 1.0f, 2f,
		-0.5f, 	0.5f,  0.5f, -1.0f,  0.0f,  0.0f, 1.0f, 0.0f, 2f,
			
		 0.5f, 	0.5f,  0.5f,  1.0f,  0.0f,  0.0f, 0.0f, 0.0f, 3f,
		 0.5f, 	0.5f, -0.5f,  1.0f,  0.0f,  0.0f, 1.0f, 0.0f, 3f,
		 0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f, 1.0f, 1.0f, 3f,
		 0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f, 1.0f, 1.0f, 3f,
		 0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f, 0.0f, 1.0f, 3f,
		 0.5f, 	0.5f,  0.5f,  1.0f,  0.0f,  0.0f, 0.0f, 0.0f, 3f,
			
		-0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f, 0.0f, 1.0f, 4f,
		 0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f, 1.0f, 1.0f, 4f,
		 0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f, 1.0f, 0.0f, 4f,
		 0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f, 1.0f, 0.0f, 4f,
		-0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f, 0.0f, 0.0f, 4f,
		-0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f, 0.0f, 1.0f, 4f,
			
		-0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 1.0f, 5f,
		 0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 1.0f, 5f,
		 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 0.0f, 5f,
		 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 0.0f, 5f,
		-0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f, 5f,
		-0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 1.0f, 5f
	};
	
	public static Texture blockAtlas = new Texture("data/blocks.png");
	public static final Map<BlockType, TilePos[]> atlasMap = new EnumMap<>(BlockType.class);
	
	public static int atlasWidth;
	public static int atlasHeight;
	public static int[] atlasIndices; // Atlas texture lookup

	static {
		atlasWidth = blockAtlas.width / 16;
		atlasHeight = blockAtlas.height / 16;
		
		atlasMap.put(BlockType.AIR, new TilePos[] {
				new TilePos(0, 0), new TilePos(0, 0),
				new TilePos(0, 0), new TilePos(0, 0),
				new TilePos(0, 0), new TilePos(0, 0)
		});
		
		// Order of faces: SOUTH, NORTH, WEST, EAST, DOWN, UP
		atlasMap.put(BlockType.GRASS, new TilePos[] {
			new TilePos(5, 0),
			new TilePos(5, 0),
			new TilePos(5, 0),
			new TilePos(5, 0),
			new TilePos(4, 0),
			new TilePos(1, 0)
		});
		
		atlasMap.put(BlockType.DIRT, new TilePos[] {
			new TilePos(4, 0), new TilePos(4, 0),
			new TilePos(4, 0), new TilePos(4, 0),
			new TilePos(4, 0), new TilePos(4, 0)
		});
		
		atlasMap.put(BlockType.STONE, new TilePos[] {
			new TilePos(2, 0), new TilePos(2, 0),
			new TilePos(2, 0), new TilePos(2, 0),
			new TilePos(2, 0), new TilePos(2, 0)
		});

		atlasMap.put(BlockType.WATER, new TilePos[] {
			new TilePos(3, 0), new TilePos(3, 0),
			new TilePos(3, 0), new TilePos(3, 0),
			new TilePos(3, 0), new TilePos(3, 0)
		});
		
		int numBlockTypes = BlockType.values().length; 
		atlasIndices = new int[numBlockTypes * 6 * 2]; // 6 faces, x and y per face
		
		for (BlockType type : BlockType.values()) {
			TilePos[] tiles = atlasMap.get(type);
			int baseIndex = type.ordinal() * 12;
			
			for (int f = 0; f < 6; f++) {
				atlasIndices[baseIndex + f*2] = tiles[f].x;
				atlasIndices[baseIndex + f*2 + 1] = tiles[f].y;
			}
		}
	}
	
	public BlockType type;
	
	public Block(BlockType type) {
		this.type = type;
	}
	
	/*
	 * Precomputes positions, normals, texcoords for a given block type
	 */
	public static float[] precomputeVertexesForType(BlockType type) {
		float[] typeVertices = new float[36 * 8];
		
		for (int i = 0; i < 36; i++) {
			int fetchOffset = i*9;
			int vertexOffset = i*8;
			
			// Positions
			typeVertices[vertexOffset] = vertices[fetchOffset];
			typeVertices[vertexOffset + 1] = vertices[fetchOffset + 1];
			typeVertices[vertexOffset + 2] = vertices[fetchOffset + 2];
			
			// Normals
			typeVertices[vertexOffset + 3] = vertices[fetchOffset + 3];
			typeVertices[vertexOffset + 4] = vertices[fetchOffset + 4];
			typeVertices[vertexOffset + 5] = vertices[fetchOffset + 5];
			
			// UVs
			int faceID = i / 6;
			int baseIndex = type.ordinal() * 12 + faceID * 2;
			int atlasX = atlasIndices[baseIndex];
			int atlasY = atlasIndices[baseIndex + 1];
			
			float vertexU = vertices[fetchOffset + 6];
			float vertexV = vertices[fetchOffset + 7];
			
			typeVertices[vertexOffset + 6] = (atlasX + vertexU) / atlasWidth;
			typeVertices[vertexOffset + 7] = 1 - (atlasY + vertexV) / atlasHeight;
		}
		return typeVertices;
	}
}
