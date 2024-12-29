import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class Rock {
	private ArrayList<Float> vertexData;
	private ArrayList<Integer> indexData;
    private int vaoId, vboId, eboId, textureId;
	private Random random;
	private Shader shader;
	public Vector3f position;
	public float rotation;
	
	public Rock(Shader shader, Random random, Vector3f position, float rotation) {
		this.shader = shader;
		this.random = random;
		this.position = position;
		this.rotation = rotation;
	}
	
	public void generateRock(int subdivisions, float radius, float irregularity) {
		// Helper function for adding vertices
		class Vec3 {
			float x, y, z;
	        Vec3(float x, float y, float z) {
	        	this.x = x;
	            this.y = y;
	            this.z = z;
	        }
	        Vec3 normalize() {
	            float length = (float) Math.sqrt(x * x + y * y + z * z);
	            return new Vec3(x / length, y / length, z / length);
	        }
	        Vec3 scale(float scale) {
	            return new Vec3(x * scale, y * scale, z * scale);
	        }
	    }

        // Generate initial icosahedron vertices and indices
		ArrayList<Vec3> vertices = new ArrayList<>();
		ArrayList<int[]> faces = new ArrayList<>();

	    float t = (float) (1.0 + Math.sqrt(5.0)) / 2.0f;
	
	    vertices.add(new Vec3(-1, t, 0).normalize());
	    vertices.add(new Vec3(1, t, 0).normalize());
	    vertices.add(new Vec3(-1, -t, 0).normalize());
	    vertices.add(new Vec3(1, -t, 0).normalize());
	
	    vertices.add(new Vec3(0, -1, t).normalize());
	    vertices.add(new Vec3(0, 1, t).normalize());
	    vertices.add(new Vec3(0, -1, -t).normalize());
	    vertices.add(new Vec3(0, 1, -t).normalize());
	
	    vertices.add(new Vec3(t, 0, -1).normalize());
	    vertices.add(new Vec3(t, 0, 1).normalize());
	    vertices.add(new Vec3(-t, 0, -1).normalize());
	    vertices.add(new Vec3(-t, 0, 1).normalize());
	
	    // Initial icosahedron faces
	    int[][] initialFaces = {
	            {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
	            {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
	            {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
	            {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
	    };
	    for (int[] face : initialFaces) {
	        faces.add(face);
	    }
	
	    // Subdivide the faces to create a more detailed rock
	    for (int i = 0; i < subdivisions; i++) {
	        ArrayList<int[]> newFaces = new ArrayList<>();
	        for (int[] face : faces) {
	            Vec3 v1 = vertices.get(face[0]);
	            Vec3 v2 = vertices.get(face[1]);
	            Vec3 v3 = vertices.get(face[2]);
	
	            Vec3 mid1 = v1.scale(0.5f).normalize();
	            Vec3 mid2 = v2.scale(0.5f).normalize();
	            Vec3 mid3 = v3.scale(0.5f).normalize();
	
	            int midIndex1 = vertices.size();
	            int midIndex2 = midIndex1 + 1;
	            int midIndex3 = midIndex1 + 2;
	
	            vertices.add(mid1);
	            vertices.add(mid2);
	            vertices.add(mid3);
	
	            newFaces.add(new int[]{face[0], midIndex1, midIndex3});
	            newFaces.add(new int[]{face[1], midIndex2, midIndex1});
	            newFaces.add(new int[]{face[2], midIndex3, midIndex2});
	            newFaces.add(new int[]{midIndex1, midIndex2, midIndex3});
	        }
	        faces = newFaces;
	    }
	
	    // Apply irregularity to vertices
	    for (int i = 0; i < vertices.size(); i++) {
	        Vec3 v = vertices.get(i).normalize();
	        float offset = 1.0f + (random.nextFloat() - 0.5f) * irregularity;
	        vertices.set(i, v.scale(radius * offset));
	    }
	
	    // Convert vertices and faces to OpenGL-friendly data
	    for (Vec3 v : vertices) {
	        vertexData.add(v.x);
	        vertexData.add(v.y);
	        vertexData.add(v.z);
	
	        // Approximate normals as normalized position (sphere assumption)
	        Vec3 normal = v.normalize();
	        vertexData.add(normal.x);
	        vertexData.add(normal.y);
	        vertexData.add(normal.z);
	
	        // Texture coordinates (spherical projection)
	        float u = (float) (0.5 + Math.atan2(v.z, v.x) / (2 * Math.PI));
	        // Clamp v.y to the valid range [-1, 1] to avoid NaN
	        float clampedY = Math.max(-1.0f, Math.min(1.0f, v.y));
	        float vTex = (float) (0.5 - Math.asin(clampedY) / Math.PI);
	        vertexData.add(u);
	        vertexData.add(vTex);
	    }
	
	    for (int[] face : faces) {
	        indexData.add(face[0]);
	        indexData.add(face[1]);
	        indexData.add(face[2]);
	    }
	}
	
	public void init() {
	    vertexData = new ArrayList<>();
	    indexData = new ArrayList<>();
	
	    int subdivisions = 5;   // Number of subdivisions for sphere detail
	    float radius = 0.5f;    // Base radius of the rock
	    float irregularity = 0.1f; // Level of height variation
	
	    generateRock(subdivisions, radius, irregularity);
	    
    /*
		        System.out.println("Vertices:");
		        for (int i = 0; i < vertexData.size(); i += 8) {
		            System.out.printf("Position: (%.2f, %.2f, %.2f), Normal: (%.2f, %.2f, %.2f), TexCoord: (%.2f, %.2f)\n",
		                    vertexData.get(i), vertexData.get(i + 1), vertexData.get(i + 2),
		                    vertexData.get(i + 3), vertexData.get(i + 4), vertexData.get(i + 5),
		                    vertexData.get(i + 6), vertexData.get(i + 7));
		        }
	*/
		       /* System.out.println("\nIndices:");
		        for (int i = 0; i < indexData.size(); i += 3) {
		            System.out.printf("Triangle: (%d, %d, %d)\n",
		                    indexData.get(i), indexData.get(i + 1), indexData.get(i + 2));
		        }*/
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
        textureId = Engine.loadTexture("assets/rock.png");

        glUseProgram(shader.shaderProgram);
        glUniform1i(glGetUniformLocation(shader.shaderProgram, "texture1"), 0); // Texture unit 0
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
}