package pacman;

/**
 * 鬼魂恐慌状态类
 * 管理鬼魂处于恐慌状态的开始时间、持续时间和有效性
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class GhostScaredStatus {
    private long startTime;
    private long duration;

    /**
     * 构造方法
     * @param duration 恐慌状态持续时间（毫秒）
     */
    public GhostScaredStatus(long duration) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 检查恐慌状态是否仍然激活
     * @return 是否处于恐慌状态
     */
    public boolean isActive() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed < duration;
    }

    /**
     * 获取剩余恐慌时间
     * @return 剩余时间（毫秒）
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return duration - elapsed;
    }

    /**
     * 获取恐慌状态结束时间
     * @return 结束时间戳
     */
    public long getEndTime() {
        return startTime + duration;
    }

    /**
     * 获取开始时间（用于测试）
     * @return 开始时间戳
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 获取持续时间（用于测试）
     * @return 持续时间
     */
    public long getDuration() {
        return duration;
    }
}
