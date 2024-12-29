import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class TexturedCube {
	private Shader shader;
	
    private int vaoId, vboId, eboId, textureId;
    private final ArrayList<Float> vertexData = new ArrayList<>();
    private final ArrayList<Integer> indexData = new ArrayList<>();

    public TexturedCube(Shader shader) {
    	this.shader = shader;
    }
    
    public void init() {
        buildCube();
 
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
        textureId = Engine.loadTexture("assets/texture.png");

        glUseProgram(shader.shaderProgram);
        glUniform1i(glGetUniformLocation(shader.shaderProgram, "texture1"), 0); // Texture unit 0
        
    }
    
    private void buildCube() {
        // Vertices for a cube face: positions, normals, and texture coordinates
        float[][] faceVertices = {
                {-0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f}, // Bottom-left
                { 0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f}, // Bottom-right
                { 0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f}, // Top-right
                {-0.5f,  0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f}  // Top-left
        };

        int[][] faceIndices = {
                {0, 1, 2, 2, 3, 0} // A single face
        };

        // Add faces (back, front, etc.)
        for (int face = 0; face < 6; face++) {
            int vertexOffset = vertexData.size() / 8;

            // Transform face vertices
            for (float[] vertex : faceVertices) {
                vertexData.add(vertex[0]);
                vertexData.add(vertex[1]);
                vertexData.add(vertex[2]);
                vertexData.add(vertex[3]);
                vertexData.add(vertex[4]);
                vertexData.add(vertex[5]);
                vertexData.add(vertex[6]);
                vertexData.add(vertex[7]);
            }

            // Add indices for the face
            for (int[] indices : faceIndices) {
                for (int index : indices) {
                    indexData.add(index + vertexOffset);
                }
            }
        }
    }

    public void render() {
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

    	glBindTexture(GL_TEXTURE_2D, textureId);

        glUseProgram(shader.shaderProgram);
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, indexData.size(), GL_UNSIGNED_INT, NULL);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
        glDeleteProgram(shader.shaderProgram);
    }

 
}

