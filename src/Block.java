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
			
		-0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 1.0f, 5f,
		 0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 1.0f, 5f,
		 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f, 5f,
		 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f, 5f,
		-0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 0.0f, 5f,
		-0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f, 0.0f, 1.0f, 5f
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
	
	public void getFaceArray(ArrayList<Integer> blockFaces) {
		for (int i = 0; i < 36; i++) {
			blockFaces.add(type.ordinal());
			blockFaces.add(i);
		}
	}
	
	public void getVertexArray(ArrayList<Float> vertices, float xOffset, float yOffset, float zOffset) {		
		for (int i = 0; i < 36; i++) {
			int vertexOffset = i * 3;
			// Positions 
			float xPos = Block.vertices[vertexOffset + 0];
			float yPos = Block.vertices[vertexOffset + 1];
			float zPos = Block.vertices[vertexOffset + 2];
			vertices.add(xOffset + xPos);
			vertices.add(yOffset + yPos);
			vertices.add(zOffset + zPos);
		}
	}
}
