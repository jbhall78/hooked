import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Textured3DShader extends Shader {
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
 	}
	
    // Vertex shader source
    private final String vertexShaderSource = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec2 aTexCoord;
                
                out vec2 TexCoord;

                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;

                void main() {
                    gl_Position = projection * view * model * vec4(aPos, 1.0);
                    TexCoord = aTexCoord;
                }
    """;

    // Fragment shader source
    private final String fragmentShaderSource = """
                #version 330 core
                out vec4 FragColor;
                
                in vec2 TexCoord;

                uniform sampler2D texture1;

                void main() {
                    FragColor = texture(texture1, TexCoord);
                }
    """;
}
