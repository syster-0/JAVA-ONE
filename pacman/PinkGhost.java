package pacman;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

import static pacman.PacManApp.TILE_SIZE;

/**
 * 粉色鬼魂 - Pinky
 * 特殊能力：拥有三重护盾，需攻击三次破坏
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PinkGhost extends Ghost {
    private static final double DEFAULT_SPEED = 2.0; // 修改原因：降低鬼魂速度以与 Pac-Man 同步
    private int shields = 3; // 三重护盾
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
    public PinkGhost(double x, double y, Image image) {
        super(x, y, TILE_SIZE, TILE_SIZE, getRandomDirection(), image);
        setMovementSpeed(DEFAULT_SPEED);
        this.shields = 3;
        this.isDead = false;
        this.deathTime = 0;
    }



    /**
     * 使用特殊能力 - 护盾保护
     */
    @Override
    public void useSpecialAbility() {
        // 护盾是被动能力，不需要主动使用
    }

    /**
     * 更新粉色鬼魂状态
     * 包括重生逻辑
     * 修改原因：与父类 update 方法签名保持一致，新增 ghosts 参数用于碰撞检测
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
        super.update(deltaTime, pacmanX, pacmanY, walls, ghosts);
    }

    /**
     * 受到攻击时减少护盾
     * @return true 表示死亡
     */
    public boolean takeDamage() {
        shields--;
        if (shields <= 0) {
            die();
            return true; // 死亡
        }
        return false; // 仍然存活
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
     * 获取剩余护盾数
     * @return 护盾数量
     */
    public int getShields() {
        return shields;
    }

    /**
     * 做出AI决策
     * 追踪 Pac-Man 并躲避攻击，偶尔会绕路以避免与其他鬼魂并行
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
        
        // 偶尔（12%概率）绕路移动，即选择第二优先方向，增加行为多样性
        if (Math.random() < 0.12) {
            double deltaX = pacmanX - currentX;
            double deltaY = pacmanY - currentY;
            // 选择第二优先方向
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                return deltaY > 0 ? Direction.DOWN : Direction.UP;
            } else {
                return deltaX > 0 ? Direction.RIGHT : Direction.LEFT;
            }
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
     * 渲染粉色鬼魂和护盾状态
     * @param gc 图形上下文对象
     */
    @Override
    public void render(GraphicsContext gc) {
        if (!isDead) {
            // 渲染护盾（根据剩余护盾数量）
            for (int i = 0; i < shields; i++) {
                double shieldRadius = 4 + i * 2;
                gc.setFill(Color.MAGENTA);
                gc.fillOval(
                    getX() + getWidth() / 2 - shieldRadius,
                    getY() + getHeight() / 2 - shieldRadius,
                    shieldRadius * 2,
                    shieldRadius * 2
                );
            }
            
            if (isScared()) {
                // 恐慌状态下渲染为蓝色
                gc.setFill(Color.BLUE);
                gc.fillOval(getX(), getY(), getWidth(), getHeight());
            } else {
                // 正常渲染图像
                super.render(gc);
            }
        } else {
            // 死亡状态：显示重生倒计时
            long remainingTime = RESPAWN_TIME - (System.currentTimeMillis() - deathTime);
            int secondsLeft = (int) (remainingTime / 1000) + 1;
            gc.setFill(Color.WHITE);
            gc.fillText("Died - Respawn in: " + secondsLeft + "s", getX(), getY() - 10);
        }
    }

    /**
     * 重置PinkGhost到初始状态
     */
    @Override
    public void reset() {
        setX(9 * 32);
        setY(12 * 32);
        setDirection(getRandomDirection());
        setMovementSpeed(DEFAULT_SPEED);
        shields = 3; // 重置护盾
        isDead = false;
        deathTime = 0;
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
     * 检查是否死亡
     * @return 是否死亡
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * 设置护盾数量
     * @param shields 新的护盾数量
     */
    public void setShields(int shields) {
        this.shields = shields;
    }
}
