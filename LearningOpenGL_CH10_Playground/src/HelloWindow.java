import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

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

public class HelloWindow {
	// Window handle
	private long window;
	private Shader shader;
	private Texture texture;
	private Texture texture2;
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
	
	public static void main(String[] args) {
		new HelloWindow().run();
	}
	
	public void run() {
		logger = Logger.getLogger(HelloWindow.class.getName());
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
		
		window = glfwCreateWindow(windowWidth, windowHeight, "LearnOpenGL", NULL, NULL);
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
	
		// Enable vsync
		glfwSwapInterval(0);
		// Make window visible
		glfwShowWindow(window);
	}
	
	private void loop() {
		GL.createCapabilities();
	
		// Set viewport
		glViewport(0, 0, windowWidth, windowHeight);
		
		glClearColor(0.2f, 0.5f, 0.7f, 0.0f);

		// Create shader, texture, camera objects
		shader = new Shader("src/vertexShader1.glsl", "src/fragmentShader1.glsl");
		texture = new Texture("data/wall.jpg");
		texture2 = new Texture("data/download.png");
		
		VAO = glGenVertexArrays();
		VBO = glGenBuffers();
		EBO = glGenBuffers();
		
		glBindVertexArray(VAO);
		
		Vector3f cameraPos = new Vector3f(0.0f, 5.0f, 3.0f);
		Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
		float yaw = 90;
		float pitch = 0;
		
		camera = new Camera(cameraPos, cameraUp, yaw, pitch);
		
		// Set up VBO
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, Block.vertices, GL_STATIC_DRAW);
		
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
		
		world = new WorldGenerator((int)(Math.random() * 1000), 150, 150, 30);
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
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT ); // clear frame buffer
		
		// Only needed if more than 1 texture
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		glActiveTexture(GL_TEXTURE1);
		texture2.bind();
		shader.setInt("texture1", 0);
		shader.setInt("texture2", 1);
		
		glBindVertexArray(VAO);
		
		// Matrix4f model = (new Matrix4f()).rotate((float) glfwGetTime() * (float) Math.toRadians(45.0f), new Vector3f(0.0f, 1.0f, 0.0f));

		Matrix4f view = camera.getViewMatrix();
		Matrix4f perspective = (new Matrix4f()).perspective((float) Math.toRadians(camera.Zoom), aspectRatio, 0.1f, 1000.0f);

		// shader.setMat4("model", model);
		shader.setMat4("view", view);
		shader.setMat4("perspective", perspective);
		shader.setVec3("globalLightDir", new Vector3f(0.8f, -1.0f, 0.3f));
		shader.setVec3("cameraDir", camera.Front);
		shader.setVec3("cameraPos", camera.Position);
		
		// Create a bunch of cubes
		glActiveTexture(GL_TEXTURE0);
		
		for (int x = 0; x < world.positions.length; x++) {
			for (int y = 0; y < world.positions[x].length; y++) {
				for (int z = 0; z < world.positions[x][y].length; z++) {
					if (world.positions[x][y][z].type != Block.BlockType.AIR
							&& world.blockAdjacentToAir(x, y, z)) {
						world.positions[x][y][z].setTexture();
						Matrix4f model = new Matrix4f();
						model.translate(x, y, z);
						shader.setMat4("model", model);
						glDrawArrays(GL_TRIANGLES, 0, 36);
					}
				}
			}
		}
	
		// glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}
	
	private void processInput() {
		if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
			camera.processKeyboard(Camera.MovementDirection.FORWARD, deltaTime);
		}
		if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
			camera.processKeyboard(Camera.MovementDirection.BACKWARD, deltaTime);
		}
		if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
			camera.processKeyboard(Camera.MovementDirection.RIGHT, deltaTime);
		}
		if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
			camera.processKeyboard(Camera.MovementDirection.LEFT, deltaTime);
		}
	}
}

