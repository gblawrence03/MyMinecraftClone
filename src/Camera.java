import org.joml.*;

import java.lang.Math;

public class Camera {
	public enum MovementDirection {
		FORWARD,
		BACKWARD, 
		LEFT,
		RIGHT
	}
	
	public Vector3f Position;
	public Vector3f Front;
	public Vector3f Up;
	public Vector3f Right;
	public Vector3f WorldUp;
	
	final float YAW = -90.0f;
	final float PITCH = 0.0f;
	final float SPEED = 10.0f;
	final float SENSITIVITY = 0.1f;
	final float ZOOM = 45.0f;
	
	public float Yaw;
	public float Pitch;
	
	public float MovementSpeed;
	public float Sensitivity;
	public float Zoom; 
	
	public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
		Position = position;
		WorldUp = up;
		Yaw = yaw;
		Pitch = pitch;
		MovementSpeed = SPEED;
		Sensitivity = SENSITIVITY;
		Zoom = ZOOM;
		updateCameraVectors();
	}
	
	public Matrix4f getViewMatrix() {
		Vector3f Target = new Vector3f();
		Position.add(Front, Target);
		return (new Matrix4f()).lookAt(Position, Target, WorldUp);		
	}
	
	public void processMouseMovement(float xOffset, float yOffset, boolean clampPitch) {
		xOffset *= Sensitivity;
		yOffset *= Sensitivity;  
		
		Yaw += xOffset;
		Pitch += yOffset;
		
		if (clampPitch) {
			if (Pitch > 89.9f) Pitch = 89.9f;
			if (Pitch < -89.9f) Pitch = -89.9f;
		}
		updateCameraVectors();
	}
		
	private void updateCameraVectors() {
		float x = (float) Math.cos(Math.toRadians(Yaw)) * (float) Math.cos(Math.toRadians(Pitch));
		float y = (float) Math.sin(Math.toRadians(Pitch));
		float z = (float) Math.sin(Math.toRadians(Yaw)) * (float) Math.cos(Math.toRadians(Pitch));	
		Front = new Vector3f(x, y, z);
		Front.normalize();
	}
	
	public void processKeyboard(MovementDirection direction, float deltaTime, float multiplier) {
		float cameraSpeed = SPEED * deltaTime / 1000.0f * multiplier;
		Vector3f cameraVelocity = new Vector3f();
		if (direction == MovementDirection.FORWARD) {
			Front.mul(cameraSpeed, cameraVelocity);
			Position.add(cameraVelocity);
		}
		if (direction == MovementDirection.BACKWARD) {
			Front.mul(cameraSpeed, cameraVelocity);
			Position.sub(cameraVelocity);
		}
		if (direction == MovementDirection.RIGHT) {
			Vector3f cameraRight = new Vector3f();
			Front.cross(WorldUp, cameraRight);
			cameraRight.normalize();
			cameraRight.mul(cameraSpeed, cameraVelocity);
			Position.add(cameraVelocity);
		}
		if (direction == MovementDirection.LEFT) {
			Vector3f cameraRight = new Vector3f();
			Front.cross(WorldUp, cameraRight);
			cameraRight.normalize();
			cameraRight.mul(cameraSpeed, cameraVelocity);
			Position.sub(cameraVelocity);
		}
		
		updateCameraVectors();	
	}
	
	public void processMouseScroll(float yOffset) {
		Zoom -= (float) yOffset;
		if (Zoom < 1.0f) Zoom = 1.0f;
		if (Zoom > 120.0f) Zoom = 120.0f;
	}
}
