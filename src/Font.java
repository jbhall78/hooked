import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import org.lwjgl.BufferUtils;

/*
import org.lwjglx.util.glu.*

import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallback;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;
*/

public class Font {
    private int fontTexture;
    private STBTTFontinfo fontInfo;
    private String fontPath;
    private ByteBuffer fontData;
    private Shader shader;
    private Engine engine;
    private List<Vector3f> randomColors;
        
    private boolean runOnce = false;
    
    public Font(Engine engine, Shader shader, String fontPath) {
    	this.fontPath = fontPath;
    	this.shader = shader;
    	this.engine = engine;
    	initFont();
    }
    
    private ByteBuffer loadFontFile(String path) {
    	ByteBuffer buffer = null;
    	InputStream in;
    	byte[] bytes;
    	
    	try {
    		in = new FileInputStream(path);
    	} catch (java.io.FileNotFoundException e) {
    		return null;
    	}
    	
    	try {
    		bytes = in.readAllBytes();
    	} catch (java.io.IOException e) {
    		try {
    			in.close();
    		} catch (java.io.IOException e2) {
    			
    		}
    		return null;
    	}	
    
		buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		
		try {
			in.close();
		} catch (java.io.IOException e) {
			
		}
		
        return buffer;
    }

    private void initFont() {
        fontInfo = STBTTFontinfo.malloc(); // or create() ?
    	
    	// Load the TrueType font
    	fontData = loadFontFile(fontPath);
    	if (! STBTruetype.stbtt_InitFont(fontInfo, fontData)) {
    		throw new RuntimeException("Failed to initialize font");
    	}	
      
    	// Create a texture to store the font bitmap (optional, for fallback)
    	fontTexture = glGenTextures();
    	glBindTexture(GL_TEXTURE_2D, fontTexture);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }	
    
