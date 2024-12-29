import org.lwjgl.glfw.GLFW;

public class Joystick {
    public void init()
    {
    	// Set up a joystick callback (optional)
    	GLFW.glfwSetJoystickCallback((jid, event) -> {
    		if (event == GLFW.GLFW_CONNECTED) {
    			System.out.println("Joystick connected: " + jid);
    			System.out.println("Name: " + GLFW.glfwGetJoystickName(jid));
    			System.out.println("GUID: " + GLFW.glfwGetJoystickGUID(jid));
    		} else if (event == GLFW.GLFW_DISCONNECTED) {
    			System.out.println("Joystick disconnected: " + jid);
    		}
    	});

    	// Check for connected joysticks
    	for (int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_16; jid++) {
    		if (GLFW.glfwJoystickPresent(jid)) {
    			System.out.println("Joystick " + jid + " is present.");
    			System.out.println("Name: " + GLFW.glfwGetJoystickName(jid));
    			System.out.println("GUID: " + GLFW.glfwGetJoystickGUID(jid));
    		}
    	}
    }
}
