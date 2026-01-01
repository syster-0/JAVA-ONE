package pacman;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Pac-Man Main Character Class
 * Controls Pac-Man's movement, eating, state changes, etc.
 * Implements core gameplay mechanics for the player character
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PacMan extends Entity {
    private int lives;
    private int score;
    private Direction currentDirection;
    private Direction nextDirection;
    private boolean isPowerMode;
    private long powerModeTimer;
    private double mouthOpenAngle;
    private double mouthClosingSpeed;
    private boolean mouthOpening;
    private Image currentImage;
    private Image upImage;
    private Image downImage;
    private Image leftImage;
    private Image rightImage;
    private double speed;
    private double originalSpeed;
    private long freezeEndTime;
    // Pac-Man specific attributes
    private boolean isFrozen;

    // Starting position
    private double startX;
    private double startY;

    // Skill count
    private int powerFoodPlusSkillCount;

    /**
     * Constructor
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param width Width
     * @param height Height
     * @param startDirection Initial direction
     */
    public PacMan(double x, double y, double width, double height, Direction startDirection) {
        super(x, y, width, height, startDirection, null);
        this.startX = x;
        this.startY = y;
        this.lives = 3;
        this.score = 0;
        this.currentDirection = startDirection;
        this.nextDirection = startDirection;
        this.isPowerMode = false;
        this.powerModeTimer = 0;
        this.mouthOpenAngle = 30;
        this.mouthClosingSpeed = 5;
        this.mouthOpening = false;
        this.currentImage = null;
        // Set default speed
        this.speed = 2.0; // Modification reason: Reduce Pac-Man speed to synchronize with ghosts
        this.originalSpeed = speed;
        this.freezeEndTime = 0;
        this.isFrozen = false;
        this.powerFoodPlusSkillCount = 0;

        // Load directional images for Pac-Man
        loadDirectionImages();
        updateCurrentImage();
        updateVelocity(); // Initialize velocity
    }

    /**
     * Loads directional images for Pac-Man
     * Loads and initializes all directional sprites for Pac-Man's movement animations
     */
    private void loadDirectionImages() {
        this.upImage = new Image("file:pacmanUp.png");
        this.downImage = new Image("file:pacmanDown.png");
        this.leftImage = new Image("file:pacmanLeft.png");
        this.rightImage = new Image("file:pacmanRight.png");
    }

    /**
     * Updates current image based on direction
     */
    private void updateCurrentImage() {
        switch (getDirection()) {
            case UP:
                setImage(upImage);
                break;
            case DOWN:
                setImage(downImage);
                break;
            case LEFT:
                setImage(leftImage);
                break;
            case RIGHT:
                setImage(rightImage);
                break;
            default:
                setImage(leftImage);
                break;
        }
    }

    @Override
    protected void updateVelocity() {
        setVelocityX(0);
        setVelocityY(0);
        switch (currentDirection) {
            case UP:
                setVelocityY(-speed);
                break;
            case DOWN:
                setVelocityY(speed);
                break;
            case LEFT:
                setVelocityX(-speed);
                break;
            case RIGHT:
                setVelocityX(speed);
                break;
        }
    }

    /**
     * Updates Pac-Man state
     * Controls movement, collision detection, and state transitions
     * @param deltaTime Time increment in milliseconds
     */
    @Override
    public void update(long deltaTime) {
        // Handle frozen state
        if (isFrozen) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= freezeEndTime) {
                isFrozen = false;
            }
        }
        // Can add additional per-frame update logic here, such as power mode timer management
        if (isPowerMode) {
            powerModeTimer += deltaTime;
            // Add power mode end condition checks here
        }
    }

    /**
     * Handles boundary crossing logic for Pac-Man
     * Implements screen wrap-around functionality for Pac-Man to move between maze edges
     */
    public void handleBoundaryCrossing() {
        // Left-right screen wrap-around for maze edges
        if (getX() + getWidth() < 0) {
            setX(20 * 32 - getWidth());
        } else if (getX() > 20 * 32 - getWidth()) {
            setX(0);
        }

        // Vertical boundary restrictions (top and bottom limits)
        if (getY() < 0) {
            setY(0);
        } else if (getY() >= PacManApp.GRID_HEIGHT * PacManApp.TILE_SIZE) {
            setY(PacManApp.GRID_HEIGHT * PacManApp.TILE_SIZE - PacManApp.TILE_SIZE);
        }
    }

    /**
     * Freezes Pac-Man
     */
    /**
     * Sets Pac-Man's frozen state
     * Freezes Pac-Man for 2 seconds by default
     */
    public void freeze() {
        isFrozen = true;
        freezeEndTime = System.currentTimeMillis() + 2000; // Freeze for 2 seconds
    }

    /**
     * Checks if Pac-Man is frozen
     * @return true if frozen, otherwise false
     */
    public boolean isFrozen() {
        return isFrozen;
    }

    /**
     * Sets Pac-Man's moving direction
     * Enhanced direction update logic with early wall collision detection to maintain player input responsiveness
     * @param direction New direction for Pac-Man movement
     * @param walls List of wall objects in game map
     */
    public void updateDirection(Direction direction, List<Wall> walls) {
        if (direction == null) {
            return;
        }
        
        // If can immediately turn to new direction, update right away
        if (canMoveInDirection(direction, walls)) {
            currentDirection = direction;
            nextDirection = direction;
            updateVelocity();
        } else {
            // Otherwise, save as nextDirection and wait for appropriate moment to turn
            nextDirection = direction;
        }
        // Update image direction
        updateCurrentImage();
    }
    
    /**
     * Checks if movement in specified direction is possible
     * @param direction Direction to check
     * @param walls List of wall objects
     * @return true if can move, false otherwise
     */
    private boolean canMoveInDirection(Direction direction, List<Wall> walls) {
        double testX = getX();
        double testY = getY();
        double speed = getMovementSpeed();
        
        // Calculate test position
        switch (direction) {
            case UP:
                testY -= speed;
                break;
            case DOWN:
                testY += speed;
                break;
            case LEFT:
                testX -= speed;
                break;
            case RIGHT:
                testX += speed;
                break;
        }
        
        // Check if colliding with walls
        for (Wall wall : walls) {
            if (testX < wall.getX() + wall.getWidth() &&
                testX + getWidth() > wall.getX() &&
                testY < wall.getY() + wall.getHeight() &&
                testY + getHeight() > wall.getY()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Moves Pac-Man
     * Updates position using velocity components and supports early direction changes
     * Modification reason: Implements smooth movement and direction switching
     */
    public void move() {
        setX(getX() + velocityX);
        setY(getY() + velocityY);
    }
    
    /**
     * Moves Pac-Man with wall collision detection
     * Modification reason: Fixed Pac-Man movement logic to prioritize using nextDirection for turning and correctly handle collisions
     * @param walls List of wall objects
     */
    public void move(List<Wall> walls) {
        // Save current position for rollback
        double oldX = getX();
        double oldY = getY();
        
        // Try to turn using nextDirection
        if (nextDirection != null && nextDirection != currentDirection) {
            if (canMoveInDirection(nextDirection, walls)) {
                currentDirection = nextDirection;
                nextDirection = null; // 转向成功后清除下一个方向
                updateVelocity();
            }
        }
        
        // Only allow movement when Pac-Man is not frozen
        if (!isFrozen) {
            // Move according to velocity
            setX(getX() + velocityX);
            setY(getY() + velocityY);
            
            // Check wall collisions
            boolean collided = false;
            for (Wall wall : walls) {
                if (getX() < wall.getX() + wall.getWidth() &&
                    getX() + getWidth() > wall.getX() &&
                    getY() < wall.getY() + wall.getHeight() &&
                    getY() + getHeight() > wall.getY()) {
                    collided = true;
                    break;
                }
            }
            
            // If collision occurs, rollback to position before collision
            if (collided) {
                setX(oldX);
                setY(oldY);
            }
            
            // Handle boundary crossing (e.g., from one end of map to the other)
            handleBoundaryCrossing();
        }
    }
    
    /**
     * Updates direction (maintains compatibility with old method)
     * @param direction New direction
     */
    public void updateDirection(Direction direction) {
        setDirection(direction);
        updateCurrentImage();
    }

    /**
     * Renders Pac-Man
     * @param gc Graphics context object
     */
    @Override
    public void render(GraphicsContext gc) {
        updateCurrentImage();
        super.render(gc);
    }

    /**
     * Resets Pac-Man to initial state
     */
    @Override
    public void reset() {
        setX(startX);
        setY(startY);
        setDirection(Direction.LEFT);
        updateCurrentImage();
        score = 0;
        lives = 3;
        isPowerMode = false;
        powerModeTimer = 0;
        powerFoodPlusSkillCount = 0;
    }

    /**
     * Eats food
     * @param food Food object to eat
     */
    public void eatFood(Food food) {
        if (food.isVisible()) {
            score += food.getPoints();
            food.setVisible(false);
        }
    }

    /**
     * Eats power food
     * @param powerFood Power food object to eat
     */
    public void eatPowerFood(PowerFood powerFood) {
        if (powerFood.isVisible()) {
            score += powerFood.getPoints();
            powerFood.setVisible(false);
            // Modification: Removed power food's ability to activate PowerMode
            // Only clones hitting ghosts will trigger scared state
        }
    }

    /**
     * Pac-Man dies
     */
    public void die() {
        lives--;
        // Reset position etc.
        setX(startX);
        setY(startY);
        setDirection(Direction.LEFT);
        updateCurrentImage();
    }

    // Getters and Setters

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public boolean isPowerMode() {
        return isPowerMode;
    }

    public void setPowerMode(boolean isPowerMode) {
        this.isPowerMode = isPowerMode;
    }

    // Getters and setters for PowerModeTimer
    public long getPowerModeTimer() {
        return powerModeTimer;
    }
    public void setPowerModeTimer(long timer) {
        this.powerModeTimer = timer;
    }
    public void setPowerModeTimer(int timer) {
        this.powerModeTimer = timer;
    }

    public int getPowerFoodPlusSkillCount() {
        return powerFoodPlusSkillCount;
    }

    public void setPowerFoodPlusSkillCount(int count) {
        this.powerFoodPlusSkillCount = count;
    }

    public void incrementSkillCount() {
        this.powerFoodPlusSkillCount++;
    }

    // Methods to get directional images
    public Image getUpImage() {
        return upImage;
    }

    public Image getDownImage() {
        return downImage;
    }

    public Image getLeftImage() {
        return leftImage;
    }

    public Image getRightImage() {
        return rightImage;
    }
}
