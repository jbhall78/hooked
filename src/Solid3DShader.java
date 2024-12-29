import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;

public class Solid3DShader extends Shader {
	
	public void init() {
	    // Load and compile shaders
        int vertexShader = Shader.createShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = Shader.createShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to link shader program: " + glGetProgramInfoLog(shaderProgram));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        modelLocation = glGetUniformLocation(shaderProgram, "model");
        projectionLocation = glGetUniformLocation(shaderProgram, "projection");
        viewLocation = glGetUniformLocation(shaderProgram, "view");        
        colorLocation = glGetUniformLocation(shaderProgram, "u_color");
	}
	
    // Vertex shader source
    private final String vertexShaderSource = """
    		             #version 330 core
        		layout(location = 0) in vec3 position;

                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;
 
        		void main() {
        			gl_Position = projection * view * model * vec4(position, 1.0);
        		}
    """;

    // Fragment shader source
    private final String fragmentShaderSource = """
    		               #version 330 core
                out vec4 color;
                
        		uniform vec4 u_color;

                void main() {
        			color = u_color;
                }
    """;
}
