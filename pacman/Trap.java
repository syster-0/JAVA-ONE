package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 陷阱类
 * 管理游戏中的各种陷阱效果，包括蜘蛛网和冰冻陷阱
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class Trap {
    private double x;
    private double y;
    private double width;
    private double height;
    private long startTime;
    private long duration;
    private boolean isActive;
    private String type;

    /**
     * 构造方法
     * @param x X坐标
     * @param y Y坐标
     * @param width 宽度
     * @param height 高度
     * @param duration 陷阱持续时间
     * @param type 陷阱类型
     */
    public Trap(double x, double y, double width, double height, long duration, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.isActive = true;
    }

    /**
     * 检查陷阱是否仍然激活
     * @return 陷阱是否激活
     */
    public boolean isActive() {
        if (isActive) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > duration) {
                isActive = false;
            }
        }
        return isActive;
    }

    /**
     * 渲染陷阱效果
     * @param gc 图形上下文对象
     */
    public void draw(GraphicsContext gc) {
        if (isActive()) {
            if (type.equals("spider")) {
                gc.setFill(Color.web("#8B4513", 0.3));
            } else if (type.equals("ice")) {
                gc.setFill(Color.web("#00FFFF", 0.3));
            }
            gc.fillRect(x, y, width, height);
        }
    }

    // Getters 和 Setters

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

    public String getType() {
        return type;
    }
}
