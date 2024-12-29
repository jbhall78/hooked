import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class Hook {
    private int hookTextureID;
    private int hookVAO;
    
	public Vector3f position;
	
	Shader texturedShader;
	Shader solidShader;
	Engine engine;
	
	public float width = 64;
	public float height = 64;

	public void init(Engine engine, Shader texturedShader, Shader solidShader) {
		hookVAO = Engine.createQuadVAO(0, 0, width, height); // Bottom-left corner

		hookTextureID = Engine.loadTexture("assets\\hook-transparent.png");
		
		this.engine = engine;
		this.texturedShader = texturedShader;
		this.solidShader = solidShader;
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
    public void render() {
    	int width = engine.width;
    	int height = engine.height;
    	
    	Matrix4f m = new Matrix4f().identity().translate((2.0f/width)*position.x, (2.0f/height)*position.y, 0);
    	
    	// Render first image (64x64 at bottom-left)
    	Engine.setMatrix(texturedShader.shaderProgram, texturedShader.modelLocation, m);
    	glBindTexture(GL_TEXTURE_2D, hookTextureID);
    	glBindVertexArray(hookVAO);
    	glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    	
    	glDisable(GL_TEXTURE_2D);

    	Engine.setColor(solidShader.shaderProgram, solidShader.colorLocation, new Vector3f(0.0f, 0.0f, 0.0f), 1.0f);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            throw new RuntimeException("OpenGL Error here: " + error + " color_loc: " + solidShader.colorLocation);
        }


    	// Draw line using immediate mode
        glBegin(GL_QUADS);
        	float x_offset = 23.0f;
        	float y_offset = -15.0f;
        	float line_width = 6.0f;
        	float x = position.x;
        	float y = position.y;
        	glVertex2f(x_offset + x, y - y_offset); // Start point
        	glVertex2f(x_offset + x + line_width, y - y_offset);
        	glVertex2f(x_offset + x + line_width, (float)height);
        	glVertex2f(x_offset + x, (float)height);
        glEnd();

    }
}