    public void renderText(String text, float x, float y, float size, float spacing) {
        //glBindTexture(GL_TEXTURE_2D, fontTexture);

    	float start = x;
        try (MemoryStack stack = stackPush()) {
            //FloatBuffer xPos = stack.floats(x);
            //FloatBuffer yPos = stack.floats(y);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int idx;
                
                if (c < 32 || c > 127) {
                	continue;  // Only printable ASCII characters
                }

                if (c == ' ') {
                    idx = STBTruetype.stbtt_FindGlyphIndex(fontInfo, 'L');
                	start += getGlyphWidth(idx, size);
                	continue;
                }
                // Get the glyph for the current character
                idx = STBTruetype.stbtt_FindGlyphIndex(fontInfo, c);
                renderGlyph(idx, start, y, size);
                start += getGlyphWidth(idx, size) + spacing;
            }
        }
    }

    private void renderGlyph(int c, float xPos, float yPos, float mag) {
    	/*
    	STBTTVertex.Buffer vertices = STBTruetype.stbtt_GetGlyphShape(fontInfo, c);
    	List<List<Vector3f>> contours = new ArrayList<>();

    	List<Vector3f> currentContour = new ArrayList<>();
    	for (int i = 0; i < vertices.capacity(); i++) {
    	    STBTTVertex v = vertices.get(i);

    	    switch (v.type()) {
    	        case STBTruetype.STBTT_vmove: // Start a new contour
    	            if (!currentContour.isEmpty()) {
    	                contours.add(currentContour);
    	                if (! runOnce) System.out.printf("new contour\n");
    	                currentContour = new ArrayList<>();
    	            }
    	            if (! runOnce) System.out.printf("vmove (%d, %d)\n", v.x(), v.y());
    	            currentContour.add(new Vector3f((float)v.x(), (float)v.y(), 0));
    	            break;

    	        case STBTruetype.STBTT_vline: // Straight line
    	            if (! runOnce) System.out.printf("vline (%d, %d)\n", v.x(), v.y());
    	            currentContour.add(new Vector3f((float)v.x(), (float)v.y(), 0));
    	            break;

    	        case STBTruetype.STBTT_vcurve: // Quadratic Bezier curve
    	            // Approximate the curve with line segments
    	            Vector3f start = currentContour.get(currentContour.size() - 1);
    	            Vector3f control = new Vector3f(v.cx(), v.cy(), 0);
    	            Vector3f end = new Vector3f(v.x(), v.y(), 0);
    	            approximateBezierCurve(currentContour, start, control, end, 10); // 10 segments
    	            break;
    	    }
    	}

    	if (!currentContour.isEmpty()) {
    	    contours.add(currentContour);
    	    if (!runOnce) System.out.printf("new contour\n");
    	}
    	
    	for (int i = 0; i < 128; i++) {
    		if (!runOnce) {
    			if (randomColors == null)
    				randomColors = new ArrayList<Vector3f>();
    			Vector3f color = new Vector3f(
  					engine.random.nextFloat(),
  					engine.random.nextFloat(), 
  					engine.random.nextFloat()
    					);
    			randomColors.add(color);
    		}
  	    }
    	
    	int direction = 0;
    	int prevDirection = 0;
        glEnable(GL_STENCIL_TEST);
        glClear(GL_STENCIL_BUFFER_BIT);          // Clear stencil buffer
    	for (int i = 0; i < contours.size(); i ++) {
    		List<Vector3f> contour = contours.get(i);
    		direction += PolygonUtils.isCounterClockwise(contour) ? 1 : -1;
    			
    		Vector3f color = randomColors.get(i);
    		if (!runOnce) System.out.printf("direction: %d\n", direction);

    		//if (prevDirection != direction) {
    		//	glDisable(GL_STENCIL_TEST);
    		//	glEnable(GL_STENCIL_TEST);
    	    //  glClear(GL_STENCIL_BUFFER_BIT);          // Clear stencil buffer
    		//}
    		if (direction > 0) {
    	        // Step 1: Render the larger circle
    	        glStencilFunc(GL_ALWAYS, 1, 0xFF);       // Always pass stencil test
    	        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);  // Do not modify stencil buffer

        		Engine.setColor(shader.shaderProgram, shader.colorLocation, color, 1.0f);
    			glBegin(GL_POLYGON);
    			for (Vector3f point : contour) {
     	       		glVertex2f(xPos + (point.x * mag), yPos + (point.y * mag));
    			}
    			glEnd();
    		} else {
    	        // Step 2: Render the smaller circle to the stencil buffer
    	        glColorMask(false, false, false, false); // Disable color writing
    	        glDepthMask(false);                      // Disable depth writing
    	        glStencilFunc(GL_ALWAYS, 1, 0xFF);       // Always pass
    	        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE); // Write stencil value

    			
    			Engine.setColor(shader.shaderProgram, shader.colorLocation, new Vector3f(0,1.0f,0), 1.0f);
    			glBegin(GL_POLYGON);
    			for (Vector3f point : contour.reversed()) {
     	       		glVertex2f(xPos + (point.x * mag), yPos + (point.y * mag));
    			}
    			glEnd();

    		       // Step 3: Clear the smaller circle's area
    	        glColorMask(true, true, true, true);    // Enable color writing
    	        glDepthMask(true);                     // Enable depth writing
    	        glStencilFunc(GL_EQUAL, 1, 0xFF);      // Pass where stencil value equals 1
    	        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP); // Keep stencil buffer unchanged
    			glBegin(GL_POLYGON);
    			for (Vector3f point : contour.reversed()) {
     	       		glVertex2f(xPos + (point.x * mag), yPos + (point.y * mag));
    			}
    			glEnd();
    		}
            
            prevDirection = direction;
    	}
    	glDisable(GL_STENCIL_TEST);
    	
    	List<Float> verticesFlat = new ArrayList<>();
    	List<Integer> holeIndices = new ArrayList<>();

    	int vertexCount = 0;
    	for (List<Vector3f> contour : contours) {
    	    if (!verticesFlat.isEmpty()) {
    	        holeIndices.add(vertexCount);
    	     //   if (! runOnce) System.out.printf("i: (%d)\n", vertexCount);
    	    }
    	    for (Vector3f point : contour) {
    	    //	if (! runOnce) System.out.printf("v: (%f, %f)\n",  point.x, point.y);
    	    	verticesFlat.add(point.x);
    	        verticesFlat.add(point.y);
    	        vertexCount++;
    	    }
    	}
//        holeIndices.add(vertexCount);
    	
    	
    	
    	double[] verticesArray = verticesFlat.stream().mapToDouble(f -> f).toArray();
    	int[] holeIndicesArray = holeIndices.stream().mapToInt(i -> i).toArray();

    	// Example using Earcut4j    	
    	List<Integer> indices = Earcut.earcut(verticesArray, holeIndicesArray, 2); // 2D triangulation    	
    	for (int i = 0; i < indices.size(); i++)
    		if (! runOnce) System.out.printf("%d\n",  indices.get(i));
    	for (int i = 0; i < indices.size(); i += 3) {
    		if (! runOnce) System.out.printf("i: [");
    		for (int j = 0; j < 3; j++)
    			if (!runOnce) System.out.printf("%d, ", indices.get(i + j));
    		if (! runOnce) System.out.printf("]\n");
    	}
    	
    	
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(verticesArray.length);
        for (double d : verticesArray) vertexBuffer.put((float)d);
        vertexBuffer.flip();

    	IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
    	for (Integer i : indices) indexBuffer.put(i);
    	indexBuffer.flip();

		Engine.setColor(shader.shaderProgram, shader.colorLocation, new Vector3f(0.25f, 0.25f, 0.25f), 1.0f);
    			
    	// Upload vertex positions to a VBO
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		// Upload indices to an EBO
		int ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

		// Draw the triangles
		//glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);

    	runOnce = true;*/
    	
        STBTTVertex.Buffer vertices = STBTruetype.stbtt_GetGlyphShape(fontInfo, c);
        if (vertices != null) {
        	try {
        		STBTTVertex prev = STBTTVertex.malloc();
        		
        	    //List<Vector3f> contour = new ArrayList<>();

        		
        		// Use the vertex as needed
        		for (int i = 0; i < vertices.capacity(); i++) {
                	STBTTVertex v = vertices.get(i);
                	
                	
                	switch ( v.type() ) {
                		case STBTruetype.STBTT_vmove:
                			//System.out.println("MOVE: " + v.x() + " - " + v.y());
                   			prev = v;
                			continue;
                		case STBTruetype.STBTT_vline:
                			//System.out.println("LINE: " + v.x() + " - " + v.y());

                			//glVertex2f((xPos + prev.x()) * mag, (yPos + prev.y()) * mag);
                			//glVertex2f((xPos + v.x()) * mag, (yPos + v.y()) * mag);
                			glBegin(GL_LINES);
                			glVertex2f(xPos + (prev.x() * mag), yPos + (prev.y() * mag));
                			glVertex2f(xPos + (v.x() * mag), yPos + (v.y() * mag));
                			glEnd();
                			//contour.add(new Vector3f((xPos + prev.x()) * mag, (yPos + prev.y()) * mag, 0.0f));
                			//contour.add(new Vector3f((xPos + v.x()) * mag, (yPos + v.y()) * mag, 0.0f));
                			break;
                		case STBTruetype.STBTT_vcurve:
                			// Define the control points for the quadratic Bezier curve
                			float x0 = prev.x(), y0 = prev.y();  // Start point
                			float x1 = v.cx(), y1 = v.cy();  // Control point
                			float x2 = v.x(), y2 = v.y();  // End point

                			// Start drawing the curve
                			glBegin(GL_LINE_STRIP);
                			for (float t = 0; t <= 1.0; t += 0.01) {  // Adjust t for smoothness
                			    // Calculate the x and y coordinates of the point on the curve at parameter t
                			    float x = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * x1 + t * t * x2;
                			    float y = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * y1 + t * t * y2;
                			    //glVertex2f((xPos + x) * mag, (yPos + y) * mag);  // Plot the point
                			    glVertex2f(xPos + (x * mag), yPos + (y * mag));  // Plot the point
                			    //contour.add(new Vector3f((xPos + x) * mag, (yPos + y) * mag, 0.0f));
                			}
                			glEnd();
                			
                			break;
                	}
                	
                	prev = v;
                }
        		glEnd();
        		//prev.free(); // Free the vertex memory when done
        	} finally {
                vertices.free();  // Free the buffer to release resources
            }
        }
    }

    private float getGlyphWidth(int c, float mag) {
        STBTTVertex.Buffer vertices = STBTruetype.stbtt_GetGlyphShape(fontInfo, c);
        float max = 0.0f;
        float width;
        if (vertices != null) {
        	try {
        		STBTTVertex prev = STBTTVertex.malloc();
        		
        		// Use the vertex as needed
        		for (int i = 0; i < vertices.capacity(); i++) {
                	STBTTVertex v = vertices.get(i);

                	switch ( v.type() ) {
                		case STBTruetype.STBTT_vmove:
                			prev = v;
                			continue;
                		case STBTruetype.STBTT_vline:
                			width = v.x() * mag;
                			if (width > max)
                				max = width;
                			break;
                		case STBTruetype.STBTT_vcurve:
                			// Define the control points for the quadratic Bezier curve
                			float x0 = prev.x();
                			//float y0 = prev.y();  // Start point
                			float x1 = v.cx();
                			//float y1 = v.cy();  // Control point
                			float x2 = v.x();
                			//float y2 = v.y();  // End point

                			for (float t = 0; t <= 1.0; t += 0.01) {  // Adjust t for smoothness
                			    // Calculate the x and y coordinates of the point on the curve at parameter t
                			    float x = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * x1 + t * t * x2;
                			    //float y = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * y1 + t * t * y2;
                			    
                			    width = x * mag;
                			    if (width > max)
                			    	max = width;
                			}
                			break;
                	}

                	prev = v;
                }
        	} finally {
                vertices.free();  // Free the buffer to release resources
            }
        }
        return max;
    }
    
    void approximateBezierCurve(List<Vector3f> contour, Vector3f p0, Vector3f p1, Vector3f p2, int segments) {
        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float u = 1 - t;
            float x = u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x;
            float y = u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y;
            if (! runOnce) System.out.printf("curve point (%f, %f)\n", x, y);
            contour.add(new Vector3f(x, y, 0));
        }
    }
    
    /*
    public static List<Float> tessellateGlyph(float[] contour) {
        List<Float> triangles = new ArrayList<>();

        GLUtessellator tess = GLU.gluNewTess();

        // Callback for handling begin/end contour and vertex data
        GLUtessellatorCallback callback = new GLUtessellatorCallbackAdapter() {
            @Override
            public void begin(int type) {
                // Ignore
            }

            @Override
            public void end() {
                // Ignore
            }

            @Override
            public void vertex(Object vertexData) {
                float[] coords = (float[]) vertexData;
                triangles.add(coords[0]);
                triangles.add(coords[1]);
            }

            @Override
            public void combine(float[] coords, Object[] data, float[] weight, Object[] outData) {
                // Not used in this example
            }

            @Override
            public void error(int errnum) {
                throw new RuntimeException("Tessellation error: " + GLU.gluErrorString(errnum));
            }
        };

        GLU.gluTessCallback(tess, GLU_TESS_VERTEX, callback);
        GLU.gluTessCallback(tess, GLU_TESS_BEGIN, callback);
        GLU.gluTessCallback(tess, GLU_TESS_END, callback);
        GLU.gluTessCallback(tess, GLU_TESS_ERROR, callback);
        GLU.gluTessProperty(tess, GLU_TESS_WINDING_RULE, GLU_TESS_WINDING_RULE);

        // Define the contour (replace with your actual glyph data)
        float[] contourData = contour; 

        // Begin polygon
        GLU.gluTessBeginPolygon(tess, null);
        GLU.gluTessBeginContour(tess);

        // Add vertices to the contour
        for (int i = 0; i < contourData.length; i += 2) {
            float[] vertex = { contourData[i], contourData[i + 1] };
            GLU.gluTessVertex(tess, vertex, 0, vertex); // 0 is arbitrary data
        }

        GLU.gluTessEndContour(tess);
        GLU.gluTessEndPolygon(tess);

        GLU.gluDeleteTess(tess);

        return triangles;
    }*/
    
}




