package pacman;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * 游戏引擎类
 * 管理游戏的核心逻辑、实体、碰撞检测和渲染
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class GameEngine {
    private Canvas canvas;
    private GraphicsContext gc;
    public static final int GRID_WIDTH = PacManApp.GRID_WIDTH;
    public static final int GRID_HEIGHT = PacManApp.GRID_HEIGHT;
    public static final int TILE_SIZE = PacManApp.TILE_SIZE;

    // 游戏状态
    private enum GameState {
        START, RUNNING, PAUSED, GAME_OVER, WIN
    }

    private GameState currentState;
    private long startTime;
    private int level;
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameStarted;
    private int powerFoodPlusSkillCount;
    private boolean hasPowerFoodPlusSkill; // 标记是否已激活强化技能
    
    // 图像资源变量 - 修改：将图像资源声明为成员变量，以便在整个类中使用
    private Image foodImage;
    private Image powerFoodImage;
    
    // 冰影陷阱状态管理 - 新增
    private boolean isPacmanFrozenByIceTrap; // 标记pacman是否被冰影陷阱冰冻
    private long iceTrapFreezeTimer; // 冰影陷阱冰冻开始时间
    private static final long ICE_TRAP_TIMEOUT = 10000; // 冰影陷阱超时时间（10秒）
    
    // 缚丝陷阱状态管理 - 新增
    private boolean isPacmanEntangledByTrap; // 标记pacman是否被缚丝陷阱缠绕
    private long entangledTrapTimer; // 缚丝陷阱开始时间
    private static final long ENTANGLED_TRAP_TIMEOUT = 3000; // 缚丝陷阱持续时间（3秒）


    // 地图系统 - 修改：移除被技能食物替代的墙，使其与参考地图一致
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X                 X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X      b p o      X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X    X       X    X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X       X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    // 实体集合
    private List<Food> foods; // 食物
    private List<PowerFood> powerFoods; // 能量豆
    private List<PowerFoodPlus> powerFoodsPlus; // 强化技能食物
    private List<Wall> walls; // 墙壁
    private List<Trap> traps; // 陷阱
    private List<Ghost> ghosts; // 鬼魂
    private List<PacmanClone> pacmanClones; // Pacman克隆体
    private PacMan pacman;

    // 状态管理映射
    private HashMap<Ghost, GhostScaredStatus> ghostScaredMap;
    private HashMap<Entity, FrozenStatus> frozenEntities;
    private HashMap<Entity, EntangledStatus> entangledEntities;
    private HashMap<Ghost, Long> ghostRespawnTimers;
    private HashMap<Ghost, Long> redGhostTeleportCooldown;

    /**
     * 构造方法
     * @param canvas 游戏画布对象
     */
    public GameEngine(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        currentState = GameState.START;
        level = 1;
        score = 0;
        lives = 3;
        gameOver = false;
        gameStarted = false;
        powerFoodPlusSkillCount = 0;
        hasPowerFoodPlusSkill = false;

        // 初始化集合
        foods = new ArrayList<>();
        powerFoods = new ArrayList<>();
        powerFoodsPlus = new ArrayList<>();
        walls = new ArrayList<>();
        traps = new ArrayList<>();
        ghosts = new ArrayList<>();
        pacmanClones = new ArrayList<>();
        ghostScaredMap = new HashMap<>();
        frozenEntities = new HashMap<>();
        entangledEntities = new HashMap<>();
        ghostRespawnTimers = new HashMap<>();
        redGhostTeleportCooldown = new HashMap<>();
        
        // 初始化冰影陷阱状态 - 新增
        isPacmanFrozenByIceTrap = false;
        iceTrapFreezeTimer = 0;
        
        // 初始化缚丝陷阱状态 - 新增
        isPacmanEntangledByTrap = false;
        entangledTrapTimer = 0;
    }

    /**
     * 初始化游戏
     * 加载所有游戏资源和实体
     */
    public void initialize() {
        // 加载 PacMan 方向图像
        Image pacmanUpImage = new Image("file:pacmanUp.png");
        Image pacmanDownImage = new Image("file:pacmanDown.png");
        Image pacmanLeftImage = new Image("file:pacmanLeft.png");
        Image pacmanRightImage = new Image("file:pacmanRight.png");
        
        // 加载鬼魂图像
        Image redGhostImage = new Image("file:redGhost.png");
        Image pinkGhostImage = new Image("file:pinkGhost.png");
        Image blueGhostImage = new Image("file:blueGhost.png");
        Image orangeGhostImage = new Image("file:orangeGhost.png");
        
        // 加载墙壁图像
        Image wallImage = new Image("file:wall.png");

        // 加载地图
        loadMap(pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage,
               redGhostImage, pinkGhostImage, blueGhostImage, orangeGhostImage,
               wallImage);
    }

    /**
     * 加载游戏地图
     * 根据tileMap数组创建所有游戏实体
     * @param pacmanUpImage 吃豆人向上图像
     * @param pacmanDownImage 吃豆人向下图像
     * @param pacmanLeftImage 吃豆人向左图像
     * @param pacmanRightImage 吃豆人向右图像
     * @param redGhostImage 红色鬼魂图像
     * @param pinkGhostImage 粉色鬼魂图像
     * @param blueGhostImage 蓝色鬼魂图像
     * @param orangeGhostImage 橙色鬼魂图像
     * @param wallImage 墙壁图像
     */
    private void loadMap(Image pacmanUpImage, Image pacmanDownImage, Image pacmanLeftImage, Image pacmanRightImage,
                        Image redGhostImage, Image pinkGhostImage, Image blueGhostImage, Image orangeGhostImage,
                        Image wallImage) {
        foods.clear();
        powerFoods.clear();
        walls.clear();
        ghosts.clear();
        pacmanClones.clear();
        traps.clear();
        ghostScaredMap.clear();
        frozenEntities.clear();
        entangledEntities.clear();
        ghostRespawnTimers.clear();
        redGhostTeleportCooldown.clear();

        // 加载食物和能量豆图像
        // 修改：使用powerFood.png作为普通食物，power food-1.png作为强化技能食物
        this.foodImage = new Image("file:powerFood.png");
        this.powerFoodImage = new Image("file:power food-1.png.png");

        for (int row = 0; row < tileMap.length; row++) {
            for (int col = 0; col < tileMap[row].length(); col++) {
                char tile = tileMap[row].charAt(col);
                double x = col * TILE_SIZE;
                double y = row * TILE_SIZE;

                switch (tile) {
                    case 'X':
                        walls.add(new Wall(x, y, TILE_SIZE, TILE_SIZE, wallImage));
                        break;
                    case 'P':
                        pacman = new PacMan(x, y, TILE_SIZE, TILE_SIZE, Direction.LEFT);
                        // 添加普通食物到PacMan的起始位置
                        foods.add(new Food(x, y, foodImage));
                        break;
                    case 'r':
                        RedGhost redGhost = new RedGhost(x, y, redGhostImage);
                        ghosts.add(redGhost);
                        redGhostTeleportCooldown.put(redGhost, 0L);
                        break;
                    case 'p':
                        PinkGhost pinkGhost = new PinkGhost(x, y, pinkGhostImage);
                        ghosts.add(pinkGhost);
                        redGhostTeleportCooldown.put(pinkGhost, 0L);
                        break;
                    case 'b':
                        BlueGhost blueGhost = new BlueGhost(x, y, blueGhostImage);
                        ghosts.add(blueGhost);
                        redGhostTeleportCooldown.put(blueGhost, 0L);
                        break;
                    case 'o':
                        OrangeGhost orangeGhost = new OrangeGhost(x, y, orangeGhostImage);
                        ghosts.add(orangeGhost);
                        redGhostTeleportCooldown.put(orangeGhost, 0L);
                        break;
                    case 'O':
                        // 空白区域，不添加任何实体
                        break;
                    case ' ':
                        // 普通食物
                        foods.add(new Food(x, y, foodImage));
                        break;
                    default:
                        // 默认添加普通食物
                        foods.add(new Food(x, y, foodImage));
                        break;
                }
            }
        }
        
        // 初始化食物和能量豆
        initializeFoods(foodImage, powerFoodImage);
        
        // 生成强化技能食物
        generatePowerFoodPlus();
    }

    /**
     * 更新游戏状态
     * @param deltaTime 两次更新之间的时间间隔
     */
    public void update(long deltaTime) {
        if (currentState == GameState.START || currentState == GameState.GAME_OVER) return;
        if (gameOver) {
            currentState = GameState.GAME_OVER;
            return;
        }
        // 游戏开始默认启动
        if (!gameStarted) {
            currentState = GameState.RUNNING;
            gameStarted = true;
        }

        // PacMan 移动（完全照搬参考代码逻辑）
        // 更新 PacMan 位置
        pacman.move(walls);
        
        // 处理边界穿越
        pacman.handleBoundaryCrossing();
        
        // 更新能量模式计时器
        if (pacman.isPowerMode()) {
            long powerTimer = pacman.getPowerModeTimer();
            powerTimer--;
            pacman.setPowerModeTimer(powerTimer);
            if (powerTimer <= 0) {
                pacman.setPowerMode(false);
                // 恢复鬼魂的正常行为
                for (Ghost ghost : new ArrayList<>(ghosts)) {
                    ghost.setScared(false);
                }
            }
        }

        // 更新鬼魂 - 使用集合副本来避免ConcurrentModificationException
        for (Ghost ghost : new ArrayList<>(ghosts)) {
            // 同步鬼魂恐慌状态（关键修改：确保ghostScaredMap与Ghost类的scared变量同步）
            if (ghostScaredMap.containsKey(ghost) && ghostScaredMap.get(ghost).isActive()) {
                ghost.setScared(true);
            } else {
                ghost.setScared(false);
            }
            // 传递PacMan的位置信息、墙壁和所有鬼魂对象给鬼魂AI
            ghost.update(deltaTime, pacman.getX(), pacman.getY(), walls, ghosts);
            
            // 处理红色鬼魂传送冷却
            if (ghost instanceof RedGhost) {
                Long cooldown = redGhostTeleportCooldown.get(ghost);
                if (cooldown > 0) {
                    redGhostTeleportCooldown.put(ghost, cooldown - 1);
                } else {
                    // 冷却结束，使用特殊能力在食物位置随机传送
                    ((RedGhost) ghost).useSpecialAbility();
                }
            }
            
            // 处理蓝色鬼魂陷阱能力
            if (ghost instanceof BlueGhost) {
                // 蓝色鬼魂定期设置陷阱
                ((BlueGhost) ghost).useSpecialAbility();
            }
            
            // 处理橙色鬼魂陷阱能力
            if (ghost instanceof OrangeGhost) {
                // 橙色鬼魂定期设置冻结陷阱
                ((OrangeGhost) ghost).useSpecialAbility();
            }
            
            // 处理粉色鬼魂护盾
            if (ghost instanceof PinkGhost) {
                // 粉色鬼魂自动启用护盾保护
                ((PinkGhost) ghost).useSpecialAbility();
            }
            
            // 处理重生计时器
            if (ghostRespawnTimers.containsKey(ghost)) {
                long respawnTime = ghostRespawnTimers.get(ghost);
                if (System.currentTimeMillis() - respawnTime > 30000) { // 30秒重生
                    ghost.reset();
                    ghostRespawnTimers.remove(ghost);
                }
            }
        }

        // 更新食物 - 使用集合副本来避免ConcurrentModificationException
        for (Food food : new ArrayList<>(foods)) {
            food.update(deltaTime);
        }

        // 更新能量豆 - 使用集合副本来避免ConcurrentModificationException
        for (PowerFood powerFood : new ArrayList<>(powerFoods)) {
            powerFood.update(deltaTime);
        }
        
        // 更新强化技能食物 - 使用集合副本来避免ConcurrentModificationException
        for (PowerFoodPlus powerFoodPlus : new ArrayList<>(powerFoodsPlus)) {
            powerFoodPlus.update(deltaTime);
        }

        // 更新墙壁 - 使用集合副本来避免ConcurrentModificationException
        for (Wall wall : new ArrayList<>(walls)) {
            wall.update(deltaTime);
        }

        // 更新克隆体 - 使用集合副本来避免ConcurrentModificationException
        for (PacmanClone clone : new ArrayList<>(pacmanClones)) {
            clone.move(walls);
        }

        // 处理碰撞
        handleCollisions();

        // 检查游戏胜利
        checkWinCondition();
    }

    /**
     * 渲染游戏
     * @param gc 图形上下文对象
     */
    public void render(GraphicsContext gc) {
        // 修改：设置地图背景为黑色
        gc.setFill(javafx.scene.paint.Color.BLACK);
        gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        
        if (currentState == GameState.START) {
            renderStartMenu(gc);
            return;
        } else if (currentState == GameState.GAME_OVER) {
            renderGameOverScreen(gc);
            return;
        } else if (currentState == GameState.WIN) {
            renderWinScreen(gc);
            return;
        }
        
        // 渲染墙壁
        for (Wall wall : walls) {
            wall.render(gc);
        }

        // 渲染食物
        for (Food food : foods) {
            food.render(gc);
        }

        // 渲染能量豆
        for (PowerFood powerFood : powerFoods) {
            powerFood.render(gc);
        }
        
        // 渲染强化技能食物
        for (PowerFoodPlus powerFoodPlus : powerFoodsPlus) {
            powerFoodPlus.render(gc);
        }

        // 渲染陷阱
        for (Trap trap : traps) {
            trap.draw(gc);
        }

        // 渲染Pac-Man
        pacman.render(gc);

        // 渲染克隆体
        for (PacmanClone clone : pacmanClones) {
            clone.render(gc);
        }

        // 渲染鬼魂
        for (Ghost ghost : ghosts) {
            ghost.render(gc);
        }

        // 渲染游戏信息
        renderGameInfo(gc);
        // 渲染操作提示HUD
        renderControlHUD(gc);
        // 渲染操作提示（兼容性）
        renderControlHints(gc);
        
        // 渲染冰影陷阱HUD - 新增
        if (isPacmanFrozenByIceTrap) {
            renderIceTrapHUD(gc);
        }
        
        // 渲染缚丝陷阱HUD - 新增
        if (isPacmanEntangledByTrap) {
            renderEntangledTrapHUD(gc);
        }
    }

    /**
     * 渲染开始菜单
     * @param gc 图形上下文对象
     */
    private void renderStartMenu(GraphicsContext gc) {
        // 使用正确的常量名称：PacManApp.SCREEN_WIDTH 和 PacManApp.SCREEN_HEIGHT
        // 修改原因：PacManApp类中定义的是SCREEN_WIDTH和SCREEN_HEIGHT，而不是WINDOW_WIDTH和WINDOW_HEIGHT
        
        // 绘制背景遮罩
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, PacManApp.SCREEN_WIDTH, PacManApp.SCREEN_HEIGHT);
        
        // 绘制开始标题
        gc.setFill(javafx.scene.paint.Color.YELLOW);
        gc.setFont(new javafx.scene.text.Font("Arial", 48));
        gc.fillText("PAC-MAN", PacManApp.SCREEN_WIDTH / 2 - 120, PacManApp.SCREEN_HEIGHT / 2 - 60);
        
        // 绘制开始提示
        gc.setFont(new javafx.scene.text.Font("Arial", 24));
        gc.fillText("Press SPACE to Start", PacManApp.SCREEN_WIDTH / 2 - 140, PacManApp.SCREEN_HEIGHT / 2);
        
        // 绘制游戏说明
        gc.setFont(new javafx.scene.text.Font("Arial", 16));
        gc.fillText("Use Arrow Keys to Move", PacManApp.SCREEN_WIDTH / 2 - 100, PacManApp.SCREEN_HEIGHT / 2 + 40);
        gc.fillText("Press Q to Release Clones", PacManApp.SCREEN_WIDTH / 2 - 100, PacManApp.SCREEN_HEIGHT / 2 + 65);
    }
    
    /**
     * 渲染游戏结束屏幕
     * @param gc 图形上下文对象
     */
    private void renderGameOverScreen(GraphicsContext gc) {
        // 使用正确的常量名称：PacManApp.SCREEN_WIDTH 和 PacManApp.SCREEN_HEIGHT
        // 修改原因：保持与PacManApp类常量定义的一致性
        
        // 绘制背景遮罩
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, PacManApp.SCREEN_WIDTH, PacManApp.SCREEN_HEIGHT);
        
        // 绘制游戏结束信息
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.setFont(new javafx.scene.text.Font("Arial", 48));
        gc.fillText("GAME OVER", PacManApp.SCREEN_WIDTH / 2 - 140, PacManApp.SCREEN_HEIGHT / 2 - 60);
        
        // 绘制最终得分
        gc.setFont(new javafx.scene.text.Font("Arial", 24));
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("Final Score: " + score, PacManApp.SCREEN_WIDTH / 2 - 100, PacManApp.SCREEN_HEIGHT / 2);
        
        // 绘制重新开始提示
        gc.fillText("Press SPACE to Play Again", PacManApp.SCREEN_WIDTH / 2 - 140, PacManApp.SCREEN_HEIGHT / 2 + 40);
    }

    /**
     * 渲染冰影陷阱HUD
     * 新增：显示冰影陷阱的倒计时和按键提示信息
     * @param gc 图形上下文对象
     */
    private void renderIceTrapHUD(GraphicsContext gc) {
        // 计算剩余时间
        long remainingTime = ICE_TRAP_TIMEOUT - (System.currentTimeMillis() - iceTrapFreezeTimer);
        int remainingSeconds = (int)(remainingTime / 1000) + 1; // 向上取整
        
        // 绘制背景遮罩
        gc.setFill(javafx.scene.paint.Color.rgb(0, 150, 255, 0.5));
        gc.fillRect(PacManApp.SCREEN_WIDTH / 2 - 200, PacManApp.SCREEN_HEIGHT / 2 - 100, 400, 200);
        
        // 绘制提示标题
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 36));
        gc.fillText("FROZEN!", PacManApp.SCREEN_WIDTH / 2 - 70, PacManApp.SCREEN_HEIGHT / 2 - 40);
        
        // 绘制倒计时
        gc.setFont(new javafx.scene.text.Font("Arial", 48));
        gc.setFill(javafx.scene.paint.Color.BLUE); // 修改：冰影陷阱HUD提示为蓝色
        gc.fillText(String.valueOf(remainingSeconds), PacManApp.SCREEN_WIDTH / 2 - 20, PacManApp.SCREEN_HEIGHT / 2 + 10);
        
        // 绘制按键提示
        gc.setFont(new javafx.scene.text.Font("Arial", 24));
        gc.setFill(javafx.scene.paint.Color.CYAN);
        gc.fillText("Press V to Break Ice!", PacManApp.SCREEN_WIDTH / 2 - 120, PacManApp.SCREEN_HEIGHT / 2 + 50);
    }
    
    /**
     * 渲染缚丝陷阱HUD
     * 新增：显示缚丝陷阱的绿色倒计时提示信息
     * @param gc 图形上下文对象
     */
    private void renderEntangledTrapHUD(GraphicsContext gc) {
        // 计算剩余时间
        long remainingTime = ENTANGLED_TRAP_TIMEOUT - (System.currentTimeMillis() - entangledTrapTimer);
        int remainingSeconds = (int)(remainingTime / 1000) + 1; // 向上取整
        
        // 绘制背景遮罩
        gc.setFill(javafx.scene.paint.Color.rgb(0, 255, 0, 0.3));
        gc.fillRect(PacManApp.SCREEN_WIDTH / 2 - 150, PacManApp.SCREEN_HEIGHT / 2 - 80, 300, 160);
        
        // 绘制提示标题
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 36));
        gc.fillText("ENTANGLED!", PacManApp.SCREEN_WIDTH / 2 - 90, PacManApp.SCREEN_HEIGHT / 2 - 30);
        
        // 绘制绿色倒计时
        gc.setFont(new javafx.scene.text.Font("Arial", 56));
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.fillText(String.valueOf(remainingSeconds), PacManApp.SCREEN_WIDTH / 2 - 25, PacManApp.SCREEN_HEIGHT / 2 + 20);
    }
    private void renderControlHints(GraphicsContext gc) {
        gc.setFill(javafx.scene.paint.Color.CYAN);
        gc.fillText("Controls:", 10, 80);
        gc.fillText("Arrow Keys - Move", 10, 100);
        gc.fillText("Q - Release Clone", 10, 120);
    }
    
    /**
     * 渲染操作提示HUD
     * Modify reason: 添加缺失的 renderControlHUD 方法以修复编译错误
     * @param gc 图形上下文对象
     */
    private void renderControlHUD(GraphicsContext gc) {
        // 调用现有的操作提示渲染方法，保持功能一致性
        renderControlHints(gc);
    }

    /**
     * 渲染游戏胜利屏幕
     * @param gc 图形上下文对象
     */
    private void renderWinScreen(GraphicsContext gc) {
        // 使用正确的常量名称：PacManApp.SCREEN_WIDTH 和 PacManApp.SCREEN_HEIGHT
        // 修改原因：保持与PacManApp类常量定义的一致性
        
        // 绘制背景遮罩
        gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, PacManApp.SCREEN_WIDTH, PacManApp.SCREEN_HEIGHT);
        
        // 绘制游戏胜利信息
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.setFont(new javafx.scene.text.Font("Arial", 48));
        gc.fillText("VICTORY!", PacManApp.SCREEN_WIDTH / 2 - 120, PacManApp.SCREEN_HEIGHT / 2 - 60);
        
        // 绘制最终得分
        gc.setFont(new javafx.scene.text.Font("Arial", 24));
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillText("Final Score: " + score, PacManApp.SCREEN_WIDTH / 2 - 100, PacManApp.SCREEN_HEIGHT / 2);
        
        // 绘制重新开始提示
        gc.fillText("Press SPACE to Play Again", PacManApp.SCREEN_WIDTH / 2 - 140, PacManApp.SCREEN_HEIGHT / 2 + 40);
    }
    
    /**
     * 渲染游戏信息
     * 照搬参考文件中的 HUD 排版和显示内容
     * 修改：添加技能数量显示、粉色鬼魂护盾层数显示以及蓝色鬼状态显示
     * 修改：增加死亡鬼魂的30秒倒计时显示
     * @param gc 图形上下文对象
     */
    /**
     * 渲染游戏信息
     * 修改：调整HUD元素之间的距离，使不同信息项有更清晰的视觉分离
     * @param gc 图形上下文对象
     */
    private void renderGameInfo(GraphicsContext gc) {
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 16));
        
        // 显示生命数、分数和技能数量 - 调整为垂直排列或增加水平间距
        int hudY = 20; // 初始Y坐标
        gc.fillText("Lives: " + String.valueOf(lives), TILE_SIZE/2, hudY);
        gc.fillText("Score: " + String.valueOf(score), TILE_SIZE/2, hudY + 25);
        gc.fillText("Skills: " + String.valueOf(powerFoodPlusSkillCount), TILE_SIZE/2, hudY + 50);
        
        // 显示鬼魂状态信息 - 修改：将鬼魂信息移动到屏幕左下角
        int statusY = PacManApp.SCREEN_HEIGHT - 120; // 从屏幕底部开始计算Y坐标
        gc.setFont(new javafx.scene.text.Font("Arial", 14));
        gc.fillText("orangeGhost: " + getGhostStatus("orange"), 20, statusY);
        
        // 显示粉色鬼魂状态和护盾层数
        int pinkShields = 3; // 默认值
        for (Ghost ghost : ghosts) {
            if (ghost instanceof PinkGhost) {
                pinkShields = ((PinkGhost) ghost).getShields();
                break;
            }
        }
        gc.fillText("pinkGhost: " + getGhostStatus("pink") + " Shield x" + pinkShields, 20, statusY + 20);
        
        gc.fillText("scaredGhost: " + getGhostStatus("scared"), 20, statusY + 40);
        gc.fillText("redGhost: " + getGhostStatus("red"), 20, statusY + 60);
        gc.fillText("blueGhost: " + getGhostStatus("blue"), 20, statusY + 80); // 添加蓝色鬼状态显示
    }
    
    /**
     * 获取鬼魂状态
     * @param ghostType 鬼魂类型
     * @return 鬼魂状态字符串
     */
    private String getGhostStatus(String ghostType) {
        for (Ghost ghost : ghosts) {
            if (ghostMatches(ghost, ghostType)) {
                // 检查是否为蓝色或橙色鬼魂的死亡状态
                if (ghost instanceof BlueGhost) {
                    BlueGhost blueGhost = (BlueGhost) ghost;
                    if (blueGhost.isDead()) {
                        long remainingTime = 30000 - (System.currentTimeMillis() - blueGhost.getDeathTime());
                        int secondsLeft = (int) (remainingTime / 1000) + 1;
                        return "died " + secondsLeft + "s";
                    }
                }
                if (ghost instanceof OrangeGhost) {
                    OrangeGhost orangeGhost = (OrangeGhost) ghost;
                    if (orangeGhost.isDead()) {
                        long remainingTime = 30000 - (System.currentTimeMillis() - orangeGhost.getDeathTime());
                        int secondsLeft = (int) (remainingTime / 1000) + 1;
                        return "died " + secondsLeft + "s";
                    }
                }
                // 恐慌状态处理
                if (ghostScaredMap.containsKey(ghost)) {
                    GhostScaredStatus status = ghostScaredMap.get(ghost);
                    if (status.isActive()) {
                        long remaining = status.getEndTime() - System.currentTimeMillis();
                        return "scared " + (remaining / 1000) + "s";
                    } else {
                        ghostScaredMap.remove(ghost);
                    }
                }
            }
        }
        return "normal";
    }
    
    /**
     * 判断鬼魂是否符合指定类型
     * @param ghost 要检查的鬼魂对象
     * @param type 鬼魂类型字符串
     * @return 是否符合指定类型
     */
    public boolean ghostMatches(Ghost ghost, String type) {
        // 精确匹配鬼魂类型
        if (type.equals("orange") && ghost instanceof OrangeGhost) return true;
        if (type.equals("pink") && ghost instanceof PinkGhost) return true;
        if (type.equals("red") && ghost instanceof RedGhost) return true;
        if (type.equals("blue") && ghost instanceof BlueGhost) return true; // 修改原因：修复蓝色鬼魂类型匹配
        if (type.equals("scared") && ghost.isScared()) return true; // 修改原因：根据是否恐慌状态匹配
        return false;
    }

    /**
     * 处理键盘输入
     * 修改原因：添加 V 键破冰功能，用于打破冰影陷阱效果
     * @param event 键盘事件
     */
    public void handleKeyEvent(KeyEvent event) {
        // 开始菜单处理
        if (currentState == GameState.START && event.getCode() == KeyCode.SPACE) {
            currentState = GameState.RUNNING;
            gameStarted = true;
            return;
        }
        // 游戏结束处理
        if (currentState == GameState.GAME_OVER && event.getCode() == KeyCode.SPACE) {
            resetGame();
            return;
        }
        
        // 冰影陷阱破冰功能 - 新增
        if (isPacmanFrozenByIceTrap && event.getCode() == KeyCode.V) {
            isPacmanFrozenByIceTrap = false;
            iceTrapFreezeTimer = 0;
            System.out.println("冰影陷阱已被打破，游戏恢复正常");
            return;
        }
        
        // 游戏进行中处理
        if (currentState == GameState.RUNNING) {
            // 方向键控制 PacMan - 新增：当被缚丝陷阱缠绕时禁用方向键
            Direction direction = Direction.fromKeyCode(event.getCode());
            if (direction != null && !isPacmanEntangledByTrap) {
                // 使用带墙壁信息的 updateDirection 方法，允许提前转向
                pacman.updateDirection(direction, walls);
            }
            // Q键使用强化技能（释放克隆体）
            else if (event.getCode() == KeyCode.Q && hasPowerFoodPlusSkill) {
                // 释放克隆体
                createPacmanClones();
                hasPowerFoodPlusSkill = false;
                powerFoodPlusSkillCount--;
            }
        }
        // 游戏胜利处理
        if (currentState == GameState.WIN && event.getCode() == KeyCode.SPACE) {
            resetGame();
            return;
        }
    }

    /**
     * 重置游戏到初始状态
     * 修改原因：按下空格键重开游戏时，直接进入运行状态而不是跳转开始界面，同时重置冰影陷阱状态
     */
    private void resetGame() {
        currentState = GameState.RUNNING;
        level = 1;
        score = 0;
        lives = 3;
        gameOver = false;
        gameStarted = true;
        powerFoodPlusSkillCount = 0;
        hasPowerFoodPlusSkill = false;
        isPacmanFrozenByIceTrap = false; // 重置冰影陷阱状态
        iceTrapFreezeTimer = 0;
        initialize();
    }

    /**
     * 创建 Pac-Man 克隆体
     * 修改原因：按照用户需求，只生成一个克隆体，而非四个
     * 修改内容：移除方向循环，只创建一个与Pacman当前方向相同的克隆体
     */
    private void createPacmanClones() {
        // 只创建一个克隆体，使用Pacman当前的方向
        PacmanClone clone = new PacmanClone(
            pacman.getX(),
            pacman.getY(),
            pacman.getDirection(),
            // 为克隆体获取合适的图像
            getCloneImage(pacman.getDirection())
        );
        pacmanClones.add(clone);
    }

    /**
     * 获取克隆体图像
     * @param dir 克隆体移动方向
     * @return 方向对应的图像
     */
    private Image getCloneImage(Direction dir) {
        // 返回与方向对应的图像
        switch(dir) {
            case UP:
                return pacman.getUpImage();
            case DOWN:
                return pacman.getDownImage();
            case LEFT:
                return pacman.getLeftImage();
            case RIGHT:
                return pacman.getRightImage();
            default:
                return pacman.getRightImage();
        }
    }

    /**
     * 初始化食物和能量豆
     * 
     * @param foodImage 普通食物图像
     * @param powerFoodImage 能量豆图像
     */
    private void initializeFoods(Image foodImage, Image powerFoodImage) {
        // 预设9个能量豆位置
        int[][] powerFoodPositions = {
            {1, 1}, {17, 1}, {1, 17}, {17, 17},
            {9, 1}, {1, 9}, {17, 9}, {9, 17},
            {9, 9}
        };
        
        // 创建能量豆
        for (int[] pos : powerFoodPositions) {
            int x = pos[0] * PacManApp.TILE_SIZE;
            int y = pos[1] * PacManApp.TILE_SIZE;
            PowerFood powerFood = new PowerFood(x, y, PacManApp.TILE_SIZE, PacManApp.TILE_SIZE, 50, powerFoodImage);
            powerFoods.add(powerFood);
        }
    }
    
    /**
     * 生成强化技能食物（Power Food Plus）
     * 直接替换9个普通豆子为技能食物
     * 修改：避免重复替换，确保恰好生成9个技能食物
     */
    private void generatePowerFoodPlus() {
        powerFoodsPlus.clear();
        int count = 0;

        // 转换食物列表为可随机选择的结构
        List<Food> foodList = new ArrayList<>(foods);
        Random random = new Random();

        // 确保只替换9个普通食物
        while (count < 9 && foodList.size() > 0) {
            // 随机选择一个普通食物
            int randomIndex = random.nextInt(foodList.size());
            Food food = foodList.get(randomIndex);
            foodList.remove(randomIndex);

            // 从普通食物中移除并替换为强化食物
            foods.remove(food);
            PowerFoodPlus powerFoodPlus = new PowerFoodPlus(
                food.getX(), food.getY(),
                PacManApp.TILE_SIZE, PacManApp.TILE_SIZE,
                10, random.nextInt(3), // 随机技能类型
                powerFoodImage
            );
            powerFoodsPlus.add(powerFoodPlus);
            
            count++;
        }
    }

    /**
     * 检查是否可以在指定位置放置食物
     * @param col 列
     * @param row 行
     * @return 如果可以放置返回true
     */
    private boolean canPlaceFood(int col, int row) {
        // 这是一个示例逻辑，实际需要根据地图数据来判断
        // 通常墙壁和障碍物位置不能放置食物
        return col > 0 && col < GRID_WIDTH - 1 && row > 0 && row < GRID_HEIGHT - 1;
    }

    /**
     * 检查给定位置是否可以移动（不与墙壁碰撞）
     * @param x 要检查的X坐标
     * @param y 要检查的Y坐标
     * @param width 对象宽度
     * @param height 对象高度
     * @return 可以移动返回true，否则返回false
     */
    private boolean canMove(double x, double y, double width, double height) {
        for (Wall wall : walls) {
            if (x < wall.getX() + wall.getWidth() &&
                x + width > wall.getX() &&
                y < wall.getY() + wall.getHeight() &&
                y + height > wall.getY()) {
                return false; // 与墙壁碰撞，不能移动
            }
        }
        return true; // 可以移动
    }

    /**
     * 处理所有碰撞
     */
    private void handleCollisions() {
        // PacMan 与食物碰撞
        Iterator<Food> foodIterator = foods.iterator();
        while (foodIterator.hasNext()) {
            Food food = foodIterator.next();
            if (collision(pacman, food)) {
                foodIterator.remove();
                score += 10;
            }
        }

        // PacMan 与能量豆碰撞
        Iterator<PowerFood> powerFoodIterator = powerFoods.iterator();
        while (powerFoodIterator.hasNext()) {
            PowerFood powerFood = powerFoodIterator.next();
            if (collision(pacman, powerFood)) {
                powerFoodIterator.remove();
                score += 50;
                powerFoodPlusSkillCount++;
                // 移除使鬼魂恐慌的功能：根据用户需求，鬼魂只能通过被克隆体击中进入恐慌
                System.out.println("吃到能量豆! 得分 +50");
            }
        }
        
        // PacMan 与强化技能食物碰撞
        Iterator<PowerFoodPlus> powerFoodPlusIterator = powerFoodsPlus.iterator();
        while (powerFoodPlusIterator.hasNext()) {
            PowerFoodPlus powerFoodPlus = powerFoodPlusIterator.next();
            if (collision(pacman, powerFoodPlus)) {
                powerFoodPlusIterator.remove();
                score += 10; // 修改：与参考保持一致，吃强化食物得10分
                hasPowerFoodPlusSkill = true;
                powerFoodPlusSkillCount++;
                System.out.println("获得技能食物! 技能剩余: " + powerFoodPlusSkillCount);
            }
        }

        // 处理 Pac-Man 与蓝色鬼魂冰影陷阱的碰撞 - 修改：适配冰影陷阱
        for (Ghost ghost : new ArrayList<>(ghosts)) {
            if (ghost instanceof BlueGhost) {
                BlueGhost blueGhost = (BlueGhost) ghost;
                if (blueGhost.isIceTrapSet()) {
                    if (collision(pacman, blueGhost.getIceTrapX(), blueGhost.getIceTrapY(), TILE_SIZE, TILE_SIZE)) {
                        // 触发冰影陷阱 - 设置 Pac-Man 冰冻状态和倒计时
                        isPacmanFrozenByIceTrap = true;
                        iceTrapFreezeTimer = System.currentTimeMillis();
                        blueGhost.removeIceTrap(); // 踩中后陷阱消失
                        System.out.println("注意：踩中冰影陷阱，10秒内按V键破冰！");
                    }
                }
            }
        }
        
        // 处理 Pac-Man 与橙色鬼魂缚丝陷阱的碰撞 - 新增
        for (Ghost ghost : new ArrayList<>(ghosts)) {
            if (ghost instanceof OrangeGhost) {
                OrangeGhost orangeGhost = (OrangeGhost) ghost;
                if (orangeGhost.isTrapSet()) {
                    if (collision(pacman, orangeGhost.getTrapX(), orangeGhost.getTrapY(), TILE_SIZE, TILE_SIZE)) {
                        // 触发缚丝陷阱 - 设置 Pac-Man 缠绕状态和倒计时
                        isPacmanEntangledByTrap = true;
                        entangledTrapTimer = System.currentTimeMillis();
                        orangeGhost.removeTrap(); // 踩中后陷阱消失
                        System.out.println("注意：踩中缚丝陷阱，3秒内无法移动！");
                    }
                }
            }
        }
        
        // 缚丝陷阱超时处理 - 新增
        if (isPacmanEntangledByTrap) {
            if (System.currentTimeMillis() - entangledTrapTimer >= ENTANGLED_TRAP_TIMEOUT) {
                // 3秒超时，恢复移动控制
                isPacmanEntangledByTrap = false;
                entangledTrapTimer = 0;
                System.out.println("缚丝陷阱效果结束，恢复移动控制！");
            }
        }
        
        // 冰影陷阱超时处理 - 新增
        if (isPacmanFrozenByIceTrap) {
            if (System.currentTimeMillis() - iceTrapFreezeTimer >= ICE_TRAP_TIMEOUT) {
                // 超时没按V键，PacMan死亡
                System.out.println("冰影陷阱超时，PacMan死亡");
                lives--;
                pacman.setLives(lives);
                if (lives == 0) {
                    gameOver = true;
                    currentState = GameState.GAME_OVER;
                } else {
                    resetPositionsWithoutResettingScoreAndLives();
                }
                isPacmanFrozenByIceTrap = false; // 重置冰影陷阱状态
                iceTrapFreezeTimer = 0;
            }
        }

        // PacMan 与鬼魂碰撞 - 使用集合副本来避免ConcurrentModificationException
        for (Ghost ghost : new ArrayList<>(ghosts)) {
            if (collision(pacman, ghost)) {
                // 检查并初始化鬼魂的恐慌状态
                if (!ghostScaredMap.containsKey(ghost)) {
                    ghostScaredMap.put(ghost, new GhostScaredStatus(0));
                }

                GhostScaredStatus scaredStatus = ghostScaredMap.get(ghost);
                if (scaredStatus.isActive()) {
                    // 恐慌状态：鬼魂被吃
                    score += 200; // 恐慌状态鬼魂被吃得分
                    System.out.println("吃鬼魂得分! 当前总分: " + score);
                    // 蓝色鬼魂被杀死后生成冰影陷阱 - 新增
                    if (ghost instanceof BlueGhost) {
                        BlueGhost blueGhost = (BlueGhost) ghost;
                        blueGhost.die();
                        System.out.println("蓝色幽灵被杀死，生成冰影陷阱！");
                    }
                    // 橙色鬼魂被杀死后生成缚丝陷阱 - 新增
                    if (ghost instanceof OrangeGhost) {
                        OrangeGhost orangeGhost = (OrangeGhost) ghost;
                        orangeGhost.die();
                        System.out.println("橙色幽灵被杀死，生成缚丝陷阱！");
                    } else {
                        // 其他鬼魂被杀死，直接重置位置
                        ghost.reset();
                    }
                    // 鬼魂重生计时器
                    ghostRespawnTimers.put(ghost, System.currentTimeMillis());
                } else {
                    // 非恐慌状态：Pacman 被吃
                    lives--;
                    pacman.setLives(lives);
                    if (lives == 0) {
                        gameOver = true;
                        currentState = GameState.GAME_OVER;
                    } else {
                        resetPositionsWithoutResettingScoreAndLives();
                    }
                }
            }
        }

        // 克隆体与墙壁碰撞 - 使用集合副本来避免ConcurrentModificationException
        List<PacmanClone> clonesToRemove = new ArrayList<>();
        for (PacmanClone clone : new ArrayList<>(pacmanClones)) {
            for (Wall wall : new ArrayList<>(walls)) {
                if (collision(clone, wall)) {
                    // 克隆体与墙壁碰撞，标记为需要移除
                    clonesToRemove.add(clone);
                }
            }
        }
        // 克隆体与鬼魂碰撞 - 使用集合副本来避免ConcurrentModificationException
        for (PacmanClone clone : new ArrayList<>(pacmanClones)) {
            for (Ghost ghost : new ArrayList<>(ghosts)) {
                if (collision(clone, ghost)) {
                    // 只有粉色鬼魂才有护盾，其他鬼魂没有
                    if (ghost instanceof PinkGhost) {
                        PinkGhost pinkGhost = (PinkGhost) ghost;
                        // 使用 PinkGhost 类的 takeDamage() 方法来减少护盾
                        if (pinkGhost.getShields() > 0) {
                            pinkGhost.takeDamage();
                            // 如果护盾被打破，进入恐慌状态
                            if (pinkGhost.getShields() <= 0) {
                                // 修改：克隆体破盾后恐慌状态持续15秒
                                ghostScaredMap.put(ghost, new GhostScaredStatus(15000));
                            }
                        } else {
                            // 护盾已被打破，进入恐慌状态
                            // 修改：克隆体破盾后恐慌状态持续15秒
                            ghostScaredMap.put(ghost, new GhostScaredStatus(15000));
                        }
                    } else {
                        // 其他类型的鬼魂直接进入恐慌状态
                        ghostScaredMap.put(ghost, new GhostScaredStatus(5000));
                    }
                    // 克隆体与鬼魂碰撞，标记为需要移除
                    clonesToRemove.add(clone);
                }
            }
        }
        // 一次性移除所有标记的克隆体
        pacmanClones.removeAll(clonesToRemove);
    }

    /**
     * 检测两个实体是否碰撞
     * @param entity1 第一个实体
     * @param entity2 第二个实体
     * @return 碰撞返回true，否则返回false
     */
    private boolean collision(Object entity1, Object entity2) {
        if (entity1 instanceof Entity && entity2 instanceof Entity) {
            Entity e1 = (Entity) entity1;
            Entity e2 = (Entity) entity2;
            return e1.getX() < e2.getX() + e2.getWidth() &&
                   e1.getX() + e1.getWidth() > e2.getX() &&
                   e1.getY() < e2.getY() + e2.getHeight() &&
                   e1.getY() + e1.getHeight() > e2.getY();
        } else if (entity1 instanceof PacmanClone && entity2 instanceof Entity) {
            PacmanClone clone = (PacmanClone) entity1;
            Entity e2 = (Entity) entity2;
            return clone.getX() < e2.getX() + e2.getWidth() &&
                   clone.getX() + clone.getWidth() > e2.getX() &&
                   clone.getY() < e2.getY() + e2.getHeight() &&
                   clone.getY() + clone.getHeight() > e2.getY();
        } else if (entity1 instanceof Entity && entity2 instanceof PacmanClone) {
            return collision(entity2, entity1);
        }
        return false;
    }

    /**
     * 检测实体与指定区域是否碰撞
     * @param entity 要检查的实体
     * @param x 区域的X坐标
     * @param y 区域的Y坐标
     * @param width 区域的宽度
     * @param height 区域的高度
     * @return 碰撞返回true，否则返回false
     */
    private boolean collision(Object entity, double x, double y, double width, double height) {
        if (entity instanceof Entity) {
            Entity e = (Entity) entity;
            return e.getX() < x + width &&
                   e.getX() + e.getWidth() > x &&
                   e.getY() < y + height &&
                   e.getY() + e.getHeight() > y;
        } else if (entity instanceof PacmanClone) {
            PacmanClone clone = (PacmanClone) entity;
            return clone.getX() < x + width &&
                   clone.getX() + clone.getWidth() > x &&
                   clone.getY() < y + height &&
                   clone.getY() + clone.getHeight() > y;
        }
        return false;
    }

    /**
     * 检查游戏胜利条件
     */
    private void checkWinCondition() {
        if (foods.isEmpty() && powerFoods.isEmpty()) {
            // 所有食物和能量豆都被吃掉，游戏胜利
            System.out.println("游戏胜利!");
            currentState = GameState.WIN;
            gameOver = true;
        }
    }

    /**
     * 重置所有实体位置，但不重置分数和生命
     * 修改：完全按照参考文件实现，只重置 Pac-Man 位置，不刷新豆子和鬼魂位置
     */
    private void resetPositionsWithoutResettingScoreAndLives() {
        // 只重置 Pac-Man 位置，不刷新豆子数量，不重置鬼魂位置
        pacman.reset();
        pacman.setVelocityX(0);
        pacman.setVelocityY(0);
        // 清理技能状态
        hasPowerFoodPlusSkill = false;
        powerFoodPlusSkillCount = 0;
        // 清除克隆体
        pacmanClones.clear();
    }

    /**
     * 重置所有实体位置（旧方法，保留用于游戏重置）
     */
    private void resetPositions() {
        initialize();
        score = 0;
        lives = 3;
        pacman.setLives(lives); // 同步生命数到 PacMan 类
        // 清理技能状态
        hasPowerFoodPlusSkill = false;
        powerFoodPlusSkillCount = 0;
        // 清除克隆体
        pacmanClones.clear();
    }

    /**
     * 检查游戏结束条件
     */
    private void checkGameOverCondition() {
        if (pacman.getLives() <= 0) {
            currentState = GameState.GAME_OVER;
        }
    }

    // Getters 和 Setters

    public PacMan getPacman() {
        return pacman;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    public int getScore() {
        return pacman != null ? pacman.getScore() : 0;
    }

    public int getLives() {
        return pacman != null ? pacman.getLives() : 0;
    }

    public int getLevel() {
        return level;
    }
}
