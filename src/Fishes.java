import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.LinkedList;

import org.joml.Matrix4f;

public class Fishes {
    private int fishVAO;
	private int fishTextureID;
	public float fishWidth = 128;
	public float fishHeight = 64;
	
	private LinkedList<Fish> fishes;
	
	Engine engine;
	Shader shader;
	
	public Fishes(Engine engine, Shader shader) {
		this.engine = engine;
		this.shader = shader;
	}
	
	public void init() {
        fishTextureID = Engine.loadTexture("assets\\fish-transparent3.png");

		fishVAO = Engine.createQuadVAO(0, 0, fishWidth, fishHeight); // Center of screen

		fishes = new LinkedList<Fish>();
	}
	
	public void render() {
		for (Fish fish : fishes) {
			drawFish(fish.position.x, fish.position.y);
		}
	}
    
	private void drawFish(float x, float y) {
		Matrix4f m = new Matrix4f().identity().translate((2.0f/engine.width)*x, (2.0f/engine.height)*y, 0);
	    	 
		// Render second image (128x64 at center)
		Engine.setMatrix(shader.shaderProgram, shader.modelLocation, m);
	    	    	
		glBindTexture(GL_TEXTURE_2D, fishTextureID);
		glBindVertexArray(fishVAO);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public Fish get(int index) {
		return fishes.get(index);
	}
	
	public int size() {
		return fishes.size();
	}
	
	public void remove(int index) {
		fishes.remove(index);
	}
	
	public void add(Fish fish) {
		fishes.add(fish);
	}
	
}
