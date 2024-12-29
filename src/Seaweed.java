import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Seaweed {
	Engine engine;
	
   	float[] seaweedVertices = {
   			// Bottom point (narrow)
   			0.0f, 0.0f, 0.0f, 0.0f, 0.0f,// Bottom center

   			// Lower section
   			-0.1f, 0.5f, 0.0f, 0.0f, 0.0f,  // Lower middle left
   			0.1f, 0.5f, 0.0f, 0.0f, 0.0f, // Lower middle right

   			// Upper section
   			-0.15f, 1.0f, 0.0f, 0.0f, 0.0f,// Upper middle left
   			0.15f, 1.0f, 0.0f, 0.0f, 0.0f,// Upper middle right

   			// Top point (narrow)
   			0.0f, 1.5f, 0.0f, 0.0f, 0.0f   // Top center
		};
   	
    private int seaweedTextureID;
    public int seaweedVAO;
    
    private ArrayList<Vector3f> seaweedPositions;
    private ArrayList<Float> seaweedRotations;
    private ArrayList<Float> seaweedRotationalVelocities;
    private ArrayList<Float> seaweedMagnitudes;
    
    private Shader shader;
    
    private ArrayList<Float> vertices;
    
    private void addVertex(ArrayList<Float> list, float x, float y, float z, float u, float v) {
    	list.add(x);
    	list.add(y);
    	list.add(z);
    	list.add(u);
    	list.add(v);
    }
    
    public Seaweed(Engine engine, Shader shader) {
    	this.engine = engine;
    	this.shader = shader;
    	seaweedTextureID = Engine.loadTexture("assets/seaweed3.png");

    	for (int i = 0; i < seaweedVertices.length; i += 5) {
    		seaweedVertices[i + 3] = engine.random.nextFloat();
    		seaweedVertices[i + 4] = seaweedVertices[i + 3] + engine.random.nextFloat() * (1.0f - seaweedVertices[i + 3]);
    	}
    	
    	vertices = new ArrayList<Float>();
    	
    	// create a rectangle with 10 segments, with an additional a point in the top center
    	float width = 0.75f;
    	float height = 1.5f;
    	float bottom_width = 0.5f;
    	float increment = 0.1f;
    	int segments = (int)(height / increment);
    	
    	// bottom middle
    	addVertex(vertices, -(width/2) + width/2, -0.5f, 0.0f, 0.5f, 0.0f);
    	
    	// bottom right
    	addVertex(vertices, -(bottom_width/2) + bottom_width, 0.0f, 0.0f, 0.0f, 1.0f);
    	
    	// top right
    	addVertex(vertices, -(width/2) + width, height, 0.0f, 1.0f, 1.0f);
    
    	// top middle
    	addVertex(vertices, -(width/2) + (width/2), height + (increment * 4), 0.0f, 0.5f, 1.0f);
    	
    	// top left
    	addVertex(vertices, -(width/2) + 0.0f, height, 0.0f, 0.0f, 0.0f);

    	// bottom left
    	addVertex(vertices, -(bottom_width/2) + 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    	
    	for (int i = 0; i < vertices.size(); i += 5) {
    		float x = vertices.get(i);
    		float y = vertices.get(i + 1);
    		float z = vertices.get(i + 2);
    		float u = vertices.get(i + 3);
    		float v = vertices.get(i + 4);
    		System.out.printf("v: (%f,%f,%f) (%f,%f)\n", x, y, z, u, v);
    	}
		seaweedPositions = new ArrayList<Vector3f>();
		seaweedRotations = new ArrayList<Float>();
		seaweedRotationalVelocities = new ArrayList<Float>();
		seaweedMagnitudes = new ArrayList<Float>();
		
		for (int i = 0; i < 256; i++) {
	    	float min = -20.0f; // Minimum value
			float max = 20.0f; // Maximum value
			float x = min + (engine.random.nextFloat() * (max - min));

			float y = -5.0f;
			
			min = -20.0f;
			max = 20.0f; // Maximum value
			float z = min + (engine.random.nextFloat() * (max - min));

			Vector3f v = new Vector3f((float)x, (float)y, (float)z);
			
			min = -90.0f;
			max = 90.0f; // Maximum value
			float rot = min + (engine.random.nextFloat() * (max - min));

			float vel, mag;
			do {
				min = 0.01f;
				max = 0.20f; // Maximum value
				vel = min + (engine.random.nextFloat() * (max - min));

				min = 9.0f;
				max = 90.0f; // Maximum value
				mag = min + (engine.random.nextFloat() * (max - min));
			} while (vel > 0.05f && mag < 35.0f);
			
			//System.out.printf("seaweed (rot:%f) (vel:%f) (mag:%f)\n", rot, vel, mag);
			seaweedPositions.add(v);			
			seaweedRotations.add(rot);
			seaweedRotationalVelocities.add(vel);
			seaweedMagnitudes.add(mag);
		}
		
        seaweedVAO = createSeaweedVAO();
        
    }
    
    private int createSeaweedVAO() {
    	/*
    	FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(seaweedVertices.length);
    	vertexBuffer.put(seaweedVertices).flip();
*/
    	
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.size());
        for (Float f : vertices) vertexBuffer.put(f);
        vertexBuffer.flip();

    	// Bind and enable the vertex array
    	int vao = glGenVertexArrays();
    	glBindVertexArray(vao);

    	int vbo = glGenBuffers();
    	glBindBuffer(GL_ARRAY_BUFFER, vbo);
    	glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

    	// Specify the layout of the vertex array
    	glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
    	glEnableVertexAttribArray(0);
    	
        // Enable vertex attribute 1 (texture coordinates)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES); 
        glEnableVertexAttribArray(1);
    	
    	return vao;
    }
    
    public void updateSeaweed() {
        for (int i = 0; i < seaweedPositions.size(); i++) {
        	float ang = seaweedRotations.get(i);
        	float vel = seaweedRotationalVelocities.get(i);
        	float mag = seaweedMagnitudes.get(i);
        	
        	float min = -90.0f + mag;
        	float max = 90.0f - mag;
        	
        	if (ang <= min) {
        		vel = -vel;
        		ang = min;
        	}
        	if (ang >= max) {
        		vel = -vel;
        		ang = max;
        	}
        	ang += vel;
        	seaweedRotations.set(i, ang);
    		seaweedRotationalVelocities.set(i, vel);
       }
    
    }
    
    public void drawSeaweed() {
        Matrix4f m = new Matrix4f();
        m.identity();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glBindTexture(GL_TEXTURE_2D, seaweedTextureID);
        // Set texture wrapping parameters for S (horizontal)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);

        // Set texture wrapping parameters for T (vertical)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	//	if (Y >= 5.0f) {
	//		Y = -5.0f;
	//	} 
	//	Y += 0.01f;
        
        for (int i = 0; i < seaweedPositions.size(); i++) {
        	Vector3f v = seaweedPositions.get(i);
        	float ang = seaweedRotations.get(i);
             	
            m.identity();
        	m.translate(v.x, v.y, v.z);
        	m.rotate((float)Math.toRadians(ang), 0.0f, 0.0f, 1.0f);
           	Engine.setMatrix(shader.shaderProgram, shader.modelLocation, m);
        	glBindVertexArray(seaweedVAO);
        	//glDrawArrays(GL_TRIANGLE_STRIP, 0, seaweedVertices.length / 5);
        	glDrawArrays(GL_POLYGON, 0, vertices.size() / 5);
        }
    }

}
