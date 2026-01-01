package pacman;

import javafx.scene.input.KeyCode;

/**
 * 方向枚举类
 * 定义游戏中所有可能的移动方向
 *
 * @author PacMan Development Team
 * @version 2.0
 * @since 2024
 */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /**
     * 根据键盘事件转换为方向
     * @param keyCode 键盘按键码
     * @return 对应的方向
     */
    public static Direction fromKeyCode(KeyCode keyCode) {
        switch (keyCode) {
            case UP:
            case W:
                return UP;
            case DOWN:
            case S:
                return DOWN;
            case LEFT:
            case A:
                return LEFT;
            case RIGHT:
            case D:
                return RIGHT;
            default:
                return null;
        }
    }

    /**
     * 获取相反方向
     * @return 相反方向
     */
    public Direction getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                return null;
        }
    }

    /**
     * 判断是否为水平方向
     * @return true表示水平方向
     */
    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    /**
     * 判断是否为垂直方向
     * @return true表示垂直方向
     */
    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    /**
     * 获取X方向的速度分量
     * @param speed 速度大小
     * @return X方向速度
     */
    public double getVelocityX(double speed) {
        switch (this) {
            case LEFT:
                return -speed;
            case RIGHT:
                return speed;
            default:
                return 0;
        }
    }

    /**
     * 获取Y方向的速度分量
     * @param speed 速度大小
     * @return Y方向速度
     */
    public double getVelocityY(double speed) {
        switch (this) {
            case UP:
                return -speed;
            case DOWN:
                return speed;
            default:
                return 0;
        }
    }
}
