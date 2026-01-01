package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

/**
 * 普通食物类
 * Pac-Man吃掉后可以获得分数
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class Food extends Entity {
    private int points;
    private boolean visible;

    /**
     * 构造方法（带完整参数）
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param width 宽度
     * @param height 高度
     * @param points 分数
     * @param image 图像资源
     */
    public Food(double x, double y, double width, double height, int points, Image image) {
        super(x, y, width, height, null, image);
        this.points = points;
        this.visible = true;
    }
    
    /**
     * 构造方法（简化版本，使用默认尺寸和分数）
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param image 图像资源
     */
    public Food(double x, double y, Image image) {
        super(x, y, PacManApp.TILE_SIZE, PacManApp.TILE_SIZE, null, image);
        this.points = 10;
        this.visible = true;
    }

    /**
     * 更新食物状态
     */
    @Override
    public void update(long deltaTime) {
        // 普通食物通常不需要更新
    }

    /**
     * 重置食物
     */
    @Override
    public void reset() {
        visible = true;
    }

    /**
     * 渲染食物
     * @param gc 图形上下文对象
     */
    @Override
    public void render(GraphicsContext gc) {
        if (visible) {
            super.render(gc);
        }
    }

    /**
     * 获取食物分数
     * @return 分数
     */
    public int getPoints() {
        return points;
    }

    /**
     * 设置食物分数
     * @param points 分数
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * 判断食物是否可见
     * @return true表示可见
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 设置食物可见性
     * @param visible 可见性
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
