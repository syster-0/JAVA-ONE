package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import java.util.List;
import static pacman.PacManApp.*;

/**
 * Pac-Man Clone Class
 * Represents the clone generated when Pac-Man uses a skill
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PacmanClone {
    private double x;
    private double y;
    private double width;
    private double height;
    private Direction direction;
    private double velocityX;
    private double velocityY;
    private Image image;
    private double rotation;
    private static final double MOVE_SPEED = 3.0; // 1.5 times the speed of the main body (main body speed is 2.0, 2.0 * 1.5 = 3.0)

    /**
     * Constructor
     * @param x X coordinate
     * @param y Y coordinate
     * @param direction Moving direction
     * @param image Clone image
     */
    public PacmanClone(double x, double y, Direction direction, Image image) {
        this.x = x;
        this.y = y;
        this.width = PacManApp.TILE_SIZE;
        this.height = PacManApp.TILE_SIZE;
        this.direction = direction;
        this.image = image;
        this.rotation = 0;
        updateVelocity();
    }

    /**
     * Update movement velocity
     */
    public void updateVelocity() {
        switch (direction) {
            case UP:
                velocityX = 0;
                velocityY = -MOVE_SPEED;
                rotation = 270;
                break;
            case DOWN:
                velocityX = 0;
                velocityY = MOVE_SPEED;
                rotation = 90;
                break;
            case LEFT:
                velocityX = -MOVE_SPEED;
                velocityY = 0;
                rotation = 180;
                break;
            case RIGHT:
                velocityX = MOVE_SPEED;
                velocityY = 0;
                rotation = 0;
                break;
            default:
                velocityX = 0;
                velocityY = 0;
                break;
        }
    }

    /**
     * Check if moving in current direction is allowed
     * @param walls List of wall objects
     * @return True if movement is allowed, otherwise false
     */
    private boolean canMove(List<Wall> walls) {
        double testX = x + velocityX;
        double testY = y + velocityY;
        
        for (Wall wall : walls) {
            if (testX < wall.getX() + wall.getWidth() &&
                testX + width > wall.getX() &&
                testY < wall.getY() + wall.getHeight() &&
                testY + height > wall.getY()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Randomly change moving direction
     */
    private void changeDirectionRandomly() {
        Direction[] directions = Direction.values();
        int randomIndex = (int)(Math.random() * directions.length);
        direction = directions[randomIndex];
        updateVelocity();
    }

    /**
     * Move the clone
     * Modification reason: According to requirements, clones should disappear directly when hitting walls instead of avoiding collisions, so collision detection and direction change logic are removed
     * @param walls List of wall objects (kept for compatibility with existing calls)
     */
    public void move(List<Wall> walls) {
        // 直接移动，不进行碰撞检测，让GameEngine的碰撞检测来处理与墙壁和鬼怪的碰撞
        x += velocityX;
        y += velocityY;

        // 处理边界穿越
        if (x < 0) {
            x = PacManApp.GRID_WIDTH * PacManApp.TILE_SIZE - PacManApp.TILE_SIZE;
        } else if (x >= PacManApp.GRID_WIDTH * PacManApp.TILE_SIZE) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        } else if (y >= PacManApp.GRID_HEIGHT * PacManApp.TILE_SIZE) {
            y = PacManApp.GRID_HEIGHT * PacManApp.TILE_SIZE - PacManApp.TILE_SIZE;
        }
    }

    /**
     * Render the clone
     * @param gc Graphics context object
     */
    public void render(GraphicsContext gc) {
        gc.save();
        gc.translate(x + width / 2, y + height / 2);
        gc.rotate(rotation);
        gc.drawImage(image, -width / 2, -height / 2, width, height);
        gc.restore();
    }

    // Getters and Setters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        updateVelocity();
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
