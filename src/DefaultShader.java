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

public class DefaultShader extends Shader {
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
        layout(location = 0) in vec3 aPos;
        layout(location = 1) in vec3 aNormal;
        layout(location = 2) in vec2 aTexCoord;

        out vec3 FragPos;
        out vec3 Normal;
        out vec2 TexCoord;

        uniform mat4 model;
        uniform mat4 view;
        uniform mat4 projection;

        void main() {
            FragPos = vec3(model * vec4(aPos, 1.0));
            Normal = mat3(transpose(inverse(model))) * aNormal;  
            TexCoord = aTexCoord;

            gl_Position = projection * view * vec4(FragPos, 1.0);
        }
    """;

    // Fragment shader source
    private final String fragmentShaderSource = """
        #version 330 core
        out vec4 FragColor;

        in vec3 FragPos;
        in vec3 Normal;
        in vec2 TexCoord;

        uniform sampler2D texture1;

        void main() {
            vec3 lightColor = vec3(1.0);
            vec3 lightPos = vec3(1.2f, 1.0f, 2.0f);
            vec3 viewPos = vec3(0.0, 0.0, 3.0);

            // Ambient
            float ambientStrength = 0.1;
            vec3 ambient = ambientStrength * lightColor;

            // Diffuse
            vec3 norm = normalize(Normal);
            vec3 lightDir = normalize(lightPos - FragPos);
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * lightColor;

            // Specular
            float specularStrength = 0.5;
            vec3 viewDir = normalize(viewPos - FragPos);
            vec3 reflectDir = reflect(-lightDir, norm);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
            vec3 specular = specularStrength * spec * lightColor;

            vec3 result = (ambient + diffuse + specular) * texture(texture1, TexCoord).rgb;
            FragColor = vec4(result, 1.0);
            //FragColor = vec4(0.2, 0.2, 0.2, 1.0);
        }
    """;
}
