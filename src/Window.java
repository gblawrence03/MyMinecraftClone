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
	private WorldGenerator world;
	
	private int VAO;
	private int VBO;
	private int EBO;
	private float lerp;
	
	private Logger logger;
	
	private int windowHeight;
	private int windowWidth;
	private float aspectRatio;
	
	private float lastX;
	private float lastY;
	private boolean firstMouse = true;
	
	private float deltaTime;
	
	private final int FLOAT_BYTES = Float.SIZE / 8;
	
	private int[] indices = {
			0, 1, 3,
			1, 2, 3,
			0, 1, 4,
			1, 4, 5,
			4, 3, 0
	};
	
	private ArrayList<Float> blockVertices = new ArrayList<Float>();
	private FloatBuffer verticesBuffer;
	
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
		
		// Create window
		windowWidth = 1280;
		windowHeight = 720;
		aspectRatio = (float) windowWidth / (float) windowHeight;
		logger.info("Ratio: " + aspectRatio);
		
		window = glfwCreateWindow(windowWidth, windowHeight, "MyMinecraftClone", NULL, NULL);
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
				glfwSetWindowShouldClose(window, true); // detected in rendering loop
			
			if (key == GLFW_KEY_UP && action == GLFW_RELEASE) {
				lerp += 0.1;
				if (lerp > 1.0)
					lerp = 1.0f;
				shader.setFloat("lerp", lerp);
			}
			
			if (key == GLFW_KEY_DOWN && action == GLFW_RELEASE) {
				lerp -= 0.1;
				if (lerp < 0.0)
					lerp = 0.0f;
				shader.setFloat("lerp", lerp);
			}
		});
		
		glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
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
		});
		
		// Set up window resize callback
		// Pass a lambda function called when window resized
		glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
			glViewport(0, 0, width, height);
			windowWidth = width;
			windowHeight = height;
			aspectRatio = (float) windowWidth / (float) windowHeight;
		});
		
		// Center the window
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			// Get the window size
			glfwGetWindowSize(window, pWidth, pHeight);
			
			// Get primary monitor resolution
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		}
		
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
		shader = new Shader("src/blockVertex.glsl", "src/blockFragment.glsl");
		
		VAO = glGenVertexArrays();
		VBO = glGenBuffers();
		EBO = glGenBuffers();
		
		glBindVertexArray(VAO);
		
		String worldSeedString = "DSBHJKAAS";
		int worldSeed = worldSeedString.hashCode();
		logger.info("Generating world. Seed for the world generator: \"" + worldSeedString + "\" -> " + worldSeed);

		world = new WorldGenerator(worldSeed, 600, 600, 50);
		generateBlockVertices();
		FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(blockVertices.size());
		
		for (int i = 0; i < blockVertices.size(); i++) {
			verticesBuffer.put(blockVertices.get(i));
		}
		
		verticesBuffer.flip();
		
	
		Vector3f cameraPos = new Vector3f(0.0f, 5.0f, 0.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		float yaw = 90;
		float pitch = 0;
		
		camera = new Camera(cameraPos, cameraUp, yaw, pitch);
		
		// Set up VBO
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		
		// Set up EBO
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		// Set up VAO
		// Position
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * FLOAT_BYTES, 0);
		glEnableVertexAttribArray(0);
		// Normal
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * FLOAT_BYTES, (3 * FLOAT_BYTES));
		glEnableVertexAttribArray(1);
		// Texture
		glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * FLOAT_BYTES, (6 * FLOAT_BYTES));
		glEnableVertexAttribArray(2);
		
		// important!
		glEnable(GL_DEPTH_TEST);
		
		shader.use();
		
		// Set to wireframe
		// glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		// Run rendering loop until closed or escaped
		
		long currentTime;
		long oldTime = System.currentTimeMillis();
		long lastFrame = oldTime;
		long frames = 0;
		deltaTime = 0.0f;
		
		// world = new WorldGenerator(100, 100, 100, 10);
		
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
	
	private void generateBlockVertices() {
		for (int x = 0; x < world.length; x++) {
			for (int y = 0; y < world.height; y++) {
				for (int z = 0; z < world.width; z++) {
					if (world.positions[x][y][z].type != Block.BlockType.AIR
						&& world.blockAdjacentToAir(x, y, z)) {
						
						ArrayList<Float> vertices = world.positions[x][y][z].getVertexArray(
									x - (int) world.length / 2, 
									y - (int) world.height / 2, 
									z - (int) world.width / 2);
							
						blockVertices.addAll(vertices);
							
					}
				}
			}
		}
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ); // clear frame buffer
		
		glBindVertexArray(VAO);
		
		Matrix4f view = camera.getViewMatrix();
		Matrix4f perspective = (new Matrix4f()).perspective((float) Math.toRadians(camera.Zoom), aspectRatio, 0.1f, 1000.0f);

		shader.setMat4("view", view);
		shader.setMat4("perspective", perspective);
		shader.setVec3("globalLightDir", new Vector3f(0.7f, -1.0f, 0.5f));
		shader.setVec3("cameraDir", camera.Front);
		shader.setVec3("cameraPos", camera.Position);
		
		// Create a bunch of cubes
		glActiveTexture(GL_TEXTURE0);
		Texture tempBlockTexture = Block.blockAtlas;
		tempBlockTexture.bind();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glDrawArrays(GL_TRIANGLES, 0, blockVertices.size() / 8);
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

