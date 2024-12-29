import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Solid2DShader extends Shader {
	
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
        
        projectionLocation = glGetUniformLocation(shaderProgram, "projection");
        colorLocation = glGetUniformLocation(shaderProgram, "u_color");
	}
	
    // Vertex shader source
    private final String vertexShaderSource = """
   		#version 330 core
   		layout(location = 0) in vec2 position;
   		uniform mat4 projection;

   		void main() {
       	    gl_Position = projection * vec4(position, 0.0, 1.0);
       	}
    """;

    // Fragment shader source
    private final String fragmentShaderSource = """
   		#version 330 core
   		out vec4 color;
   		uniform vec4 u_color;

   		void main() {
   		    //color = vec4(1.0, 1.0, 1.0, 1.0); // White color for testing
   		    color = u_color;
   		}
    """;
}
