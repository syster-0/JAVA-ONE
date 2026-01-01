package pacman;

/**
 * 缠绕状态类
 * 管理实体的缠绕状态，限制其移动能力
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class EntangledStatus {
    private long startTime;
    private long duration;

    /**
     * 构造方法
     * @param duration 缠绕状态持续时间（毫秒）
     */
    public EntangledStatus(long duration) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 检查缠绕状态是否仍然激活
     * @return 是否处于缠绕状态
     */
    public boolean isActive() {
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed < duration;
    }

    /**
     * 获取剩余缠绕时间
     * @return 剩余时间（毫秒）
     */
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        return duration - elapsed;
    }
}
