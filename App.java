/**
 * Pac-Man Game Application Entry Point
 * Main class responsible for initializing and launching the Pac-Man game
 * This class sets up the Swing GUI window and integrates the PacMan game panel
 * 
 * @author Pac-Man Development Team
 * @version 1.0
 */
import javax.swing.JFrame;

/**
 * Main application class for the Pac-Man game
 * Creates the main window and initializes the game panel
 */
public class App {
    /**
     * Main method - entry point of the application
     * Initializes the game window and starts the Pac-Man game
     * 
     * @param args Command line arguments (not used for this application)
     * @throws Exception Generic exception handling for any initialization errors
     */
    public static void main(String[] args) throws Exception {
        // Game board configuration parameters
        int rowCount = 21;        // Number of rows in the game grid
        int columnCount = 19;     // Number of columns in the game grid
        int tileSize = 32;        // Size of each tile in pixels
        int boardWidth = columnCount * tileSize;    // Total board width
        int boardHeight = rowCount * tileSize;      // Total board height

        // Initialize the main game window
        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);      // Center window on screen
        frame.setResizable(false);              // Prevent window resizing
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit application on window close

        // Create and add the PacMan game panel to the window
        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();  // Set focus to game panel for keyboard input
        frame.setVisible(true);     // Make window visible
    }
}
