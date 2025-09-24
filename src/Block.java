import java.util.HashMap;

public class Block {
	public enum BlockType {
		STONE,
		AIR,
		WATER,
		GRASS
	}
	
	public static float[] vertices = {
			// positions 		 // normals 		// texture coords
			-0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
			0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
			0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
			-0.5f, 	0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
			-0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
			0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
			0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
			0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
			-0.5f, 	0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
			-0.5f, 	0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			-0.5f, 	0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			-0.5f, 	-0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			-0.5f, 	-0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			-0.5f, 	0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			0.5f, 	0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
			0.5f, 	0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
			0.5f, 	-0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 	-0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
			0.5f, 	-0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
			0.5f, 	0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
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
	
	public static HashMap<BlockType, Texture> textureMap;
	
	static {
		textureMap = new HashMap<>();
		textureMap.put(BlockType.STONE, new Texture("data/stone.jpg"));
		textureMap.put(BlockType.WATER, new Texture("data/water.jpg"));
		textureMap.put(BlockType.GRASS, new Texture("data/grass.jpg"));
	}
	
	public BlockType type;
	
	public Block(BlockType type) {
		this.type = type;
	}
	
	public void setTexture() {
		textureMap.get(type).bind();
	}
}
