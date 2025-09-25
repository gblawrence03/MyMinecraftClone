import java.util.HashMap;

public class Block {
	public enum BlockType {
		AIR,
		GRASS,
		STONE,
		WATER
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
	
	public static HashMap<BlockType, Texture> textureMap;
	public static Texture blockAtlas = new Texture("data/blocks.png");
	
	public BlockType type;
	
	public Block(BlockType type) {
		this.type = type;
	}
}
