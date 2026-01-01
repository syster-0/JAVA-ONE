package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * 游戏实体抽象基类
 * 所有游戏对象（吃豆人、鬼魂、食物、墙壁等）的父类
 * 定义了通用属性和方法
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public abstract class Entity {
    // 位置属性
    protected double x;
    protected double y;
    protected double width;
    protected double height;

    // 运动属性
    protected double velocityX;
    protected double velocityY;
    protected Direction direction;
    protected double speed;

    // 视觉属性
    protected Image image;
    protected boolean visible = true;

    /**
     * 构造方法
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param width 宽度
     * @param height 高度
     * @param direction 初始方向
     * @param image 实体图像
     */
    public Entity(double x, double y, double width, double height, Direction direction, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction = direction;
        this.image = image;
        this.speed = 2.0; // 默认移动速度
    }

    /**
     * 更新实体状态
     * 抽象方法，需要子类实现
     * @param deltaTime 时间增量
     */
    public abstract void update(long deltaTime);

    /**
     * 渲染实体
     * @param gc 图形上下文对象
     */
    public void render(GraphicsContext gc) {
        if (visible && image != null) {
            gc.drawImage(image, x, y, width, height);
        }
    }

    /**
     * 更新速度
     */
    protected void updateVelocity() {
        if (direction != null) {
            velocityX = direction.getVelocityX(speed);
            velocityY = direction.getVelocityY(speed);
        } else {
            velocityX = 0;
            velocityY = 0;
        }
    }
    
    /**
     * 移动实体
     */
    protected void move() {
        setX(getX() + velocityX);
        setY(getY() + velocityY);
    }

    /**
     * 重置实体到初始状态
     */
    public abstract void reset();

    // Getters 和 Setters

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public double getMovementSpeed() {
        return speed;
    }

    public void setMovementSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }
}
