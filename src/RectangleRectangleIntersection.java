import org.joml.Vector3f;

public class RectangleRectangleIntersection {
	    /**
	     * Checks if two axis-aligned bounding boxes intersect in 2D.
	     *
	     * @param min1 The minimum corner (x, y) of the first box.
	     * @param max1 The maximum corner (x, y) of the first box.
	     * @param min2 The minimum corner (x, y) of the second box.
	     * @param max2 The maximum corner (x, y) of the second box.
	     * @return True if the boxes intersect, false otherwise.
	     */
	    public static boolean intersects(Vector3f min1, Vector3f max1, Vector3f min2, Vector3f max2) {
	        // Check for separation along the x and y axes
	        return (min1.x <= max2.x && max1.x >= min2.x) &&
	               (min1.y <= max2.y && max1.y >= min2.y);
	    }
/*
	    public static void main(String[] args) {
	        // Example usage
	        Vector2f box1Min = new Vector2f(0, 0);
	        Vector2f box1Max = new Vector2f(2, 2);

	        Vector2f box2Min = new Vector2f(1, 1);
	        Vector2f box2Max = new Vector2f(3, 3);

	        boolean isIntersecting = intersects(box1Min, box1Max, box2Min, box2Max);

	        System.out.println("Boxes intersect: " + isIntersecting);
	    }
	}*/

}
