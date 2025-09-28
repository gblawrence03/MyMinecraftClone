import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.lwjgl.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.*;
import java.util.Scanner;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import org.joml.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * My window class to do stuff yk how it is 
 * @author George Lawrence
 *
 */

public class Window {
	// Window handle
	private long window;
	private Shader shader;
	private Camera camera;
	private Matrix4f perspective;
	
	private WorldGenerator world;

	private Map<Block.BlockType, Integer> blockVBOs = new EnumMap<>(Block.BlockType.class);
	private Map<Block.BlockType, Integer> blockVAOs = new EnumMap<>(Block.BlockType.class);
	private Map<Block.BlockType, Integer> instanceVBOs = new EnumMap<>(Block.BlockType.class);
	private Map<Block.BlockType, List<Integer>> instancePositionsMap;
	
	private Logger logger;
	
	private int windowHeight;
	private int windowWidth;
	private int screenHeight;
	private int screenWidth;
	private int windowedHeight;
	private int windowedWidth;
	private float aspectRatio;
	
	private float lastX;
	private float lastY;
	private boolean firstMouse = true;
	
	private float deltaTime;
	
	private final int FLOAT_BYTES = Float.SIZE / 8;

	private GLFWVidMode vidmode;
	
	public static void main(String[] args) {
		new Window().run();
	}
	
