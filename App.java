/**
 * Pac-Man Game Application Entry Point
 * 
 * Main class responsible for initializing and launching the enhanced Pac-Man game
 * implementation. This class sets up the Swing GUI framework, configures the
 * game window dimensions, and integrates the PacMan game panel which contains
 * all core gameplay mechanics, special power-ups, traps, and ghost abilities.
 * 
 * Enhanced Features Included:
 * - Classic Pac-Man arcade gameplay with modern enhancements
 * - Special PowerFoodPlus items that create Pac-Man clones
 * - Unique ghost abilities (pink ghost shields, red ghost teleportation)
 * - Advanced trap systems (spider entanglement and ice freezing effects)
 * - Comprehensive score tracking and player life management
 * - Interactive HUD and game state notifications
 * 
 * @author Pac-Man Development Team
 * @version 1.2
 * @since 2024
 * @see PacMan
 */
import javax.swing.JFrame;

/**
 * Main application class for the enhanced Pac-Man game
 * 
 * This class creates the primary application window and initializes the complete
 * Pac-Man game experience with special features like PowerFoodPlus, ghost shields,
 * teleportation, and advanced trap systems designed to provide a more engaging
 * and challenging gameplay experience for users worldwide.
 * 
 * @author Pac-Man Development Team
 * @version 1.2
 * @since 2024
 * @category Game Application
 */
public class App {
    /**
     * Main method - primary entry point of the application
     * 
     * Initializes the Java Swing game window, configures all necessary UI properties,
     * and starts the enhanced Pac-Man game with comprehensive gameplay features.
     * This method follows best practices for Java Swing application initialization
     * and ensures proper resource management and UI responsiveness.
     * 
     * @param args Command line arguments (not used for this application)
     * @throws Exception Generic exception handling for any initialization errors during
     * game window setup or Pac-Man panel creation
     */
    public static void main(String[] args) throws Exception {
        // Game board configuration parameters
        int rowCount = 21;        // Number of vertical tiles in the game grid (21 rows)
        int columnCount = 19;     // Number of horizontal tiles in the game grid (19 columns)
        int tileSize = 32;        // Size of each individual tile (32x32 pixels for optimal gameplay)
        int boardWidth = columnCount * tileSize;    // Total width of game board in pixels
        int boardHeight = rowCount * tileSize;      // Total height of game board in pixels

        // Initialize the main game window using Java Swing framework
        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);      // Center the game window on user's screen for optimal user experience
        frame.setResizable(false);              // Prevent window resizing to maintain consistent game balance
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit application gracefully when window is closed

        // Create and add the main Pac-Man game panel to the window
        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();  // Ensure keyboard input is properly received by game panel
        frame.setVisible(true);     // Make the game window visible to start the gaming experience
    }
}
