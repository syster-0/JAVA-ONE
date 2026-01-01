package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

import static pacman.PacManApp.TILE_SIZE;

/**
 * Orange Ghost - Clyde
 * Special Ability: Leave Silk Trap on death, lasting 10 seconds
 * 
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class OrangeGhost extends Ghost {
    private static final double DEFAULT_SPEED = 2.0; // Default speed for orange ghost
    private long silkTrapCooldown = 0; // Silk trap cooldown timer
    private static final long SILK_TRAP_COOLDOWN = 15000; // 15 seconds
    private boolean isDead = false; // Whether dead
    private long deathTime = 0; // Death time
    private double trapX = -1; // Trap X coordinate
    private double trapY = -1; // Trap Y coordinate
    private long trapEndTime = 0; // Trap end time
    private static final long TRAP_DURATION = 10000; // Trap duration (10 seconds)

    /**
     * Constructor
     *
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param image Image resource
     */
    public OrangeGhost(double x, double y, Image image) {
        super(x, y, TILE_SIZE, TILE_SIZE, getRandomDirection(), image);
        setMovementSpeed(DEFAULT_SPEED);
        this.isDead = false;
        this.deathTime = 0;
        this.silkTrapCooldown = 0;
    }

    /**
     * Use special ability - Set silk trap
     */
    @Override
    public void useSpecialAbility() {
        setSilkTrap();
    }

    /**
     * Update orange ghost state
     * Includes trap generation and respawn logic
     * Modify reason: Keep signature consistent with parent update method, add ghosts parameter for collision detection
     * @param deltaTime Time increment (milliseconds)
     * @param pacmanX Pac-Man X coordinate
     * @param pacmanY Pac-Man Y coordinate
     * @param walls Wall object list
     * @param ghosts All ghosts object list
     */
    @Override
    public void update(long deltaTime, double pacmanX, double pacmanY, List<Wall> walls, List<Ghost> ghosts) {
        if (isDead) {
            // Death state: Respawn timer (30 seconds)
            if (System.currentTimeMillis() - deathTime >= 30000) {
                respawn();
            }
            return;
        }

        this.pacmanX = pacmanX; // Update Pac-Man X
        this.pacmanY = pacmanY; // Update Pac-Man Y
        
        silkTrapCooldown -= deltaTime;
        
        // Use special ability when cooldown ends
        if (silkTrapCooldown <= 0) {
            useSpecialAbility();
            silkTrapCooldown = SILK_TRAP_COOLDOWN;
        }

        super.update(deltaTime, pacmanX, pacmanY, walls, ghosts);
    }

    /**
     * Set silk trap
     */
    private void setSilkTrap() {
        trapX = getX();
        trapY = getY();
        trapEndTime = System.currentTimeMillis() + TRAP_DURATION;
    }

    /**
     * Mark ghost as dead
     */
    public void die() {
        isDead = true;
        deathTime = System.currentTimeMillis();
        // Leave silk trap at death position
        setSilkTrap();
    }

    /**
     * Respawn ghost
     */
    private void respawn() {
        isDead = false;
        reset();
    }

    /**
     * Make AI decision
     * Pursue Pac-Man actively, and sets silk trap to limit his movement
     * Modify reason: Add scared state handling - flee from Pac-Man when scared
     * @return Next movement direction
     */
    @Override
    public Direction makeAIDecision() {
        double currentX = getX();
        double currentY = getY();
        
        // Check if scared
        if (isScared()) {
            // Flee from Pac-Man
            double deltaX = pacmanX - currentX;
            double deltaY = pacmanY - currentY;
            
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                return deltaX > 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                return deltaY > 0 ? Direction.UP : Direction.DOWN;
            }
        }

        // Occasional (15% chance) detour for behavioral variety
        if (Math.random() < 0.15) {
            Direction[] directions = Direction.values();
            return directions[(int)(Math.random() * directions.length)];
        }

        // Move towards Pac-Man
        double deltaX = pacmanX - currentX;
        double deltaY = pacmanY - currentY;
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return deltaY > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Render orange ghost and silk trap
     * @param gc Graphics context object
     */
    @Override
    public void render(GraphicsContext gc) {
        if (!isDead) {
            // Render silk trap
            if (System.currentTimeMillis() < trapEndTime) {
                gc.setFill(Color.BURLYWOOD);
                gc.fillOval(trapX, trapY, getWidth(), getHeight());
            }
            
            if (isScared()) {
                // Render as blue when scared
                gc.setFill(Color.BLUE);
                gc.fillOval(getX(), getY(), getWidth(), getHeight());
            } else {
                // Normal rendering
                super.render(gc);
            }
        } else {
            // Death state: Show respawn countdown
            long remainingTime = 30000 - (System.currentTimeMillis() - deathTime);
            int secondsLeft = (int) (remainingTime / 1000) + 1;
            gc.setFill(Color.WHITE);
            gc.fillText("Died - Respawn in: " + secondsLeft + "s", getX(), getY() - 10);
        }
    }

    /**
     * Reset OrangeGhost to initial state
     * Modify reason: Update reset logic to match death respawn concept
     */
    @Override
    public void reset() {
        setX(9 * 32);
        setY(10 * 32);
        setDirection(getRandomDirection());
        setMovementSpeed(DEFAULT_SPEED);
        isDead = false;
        deathTime = 0;
        trapX = -1;
        trapY = -1;
        trapEndTime = 0;
        silkTrapCooldown = SILK_TRAP_COOLDOWN;
    }

    /**
     * Check if trap is currently set
     * @return true if trap is set
     */
    public boolean isTrapSet() {
        return System.currentTimeMillis() < trapEndTime;
    }

    /**
     * Get trap X coordinate
     * @return trap X coordinate
     */
    public double getTrapX() {
        return trapX;
    }

    /**
     * Get trap Y coordinate
     * @return trap Y coordinate
     */
    public double getTrapY() {
        return trapY;
    }

    /**
     * Remove current trap
     */
    public void removeTrap() {
        trapEndTime = 0;
        trapX = -1;
        trapY = -1;
    }

    /**
     * Check if ghost is dead
     * @return Whether dead
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Get death time
     * @return Death timestamp (milliseconds)
     */
    public long getDeathTime() {
        return deathTime;
    }

    /**
     * Get silk trap cooldown
     * @return Cooldown timer (milliseconds)
     */
    public long getSilkTrapCooldown() {
        return silkTrapCooldown;
    }

    /**
     * Get random direction
     * @return Random direction
     */
    private static Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[(int)(Math.random() * directions.length)];  
    }
}
