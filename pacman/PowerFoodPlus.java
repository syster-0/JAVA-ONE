package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;

/**
 * 强化技能食物类
 * Pac-Man吃掉后会获得特殊技能效果
 * 例如：克隆、加速、冰冻鬼魂等
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PowerFoodPlus extends PowerFood {
    // 强化技能持续时间（帧）
    private static final int POWER_PLUS_DURATION = 2000;
    private int skillType;
    private boolean activated;

    /**
     * 简化构造方法，仅接受坐标
     * 用于与现有GameEngine代码保持一致
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     */
    public PowerFoodPlus(double x, double y) {
        super(x, y, PacManApp.TILE_SIZE, PacManApp.TILE_SIZE, 100, null);
        this.skillType = 0;
        this.activated = false;
    }
    
    /**
     * 构造方法
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param width 宽度
     * @param height 高度
     * @param points 分数
     * @param skillType 技能类型
     * @param image 图像资源
     */
    public PowerFoodPlus(double x, double y, double width, double height, int points, int skillType, Image image) {
        super(x, y, width, height, points, image);
        this.skillType = skillType;
        this.activated = false;
    }

    /**
     * 简化构造方法
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param skillType 技能类型
     * @param image 图像资源
     */
    public PowerFoodPlus(double x, double y, int skillType, Image image) {
        super(x, y, PacManApp.TILE_SIZE, PacManApp.TILE_SIZE, 50, image);
        this.skillType = skillType;
        this.activated = false;
    }

    /**
     * 更新技能食物状态
     */
    @Override
    public void update(long deltaTime) {
        super.update(deltaTime);
        // 充能食物不闪烁，始终保持可见（除非被吃掉）
        // 移除了原有的闪烁逻辑，符合用户需求
    }

    /**
     * 重置技能食物
     */
    @Override
    public void reset() {
        super.reset();
        this.activated = false;
    }

    /**
     * 获取技能类型
     * @return 技能类型
     */
    public int getSkillType() {
        return skillType;
    }

    /**
     * 设置技能类型
     * @param skillType 技能类型
     */
    public void setSkillType(int skillType) {
        this.skillType = skillType;
    }

    /**
     * 获取是否已激活
     * @return 是否已激活
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * 设置激活状态
     * @param activated 激活状态
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * 获取强化技能持续时间
     * @return 持续时间（帧）
     */
    public static int getPowerPlusDuration() {
        return POWER_PLUS_DURATION;
    }
}