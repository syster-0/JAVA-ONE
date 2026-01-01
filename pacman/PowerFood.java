package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

/**
 * 能量豆类
 * Pac-Man吃掉后会进入能量模式
 * 能量模式下可以吃掉鬼魂
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PowerFood extends Food {
    // 能量模式持续时间（帧）
    private static final int POWER_DURATION = 1000;
    private boolean active;

    /**
     * 构造方法
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param width 宽度
     * @param height 高度
     * @param points 分数
     * @param image 图像资源
     */
    public PowerFood(double x, double y, double width, double height, int points, Image image) {
        super(x, y, width, height, points, image);
        this.active = true;
    }

    /**
     * 更新能量豆状态
     */
    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        // 能量豆不闪烁，始终保持可见（除非被吃掉）
        active = true;
    }

    /**
     * 重置能量豆
     */
    @Override
    public void reset() {
        super.reset();
        active = true;
    }

    /**
     * 渲染能量豆
     * @param gc 图形上下文对象
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isVisible() && active) {
            super.render(gc);
        }
    }

    /**
     * 获取能量模式持续时间
     * @return 持续时间
     */
    public static int getPowerDuration() {
        return POWER_DURATION;
    }
}
