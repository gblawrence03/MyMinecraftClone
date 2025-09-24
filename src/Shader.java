import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderiv;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.logging.*;

import org.lwjgl.Version;
import org.joml.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Shader {
	public int ID;
	private Logger logger;
	
	public Shader(String vertexPath, String fragmentPath) {
		logger = Logger.getLogger(Shader.class.getName());
		logger.setLevel(Level.INFO);
		
		String vertexCode = "";
		String fragmentCode = "";
		
		try {
			File vertexShaderFile = new File(vertexPath);
			File fragmentShaderFile = new File(fragmentPath);
			Scanner vertexShaderScanner = new Scanner(vertexShaderFile);
			Scanner fragmentShaderScanner = new Scanner(fragmentShaderFile);
			
			while (vertexShaderScanner.hasNextLine()) {
				vertexCode += vertexShaderScanner.nextLine() + '\n';
			}
			vertexShaderScanner.close();
			
			while (fragmentShaderScanner.hasNextLine()) {
				fragmentCode += fragmentShaderScanner.nextLine() + '\n';
			}
			fragmentShaderScanner.close();
			
		} catch (FileNotFoundException e) {
			logger.warning("Shader could not be loaded.");
			e.printStackTrace();
		}
		
		int vertex; 
		int fragment;
		
		vertex = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertex, vertexCode);
		glCompileShader(vertex);
		checkShaderCompilation(vertex, "vertex");
		
		fragment = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragment, fragmentCode);
		glCompileShader(fragment);
		checkShaderCompilation(fragment, "fragment");
		
		// Create and link shader program
		ID = glCreateProgram();
		glAttachShader(ID, vertex);
		glAttachShader(ID, fragment);
		glLinkProgram(ID);
				
		glDeleteShader(vertex);
		glDeleteShader(fragment);
		
		// Check if program linking failed
		checkShaderLinking();

		return;
	}
	
	private void checkShaderCompilation(int shader, String shaderType) {
		int[] success = new int[1];
		glGetShaderiv(shader, GL_COMPILE_STATUS, success);
		
		if (success[0] == 0) {
			String infoLog = glGetShaderInfoLog(shader);
			logger.warning("" + shaderType + " shader compilation failed.\n" + infoLog);
		}		
	}
	
	private void checkShaderLinking() {
		int[] success = new int[1];
		glGetProgramiv(ID, GL_LINK_STATUS, success);
		
		if (success[0] == 0) {
			String infoLog = glGetProgramInfoLog(ID);
			logger.warning("Shader program linking failed.\n" + infoLog);
		}		
	}
	
	public void use() {
		glUseProgram(ID);
	}
	
	void setBool(String name, boolean value) {
		if (value)
			glUniform1i(glGetUniformLocation(ID, name), 1);
		else 
			glUniform1i(glGetUniformLocation(ID, name), 0);
	}
	
	void setInt(String name, int value) {
		glUniform1i(glGetUniformLocation(ID, name), value);
	}
	
	void setFloat(String name, float value) {
		glUniform1f(glGetUniformLocation(ID, name), value);
	}
	
	void setMat4(String name, Matrix4f value) {
		int loc = glGetUniformLocation(ID, name);
		glUniformMatrix4fv(loc, false, value.get(new float[16]));		
	}
	
	void setVec3(String name, float x, float y, float z) {
		int loc = glGetUniformLocation(ID, name);
		glUniform3f(loc, x, y, z);		
	}
	
	void setVec3(String name, Vector3f vec) {
		int loc = glGetUniformLocation(ID, name);
		glUniform3f(loc, vec.get(0), vec.get(1), vec.get(2));		
	}
}