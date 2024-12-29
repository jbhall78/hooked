import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class Window {
	private long window;
	private Engine engine;
    private String title = "Fish in a Barrel";

	public Window(Engine engine) {
		this.engine = engine;
	}
	
	public long getWindow() {
		return window;
	}
	
	// Define the keyCallback method
	private void keyCallback(long window, int key, int scancode, int action, int mods) {
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
	        glfwSetWindowShouldClose(window, true); 
	    }
	 /*   if (key == GLFW_KEY_ENTER && action == GLFW_PRESS && (mods & GLFW_MOD_ALT) != 0) {
	        toggleFullscreen(); 
	    } */
	}
	
    public void open() {
    	System.out.println("Initializing window...");
        glfwInit();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        window = glfwCreateWindow(engine.width, engine.height, title, engine.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, this::keyCallback);
        
        /* glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_ENTER && action == GLFW_PRESS && (mods & GLFW_MOD_ALT) != 0) {
                toggleFullscreen();
            }
        }); */

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(0);  // No vsync
        glfwShowWindow(window);    	
    }
    
    /*
    private void toggleFullscreen() {
        engine.fullscreen = !engine.fullscreen;
        glfwDestroyWindow(window);
        window = glfwCreateWindow(engine.width, engine.height, title, engine.fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwShowWindow(window);
    }
    */

    public void close() {
        glfwDestroyWindow(window);
    }
}
