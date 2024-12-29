import java.io.*;

public class Scores {
	    private final String filePath;

	    public Scores(String filePath) {
	        this.filePath = filePath;
	    }

	    // Method to create a file
	    public long readHighScore() {
	        File file = new File(filePath);
	        try {
	            if (! file.exists()) {
	            	file.createNewFile();
	            }
	        } catch (IOException e) {
	            System.err.println("Error creating file: " + e.getMessage());
	        }
	        return readLong();
	    }

	    // Method to write a long value to the file
	    private void writeLong(long value) {
	        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath))) {
	            dos.writeLong(value);
	            //System.out.println("Wrote long value: " + value);
	        } catch (IOException e) {
	            System.err.println("Error writing long value to file: " + e.getMessage());
	        }
	    }

	    // Method to read a long value from the file
	    private long readLong() {
	        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {
	            long value = dis.readLong();
	            //System.out.println("Read long value: " + value);
	            return value;
	        } catch (EOFException e) {
	            System.err.println("Reached end of file or not enough data.");
	        } catch (IOException e) {
	            System.err.println("Error reading int value from file: " + e.getMessage());
	        }
	        return 0; // Return 0 to indicate failure or no data
	    }
	    
	    public void updateHighScore(long score) {	
	    	writeLong(score);
	    }
}
