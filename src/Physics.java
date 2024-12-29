import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Physics {
    private int fishCounter = 0;
    private float fishSpeed = 0.25f;
    private int nextFish = 0;
    private int nextFishTicks = 1000;
    private Engine engine;
    
    public Physics(Engine engine) {
    	this.engine = engine;
    }
    
    public void applyInput() {
        // Poll joystick input
        for (int jid = GLFW.GLFW_JOYSTICK_1; jid <= GLFW.GLFW_JOYSTICK_16; jid++) {
        	if (GLFW.glfwJoystickPresent(jid)) {
        		// Get joystick axes (analog sticks)
        		FloatBuffer axes = GLFW.glfwGetJoystickAxes(jid);
        		if (axes != null) {
        			//System.out.println("Joystick " + jid + " axes: ");
        			for (int i = 0; i < axes.limit(); i++) {
        				//System.out.printf("  Axis %d: %.2f%n", i, axes.get(i));
        				int axis = i;
        				switch (axis) {
        				case 0: // first joystick x
        				//case 2: // second joystick x
        					{
        						float x = engine.hook.position.x;
        						
        						// read joystick and apply delta
        						x += axes.get(axis) * 0.85f;
        						
        						float upper_limit = engine.width - (engine.hook.width / 2);
        						float lower_limit = 0.0f - (engine.hook.width / 4);
        								
        						// ensure the hook appears on screen
        						if (x >= upper_limit) {
        							x = upper_limit;
        						} else if (x <= lower_limit) {
        							x = lower_limit;
        						}
        						
        						engine.hook.position.x = x;
        					}	
        					break;
        				case 1: // first joystick y
        				//case 3: // second joystick y
        					{
        						float y = engine.hook.position.y;
        	
        						// read joystick and apply delta
        						y -= axes.get(axis) * 0.85f;
        						
        						float upper_limit = engine.height - (engine.hook.height / 2) - 120.0f;
        						float lower_limit = 0.0f - (engine.hook.height / 4);
        						
        						// ensure the hook appears on screen
        						if (y >= upper_limit) {
        							y = upper_limit;
        						} else if (y <= 0.0f + lower_limit) {
        							y = 0.0f + lower_limit;
        						}
        						
        						engine.hook.position.y = y;
        					}
        					break;
        				}
    				}
    			}
    				
    			// Get joystick buttons
    			ByteBuffer buttons = GLFW.glfwGetJoystickButtons(jid);
    			if (buttons != null) {
    				//System.out.println("Joystick " + jid + " buttons: ");
    				for (int i = 0; i < buttons.limit(); i++) {
    					//System.out.printf("  Button %d: %s%n", i, buttons.get(i) == GLFW.GLFW_PRESS ? "Pressed" : "Released");
    				}
    			}
        	}
        }				
    }
    
    private void collisionDetect() {
        restart:
            for (int i = 0; i < engine.fishes.size(); i++) {
            	Fish fish = engine.fishes.get(i);
            	// if the hook is over the fish
            	float x = engine.hook.position.x;
            	float y = engine.hook.position.y;
            	if (x >= fish.position.x && x <= fish.position.x + 20.0f &&
            			y <= fish.position.y && y >= fish.position.y - 16.0f) {
            		engine.fishes.remove(i);
            		engine.score += 100;
            		if (engine.score > engine.highScore) {
            			engine.highScore = engine.score;
            			engine.scores.updateHighScore(engine.highScore);
            		}
            		fishSpeed += 0.008f;
            		nextFishTicks -= 5;
            		break restart;
            	}
            	// if the fish went off the screen
     			if (fish.position.x <= -128) { // 128 width fish
     				engine.fishes.remove(i);
     				engine.score -= 20;
     				break restart;
     			}
            }    	
    }
    
    public void newFish() {
		Fish fish = new Fish();
		fish.position = new Vector3f();
		fish.position.x = engine.width;

		Vector3f box1Min = new Vector3f();
		Vector3f box1Max = new Vector3f();
		Vector3f box2Min = new Vector3f();
		Vector3f box2Max = new Vector3f();

		boolean intersects;
		do {
			double min = 100.0; // Minimum value
			double max = engine.height - 200.0; // Maximum value
	
			// Generate a random double between min and max
			double randomValue = min + (engine.random.nextDouble() * (max - min));
			fish.position.y = (float)randomValue;
					
			intersects = false;
			
			box1Min.x = fish.position.x;
			box1Min.y = fish.position.y;
			box1Max.x = fish.position.x + engine.fishes.fishWidth;
			box1Max.y = fish.position.y + engine.fishes.fishHeight;
			
			for (int i = 0; i < engine.fishes.size(); i++) {
				Fish f = engine.fishes.get(i);
				box2Min.x = f.position.x;
				box2Min.y = f.position.y;
				box2Max.x = f.position.x + engine.fishes.fishWidth;
				box2Max.y = f.position.y + engine.fishes.fishHeight;
				
				if (RectangleRectangleIntersection.intersects(box1Min, box1Max, box2Min, box2Max)) {
					intersects = true;
					break;
				}
			}
		} while (intersects);
		
		engine.fishes.add(fish);
		//System.out.println("Adding Fish: " + randomValue);    	
    }
    
    public void runFrame() {
    	applyInput();
    	
    	collisionDetect();

 		// apply velocities
    	for (int i = 0; i < engine.fishes.size(); i++) {
    		Fish fish = engine.fishes.get(i);
    		fish.position.x -= fishSpeed;
    	}
 		
    	// add a fish if enough time has passed
 		if (fishCounter++ == nextFish) {
 			newFish();
 			fishCounter = 0;
 			nextFish = engine.random.nextInt((nextFishTicks > 2) ? nextFishTicks : 2);
 		}
    }
}
