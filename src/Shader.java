import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
	public int shaderProgram;
    public int modelLocation, projectionLocation, viewLocation;
	public int colorLocation;

    static int createShader(int type, String source) {
	   int shaderId = glCreateShader(type);
       glShaderSource(shaderId, source);
       glCompileShader(shaderId);
       if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
    	   throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shaderId));
       }
       return shaderId;
   }
}
