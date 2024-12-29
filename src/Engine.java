import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import java.util.LinkedList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Math;

public class Engine {
    public Random random;
    
    // screen attributes
	public int width = 576;
    public int height = 840;//1024;
    public boolean fullscreen = false;
    
    private float fov = 70.0f;
    private float aspectRatio = (float)width / height;  // Aspect ratio of the window
    private float near = 0.1f;
    //private float far = 100.0f;
    private float far = 10000.0f;

    // timing
    private long lastTime, lastServerTime;
    private int fps;
    private int frames;
 
    // shaders
    private DefaultShader defaultShader;
    private Solid2DShader solid2DShader;
    private Solid3DShader solid3DShader;
    private Textured3DShader textured3DShader;
    private Textured2DShader textured2DShader;
 
    // fonts
 	private Font font;
	    
 	// scene objects
	private Seaweed seaweed;
	private SeaFloor seaFloor;
	public LinkedList<Rock> rocks;
	
	// game objects	
	public Fishes fishes;
    public Hook hook;
        
    // scoring
    public long highScore = 1000L;
    public Scores scores;
    public long score;
    
    private TexturedCube texturedCube;
    
    private Physics physics;
    private Window window;
    
    private Joystick joystick;
    
    public static void main(String[] args) {
        System.out.println("Starting game...");
        new Engine().run();
    }

    public void run() {
    	window = new Window(this);
    	window.open();
    	
        init();
        loop();

        window.close();
        glfwTerminate();
    }
    
    private void initShaders() {
        // Set up shaders
        System.out.println("Creating shaders...");

        defaultShader = new DefaultShader();
        defaultShader.init();
        
        solid2DShader = new Solid2DShader();
        solid2DShader.init();
        
        solid3DShader = new Solid3DShader();
        solid3DShader.init();
        
        textured3DShader = new Textured3DShader();
        textured3DShader.init();
        
        textured2DShader = new Textured2DShader();
        textured2DShader.init();
    }
    
    private void initSceneObjects() {
        // create objects
        texturedCube = new TexturedCube(defaultShader);
        texturedCube.init();
        
        seaFloor = new SeaFloor(this, defaultShader);
        seaFloor.init();
        
        rocks = new LinkedList<Rock>();
        for (int i = 0; i < 96; i ++) {
        	float gridSize = 20.0f;
 			float min = -gridSize;
 	 		float max = gridSize;
 	 		float randomValue = min + (random.nextFloat() * (max - min));
        	float x = randomValue;
        	
 			min = -5.5f;
 	 		max = -5.0f;
 	 		randomValue = min + (random.nextFloat() * (max - min));
        	float y = randomValue;

 			min = -gridSize;
 	 		max = gridSize;
 	 		randomValue = min + (random.nextFloat() * (max - min));
        	float z = randomValue;
        
        	min = 0.0f;
        	max = 360.0f;
 	 		randomValue = min + (random.nextFloat() * (max - min));
        	float rotation = randomValue;
        	
        	Rock rock = new Rock(defaultShader, random, new Vector3f(x, y, z), rotation);
        	//Rock rock = new Rock(defaultShader, random, new Vector3f(0.0f, 0.0f, 0.0f), 0.0f);
        	rock.init();
        	
        	//System.out.printf("Rock: (%f,%f,%f) %f\n", x, y, z, rotation);
        	rocks.add(rock);
        }	

        //sphereVAO = createSphereVAO(32, 32); // 32 segments for a smooth sphere
    }
    
    private void initTextures() {
    	glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Load textures
        //sphereTextureID = loadTexture("assets\\map.png");    
    }
    
    private void initScores() {
    	scores = new Scores("scores.dat");
    	highScore = scores.readHighScore();    	
    }

    // primary initialization function
    private void init() {
    	random = new Random(); // declare a random number generator in the main class so we can reuse it program wide
    	
    	joystick = new Joystick();
    	joystick.init();
    	
    	initTextures();
    	
        initShaders();
        
        initSceneObjects();

        initScores();
        
        
        seaweed = new Seaweed(this, defaultShader);

        hook = new Hook();
        hook.init(this, textured2DShader, solid2DShader);
        hook.setPosition(new Vector3f(width/2, height/2, 0));
        
        fishes = new Fishes(this, textured2DShader);
        fishes.init();
        
        font = new Font(this, solid2DShader, "assets/ClashDisplay-Variable.ttf");
        
        glClearColor(0.0f, 0.5f, 0.50f, 1.0f);
        
        lastTime = System.nanoTime();
        
        physics = new Physics(this);
    }

    // main loop
    private void loop() {
    	int error;
        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            throw new RuntimeException("OpenGL Error during initialization: " + error);
        } 
        
