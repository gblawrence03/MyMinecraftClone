import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.util.*;
import java.util.logging.*;

import java.nio.*;

public class Texture {
	
	public int ID;
	
	public Texture( String imagePath ) {
		int width, height, channels;
		ByteBuffer imageBuffer;
		
		Logger logger;
		logger = Logger.getLogger(Texture.class.getName());
		logger.setLevel(Level.OFF);
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			
			// Load file to buffer using stbi
			stbi_set_flip_vertically_on_load(true);
			imageBuffer = stbi_load(imagePath, w, h, comp, 4);
			if (imageBuffer == null) {
				throw new RuntimeException("Failed to load a texture file!"
											+ System.lineSeparator() + stbi_failure_reason() 
											+ System.lineSeparator() + imagePath);
			}
			
			width = w.get();
			height = h.get();
			channels = comp.get();
			
			logger.info("Image " + imagePath + " loaded with width " + width + ", height " + height + " and " + channels + " channels");
		}
		
		ID = glGenTextures();
		bind();
		// Set wrap and filter parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
		glGenerateMipmap(GL_TEXTURE_2D);
		
		stbi_image_free(imageBuffer);
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, ID);
	}
	
}
 