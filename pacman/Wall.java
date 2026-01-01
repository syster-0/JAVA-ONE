package pacman;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * 墙壁类
 * 继承自Entity，作为游戏中的障碍物
 * 
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class Wall extends Entity {
    /**
     * 构造方法
     * 
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param width 宽度
     * @param height 高度
     * @param image 墙壁图像
     */
    public Wall(double x, double y, double width, double height, Image image) {
        super(x, y, width, height, null, image);
    }

    /**
     * 更新墙壁状态
     * 墙壁为静态障碍物，无需更新
     */
    @Override
    public void update(long deltaTime) {
        // 墙壁是静态的，不需要更新
    }

    /**
     * 渲染墙壁
     * 
     * @param gc 图形上下文对象
     */
    @Override
    public void render(GraphicsContext gc) {
        super.render(gc);
    }

    /**
     * 重置墙壁
     * 墙壁为静态对象，重置无实际效果
     */
    @Override
    public void reset() {
        // 墙壁无需重置
    }
}