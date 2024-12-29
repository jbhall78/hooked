
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class SeaFloor {
    private int vaoId, vboId, eboId, textureId;
    private Shader shader;
	private ArrayList<Float> vertexData;
	private ArrayList<Integer> indexData;
	private Engine engine;
    private float seaFloorHeight = -10.0f;

    public SeaFloor(Engine engine, Shader shader) {
    	this.engine = engine;
    	this.shader = shader;
    }

	private void generate(ArrayList<Float> vertexData, ArrayList<Integer> indexData, int gridSize, float cellSize, float heightScale) {
        // Create the vertices
        for (int z = -gridSize; z <= gridSize; z++) {
            for (int x = -gridSize; x <= gridSize; x++) {
                float height = engine.random.nextFloat() * 5.0f; // Random height for the vertex
                vertexData.add(x * cellSize);  // Position X
                vertexData.add(height);       // Position Y (height)
                vertexData.add(z * cellSize); // Position Z

                vertexData.add(0.0f);         // Normal X (placeholder, computed later)
                vertexData.add(engine.random.nextFloat() * 1.0f);         // Normal Y (upward for simplicity)
                vertexData.add(0.0f);         // Normal Z

                vertexData.add((float) x / (gridSize / 8)); // Texture U
                vertexData.add((float) z / (gridSize / 8)); // Texture V
            }
        }

        // Create the indices for the grid
        for (int z = 0; z < gridSize; z++) {
            for (int x = 0; x < gridSize; x++) {
                int topLeft = z * (gridSize + 1) + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * (gridSize + 1) + x;
                int bottomRight = bottomLeft + 1;

                // First triangle
                indexData.add(topLeft);
                indexData.add(bottomLeft);
                indexData.add(topRight);

                // Second triangle
                indexData.add(topRight);
                indexData.add(bottomLeft);
                indexData.add(bottomRight);
            }
        }
    }
	
	public void init() {
		vertexData = new ArrayList<>();
		indexData = new ArrayList<>();

		int gridSize = 120;    // Number of grid cells along one side
		float cellSize = 1.0f; // Size of each grid cell
		float heightScale = 3.0f; // Maximum height variation

		generate(vertexData, indexData, gridSize, cellSize, heightScale);
		
        // Convert ArrayLists to buffers
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.size());
        for (Float f : vertexData) vertexBuffer.put(f);
        vertexBuffer.flip();

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexData.size());
        for (Integer i : indexData) indexBuffer.put(i);
        indexBuffer.flip();
    
        
        // Set up VAO, VBO, and EBO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Define vertex attributes
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Load texture
        textureId = Engine.loadTexture("assets/seafloor.png");

        glUseProgram(shader.shaderProgram);
        glUniform1i(glGetUniformLocation(shader.shaderProgram, "texture1"), 0); // Texture unit 0
	}
	
	public void render() {
		Matrix4f m = new Matrix4f();
		Vector3f v = new Vector3f(0.0f, seaFloorHeight, 100.0f);
		m.identity();
		m.translate(v.x, v.y, v.z);
	      	
		Engine.setMatrix(shader.shaderProgram, shader.modelLocation, m);

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);
		
		glBindTexture(GL_TEXTURE_2D, textureId);
		
		glUseProgram(shader.shaderProgram);
		glBindVertexArray(vaoId);
		glDrawElements(GL_TRIANGLES, indexData.size(), GL_UNSIGNED_INT, NULL);
		glBindVertexArray(0);
	}
	
    //public static void main(String[] args) {
    /*
 */
/*
        System.out.println("Vertices:");
        for (int i = 0; i < vertexData.size(); i += 8) {
            System.out.printf("Position: (%.2f, %.2f, %.2f), Normal: (%.2f, %.2f, %.2f), TexCoord: (%.2f, %.2f)\n",
                    vertexData.get(i), vertexData.get(i + 1), vertexData.get(i + 2),
                    vertexData.get(i + 3), vertexData.get(i + 4), vertexData.get(i + 5),
                    vertexData.get(i + 6), vertexData.get(i + 7));
        }

        System.out.println("\nIndices:");
        for (int i = 0; i < indexData.size(); i += 3) {
            System.out.printf("Triangle: (%d, %d, %d)\n",
                    indexData.get(i), indexData.get(i + 1), indexData.get(i + 2));
        }
    }*/
}
