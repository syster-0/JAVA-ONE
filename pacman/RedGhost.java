package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

import static pacman.PacManApp.TILE_SIZE;

/**
 * 红色鬼魂 - Blinky
 * 特殊能力：闪现移动，可瞬间传送到随机位置
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class RedGhost extends Ghost {
    private static final double DEFAULT_SPEED = 2.0; // 修改原因：降低鬼魂速度以与 Pac-Man 同步
    private long teleportCooldown = 0; // 闪现冷却时间
    private static final long TELEPORT_COOLDOWN = 15000; // 闪现冷却（15秒）
    private long teleportTimer = 0; // 闪现计时器
    private boolean isTeleporting = false; // 是否正在闪现
    private long respawnTimer = 0; // 重生计时器
    private static final long RESPAWN_TIME = 30000; // 重生时间（30秒）
    private boolean isDead = false; // 是否死亡
    private long deathTime = 0; // 死亡时间
    private double pacmanX; // Pac-Man X 坐标
    private double pacmanY; // Pac-Man Y 坐标

    /**
     * 构造方法
     *
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param image 图像资源
     */
    public RedGhost(double x, double y, Image image) {
        super(x, y, TILE_SIZE, TILE_SIZE, getRandomDirection(), image);
        setMovementSpeed(DEFAULT_SPEED);
        this.teleportCooldown = 0;
        this.teleportTimer = 0;
        this.isTeleporting = false;
        this.isDead = false;
        this.deathTime = 0;
    }

    /**
     * 获取随机方向
     * 修改原因：让鬼魂初始方向随机化，避免所有鬼魂都沿同一方向移动
     * @return 随机方向
     */
    private static Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[(int)(Math.random() * directions.length)];
    }

    /**
     * 使用特殊能力 - 闪现移动
     * 修改原因：增加walls参数以传递给teleport方法，用于墙壁检测
     * @param walls 墙壁对象列表
     */
    public void useSpecialAbility(List<Wall> walls) {
        if (!isDead && teleportCooldown == 0) {
            teleport(walls);
        }
    }
    
    /**
     * 使用特殊能力 - 重写父类抽象方法（保持兼容性）
     */
    @Override
    public void useSpecialAbility() {
        // 空实现，保持父类抽象方法完整性
    }

    /**
     * 闪现到随机位置
     * 修改原因：添加墙壁碰撞检测，确保闪现位置不会是墙壁
     * @param walls 墙壁对象列表，用于碰撞检测
     */
    private void teleport(List<Wall> walls) {
        isTeleporting = true;
        boolean validPositionFound = false;
        double newX = getX();
        double newY = getY();
        int maxAttempts = 100; // 防止无限循环
        int attempts = 0;
        
        // 尝试找到一个没有墙壁的位置
        while (!validPositionFound && attempts < maxAttempts) {
            // 随机选择新坐标（0-19行和列）
            newX = (int)(Math.random() * 20) * TILE_SIZE;
            newY = (int)(Math.random() * 20) * TILE_SIZE;
            
            // 检查该位置是否与墙壁碰撞
            boolean collision = false;
            for (Wall wall : walls) {
                if (newX < wall.getX() + wall.getWidth() &&
                    newX + getWidth() > wall.getX() &&
                    newY < wall.getY() + wall.getHeight() &&
                    newY + getHeight() > wall.getY()) {
                    collision = true;
                    break;
                }
            }
            
            // 如果没有碰撞，则为有效位置
            if (!collision) {
                validPositionFound = true;
            }
            
            attempts++;
        }
        
        // 更新位置到有效位置
        setX(newX);
        setY(newY);
        
        // 重新设置冷却时间
        teleportCooldown = TELEPORT_COOLDOWN;
        teleportTimer = System.currentTimeMillis();
        
        isTeleporting = false;
    }

    /**
     * 更新红色鬼魂状态
     * 包括闪现冷却和重生逻辑
     * 修改原因：传递walls参数给useSpecialAbility方法以支持墙壁检测
     * @param deltaTime 时间增量（毫秒）
     * @param pacmanX Pac-Man 的 X 坐标
     * @param pacmanY Pac-Man 的 Y 坐标
     * @param walls 墙壁对象列表
     * @param ghosts 所有鬼魂对象列表
     */
    @Override
    public void update(long deltaTime, double pacmanX, double pacmanY, List<Wall> walls, List<Ghost> ghosts) {
        if (isDead) {
            // 死亡状态：重生计时器
            if (System.currentTimeMillis() - deathTime >= RESPAWN_TIME) {
                // 重生
                respawn();
            }
            return;
        }
        
        this.pacmanX = pacmanX; // 更新 Pac-Man X 坐标
        this.pacmanY = pacmanY; // 更新 Pac-Man Y 坐标
        
        // 更新闪现冷却
        if (teleportCooldown > 0) {
            teleportCooldown -= deltaTime;
            if (teleportCooldown < 0) {
                teleportCooldown = 0;
            }
        }
        
        // 周期性使用闪现（每15秒）
        if (teleportCooldown == 0) {
            useSpecialAbility(walls); // 修改：传递walls参数
        }
        
        super.update(deltaTime, pacmanX, pacmanY, walls, ghosts);
    }

    /**
     * 标记鬼魂死亡
     */
    public void die() {
        isDead = true;
        deathTime = System.currentTimeMillis();
    }

    /**
     * 重新生成鬼魂
     */
    private void respawn() {
        isDead = false;
        reset();
    }

    /**
     * 做出AI决策
     * 积极追踪 Pac-Man，偶尔会随机选择方向以避免与其他鬼魂并行
     * 修改原因：增加恐慌状态处理，当处于恐慌时逃离 Pac-Man
     * @return 下一步移动方向
     */
    @Override
    protected Direction makeAIDecision() {
        double currentX = getX();
        double currentY = getY();
        
        // 检查是否处于恐慌状态
        if (isScared()) {
            // 恐慌状态：逃离 Pac-Man
            double deltaX = pacmanX - currentX;
            double deltaY = pacmanY - currentY;
            
            // 优先沿 X 轴反向移动
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                return deltaX > 0 ? Direction.LEFT : Direction.RIGHT;
            } else {
                return deltaY > 0 ? Direction.UP : Direction.DOWN;
            }
        }
        
        // 偶尔（10%概率）随机选择方向，避免总是与其他鬼魂选择相同路径
        if (Math.random() < 0.1) {
            Direction[] directions = Direction.values();
            return directions[(int)(Math.random() * directions.length)];
        }
        
        // 计算与 Pac-Man 的距离差
        double deltaX = pacmanX - currentX;
        double deltaY = pacmanY - currentY;
        
        // 优先沿 X 轴移动向 Pac-Man
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            return deltaY > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    /**
     * 渲染红色鬼魂
     * 修改原因：移除闪现冷却时间的HUD显示，符合用户需求
     * @param gc 图形上下文对象
     */
    @Override
    public void render(GraphicsContext gc) {
        if (!isDead) {
            if (isScared()) {
                // 恐慌状态下渲染为蓝色
                gc.setFill(Color.BLUE);
                gc.fillOval(getX(), getY(), getWidth(), getHeight());
            } else {
                // 正常渲染图像
                super.render(gc);
            }
            // 修改：移除闪现冷却状态显示
        } else {
            // 死亡状态：显示重生倒计时
            long remainingTime = RESPAWN_TIME - (System.currentTimeMillis() - deathTime);
            int secondsLeft = (int) (remainingTime / 1000) + 1;
            gc.setFill(Color.WHITE);
            gc.fillText("Died - Respawn in: " + secondsLeft + "s", getX(), getY() - 10);
        }
    }

    /**
     * 重置RedGhost到初始状态
     */
    @Override
    public void reset() {
        setX(9 * 32);
        setY(12 * 32);
        setDirection(getRandomDirection());
        setMovementSpeed(DEFAULT_SPEED);
        teleportCooldown = TELEPORT_COOLDOWN;
        isTeleporting = false;
        isDead = false;
        deathTime = 0;
    }

    /**
     * 检查是否死亡
     * @return 是否死亡
     */
    public boolean isDead() {
        return isDead;
    }
}
