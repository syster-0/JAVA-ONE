package pacman;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyEvent;
import javafx.animation.AnimationTimer;

/**
 * Pac-Man游戏应用程序主类
 * 负责初始化游戏界面、启动游戏循环和处理用户输入
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public class PacManApp extends Application {
    public static final int GRID_WIDTH = 19;    // 网格宽度（19个格子）
    public static final int GRID_HEIGHT = 21;   // 网格高度（21个格子）
    public static final int TILE_SIZE = 32;     // 每个格子的大小（像素）
    public static final int SCREEN_WIDTH = GRID_WIDTH * TILE_SIZE;
    public static final int SCREEN_HEIGHT = GRID_HEIGHT * TILE_SIZE;

    private GameEngine gameEngine;
    private Canvas canvas;
    private GraphicsContext gc;
    private long lastUpdateTime;

    @Override
    public void start(Stage primaryStage) {
        // 创建Canvas
        canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // 初始化游戏引擎
        gameEngine = new GameEngine(canvas);
        gameEngine.initialize();

        // 设置舞台和场景
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

        // 键盘事件处理
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            gameEngine.handleKeyEvent(event);
        });

        primaryStage.setTitle("Pac-Man - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // 启动游戏主循环
        startGameLoop();
    }

    /**
     * 启动游戏主循环
     */
    private void startGameLoop() {
        lastUpdateTime = System.nanoTime();

        new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                // 更新游戏逻辑
                update(currentTime);
                // 渲染游戏
                render();
            }
        }.start();
    }

    /**
     * 更新游戏逻辑
     * @param currentTime 当前时间
     */
    private void update(long currentTime) {
        // 计算时间差
        double deltaTime = (currentTime - lastUpdateTime) / 1e9;
        lastUpdateTime = currentTime;

        // 限制更新频率
        if (deltaTime > 0.05) {
            deltaTime = 0.05;
        }

        // 更新游戏引擎
        gameEngine.update((long)(deltaTime * 1000));
    }

    /**
     * 渲染游戏
     */
    private void render() {
        // 清除画布
        gc.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // 渲染游戏
        gameEngine.render(gc);
    }

    /**
     * 主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
}
