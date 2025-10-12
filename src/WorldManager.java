import java.util.HashMap;

record ChunkPos(int cx, int cz) { };

public class WorldManager {
	public HashMap<ChunkPos, Chunk> chunkMap;
	
	public WorldManager(WorldGenerator worldGen) {
		chunkMap = new HashMap<ChunkPos, Chunk>();
		
		for (int cx = 0; cx < 4; cx++) {
			for (int cz = 0; cz < 4; cz++) {
				Chunk chunk = worldGen.GenerateChunk(cx, cz);
				chunkMap.put(new ChunkPos(cx, cz), chunk);
			}
		}
	}
	
	public void RecalculateLight() {
		// Propagate sunlight for all chunks
		for (Chunk chunk : chunkMap.values()) {
			chunk.PropagateSunlight();
		}
		
		// Perform passes through all chunks
		boolean changeMade = true;
		while (changeMade) {
			changeMade = false;
			for (Chunk chunk : chunkMap.values()) {
				changeMade = changeMade || chunk.LightUpdate();
			}
		}
	}
	
	public void UpdateAll() {
		UpdateChunkNeighbours();
		RecalculateLight();
		for (Chunk chunk : chunkMap.values()) {
			chunk.Update();
		}
	}
	
	public void UpdateChunkNeighbours() {
		for (ChunkPos pos : chunkMap.keySet()) {
			Chunk chunk = chunkMap.get(pos);
			ChunkPos westPos = new ChunkPos(pos.cx() - 1, pos.cz());
			ChunkPos eastPos = new ChunkPos(pos.cx() + 1, pos.cz());
			ChunkPos southPos = new ChunkPos(pos.cx(), pos.cz() - 1);
			ChunkPos northPos = new ChunkPos(pos.cx(), pos.cz() + 1);
			
			chunk.SetNeighbour(-1, 0, chunkMap.getOrDefault(westPos, null));
			chunk.SetNeighbour(1, 0, chunkMap.getOrDefault(eastPos, null));
			chunk.SetNeighbour(0, -1, chunkMap.getOrDefault(southPos, null));
			chunk.SetNeighbour(0, 1, chunkMap.getOrDefault(northPos, null));
		}	
	}
}