    	long HZ360 = 1_000_000_000L / 360;
    	
        while (!glfwWindowShouldClose(window.getWindow())) {
        	//
        	// count our frames and run physics at 360HZ and graphics as fast as possible
        	//
        	frames++;
        	long currentTime = System.nanoTime();
        	if (currentTime - lastServerTime >= HZ360) {
        	    physics.runFrame();
        	    seaweed.updateSeaweed();
        	    lastServerTime = currentTime;
        	}
        	// count our fps every second
            if (currentTime - lastTime >= 1_000_000_000L) {
               fps = frames;
               frames = 0;
               lastTime = currentTime;
            }            
            
            render();
            
            glfwPollEvents();
        }
    }

    // Send a matrix to a shader
    public static void setMatrix(int shader, int location, Matrix4f matrix) {
        glUseProgram(shader);
        
    	try (MemoryStack stack = MemoryStack.stackPush()) {
    	    FloatBuffer buffer = stack.mallocFloat(16);
    	    matrix.get(buffer);
    	    glUniformMatrix4fv(location, false, buffer);
    	}
    }
    
    // Send a color to a shader
    public static void setColor(int shader, int location, Vector3f v, float alpha) {
    	glUseProgram(shader);
    	glUniform4f(location, v.x, v.y, v.z, alpha); // Set the uniform
    }
        
    private void render() {
        // clear the screen
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        // point the camera
        Matrix4f m = new Matrix4f().lookAt(
        	    new Vector3f(0.0f, 0.0f, 10.0f),  // Camera position
        	    new Vector3f(0.0f, 0.0f, 0.0f),  // Look at point
        	    new Vector3f(0.0f, 1.0f, 0.0f)   // Up direction
        );
        // send the view matrix to the shaders
        setMatrix(textured3DShader.shaderProgram, textured3DShader.viewLocation, m);
        setMatrix(solid3DShader.shaderProgram, solid3DShader.viewLocation, m);
        setMatrix(defaultShader.shaderProgram, defaultShader.viewLocation, m);
        
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        // set our perspective matrix
        m = new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, near, far);
        setMatrix(textured3DShader.shaderProgram, textured3DShader.projectionLocation, m);
        setMatrix(solid3DShader.shaderProgram, solid3DShader.projectionLocation, m);
        setMatrix(defaultShader.shaderProgram, defaultShader.projectionLocation, m);
        
        
        // reset our model matrix
        m = new Matrix4f();
        m.identity();
        setMatrix(textured3DShader.shaderProgram, textured3DShader.modelLocation, m);
        setMatrix(solid3DShader.shaderProgram, solid3DShader.modelLocation, m);
        setMatrix(defaultShader.shaderProgram, defaultShader.modelLocation, m);
        
        // render 3D objects
        render3D();
        
        //
        // Switch to 2D rendering
        // 
        glViewport(0, 0, width, height);
        glDisable(GL_DEPTH_TEST);

        // send the 2d orthographic matrix to the shaders
        Matrix4f orthoMatrix = new Matrix4f().setOrtho(0, width, 0, height, -1, 1);
        setMatrix(textured2DShader.shaderProgram, textured2DShader.projectionLocation, orthoMatrix);
        setMatrix(solid2DShader.shaderProgram, solid2DShader.projectionLocation, orthoMatrix);

        // render 2D elements
        render2D();

        //
        // clean up post drawing
        //
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glEnable(GL_DEPTH_TEST);
        
        glUseProgram(0);
        
        // swap buffers
        glfwSwapBuffers(window.getWindow());

        // check for errors
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
          //  throw new RuntimeException("OpenGL Error during frame: " + error);
        }
    	
    }

    private void render3D() {
        //drawGrid();
        seaFloor.render();
        drawRocks();
        seaweed.drawSeaweed();
        
        //drawCube();
    }
    
    private void render2D() {
        drawScoresBackground();
        
        fishes.render();
        hook.render();
       
        // draw high scores and fps counter
        drawHUD();    	
    }
    
    private void drawRocks() {
        Matrix4f m = new Matrix4f();
        
        for (Rock rock : rocks) {
        	m.identity();
        	m.translate(rock.position.x, rock.position.y, rock.position.z);
        	m.rotate(Math.toRadians(rock.rotation), new Vector3f(0.0f, 1.0f, 0.0f));
        	setMatrix(defaultShader.shaderProgram, defaultShader.modelLocation, m);
        	rock.render(); 
        }
    }
    
    
    private void drawScoresBackground() {
    	//renderText("Hello, World!", 0.0f, 20.0f, 0.03f, 10.0f);
    	setColor(solid2DShader.shaderProgram, solid2DShader.colorLocation, new Vector3f(0.2f, 0.2f, 0.2f), 1.0f);
    	glBegin(GL_QUADS);
    		glVertex2f(0.0f, height); // Start point
    		glVertex2f(0.0f, height - 120.0f);
    		glVertex2f(width, height - 120.0f);
    		glVertex2f(width, height);
    	glEnd();
    }

    /*
    private void drawCube() {
        Matrix4f m = new Matrix4f();
        Vector3f v = new Vector3f(0.0f, 0.0f, 5.0f);
        m.identity();
      	m.translate(v.x, v.y, v.z);
      	setMatrix(defaultShader.shaderProgram, defaultShader.modelLocation, m);
       	//m.rotate((float)Math.toRadians(ang), 0.0f, 0.0f, 1.0f);
        texturedCube.render();        
    }
    */
    
 