	public void run() {
		logger = Logger.getLogger(Window.class.getName());
		logger.setLevel(Level.INFO);
		logger.info("LWJGL version: " + Version.getVersion());
		
		init();
		loop();
		
		// Free callback, destroy window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		// Terminate GLFW
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void centerWindow() {
		// Center the window
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			// Get the window size
			glfwGetWindowSize(window, pWidth, pHeight);
			
			// Get primary monitor resolution
			// GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		}
	}
	
	private void init() {
		// Error callback prints error messages in System.err
		GLFWErrorCallback.createPrint(System.err).set();
		
		// Initialise GLFW
		if ( !glfwInit() ) throw new IllegalStateException("Unable to initialise GLFW");
		
		// Configure GLFW
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // window stays hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // window will be resizable
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Setting glfw version
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		
		// Get primary monitor resolution
		vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		
		// Create window
		windowWidth = 1280;
		windowHeight = 720;
		screenWidth = vidmode.width();
		screenHeight = vidmode.height();
		
		aspectRatio = (float) windowWidth / (float) windowHeight;
		logger.info("Ratio: " + aspectRatio);
		
		window = glfwCreateWindow(windowWidth, windowHeight, "MyMinecraftClone", NULL, NULL);
		centerWindow();
		if (window == NULL ) throw new RuntimeException("Failed to create the GLFW window");
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		
		lastX = windowWidth / 2;
		lastY = windowHeight / 2;
	
		// Set up key callback
		// We pass a lambda function which is called when key pressed
		// An alternative method to this would be to call glfwGetKey()
		// in the render loop and check if escape has been pressed
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			
			if (key == GLFW_KEY_F11 && action == GLFW_RELEASE) {
				// Set to windowed
				if (glfwGetWindowMonitor(window) != NULL) {
					glfwSetWindowMonitor(window, NULL, 100, 100, windowedWidth, windowedHeight, vidmode.refreshRate());
					centerWindow();
					lastX = 0;
					lastY = 0;
					glfwSetCursorPos(window, 0, 0);
				// Set to fullscreen
				} else {
					windowedWidth = windowWidth;
					windowedHeight = windowHeight;
					glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), 100, 100, screenWidth, screenHeight, vidmode.refreshRate());
					lastX = 0;
					lastY = 0;
					glfwSetCursorPos(window, 0, 0);
				}
			}
		});
		
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if (button == GLFW_MOUSE_BUTTON_LEFT && glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_NORMAL) {
				// We want to reset the cursor pos and LastX and LastY
				// This is so that the position doesn't "jump" when reentering the window
				glfwSetCursorPos(window, screenWidth/2, screenHeight/2);
				lastX = screenWidth/2;
				lastY = screenHeight/2;
				glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			}
		});
		
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
			// We don't want to process mouse movement if the window is unfocused
			if (glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_NORMAL) return;
			if (firstMouse) {
				lastX = (float) xpos;
				lastY = (float) ypos;
				firstMouse = false;
			}
			float xOffset = (float) xpos - lastX;
			float yOffset = lastY - (float) ypos;
			
			lastX = (float) xpos;
			lastY = (float) ypos;			
			
			camera.processMouseMovement(xOffset, yOffset, true);
		});
		
		glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
			camera.processMouseScroll((float) yoffset);
			perspective = (new Matrix4f()).perspective((float) Math.toRadians(camera.Zoom), aspectRatio, 0.1f, 1000.0f);
		});
		
		// Set up window resize callback
		// Pass a lambda function called when window resized
		glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
			glViewport(0, 0, width, height);
			windowWidth = width;
			windowHeight = height;
			aspectRatio = (float) windowWidth / (float) windowHeight;
		});
		
		// Make OpenGL context current
		glfwMakeContextCurrent(window);
	
		// Disable vsync
		glfwSwapInterval(0);
		// Make window visible
		glfwShowWindow(window);
	}
	
	private void loop() {
		GL.createCapabilities();
	
		// Set viewport
		glViewport(0, 0, windowWidth, windowHeight);
		
		glClearColor(0.6f, 0.7f, 0.85f, 0.0f);
		
		// Create shader, texture, camera objects
		Vector3f cameraPos = new Vector3f(0.0f, 5.0f, 0.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		float yaw = 90;
		float pitch = 0;
		
		camera = new Camera(cameraPos, cameraUp, yaw, pitch);
		perspective = (new Matrix4f()).perspective((float) Math.toRadians(camera.Zoom), aspectRatio, 0.1f, 1000.0f);

		shader = new Shader("src/blockVertex.glsl", "src/blockFragment.glsl");
		shader.use();
		
		// World generation
		
		String worldSeedString = "fsdfsdf";
		int worldSeed = worldSeedString.hashCode();
		logger.info("Generating world. Seed for the world generator: \"" + worldSeedString + "\" -> " + worldSeed);
		
		long startTime = System.currentTimeMillis();
		world = new WorldGenerator(worldSeed, 1000, 1000, 50);
		long endTime = System.currentTimeMillis();
		
		logger.info("World generation took " + (endTime - startTime) / 1000f + " seconds.");
		
		// Map each block type to a list of block positions
		instancePositionsMap = new EnumMap<>(Block.BlockType.class);
		for (Block.BlockType type : Block.BlockType.values()) {
			instancePositionsMap.put(type, new ArrayList<Integer>());
		}
		
		startTime = System.currentTimeMillis();
		generateInstanceVertices();
		endTime = System.currentTimeMillis();
		logger.info("Generating instance position maps took " + (endTime - startTime) / 1000f + " seconds.");
		
		// VAOs and VBOs for each block type
		startTime = System.currentTimeMillis();
		for (Block.BlockType type : Block.BlockType.values()) {
			int vao = glGenVertexArrays();
			glBindVertexArray(vao);
			
			float[] typeVertices = Block.precomputeVertexesForType(type);
			
			int vbo = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, typeVertices, GL_STATIC_DRAW);
			
			// Positions
			int stride = 8 * FLOAT_BYTES;
			glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
			glEnableVertexAttribArray(0);
			// Normals
			glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * FLOAT_BYTES);
			glEnableVertexAttribArray(1);
			// TexCoords
			glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * FLOAT_BYTES);
			glEnableVertexAttribArray(2);
			
			// Instance Buffer
			List<Integer> instancePositions = instancePositionsMap.get(type);
			IntBuffer instanceBuffer = BufferUtils.createIntBuffer(instancePositions.size());
			for (int val: instancePositions) instanceBuffer.put(val);
			instanceBuffer.flip();
			
			int instanceVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
			glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_STATIC_DRAW);
			glVertexAttribIPointer(3, 3, GL_INT, 3 * Integer.BYTES, 0);
			glEnableVertexAttribArray(3);
			glVertexAttribDivisor(3, 1);
			
			glBindVertexArray(0);
			
			blockVBOs.put(type, vbo);
			blockVAOs.put(type, vao);
			instanceVBOs.put(type, instanceVBO);
		}
		endTime = System.currentTimeMillis();
		logger.info("Setting up VBOs and VAOs took " + (endTime - startTime) / 1000f + " seconds.");
		
		// important!
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// Activate the atlas texture
		glActiveTexture(GL_TEXTURE0);
		Block.blockAtlas.bind();
		
		// Set to wireframe
		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		// Run rendering loop until closed or escaped
		
		long currentTime;
		long oldTime = System.currentTimeMillis();
		long lastFrame = oldTime;
		long frames = 0;
		deltaTime = 0.0f;
		
		while ( !glfwWindowShouldClose(window) ) {	
			glfwSwapBuffers(window); // swap colour buffers
			processInput();			
			render();
			// poll for window events, invokes key callback and resize callback
			glfwPollEvents();
			
			// FPS and deltatime
			frames++;
			currentTime = System.currentTimeMillis();
			deltaTime = currentTime - lastFrame;
			if (currentTime - oldTime >= 1000) {
				System.out.println("FPS: " + frames);
				frames = 0;
				oldTime = currentTime;
			}
			lastFrame = currentTime;
		}
		logger.info("Window closed");
	}
	
	/*
	 * Fills the instance maps with the positions of each block of the same type
	 */
	private void generateInstanceVertices() {
		for (int x = 0; x < world.length; x++) {
			for (int y = 0; y < world.height; y++) {
				for (int z = 0; z < world.width; z++) {
					Block block = world.positions[x][y][z];
					if (block.type != Block.BlockType.AIR
						&& world.blockVisible(x, y, z)) {
						
						int offsetX = x - (int) world.length / 2;
						int offsetY = y - (int) world.height / 2;
						int offsetZ = z - (int) world.width / 2;
						
						instancePositionsMap.get(block.type).add(offsetX);
						instancePositionsMap.get(block.type).add(offsetY);
						instancePositionsMap.get(block.type).add(offsetZ);
					}
				}
			}
		}
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ); // clear frame buffer
		
		Matrix4f view = camera.getViewMatrix();

		shader.setMat4("view", view);
		shader.setMat4("perspective", perspective);
		shader.setVec3("globalLightDir", new Vector3f(0.7f, -1.0f, 0.5f));

		/*
		 * Instanced rendering, with one draw call per block type
		 */
		for (Block.BlockType type : Block.BlockType.values()) {
			int vao = blockVAOs.get(type);
			glBindVertexArray(vao);
			int numBlocks = instancePositionsMap.get(type).size() / 3;
			glDrawArraysInstanced(GL_TRIANGLES, 0, 36, numBlocks);
		}
	}
	
	private void processInput() {
		float multiplier = 1.0f;
		
		// Slow movement
		if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
			multiplier = 0.5f;
		}
		
		// Fast movement
		if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
			multiplier = 3f;
		}
		
		Boolean wPressed = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
		Boolean sPressed = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
		Boolean dPressed = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;
		Boolean aPressed = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
		
		// Slow down diagonal movement
		if ((wPressed || sPressed) && (dPressed || aPressed)) {
			multiplier *= 0.71f;
		}
		
		if (wPressed) {
			camera.processKeyboard(Camera.MovementDirection.FORWARD, deltaTime, multiplier);
		}
		if (sPressed) {
			camera.processKeyboard(Camera.MovementDirection.BACKWARD, deltaTime, multiplier);
		}
		if (dPressed) {
			camera.processKeyboard(Camera.MovementDirection.RIGHT, deltaTime, multiplier);
		}
		if (aPressed) {
			camera.processKeyboard(Camera.MovementDirection.LEFT, deltaTime, multiplier);
		}
	}
}

