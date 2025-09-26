import java.util.ArrayList;
import java.util.HashMap;

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
	
	static private class TexIndex {
		private BlockType blockType;
		private BlockFace blockFace;
		
		public TexIndex(BlockType blockType, BlockFace blockFace) {
			this.blockType = blockType;
			this.blockFace = blockFace; 
		}
		
		@Override
		public int hashCode() {
			return this.blockType.ordinal() ^ this.blockFace.ordinal();
		}
		
		@Override
	    public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        TexIndex other = (TexIndex) obj;
	        if (blockType != other.blockType)
	            return false;
	        if (blockFace != other.blockFace)
	            return false;
	        return true;
	    }
	}
	
	public static float[] vertices = {
			// positions 		 // normals 		// texture coords
			-0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
			0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
			0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			-0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
			-0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
			
			-0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
			0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
			0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
			0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
			-0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
			-0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
			
			-0.5f, 	0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			-0.5f, 	0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			-0.5f, 	-0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			-0.5f, 	0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			
			0.5f, 	0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			0.5f, 	0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			0.5f, 	-0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			0.5f, 	-0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			0.5f, 	-0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 	0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			
			-0.5f, 	-0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 	-0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
			0.5f, 	-0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
			0.5f, 	-0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
			-0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
			-0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
			
			-0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
			0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
			-0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
			-0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
	};
	
	public static HashMap<TexIndex, Integer> textureMap;
	public static Texture blockAtlas = new Texture("data/blocks.png");
	
	public static int atlasWidth;
	public static int atlasHeight;

	static {
		atlasWidth = blockAtlas.width / 16;
		atlasHeight = blockAtlas.height / 16;
		
		textureMap = new HashMap<>();
		// Grass
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.UP), 1);
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.DOWN), 4);
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.NORTH), 5);
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.SOUTH), 5);
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.WEST), 5);
		textureMap.put(new TexIndex(BlockType.GRASS, BlockFace.EAST), 5);
		// Stone
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.UP), 2);
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.DOWN), 2);
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.NORTH), 2);
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.SOUTH), 2);
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.WEST), 2);
		textureMap.put(new TexIndex(BlockType.STONE, BlockFace.EAST), 2);
		// Water
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.UP), 3);
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.DOWN), 3);
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.NORTH), 3);
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.SOUTH), 3);
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.WEST), 3);
		textureMap.put(new TexIndex(BlockType.WATER, BlockFace.EAST), 3);
		// Dirt
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.UP), 4);
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.DOWN), 4);
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.NORTH), 4);
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.SOUTH), 4);
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.WEST), 4);
		textureMap.put(new TexIndex(BlockType.DIRT, BlockFace.EAST), 4);
	}
	
	public BlockType type;
	
	public Block(BlockType type) {
		this.type = type;
	}
	
	
	public ArrayList<Float> getVertexArray(float xOffset, float yOffset, float zOffset) {
		ArrayList<Float> vertices = new ArrayList<Float>(36 * 8);
		
		for (int i = 0; i < 36; i++) {
			// Positions 
			float xPos = Block.vertices[i * 8 + 0];
			float yPos = Block.vertices[i * 8 + 1];
			float zPos = Block.vertices[i * 8 + 2];
			vertices.add(xOffset + xPos);
			vertices.add(yOffset + yPos);
			vertices.add(zOffset + zPos);
			// Normals 
			vertices.add(Block.vertices[i * 8 + 3]);
			vertices.add(Block.vertices[i * 8 + 4]);
			vertices.add(Block.vertices[i * 8 + 5]);
			
			// Get the face and texture based on the index of the vertex (groups of 6 vertices)
			BlockFace face = BlockFace.values()[i / 6];
			int atlasIndex = textureMap.get(new TexIndex(type, face));
			
			// Calculate texture coordinates

			int atlasXPos = atlasIndex / atlasHeight;
			int atlasYPos = atlasIndex % atlasWidth;
			
			vertices.add(((float) atlasXPos + Block.vertices[i * 8 + 6]) / (float) atlasWidth);
			vertices.add(1 - ((float) atlasYPos + Block.vertices[i * 8 + 7]) / (float) atlasHeight);
		}
		
		return vertices;
	}
}