/*
    private void drawGrid() {
    	glDisable(GL_TEXTURE_2D);
    	float size = 20.0f;

    	setColor(solid3DShader.shaderProgram, _3D_wireframeColorLocation, new Vector3f(0.50f, 0.15f, 0.0f));
    	    	
    	// Draw line using immediate mode
        glBegin(GL_LINES);
        	for (float x = -size; x < size; x += 0.5f) {
        		for (float y = -size; y < size; y += 0.5f) {			
        			glVertex3f(x,        -5.0f, y); // Start point
        			glVertex3f(x,        -5.0f, y + 0.5f);
        			glVertex3f(x,        -5.0f, y);
        			glVertex3f(x + 0.5f, -5.0f, y);
        			glVertex3f(x + 0.5f, -5.0f, y);
        			glVertex3f(x + 0.5f, -5.0f, y + 0.5f);
        			glVertex3f(x,        -5.0f, y + 0.5f);
        			glVertex3f(x + 0.5f, -5.0f, y + 0.5f);
        		}
        	}
        glEnd();
        
        setColor(solid3DShader.shaderProgram, _3D_wireframeColorLocation, new Vector3f(1.0f, 0.0f, 0.0f));
        glBegin(GL_LINES);
   			glVertex3f(0.0f, 0.0f, 0.0f);
   			glVertex3f(1.0f, 0.0f, 0.0f);
   	   	glEnd();
   	   	setColor(solid3DShader.shaderProgram, _3D_wireframeColorLocation, new Vector3f(0.0f, 1.0f, 0.0f));
   	    glBegin(GL_LINES);
   			glVertex3f(0.0f, 0.0f, 0.0f);
   			glVertex3f(0.0f, 1.0f, 0.0f);
   	   	glEnd();
   	   	setColor(solid3DShader.shaderProgram, _3D_wireframeColorLocation, new Vector3f(0.0f, 0.0f, 1.0f));
   	    glBegin(GL_LINES);
   			glVertex3f(0.0f, 0.0f, 0.0f);
   			glVertex3f(0.0f, 0.0f, 1.0f);
   	    glEnd();
             
    	glEnable(GL_TEXTURE_2D);
    }
  */  
  
    private void drawHUD() {
    	//renderText("Hello, World!", 0.0f, 20.0f, 0.03f, 10.0f);
    	setColor(solid2DShader.shaderProgram, solid2DShader.colorLocation, new Vector3f(1.0f, 1.0f, 0.0f), 1.0f);
    	//font.renderText("r86", 0.0f, 0.0f, 0.04f, 3.0f);
    	font.renderText("High Score: " + highScore, 10.0f, height - 50.0f, 0.04f, 3.0f);
    	setColor(solid2DShader.shaderProgram, solid2DShader.colorLocation, new Vector3f(0.0f, 1.0f, 0.0f), 1.0f);
    	font.renderText("Score: " + score, 10.0f, height - 100.0f, 0.04f, 3.0f);
    	setColor(solid2DShader.shaderProgram, solid2DShader.colorLocation, new Vector3f(1.0f, 0.0f, 1.0f), 1.0f);
    	//font.renderText("FPS: " + fps, width - 120.0f, height - 100.0f, 0.02f, 3.0f);
    }

    public static int loadTexture(String path) {
        int textureID;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = STBImage.stbi_load(path, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture file " + path);
            }
            
            textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            STBImage.stbi_image_free(image);
        }
        return textureID;
    }

    public static int createQuadVAO(float x, float y, float width, float height) {
        float[] vertices = {
                // Positions        // Texture Coords
                x, y,              0.0f, 0.0f,
                x + width, y,      1.0f, 0.0f,
                x, y + height,     0.0f, 1.0f,
                x + width, y + height, 1.0f, 1.0f
        };

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return vao;
    }
}
