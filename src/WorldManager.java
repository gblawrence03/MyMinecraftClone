import java.util.ArrayList;
import java.util.HashMap;

record ChunkPos(int cx, int cz) { };

public class WorldManager {
	public HashMap<ChunkPos, Chunk> chunkMap;
	public boolean LightChangeMade = true;
	public int LightSteps = 0;
	public WorldGenerator worldGen;
	
	private boolean LIGHT_DEBUG = false;
	
	private int chunkRenderDistance = 10;
	
	public WorldManager(WorldGenerator worldGen) {
		this.worldGen = worldGen;
		chunkMap = new HashMap<ChunkPos, Chunk>();
		
		GenerateChunks(0, 0);
	}
	
	// Generate chunks centred around a specified position
	public void GenerateChunks(int cx, int cz) {
		int r = chunkRenderDistance;
		
		ArrayList<ChunkPos> required = new ArrayList<ChunkPos>();
		
		// Determine which chunks need to exist
		for (int x = cx - r; x <= cx + r; x++) {
			for (int z = cz - r; z <= cz + r; z++) {
				if ((x - cx)*(x - cx) + (z - cz)*(z - cz) <= r*r) {
					required.add(new ChunkPos(x, z));
				}
			}
		}
		
		ArrayList<Chunk> chunksToUpdate = new ArrayList<Chunk>();
		ArrayList<Chunk> newChunks = new ArrayList<Chunk>();
		// Add the chunks to the chunkmap if they don't exist
		for (ChunkPos chunkPos : required) {
			if (!chunkMap.containsKey(chunkPos)) {
				Chunk chunk = worldGen.GenerateChunk(chunkPos.cx(), chunkPos.cz());
				chunkMap.put(chunkPos, chunk);
				chunksToUpdate.add(chunk);
				newChunks.add(chunk);
			} 
		}
		
		// Remove chunks that don't need to be loaded
		chunkMap.keySet().removeIf(pos -> !required.contains(pos));
		
		// Update chunk neighbours
		UpdateChunkNeighbours();
		
		// Light updates
		
		for (Chunk newChunk : newChunks) {
			// Add the new chunk's neighbours
			for (Chunk neighbour : newChunk.neighbourMap.values()) {
				if (neighbour != null) chunksToUpdate.add(neighbour);
			}
		}
		
		RecalculateLightFor(chunksToUpdate);
		
		// Generate meshes
		for (Chunk chunk : chunksToUpdate) {
			chunk.Update();
		}
	}
	
	// For light debugging only!
	public void SingleLightStep() {
		if (LIGHT_DEBUG) {
			if (LightChangeMade) {
				LightSteps++;
				LightChangeMade = false;
				for (Chunk chunk : chunkMap.values()) {
					LightChangeMade = chunk.LightUpdate() || LightChangeMade;
				}
				System.out.println(LightSteps);
				
				for (Chunk chunk : chunkMap.values()) {
					chunk.Update();
				}
			}
		}		
	}
	
	public void RecalculateLightFor(ArrayList<Chunk> chunks) {
		for (Chunk chunk : chunks) {
			chunk.PropagateSunlight();
		}
		
		boolean changeMade = true;
		while (changeMade) {
			changeMade = false;
			for (Chunk chunk : chunks) {
				// This !!!MUST!!! be in this order!!!!!!!!!
				// Otherwise lazy evaluation causes the lightupdate to not happen!!!!!!!!!!!!
				changeMade = chunk.LightUpdate() || changeMade;
			}
		}
	}
	
	// Recalculate light for all chunks
	public void RecalculateLight() {
		// Propagate sunlight for all chunks
		for (Chunk chunk : chunkMap.values()) {
			chunk.PropagateSunlight();
		}
		
		
		// Perform passes through all chunks
		if (!LIGHT_DEBUG) {
			boolean changeMade = true;
			int i = 0;
			while (changeMade) {
				i++;
				changeMade = false;
				for (Chunk chunk : chunkMap.values()) {
					// This !!!MUST!!! be in this order!!!!!!!!!
					// Otherwise lazy evaluation causes the lightupdate to not happen!!!!!!!!!!!!
					changeMade = chunk.LightUpdate() || changeMade;
				}
				// System.out.println(i);
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
	
	public void UpdateChunkNeighbours(ChunkPos pos) {
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
	
	// Update all generated chunk
	public void UpdateChunkNeighbours() {
		for (ChunkPos pos : chunkMap.keySet()) {
			UpdateChunkNeighbours(pos);
		}	
	}
}
