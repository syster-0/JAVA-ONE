package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

import static pacman.PacManApp.TILE_SIZE;

/**
 * Blue Ghost - Inky
 * Features: Normal state is blue, turns blue scared ghost in scared mode (when power food is eaten)
 *           Special Ability: Creates Ice Shadow Trap when killed, Pacman freezes when stepping on it
 *
 * @author PacMan Development Team
 * @version 4.0 - Implemented user new requirement: Creates trap when killed
 * @since 2024
 */
public class BlueGhost extends Ghost {
    private static final double DEFAULT_SPEED = 1.5; // Modify reason: Match reference
    private boolean isIceTrapSet = false; // Whether ice trap is set
    private double iceTrapX, iceTrapY; // Ice trap position
    private boolean isFrozen = false; // New: Whether frozen
    private long freezeTimer = 0; // New: Freeze timer
    private static final long FREEZE_DURATION = 3000; // New: Freeze duration
    private long trapDurationTimer = 0; // New: Ice trap duration timer
    private static final long TRAP_DURATION = 10000; // New: Ice trap duration (10 seconds)
    private long respawnTimer = 0; // New: Respawn timer
    private static final long RESPAWN_TIME = 30000; // New: Respawn time (30 seconds)
    private boolean isDead = false; // New: Whether dead
    private long deathTime = 0; // New: Death time

    /**
     * Constructor
     *
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param image Image resource
     */
    public BlueGhost(double x, double y, Image image) {
        super(x, y, TILE_SIZE, TILE_SIZE, getRandomDirection(), image);
        setMovementSpeed(DEFAULT_SPEED);
        this.isIceTrapSet = false;
        this.iceTrapX = 0;
        this.iceTrapY = 0;
        this.isFrozen = false;
        this.freezeTimer = 0;
        this.trapDurationTimer = 0;
        this.isDead = false; // Modification: Explicitly initialize death status to false
        this.deathTime = 0; // Modification: Initialize death time to 0 to avoid misjudgment
        this.respawnTimer = 0;
    }

    /**
     * Get random direction
     * @return Random direction
     */
    private static Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[(int)(Math.random() * directions.length)];
    }

    /**
     * Use special ability - Modified: Disable normal trap, only create ice trap when killed
     */
    @Override
    public void useSpecialAbility() {
        // Empty implementation: According to new requirements, blue ghost no longer sets traps actively
        // Trap only created when killed
    }

    /**
     * Mark ghost as dead, create ice trap
     * Modify reason: Implement user new requirement, create trap after death
     */
    public void die() {
        isDead = true;
        deathTime = System.currentTimeMillis();
        
        // Create ice trap after death
        isIceTrapSet = true;
        iceTrapX = getX();
        iceTrapY = getY();
        trapDurationTimer = TRAP_DURATION;
    }
    
    /**
     * Respawn ghost
     * Modify reason: Implement 30-second respawn logic
     */
    private void respawn() {
        isDead = false;
        reset();
    }

    /**
     * Break ice method - New: Used to break frozen trap
     * Function: Break frozen state at current position
     */
    public void breakIce() {
        isFrozen = false;
        freezeTimer = 0;
    }

    /**
     * Mark as frozen
     */
    public void freeze() {
        isFrozen = true;
        freezeTimer = System.currentTimeMillis();
    }

    /**
     * Update blue ghost state
     * Modify: Remove normal trap update, add frozen state handling, add death respawn and trap timeout logic
     * @param deltaTime Time increment (milliseconds)
     * @param pacmanX Pac-Man's X coordinate
     * @param pacmanY Pac-Man's Y coordinate
     * @param walls Wall object list
     * @param ghosts All ghost object list
     */
    @Override
    public void update(long deltaTime, double pacmanX, double pacmanY, List<Wall> walls, List<Ghost> ghosts) {
        if (isDead) {
            // Death state: Respawn timer
            if (System.currentTimeMillis() - deathTime >= RESPAWN_TIME) {
                // Respawn
                respawn();
            }
            return;
        }
        
        // Check frozen state
        if (isFrozen) {
            if (System.currentTimeMillis() - freezeTimer >= FREEZE_DURATION) {
                breakIce();
            } else {
                return; // Can't move when frozen
            }
        }
        
        this.pacmanX = pacmanX;
        this.pacmanY = pacmanY;
        super.update(deltaTime, pacmanX, pacmanY, walls, ghosts);
        
        // Update ice trap duration
        if (isIceTrapSet) {
            trapDurationTimer -= deltaTime;
            if (trapDurationTimer <= 0) {
                removeIceTrap();
            }
        }
    }

    /**
     * Remove ice trap
     * Modify reason: Update method name to match new ice trap concept
     */
    public void removeIceTrap() {
        isIceTrapSet = false;
        iceTrapX = 0;
        iceTrapY = 0;
    }

    /**
     * Make AI decision
     * Modify: Match reference, basic Pac-Man tracking behavior
     * @return Next movement direction
     */
    @Override
    protected Direction makeAIDecision() {
        double currentX = getX();
        double currentY = getY();
        
        // Scared state: Escape from Pac-Man
        if (isScared()) {
            double deltaX = pacmanX - currentX;
            double deltaY = pacmanY - currentY;
            
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                return deltaX > 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                return deltaY > 0 ? Direction.UP : Direction.DOWN;
            }
        }
        
        // Normal state: Chase Pac-Man
        double deltaX = pacmanX - currentX;
        double deltaY = pacmanY - currentY;
        
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return deltaY > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * Render blue ghost and ice shadow trap
     * Modify: Add death state rendering
     * @param gc Graphics context object
     */
    @Override
    public void render(GraphicsContext gc) {
        if (!isDead) {
            // Render ice shadow trap
            if (isIceTrapSet) {
                gc.setFill(Color.CYAN);
                gc.fillRect(iceTrapX, iceTrapY, TILE_SIZE, TILE_SIZE);
            }
            
            // Render frozen state
            if (isFrozen) {
                gc.setFill(Color.LIGHTBLUE);
                gc.fillOval(getX(), getY(), getWidth(), getHeight());
            } else if (isScared()) {
                // Render scared state
                gc.setFill(Color.BLUE);
                gc.fillOval(getX(), getY(), getWidth(), getHeight());
            } else {
                // Normal rendering
                super.render(gc);
            }
        } else {
            // Death state: Show respawn countdown
            long remainingTime = RESPAWN_TIME - (System.currentTimeMillis() - deathTime);
            int secondsLeft = (int) (remainingTime / 1000) + 1;
            gc.setFill(Color.WHITE);
            gc.fillText("Died - Respawn in: " + secondsLeft + "s", getX(), getY() - 10);
        }
    }

    /**
     * Reset BlueGhost to initial state
     * Modify: Update reset logic to fit death and respawn concept
     */
    @Override
    public void reset() {
        setX(9 * 32);
        setY(12 * 32);
        setDirection(getRandomDirection());
        setMovementSpeed(DEFAULT_SPEED);
        isIceTrapSet = false;
        iceTrapX = 0;
        iceTrapY = 0;
        isFrozen = false;
        freezeTimer = 0;
        trapDurationTimer = 0;
        isDead = false;
        deathTime = 0;
    }
    
    /**
     * Check if dead
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
    
    // Getters 和 Setters

    public void setIceTrapSet(boolean trapSet) {
        isIceTrapSet = trapSet;
    }
    
    public boolean isIceTrapSet() {
        return isIceTrapSet;
    }

    public double getIceTrapX() {
        return iceTrapX;
    }

    public double getIceTrapY() {
        return iceTrapY;
    }

    public boolean isFrozen() {
        return isFrozen;
    }
}
