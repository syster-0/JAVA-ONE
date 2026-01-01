package pacman;

/**
 * 冰冻状态类
 * 管理实体的冰冻状态，包括开始时间和持续时间
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class FrozenStatus {
    private long startTime;
    private long duration;

    /**
     * 构造方法
     * @param duration 冰冻状态持续时间（毫秒）
     */
    public FrozenStatus(long duration) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 检查冰冻状态是否仍然激活
     * @return 是否处于冰冻状态
     */
    public boolean isActive() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed < duration;
    }

    /**
     * 获取剩余冰冻时间
     * @return 剩余时间（毫秒）
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return duration - elapsed;
    }
}
