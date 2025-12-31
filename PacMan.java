/**
 * PacMan Game - A modern implementation of the classic Pac-Man arcade game with enhanced features
 * 
 * This class serves as the main entry point and controller for the Pac-Man game. It extends JPanel
 * to provide the game canvas and implements ActionListener and KeyListener for handling game loops
 * and user input respectively. The game includes all traditional Pac-Man elements plus special power-ups,
 * traps, and unique ghost abilities for an enhanced gaming experience.
 * 
 * Features:
 * - Classic Pac-Man gameplay mechanics
 * - Multiple ghost types with unique abilities (Blue, Orange, Pink with shields, Red with teleport)
 * - Special power-ups (PowerFoodPlus, clones, etc.)
 * - Trap system (Spider silk, ice shadow)
 * - Entity status effects (Frozen, Entangled, Scared)
 * - Score tracking and lives system
 * - Game over/start states
 * 
 * @author PacMan Team
 * @version 1.0
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    /**
     * Block class represents any game entity that occupies a tile on the board
     * This includes walls, ghosts, food, and Pac-Man himself
     */
    class Block {
        int x;          // X-coordinate position on the board
        int y;          // Y-coordinate position on the board
        int width;      // Width of the block
        int height;     // Height of the block
        Image image;    // Visual representation of the block

        int startX;     // Initial X-coordinate for reset purposes
        int startY;     // Initial Y-coordinate for reset purposes
        char direction = 'U'; // Current movement direction: 'U'(up), 'D'(down), 'L'(left), 'R'(right)
        int velocityX = 0; // Horizontal velocity (pixels per frame)
        int velocityY = 0; // Vertical velocity (pixels per frame)

        /**
         * Block constructor
         * @param image Visual representation of the block
         * @param x Initial X-coordinate
         * @param y Initial Y-coordinate
         * @param width Width of the block in pixels
         * @param height Height of the block in pixels
         */
        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        /**
         * Updates the block's movement direction
         * @param direction New direction to move in ('U', 'D', 'L', or 'R')
         * 
         * This method checks for wall collisions after moving and reverts direction
         * if the new movement would result in a collision
         */
        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        /**
         * Updates the block's velocity based on its current direction
         * Velocity is calculated as a fraction of the tile size for consistent movement
         * across different screen resolutions
         */
        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        /**
         * Resets the block to its initial position
         */
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    /**
     * PowerFoodPlus class represents a special power-up item
     * Eating this power-up grants Pac-Man special abilities
     */
    class PowerFoodPlus {
        int x;          // X-coordinate position on the board
        int y;          // Y-coordinate position on the board
        int width = 4;  // Width of the power-up
        int height = 4; // Height of the power-up
        Image image;    // Visual representation of the power-up

        /**
         * PowerFoodPlus constructor
         * @param image Visual representation of the power-up
         * @param x Initial X-coordinate
         * @param y Initial Y-coordinate
         */
        PowerFoodPlus(Image image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
    }

    /**
     * PacmanClone class represents a clone of Pac-Man created by special power-ups
     * Clones mimic Pac-Man's behavior but move faster and rotate
     */
    class PacmanClone {
        int x;              // X-coordinate position on the board
        int y;              // Y-coordinate position on the board
        int width = tileSize; // Width of the clone (matches Pac-Man)
        int height = tileSize; // Height of the clone (matches Pac-Man)
        char direction;     // Movement direction of the clone
        int velocityX = 0;  // Horizontal velocity of the clone
        int velocityY = 0;  // Vertical velocity of the clone
        Image image;        // Visual representation of the clone
        float rotation = 0; // Rotation angle of the clone in degrees
        
        /**
         * PacmanClone constructor
         * @param x Initial X-coordinate
         * @param y Initial Y-coordinate
         * @param direction Initial movement direction
         * @param image Visual representation of the clone
         */
        PacmanClone(int x, int y, char direction, Image image) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.image = image;
            updateVelocity();
        }

        /**
         * Updates the clone's velocity based on its current direction
         * Clones move 1.5x faster than regular Pac-Man
         */
        void updateVelocity() {
            int cloneSpeed = (int)(tileSize / 4 * 1.5);
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -cloneSpeed;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = cloneSpeed;
            }
            else if (this.direction == 'L') {
                this.velocityX = -cloneSpeed;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = cloneSpeed;
                this.velocityY = 0;
            }
        }

        /**
         * Moves the clone according to its current velocity and updates rotation
         * Clones rotate continuously as they move
         */
        void move() {
            this.x += this.velocityX;
            this.y += this.velocityY;
            this.rotation += 10; // Rotate 10 degrees each frame
            if (this.rotation >= 360) {
                this.rotation -= 360; // Wrap rotation to 0-359 degrees
            }
        }
    }

    /**
     * GhostScaredStatus class tracks the scared state of ghosts
     * When ghosts are scared, they can be eaten by Pac-Man for extra points
     */
    class GhostScaredStatus {
        long startTime;          // Timestamp when the scared state started
        long duration = 15000;   // Duration of the scared state in milliseconds (15 seconds)

        /**
         * GhostScaredStatus constructor
         * Initializes the startTime to the current system time
         */
        GhostScaredStatus() {
            this.startTime = System.currentTimeMillis();
        }

        /**
         * Checks if the scared state is still active
         * @return true if the scared state duration hasn't expired
         */
        boolean isActive() {
            return System.currentTimeMillis() - startTime < duration;
        }

        /**
         * Gets the remaining time for the scared state
         * @return Remaining milliseconds (0 if the state is no longer active)
         */
        long getRemainingTime() {
            long remaining = duration - (System.currentTimeMillis() - startTime);
            return Math.max(0, remaining);
        }
    }
    
    /**
     * Trap class represents a trap that can be placed on the board
     * Traps have various effects on game entities that come into contact
     * with them
     */
    class Trap {
        int x;          // X-coordinate of the trap
        int y;          // Y-coordinate of the trap
        int width;      // Width of the trap
        int height;     // Height of the trap
        long startTime; // Timestamp when the trap was created
        long duration;  // Duration of the trap in milliseconds
        boolean isActive; // Whether the trap is currently active
        String type;    // Type of trap ("spider" or "ice")
        
        /**
         * Trap constructor
         * @param x Initial X-coordinate
         * @param y Initial Y-coordinate
         * @param duration Duration the trap remains active
         * @param type Type of trap
         */
        Trap(int x, int y, long duration, String type) {
            this.x = x;
            this.y = y;
            this.width = tileSize;
            this.height = tileSize;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
            this.isActive = true;
            this.type = type;
        }
        
        /**
         * Checks if the trap is still active
         * @return true if the trap duration hasn't expired
         */
        boolean isActive() {
            if (!isActive) return false;
            if (System.currentTimeMillis() - startTime > duration) {
                isActive = false;
                return false;
            }
            return true;
        }
        
        /**
         * Draws the trap on the game board
         * @param g Graphics context used for drawing
         */
        void draw(Graphics g) {
            if (!isActive()) return;
            
            if (type.equals("spider")) {
                // Draw spider silk trap with green translucent color
                g.setColor(new Color(0, 255, 0, 100));
                g.fillRect(x, y, width, height);
            } else if (type.equals("ice")) {
                // Draw ice shadow trap with blue translucent color
                g.setColor(new Color(0, 0, 255, 100));
                g.fillRect(x, y, width, height);
            }
        }
    }
    
    /**
     * FrozenStatus class represents the frozen effect applied to entities by ice traps
     * Frozen entities have their movement speed reduced significantly
     */
    class FrozenStatus {
        long startTime; // Timestamp when the frozen effect started
        long duration;  // Duration of the frozen effect in milliseconds
        
        /**
         * FrozenStatus constructor
         * @param duration Duration the effect will last
         */
        FrozenStatus(long duration) {
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        /**
         * Checks if the frozen effect is still active
         * @return true if the effect duration hasn't expired
         */
        boolean isActive() {
            return System.currentTimeMillis() - startTime < duration;
        }
        
        /**
         * Gets the speed multiplier for frozen entities
         * @return Speed multiplier (1/3 while frozen, 1 otherwise)
         */
        double getSpeedMultiplier() {
            return isActive() ? 1.0 / 3.0 : 1.0; // Ice trap effect: speed reduced to 1/3
        }
    }
    
    /**
     * EntangledStatus class represents the entangled effect applied to entities by spider traps
     * Entangled entities may have movement restrictions or other penalties
     */
    class EntangledStatus {
        long startTime; // Timestamp when the entangled effect started
        long duration;  // Duration of the entangled effect in milliseconds
        
        /**
         * EntangledStatus constructor
         * @param duration Duration the effect will last
         */
        EntangledStatus(long duration) {
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        /**
         * Checks if the entangled effect is still active
         * @return true if the effect duration hasn't expired
         */
        boolean isActive() {
            return System.currentTimeMillis() - startTime < duration;
        }
    }

    // Board dimensions and tile properties
    private int rowCount = 21;     // Number of rows on the game board
    private int columnCount = 19;  // Number of columns on the game board
    private int tileSize = 32;     // Size of each tile in pixels (square)
    private int boardWidth = columnCount * tileSize;    // Total board width in pixels
    private int boardHeight = rowCount * tileSize;      // Total board height in pixels

    // Game entity images
    private Image wallImage;           // Image for wall tiles
    private Image blueGhostImage;      // Image for blue ghosts
    private Image orangeGhostImage;    // Image for orange ghosts
    private Image pinkGhostImage;      // Image for pink ghosts
    private Image redGhostImage;       // Image for red ghosts
    private Image powerFoodPlusImage;  // Image for PowerFoodPlus items

    // Pac-Man directional images
    private Image pacmanUpImage;    // Pac-Man facing upward
    private Image pacmanDownImage;  // Pac-Man facing downward
    private Image pacmanLeftImage;  // Pac-Man facing left
    private Image pacmanRightImage; // Pac-Man facing right
    
    // Special game systems and status trackers
    private HashSet<Trap> traps; // Collection of currently active traps on the board
    private HashMap<Block, Integer> pinkGhostShields; // Shield count for each pink ghost
    private HashMap<Block, Long> redGhostTeleportCooldown; // Teleport ability cooldowns for red ghosts
    private HashMap<Block, FrozenStatus> frozenEntities; // Frozen status tracking for game entities
    private HashMap<Block, EntangledStatus> entangledEntities; // Entangled status tracking for game entities
    private HashMap<Block, Long> ghostRespawnTimers; // Respawn timers for dead ghosts
    
    // UI state for displaying active trap effects
    private String activeTrapEffect = "";         // Currently active trap effect for UI display
    private long trapEffectStartTime = 0;         // Timestamp when the trap effect started
    private long trapEffectDuration = 0;          // Duration of the trap effect

    /**
     * Game board tile map definition
     * Map legend:
     * - 'X': Wall tile
     * - 'O': Skip tile (no collision, no points)
     * - 'P': Pac-Man starting position
     * - ' ': Regular food pellet
     * - 'b': Blue ghost starting position
     * - 'o': Orange ghost starting position
     * - 'p': Pink ghost starting position
     * - 'r': Red ghost starting position
     */
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X      bpo        X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    // Game entity collections
    HashSet<Block> walls;               // Collection of all wall blocks
    HashSet<Block> foods;               // Collection of all food pellets
    HashSet<Block> ghosts;              // Collection of all ghost entities
    HashSet<PowerFoodPlus> powerFoodsPlus; // Collection of all PowerFoodPlus items
    HashSet<PacmanClone> pacmanClones; // Collection of Pac-Man clones
    Block pacman;                       // Main Pac-Man player entity

    // Game state variables
    int powerFoodPlusSkillCount = 0;   // Number of special skills currently available to the player
    HashMap<Block, GhostScaredStatus> ghostScaredMap; // Tracks scared status of individual ghosts
    HashSet<Block> deadGhosts;         // Collection of ghosts that are currently dead and need respawn

    // Game control variables
    Timer gameLoop;                    // Main game loop timer
    char[] directions = {'U', 'D', 'L', 'R'}; // Valid movement directions (Up, Down, Left, Right)
    Random random = new Random();      // Random number generator for various game elements
    int score = 0;                     // Player's current score
    int lives = 3;                     // Number of lives the player has remaining
    boolean gameOver = false;          // Tracks if the game has ended (player lost)
    boolean gameStarted = false;       // Tracks if the game has started

    /**
     * PacMan constructor - initializes the game
     */
    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load game entity images from files
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        powerFoodPlusImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        // Set initial random directions for all ghosts
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        // Initialize game loop timer to run at 20 frames per second
        gameLoop = new Timer(50, this); // 20 FPS = 1000ms / 50ms per frame
        gameLoop.start();
    }

    /**
     * Loads the game map from the tileMap array and initializes all game entities
     * 
     * This method processes each character in the tileMap array and creates the
     * corresponding game entities. It also initializes special game systems
     * and collections.
     */
    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        powerFoodsPlus = new HashSet<PowerFoodPlus>();
        pacmanClones = new HashSet<PacmanClone>();
        ghostScaredMap = new HashMap<Block, GhostScaredStatus>();
        powerFoodPlusSkillCount = 0;
        
        // Initialize special ghost-related variables and collections
        traps = new HashSet<Trap>();
        pinkGhostShields = new HashMap<Block, Integer>();
        redGhostTeleportCooldown = new HashMap<Block, Long>();
        frozenEntities = new HashMap<Block, FrozenStatus>();
        entangledEntities = new HashMap<Block, EntangledStatus>();
        ghostRespawnTimers = new HashMap<Block, Long>();

        // Process each tile in the tileMap
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') { // Wall tile
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { // Blue ghost starting position
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { // Orange ghost starting position
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { // Pink ghost starting position
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    pinkGhostShields.put(ghost, 3); // Pink ghosts start with 3 shields
                }
                else if (tileMapChar == 'r') { // Red ghost starting position
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { // Pac-Man starting position
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { // Regular food pellet
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }

        generatePowerFoodPlus(); // Generate special power-up items
    }

    /**
     * Generates PowerFoodPlus items on the game board
     * 
     * This method clears existing power-ups and creates new ones by selecting
     * random food positions from available food pellets. It limits the number
     * of power-ups to a maximum of 9 and prevents infinite loops by using a
     * maximum attempt count.
     */
    public void generatePowerFoodPlus() {
        powerFoodsPlus.clear();
        int count = 0;
        int maxAttempts = 100;
        int attempts = 0;

        // Convert foods to list for random selection
        java.util.List<Block> foodList = new java.util.ArrayList<>(foods);

        while (count < 9 && attempts < maxAttempts && foodList.size() > 0) {
            int randomIndex = random.nextInt(foodList.size());
            Block food = foodList.get(randomIndex);
            foodList.remove(randomIndex);

            PowerFoodPlus plus = new PowerFoodPlus(powerFoodPlusImage, food.x, food.y);
            powerFoodsPlus.add(plus);
            foods.remove(food);
            count++;
            attempts++;
        }
    }

    /**
     * Paints the game components on the screen
     * 
     * This method is called automatically by Swing to redraw the component.
     * It delegates the actual drawing to the draw() method.
     * 
     * @param g Graphics object used for drawing
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Draws all game elements on the screen
     * 
     * This method renders the complete game interface including Pac-Man,
     * ghosts, walls, foods, power-ups, traps, UI elements, and status information.
     * It handles both the main gameplay screen and the start/game-over screens.
     * 
     * @param g Graphics context used for drawing all game elements
     */
    public void draw(Graphics g) {
        // Check if game hasn't started yet
        if (!gameStarted) {
            // Draw start menu
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, boardWidth, boardHeight);
            
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String title = "PAC MAN";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (boardWidth - titleWidth) / 2, boardHeight / 2 - 50);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String pressSpace = "PRESS SPACE TO START";
            int pressSpaceWidth = g.getFontMetrics().stringWidth(pressSpace);
            g.drawString(pressSpace, (boardWidth - pressSpaceWidth) / 2, boardHeight / 2 + 20);
            
            return; // Exit draw method early
        }
        
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // Draw rotating pacman clones
        for (PacmanClone clone : pacmanClones) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform oldTransform = g2d.getTransform();
            
            // Rotate around center
            int centerX = clone.x + clone.width / 2;
            int centerY = clone.y + clone.height / 2;
            g2d.rotate(Math.toRadians(clone.rotation), centerX, centerY);
            
            g2d.drawImage(clone.image, clone.x, clone.y, clone.width, clone.height, null);
            g2d.setTransform(oldTransform);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw power food plus
        for (PowerFoodPlus plus : powerFoodsPlus) {
            g.drawImage(plus.image, plus.x, plus.y, plus.width, plus.height, null);
        }
        
        // Draw all active traps
        for (Trap trap : traps) {
            trap.draw(g);
        }

        // Draw score board and ghost status info
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        
        if (gameOver) {
            // Draw game over screen with final score
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, boardWidth, boardHeight);
            
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String gameOverText = "GAME OVER";
            int gameOverWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, (boardWidth - gameOverWidth) / 2, boardHeight / 2 - 50);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String finalScore = "Final Score: " + String.valueOf(score);
            int finalScoreWidth = g.getFontMetrics().stringWidth(finalScore);
            g.drawString(finalScore, (boardWidth - finalScoreWidth) / 2, boardHeight / 2 + 20);
            
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            String pressSpaceAgain = "Press SPACE to play again";
            int pressSpaceAgainWidth = g.getFontMetrics().stringWidth(pressSpaceAgain);
            g.drawString(pressSpaceAgain, (boardWidth - pressSpaceAgainWidth) / 2, boardHeight / 2 + 60);
            
            return; // Exit draw method early
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        
        // Draw operation HUD in top-left corner
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Controls:", tileSize/2, tileSize + tileSize/2);
        g.drawString("Arrow Keys: Move", tileSize/2, tileSize + tileSize);
        g.drawString("Q: Fire Clone", tileSize/2, tileSize + tileSize + tileSize/2);
        
        // Draw skill HUD
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Skills: " + powerFoodPlusSkillCount, tileSize/2, tileSize + tileSize*2);

        // Draw active trap effects in center of screen
        if (activeTrapEffect != "") {
            long elapsedTime = System.currentTimeMillis() - trapEffectStartTime;
            long remainingTime = trapEffectDuration - elapsedTime;
            long seconds = remainingTime / 1000;
            
            // Draw semi-transparent background
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(boardWidth/2 - 150, boardHeight/2 - 50, 300, 100);
            
            // Draw effect text with appropriate color
            if (activeTrapEffect.equals("ENTANGLED!")) {
                g.setColor(Color.GREEN);
            } else if (activeTrapEffect.equals("FROZEN!")) {
                g.setColor(Color.BLUE);
            }
            
            g.setFont(new Font("Arial", Font.BOLD, 36));
            int textWidth = g.getFontMetrics().stringWidth(activeTrapEffect);
            g.drawString(activeTrapEffect, boardWidth/2 - textWidth/2, boardHeight/2 - 10);
            
            // Draw countdown
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String countdown = String.valueOf(seconds);
            textWidth = g.getFontMetrics().stringWidth(countdown);
            g.drawString(countdown, boardWidth/2 - textWidth/2, boardHeight/2 + 40);
            
            g2d.dispose();
        }
        
        // Draw ghost status information
        int statusY = boardHeight - 80;
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("orangeGhost: " + getGhostStatus("orange"), 20, statusY);
        g.drawString("pinkGhost: " + getGhostStatus("pink"), 20, statusY + 20);
        g.drawString("scaredGhost: " + getGhostStatus("scared"), 20, statusY + 40);
        g.drawString("redGhost: " + getGhostStatus("red"), 20, statusY + 60);
    }

    /**
     * Gets the current status string for a specific ghost type
     * 
     * @param ghostType Type of ghost to check ("orange", "pink", "scared", "red")
     * @return Status string showing current state and remaining time for effects
     *         or respawn
     */
    private String getGhostStatus(String ghostType) {
        for (Block ghost : ghosts) {
            if (ghostMatches(ghost, ghostType)) {
                if (ghostScaredMap.containsKey(ghost)) {
                    GhostScaredStatus status = ghostScaredMap.get(ghost);
                    if (status.isActive()) {
                        long remaining = status.getRemainingTime();
                        return (remaining / 1000) + "s"; // Show scared time remaining in seconds
                    } else {
                        ghostScaredMap.remove(ghost);
                    }
                }
                
                // Add shield count for pink ghosts
                if (ghostType.equals("pink") && pinkGhostShields.containsKey(ghost)) {
                    return "Shield x" + pinkGhostShields.get(ghost);
                }
                
                return "Disembodied"; // Normal ghost state if no special effects
            }
        }
        
        // Check respawn timers for this ghost type
        for (Block ghost : ghostRespawnTimers.keySet()) {
            if (ghostMatches(ghost, ghostType)) {
                long respawnTime = ghostRespawnTimers.get(ghost);
                long remainingTime = 30000 - (System.currentTimeMillis() - respawnTime);
                if (remainingTime > 0) {
                    return "Respawning: " + (remainingTime / 1000) + "s"; // Show respawn countdown
                }
            }
        }
        
        // Fallback case for dead ghosts not yet added to timers
        return "Respawning: 30s"; // Default to 30 second respawn timer
    }

    /**
     * Checks if a ghost entity matches a specified type
     * 
     * @param ghost The ghost block entity to check
     * @param type  The type of ghost to match ("orange", "pink", "red", "blue", "scared")
     * @return true if the ghost matches the specified type
     */
    private boolean ghostMatches(Block ghost, String type) {
        if (type.equals("orange") && ghost.image == orangeGhostImage) return true;
        if (type.equals("pink") && ghost.image == pinkGhostImage) return true;
        if (type.equals("red") && ghost.image == redGhostImage) return true;
        if (type.equals("blue") && ghost.image == blueGhostImage) return true;
        if (type.equals("scared") && ghost.image == blueGhostImage) return true;
        return false;
    }

    /**
     * Moves Pac-Man and handles collision detection
     * 
     * This method updates Pac-Man's position based on current velocity,
     * handles portal transitions (left-right wrap), and detects collisions
     * with walls and other game elements.
     */
    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Portal system: middle row, left and right ends
        if (pacman.y >= tileSize*8 && pacman.y <= tileSize*10) { // Middle area
            if (pacman.x <= 0) { // Left portal - wrap to right
                pacman.x = boardWidth - pacman.width;
            } else if (pacman.x + pacman.width >= boardWidth) { // Right portal - wrap to left
                pacman.x = 0;
            }
        }

        // Check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Check ghost collisions
        Block ghostToRemove = null; // Track which ghost needs to be removed from the game field
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (ghostScaredMap.containsKey(ghost) && ghostScaredMap.get(ghost).isActive()) {
                    // Handle special ghost death effects based on ghost type
                    if (ghostMatches(ghost, "orange")) {
                        // Orange ghost: create spider silk trap
                        Trap spiderTrap = new Trap(ghost.x, ghost.y, 10000, "spider");
                        traps.add(spiderTrap);
                    } else if (ghostMatches(ghost, "blue")) {
                        // Blue ghost: create ice shadow trap
                        Trap iceTrap = new Trap(ghost.x, ghost.y, 10000, "ice");
                        traps.add(iceTrap);
                    }
                    
                    // Add ghost to respawn timer
                    ghostRespawnTimers.put(ghost, System.currentTimeMillis());
                    
                    // Generate additional traps based on ghost type
                    if (ghost.image == orangeGhostImage) {
                        // Orange ghost: generate spider silk trap
                        Trap silkTrap = new Trap(ghost.x, ghost.y, 10000, "spider");
                        traps.add(silkTrap);
                    } else if (ghost.image == blueGhostImage) {
                        // Blue ghost: generate ice shadow trap
                        Trap iceTrap = new Trap(ghost.x, ghost.y, 10000, "ice");
                        traps.add(iceTrap);
                    }
                    
                    ghostToRemove = ghost; // Mark ghost for removal
                    ghostScaredMap.remove(ghost); // Remove scared status
                    score += 300; // Award bonus score for eating scared ghost
                } else {
                    // Pac-Man collided with normal ghost - lose a life
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true; // Game ends if no lives remain
                        return;
                    }
                    resetPositions(); // Reset all game entity positions
                }
                break; // Stop checking for more ghost collisions
            }

            // Force ghost to move up when in the portal area and not already moving vertically
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            // Portal system for ghosts: middle row, left and right ends
            if (ghost.y >= tileSize*8 && ghost.y <= tileSize*10) { // Middle area
                if (ghost.x <= 0) { // Left portal
                    ghost.x = boardWidth - ghost.width;
                } else if (ghost.x + ghost.width >= boardWidth) { // Right portal
                    ghost.x = 0;
                }
            }
            
            // Handle red ghost special teleportation ability
            if (ghostMatches(ghost, "red")) {
                long currentTime = System.currentTimeMillis();
                // Check teleport cooldown (10 second minimum delay)
                if (!redGhostTeleportCooldown.containsKey(ghost) || currentTime - redGhostTeleportCooldown.get(ghost) > 10000) {
                    if (random.nextDouble() < 0.01) { // 1% chance per frame to teleport
                        // Teleport to a random food position
                        java.util.List<Block> foodList = new java.util.ArrayList<>(foods);
                        if (foodList.size() > 0) {
                            int randomIndex = random.nextInt(foodList.size());
                            Block randomFood = foodList.get(randomIndex);
                            ghost.x = randomFood.x - (tileSize / 2) + 2;
                            ghost.y = randomFood.y - (tileSize / 2) + 2;
                            redGhostTeleportCooldown.put(ghost, currentTime); // Reset cooldown timer
                        }
                    }
                }
            }
            
            // Apply speed modifiers from status effects
            double speedMultiplier = 1.0; // Default speed multiplier
            // Clean up inactive frozen status for ghost
            if (frozenEntities.containsKey(ghost) && !frozenEntities.get(ghost).isActive()) {
                frozenEntities.remove(ghost);
            }
            // Apply frozen status effect
            if (frozenEntities.containsKey(ghost) && frozenEntities.get(ghost).isActive()) {
                speedMultiplier = frozenEntities.get(ghost).getSpeedMultiplier();
            }
            
            // Clean up inactive entangled status for ghost
            if (entangledEntities.containsKey(ghost) && !entangledEntities.get(ghost).isActive()) {
                entangledEntities.remove(ghost);
            }
            // Apply entanglement effect to ghost (completely stops movement)
            if (entangledEntities.containsKey(ghost) && entangledEntities.get(ghost).isActive()) {
                ghost.velocityX = 0;
                ghost.velocityY = 0;
                continue; // Skip further movement logic for this frame
            }
            
            // Move ghost with applied speed modifiers
            ghost.x += (int)(ghost.velocityX * speedMultiplier);
            ghost.y += (int)(ghost.velocityY * speedMultiplier);
            
            // Check ghost wall collisions and change direction on impact
            for (Block wall : walls) {
                if (collision(ghost, wall) || 
                    (ghost.x <= 0 && !(ghost.y >= tileSize*8 && ghost.y <= tileSize*10)) || 
                    (ghost.x + ghost.width >= boardWidth && !(ghost.y >= tileSize*8 && ghost.y <= tileSize*10))) {
                    // Reverse ghost movement due to collision
                    ghost.x -= (int)(ghost.velocityX * speedMultiplier);
                    ghost.y -= (int)(ghost.velocityY * speedMultiplier);
                    // Set random new direction to navigate around obstacle
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // Remove ghost from field if marked for removal (after eating)
        if (ghostToRemove != null) {
            ghosts.remove(ghostToRemove);
        }

        // Handle clone movement and collision detection
        PacmanClone cloneToRemove = null; // Track which clone needs to be removed
        for (PacmanClone clone : pacmanClones) {
            clone.move();

            // Check clone collision with walls
            for (Block wall : walls) {
                if (collision(clone, wall)) {
                    cloneToRemove = clone; // Mark clone for removal on wall hit
                    break;
                }
            }

            // Check clone collision with ghosts if not already marked for removal
            if (cloneToRemove == null) {
                for (Block ghost : ghosts) {
                    if (collision(clone, ghost)) {
                        // Handle pink ghost shields when hit by clone
                        if (ghostMatches(ghost, "pink")) {
                            if (pinkGhostShields.containsKey(ghost)) {
                                int shields = pinkGhostShields.get(ghost);
                                shields--; // Reduce shield count
                                if (shields > 0) {
                                    // Still shields remaining - update count
                                    pinkGhostShields.put(ghost, shields);
                                    cloneToRemove = clone; // Clone breaks shield and is removed
                                    break;
                                } else {
                                    // All shields broken - remove shield and scare ghost
                                    pinkGhostShields.remove(ghost);
                                    ghostScaredMap.put(ghost, new GhostScaredStatus());
                                    cloneToRemove = clone; // Clone is removed after breaking all shields
                                    break;
                                }
                            }
                        }
                        // For other ghosts or pink ghosts with no shields - scare the ghost
                        ghostScaredMap.put(ghost, new GhostScaredStatus());
                        cloneToRemove = clone; // Clone is removed after hitting ghost
                        break;
                    }
                }
            }

            // Check boundary collision
            if (cloneToRemove == null && (clone.x < 0 || clone.x + clone.width > boardWidth ||
                    clone.y < 0 || clone.y + clone.height > boardHeight)) {
                cloneToRemove = clone;
            }
        }
        if (cloneToRemove != null) {
            pacmanClones.remove(cloneToRemove);
        }

        // Remove the ghost if killed
        if (ghostToRemove != null) {
            ghosts.remove(ghostToRemove);
        }
        
        // Handle trap effects
        for (Trap trap : traps) {
            if (!trap.isActive()) continue;
            
            // Check if pacman is in trap
            if (collision(pacman, trap)) {
                if (trap.type.equals("spider")) {
                    // Spider trap: entangle pacman for 3 seconds
                    if (!entangledEntities.containsKey(pacman) || !entangledEntities.get(pacman).isActive()) {
                        EntangledStatus entangled = new EntangledStatus(3000);
                        entangledEntities.put(pacman, entangled);
                        activeTrapEffect = "ENTANGLED!";
                        trapEffectStartTime = System.currentTimeMillis();
                        trapEffectDuration = 3000;
                    }
                } else if (trap.type.equals("ice")) {
                    // Ice trap: freeze pacman for 7 seconds
                    if (!frozenEntities.containsKey(pacman) || !frozenEntities.get(pacman).isActive()) {
                        FrozenStatus frozen = new FrozenStatus(7000);
                        frozenEntities.put(pacman, frozen);
                        activeTrapEffect = "FROZEN!";
                        trapEffectStartTime = System.currentTimeMillis();
                        trapEffectDuration = 7000;
                    }
                }
            }
            
            // Check if ghosts are in trap
            for (Block ghost : ghosts) {
                if (collision(ghost, trap)) {
                    if (trap.type.equals("spider")) {
                        // Spider trap: entangle ghost for 3 seconds
                        if (!entangledEntities.containsKey(ghost) || !entangledEntities.get(ghost).isActive()) {
                            EntangledStatus entangled = new EntangledStatus(3000);
                            entangledEntities.put(ghost, entangled);
                        }
                    } else if (trap.type.equals("ice")) {
                        // Ice trap: freeze ghost for 7 seconds
                        if (!frozenEntities.containsKey(ghost) || !frozenEntities.get(ghost).isActive()) {
                            FrozenStatus frozen = new FrozenStatus(7000);
                            frozenEntities.put(ghost, frozen);
                        }
                    }
                }
            }
        }
        
        // Clean up inactive trap statuses for pacman to prevent permanent movement issues
        if (entangledEntities.containsKey(pacman) && !entangledEntities.get(pacman).isActive()) {
            entangledEntities.remove(pacman);
        }
        if (frozenEntities.containsKey(pacman) && !frozenEntities.get(pacman).isActive()) {
            frozenEntities.remove(pacman);
            // Reset pacman's velocity after frozen status ends to restore normal movement
            pacman.updateVelocity();
        }
        // Clean up inactive trap statuses for ghosts
        for (Block ghost : ghosts) {
            if (entangledEntities.containsKey(ghost) && !entangledEntities.get(ghost).isActive()) {
                entangledEntities.remove(ghost);
            }
            if (frozenEntities.containsKey(ghost) && !frozenEntities.get(ghost).isActive()) {
                frozenEntities.remove(ghost);
            }
        }
        
        // Apply entanglement effect to pacman first (highest priority)
        if (entangledEntities.containsKey(pacman) && entangledEntities.get(pacman).isActive()) {
            // Stop pacman movement completely
            pacman.velocityX = 0;
            pacman.velocityY = 0;
        } else {
            // Apply frozen effect only if not entangled
            if (frozenEntities.containsKey(pacman) && frozenEntities.get(pacman).isActive()) {
                // Reduce pacman speed by 50%
                double speedMultiplier = frozenEntities.get(pacman).getSpeedMultiplier();
                // Save current direction to restore after speed modification
                char currentDirection = pacman.direction;
                // Calculate new velocity
                int baseVelocity = tileSize / 4;
                if (currentDirection == 'U') {
                    pacman.velocityX = 0;
                    pacman.velocityY = (int)(-baseVelocity * speedMultiplier);
                } else if (currentDirection == 'D') {
                    pacman.velocityX = 0;
                    pacman.velocityY = (int)(baseVelocity * speedMultiplier);
                } else if (currentDirection == 'L') {
                    pacman.velocityX = (int)(-baseVelocity * speedMultiplier);
                    pacman.velocityY = 0;
                } else if (currentDirection == 'R') {
                    pacman.velocityX = (int)(baseVelocity * speedMultiplier);
                    pacman.velocityY = 0;
                }
            }
        }
        
        // Update trap effect UI
        if (activeTrapEffect != "" && System.currentTimeMillis() - trapEffectStartTime > trapEffectDuration) {
            activeTrapEffect = "";
        }
        
        // Remove inactive traps
        java.util.Iterator<Trap> trapIterator = traps.iterator();
        while (trapIterator.hasNext()) {
            Trap trap = trapIterator.next();
            if (!trap.isActive()) {
                trapIterator.remove();
            }
        }
        
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Check power food plus collision
        PowerFoodPlus plusEaten = null;
        for (PowerFoodPlus plus : powerFoodsPlus) {
            if (collision(pacman, plus)) {
                plusEaten = plus;
                powerFoodPlusSkillCount++;
                score += 10; 
            }
        }
        powerFoodsPlus.remove(plusEaten);

        if (foods.isEmpty() && powerFoodsPlus.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean collision(PacmanClone clone, Block block) {
        return  clone.x < block.x + block.width &&
                clone.x + clone.width > block.x &&
                clone.y < block.y + block.height &&
                clone.y + clone.height > block.y;
    }

    private boolean collision(Block block, PowerFoodPlus plus) {
        return  block.x < plus.x + plus.width &&
                block.x + block.width > plus.x &&
                block.y < plus.y + plus.height &&
                block.y + block.height > plus.y;
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }
    
    /**
     * Detects collision between Block and Trap
     * @param block Block object (Pacman or Ghost)
     * @param trap Trap object
     * @return Returns true if collision occurs, false otherwise
     */
    public boolean collision(Block block, Trap trap) {
        return  block.x < trap.x + trap.width &&
                block.x + block.width > trap.x &&
                block.y < trap.y + trap.height &&
                block.y + block.height > trap.y;
    }
    
    /**
     * Detects collision between Trap and Block (for code completeness)
     * @param trap Trap object
     * @param block Block object (Pacman or Ghost)
     * @return Returns true if collision occurs, false otherwise
     */
    public boolean collision(Trap trap, Block block) {
        return collision(block, trap);
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        pacmanClones.clear();
        powerFoodPlusSkillCount = 0;
        
        for (Block ghost : ghosts) {
            ghost.reset();
            ghostScaredMap.remove(ghost);
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        
        // Check for ghost respawns
        checkGhostRespawns();
        
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }
    
    /**
     * Checks and handles respawning of killed ghosts
     * Killed ghosts will respawn after 30 seconds
     */
    private void checkGhostRespawns() {
        java.util.Iterator<java.util.Map.Entry<Block, Long>> iterator = ghostRespawnTimers.entrySet().iterator();
        long currentTime = System.currentTimeMillis();
        
        while (iterator.hasNext()) {
            java.util.Map.Entry<Block, Long> entry = iterator.next();
            Block ghost = entry.getKey();
            Long respawnTime = entry.getValue();
            
            // Check if 30 seconds have passed since death
            if (currentTime - respawnTime > 30000) {
                // Respawn the ghost
                ghosts.add(ghost);
                iterator.remove(); // Remove from respawn timers
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        // Handle game start with space key
        if (!gameStarted) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                gameStarted = true;
            }
            return; // Exit early if game hasn't started
        }
        
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        
        // Entanglement trap effect: blocks player input for up/down/left/right movement commands
        boolean isEntangled = entangledEntities.containsKey(pacman) && entangledEntities.get(pacman).isActive();
        if (isEntangled) {
            // Allow skill key and other non-movement keys, but disable up/down/left/right arrow keys
            if (e.getKeyCode() == KeyEvent.VK_Q && powerFoodPlusSkillCount > 0) {
                Image cloneImage = null;
                if (pacman.direction == 'U') {
                    cloneImage = pacmanUpImage;
                } else if (pacman.direction == 'D') {
                    cloneImage = pacmanDownImage;
                } else if (pacman.direction == 'L') {
                    cloneImage = pacmanLeftImage;
                } else if (pacman.direction == 'R') {
                    cloneImage = pacmanRightImage;
                }
                
                PacmanClone clone = new PacmanClone(pacman.x, pacman.y, pacman.direction, cloneImage);
                pacmanClones.add(clone);
                powerFoodPlusSkillCount--; // Reduce skill count when used
            }
            return; // 阻止其他按键处理
        }
        
        // 正常移动输入处理
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }
        else if (e.getKeyCode() == KeyEvent.VK_Q && powerFoodPlusSkillCount > 0) {
            Image cloneImage = null;
            if (pacman.direction == 'U') {
                cloneImage = pacmanUpImage;
            } else if (pacman.direction == 'D') {
                cloneImage = pacmanDownImage;
            } else if (pacman.direction == 'L') {
                cloneImage = pacmanLeftImage;
            } else if (pacman.direction == 'R') {
                cloneImage = pacmanRightImage;
            }
            
            PacmanClone clone = new PacmanClone(pacman.x, pacman.y, pacman.direction, cloneImage);
            pacmanClones.add(clone);
            powerFoodPlusSkillCount--; // Reduce skill count when used
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
}
