package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract Base Class for Ghosts
 * Defines common attributes and behaviors for all ghosts
 * Provides AI decision framework and basic movement functionality
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public abstract class Ghost extends Entity {
    // Ghost-specific states
    private boolean scared = false;
    private long scaredTimer = 0;
    private boolean frozen = false;
    private long frozenTimer = 0;
    private boolean entangled = false;
    private long entangledTimer = 0;
    
    // Pac-Man's position information for AI decision-making
    protected double pacmanX = 0;
    protected double pacmanY = 0;

    /**
     * Constructor
     *
     * @param x Initial X coordinate
     * @param y Initial Y coordinate
     * @param width Width
     * @param height Height
     * @param initialDirection Initial direction
     * @param image Image resource
     */
    public Ghost(double x, double y, double width, double height, Direction initialDirection, Image image) {
        super(x, y, width, height, initialDirection, image);
    }

    /**
     * Updates entity state (inherited from Entity abstract class, must implement)
     * @param deltaTime Time increment
     */
    @Override
    public void update(long deltaTime) {
        // 默认实现：不做任何操作，因为我们使用带Pac-Man参数的update方法
    }

    /**
     * Updates ghost state and movement
     * Fully copies ghost movement logic from reference PacMan.java
     * Adds ghost-to-ghost collision detection to solve ghost stuck problem after collision
     * Adds intersection random turning functionality: performs random turning check at each intersection
     * Modification reason: Implements user requirement - ghosts make random turning decisions at intersections
     * @param deltaTime Time increment (milliseconds)
     * @param pacmanX Pac-Man's X coordinate
     * @param pacmanY Pac-Man's Y coordinate
     * @param walls List of wall objects
     * @param ghosts List of all ghost objects (for collision detection)
     */
    public void update(long deltaTime, double pacmanX, double pacmanY, List<Wall> walls, List<Ghost> ghosts) {
        // Save Pac-Man position information
        this.pacmanX = pacmanX;
        this.pacmanY = pacmanY;
        
        // If frozen or entangled, skip movement
        if (isFrozen() || isEntangled()) {
            // Handle special state timers
            if (scared) {
                scaredTimer++;
                if (scaredTimer >= 300) {
                    scared = false;
                    scaredTimer = 0;
                }
            }
            
            if (frozen) {
                frozenTimer++;
                if (frozenTimer >= 100) {
                    frozen = false;
                    frozenTimer = 0;
                }
            }
            
            if (entangled) {
                entangledTimer++;
                if (entangledTimer >= 150) {
                    entangled = false;
                    entangledTimer = 0;
                }
            }
            return;
        }
        
        // Update scared state
        if (isScared()) {
            scaredTimer -= deltaTime;
            if (scaredTimer <= 0) {
                setScared(false);
            }
        }
        
        // Check for intersections and perform random turning decisions
        checkIntersectionAndRandomTurn(walls);
        
        // Exactly follow reference movement logic: move directly, roll back and change direction randomly on collision
        // Save old position
        double oldX = getX();
        double oldY = getY();
        
        // Update velocity
        updateVelocity();
        // Move ghost
        move();
        
        // Check for wall collisions or boundary collisions
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
        
        // Check for boundary collision
        if (getX() <= 0 || getX() + getWidth() >= 19 * 32 || getY() <= 0 || getY() + getHeight() >= 21 * 32) {
            collided = true;
        }
        
        // Check for collisions with other ghosts
        for (Ghost otherGhost : ghosts) {
            if (otherGhost != this) { // 跳过自身
                if (getX() < otherGhost.getX() + otherGhost.getWidth() &&
                    getX() + getWidth() > otherGhost.getX() &&
                    getY() < otherGhost.getY() + otherGhost.getHeight() &&
                    getY() + getHeight() > otherGhost.getY()) {
                    collided = true;
                    break;
                }
            }
        }
        
        // If collided, roll back position and randomly change direction
        if (collided) {
            setX(oldX);
            setY(oldY);
            
            // Randomly select new direction
            Direction[] directions = Direction.values();
            Direction newDirection;
            do {
                newDirection = directions[(int)(Math.random() * directions.length)];
            } while (newDirection == getDirection().getOpposite()); // 避免直接反向
            
            setDirection(newDirection);
            updateVelocity();
        }
        
        // Handle other special state timers
        if (frozen) {
            frozenTimer++;
            if (frozenTimer >= 100) {
                frozen = false;
                frozenTimer = 0;
            }
        }
        
        if (entangled) {
            entangledTimer++;
            if (entangledTimer >= 150) {
                entangled = false;
                entangledTimer = 0;
            }
        }
    }

    // 抽象方法，子类必须实现

    /**
     * Uses special ability
     */
    public abstract void useSpecialAbility();

    /**
     * Makes AI decision
     */
    protected abstract Direction makeAIDecision();

    /**
     * Checks for intersections and performs random turning decisions
     * Intersection definition: At least one other movable direction exists besides current moving direction
     * Modification reason: Implements user requirement - ghosts make random turning decisions at intersections
     * @param walls List of wall objects
     */
    private void checkIntersectionAndRandomTurn(List<Wall> walls) {
        // Collect all movable directions (excluding opposite direction)
        List<Direction> possibleDirections = new ArrayList<>();
        Direction currentDir = getDirection();
        
        // Check all four directions
        for (Direction dir : Direction.values()) {
            // Skip opposite direction
            if (dir == currentDir.getOpposite()) {
                continue;
            }
            
            // Check if movement to this direction is possible
            if (canMoveInDirection(dir, walls)) {
                possibleDirections.add(dir);
            }
        }
        
        // If possible directions >1, it means at an intersection, need to perform random turning decision
        if (possibleDirections.size() > 1) {
            // Random chance to turn: 3/5 probability to turn, 2/5 to stay
            if (Math.random() < 0.6) { // 3/5 = 0.6
                // Remove current direction and randomly select one from remaining
                possibleDirections.remove(currentDir);
                if (!possibleDirections.isEmpty()) {
                    int randomIndex = (int)(Math.random() * possibleDirections.size());
                    setDirection(possibleDirections.get(randomIndex));
                    updateVelocity();
                }
            }
            // Otherwise, don't turn and keep current direction
        }
    }
    
    /**
     * Checks if movement in specified direction is possible
     * Helper method for intersection detection and random turning
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
        
        // Check for wall collisions
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

    public boolean isScared() {
        return scared;
    }

    public void setScared(boolean scared) {
        this.scared = scared;
    }

    /**
     * Sets ghost scared state and resets timer
     * @param scared Scared state
     * @param timer Timer value
     */
    public void setScared(boolean scared, long timer) {
        this.scared = scared;
        this.scaredTimer = timer;
    }

    public long getScaredTimer() {
        return scaredTimer;
    }

    public void setScaredTimer(long scaredTimer) {
        this.scaredTimer = scaredTimer;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public long getFrozenTimer() {
        return frozenTimer;
    }

    public void setFrozenTimer(long frozenTimer) {
        this.frozenTimer = frozenTimer;
    }

    public boolean isEntangled() {
        return entangled;
    }

    public void setEntangled(boolean entangled) {
        this.entangled = entangled;
    }

    public long getEntangledTimer() {
        return entangledTimer;
    }

    public void setEntangledTimer(long entangledTimer) {
        this.entangledTimer = entangledTimer;
    }
}
