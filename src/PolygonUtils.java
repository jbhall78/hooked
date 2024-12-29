import org.joml.Vector3f;
import java.util.List;

public class PolygonUtils {

    /**
     * Calculates the signed area of a polygon using the Shoelace formula.
     *
     * @param vertices a List of Vector3f points representing the vertices of the polygon.
     *                 Only the x and y coordinates are used.
     * @return the signed area of the polygon. Positive if vertices are counterclockwise,
     *         negative if clockwise.
     */
    public static float calculateSignedArea(List<Vector3f> vertices) {
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 vertices.");
        }

        float area = 0.0f;

        // Iterate through vertices
        for (int i = 0; i < vertices.size(); i++) {
            // Current vertex
            Vector3f current = vertices.get(i);
            // Next vertex (wrap around to the first vertex if at the end)
            Vector3f next = vertices.get((i + 1) % vertices.size());

            // Shoelace term: x_i * y_{i+1} - x_{i+1} * y_i
            area += current.x * next.y - next.x * current.y;
        }

        // Divide by 2 to get the actual signed area
        return area / 2.0f;
    }

    /**
     * Determines the winding order of a polygon based on its signed area.
     *
     * @param vertices a List of Vector3f points representing the vertices of the polygon.
     * @return true if the polygon is counterclockwise (CCW), false if clockwise (CW).
     */
    public static boolean isCounterClockwise(List<Vector3f> vertices) {
        return calculateSignedArea(vertices) > 0.0f;
    }

   /* public static void main(String[] args) {
        // Example usage
        List<Vector3f> polygon = List.of(
            new Vector3f(0.0f, 0.0f, 0.0f),
            new Vector3f(4.0f, 0.0f, 0.0f),
            new Vector3f(4.0f, 3.0f, 0.0f),
            new Vector3f(0.0f, 3.0f, 0.0f)
        );

        float signedArea = calculateSignedArea(polygon);
        System.out.println("Signed Area: " + signedArea);
        System.out.println("Is Counterclockwise: " + isCounterClockwise(polygon));
    }*/
}
