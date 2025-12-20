import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    class PowerFoodPlus {
        int x;
        int y;
        int width = 4;
        int height = 4;
        Image image;

        PowerFoodPlus(Image image, int x, int y) {
            this.image = image;
            this.x = x;
            this.y = y;
        }
    }

    class PacmanClone {
        int x;
        int y;
        int width = tileSize;
        int height = tileSize;
        char direction;
        int velocityX = 0;
        int velocityY = 0;
        Image image;
        float rotation = 0; 
        PacmanClone(int x, int y, char direction, Image image) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.image = image;
            updateVelocity();
        }

        void updateVelocity() {
            // Clone speed is 1.5x faster than pacman
            int cloneSpeed = (int)(tileSize / 4 * 1.5);
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -cloneSpeed;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = cloneSpeed;
            }
            else if (this.direction == 'L') {
                this.velocityX = -cloneSpeed;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = cloneSpeed;
                this.velocityY = 0;
            }
        }

        void move() {
            this.x += this.velocityX;
            this.y += this.velocityY;
            this.rotation += 10; // Rotate 10 degrees each move
            if (this.rotation >= 360) {
                this.rotation -= 360;
            }
        }
    }

    class GhostScaredStatus {
        long startTime;
        long duration = 15000; // 15 seconds

        GhostScaredStatus() {
            this.startTime = System.currentTimeMillis();
        }

        boolean isActive() {
            return System.currentTimeMillis() - startTime < duration;
        }

        long getRemainingTime() {
            long remaining = duration - (System.currentTimeMillis() - startTime);
            return Math.max(0, remaining);
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image powerFoodPlusImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    // X = wall, O = skip, P = pac man, ' ' = food
    // Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X      bpo        X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    HashSet<PowerFoodPlus> powerFoodsPlus;
    HashSet<PacmanClone> pacmanClones;
    Block pacman;

    boolean hasPowerFoodPlusSkill = false;
    HashMap<Block, GhostScaredStatus> ghostScaredMap;
    HashSet<Block> deadGhosts;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        powerFoodPlusImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        powerFoodsPlus = new HashSet<PowerFoodPlus>();
        pacmanClones = new HashSet<PacmanClone>();
        ghostScaredMap = new HashMap<Block, GhostScaredStatus>();
        hasPowerFoodPlusSkill = false;

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }


        generatePowerFoodPlus();
    }

    public void generatePowerFoodPlus() {
        powerFoodsPlus.clear();
        int count = 0;
        int maxAttempts = 100;
        int attempts = 0;

        // Convert foods to list for random selection
        java.util.List<Block> foodList = new java.util.ArrayList<>(foods);

        while (count < 9 && attempts < maxAttempts && foodList.size() > 0) {
            int randomIndex = random.nextInt(foodList.size());
            Block food = foodList.get(randomIndex);
            foodList.remove(randomIndex);

            PowerFoodPlus plus = new PowerFoodPlus(powerFoodPlusImage, food.x, food.y);
            powerFoodsPlus.add(plus);
            foods.remove(food);
            count++;
            attempts++;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // Draw rotating pacman clones
        for (PacmanClone clone : pacmanClones) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform oldTransform = g2d.getTransform();
            
            // Rotate around center
            int centerX = clone.x + clone.width / 2;
            int centerY = clone.y + clone.height / 2;
            g2d.rotate(Math.toRadians(clone.rotation), centerX, centerY);
            
            g2d.drawImage(clone.image, clone.x, clone.y, clone.width, clone.height, null);
            g2d.setTransform(oldTransform);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw power food plus
        for (PowerFoodPlus plus : powerFoodsPlus) {
            g.drawImage(plus.image, plus.x, plus.y, plus.width, plus.height, null);
        }

        // Draw score board and ghost status info
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }

        // Draw ghost status information
        int statusY = boardHeight - 80;
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("orangeGhost: " + getGhostStatus("orange"), 20, statusY);
        g.drawString("pinkGhost: " + getGhostStatus("pink"), 20, statusY + 20);
        g.drawString("scaredGhost: " + getGhostStatus("scared"), 20, statusY + 40);
        g.drawString("redGhost: " + getGhostStatus("red"), 20, statusY + 60);
    }

    private String getGhostStatus(String ghostType) {
        for (Block ghost : ghosts) {
            if (ghostMatches(ghost, ghostType)) {
                if (ghostScaredMap.containsKey(ghost)) {
                    GhostScaredStatus status = ghostScaredMap.get(ghost);
                    if (status.isActive()) {
                        long remaining = status.getRemainingTime();
                        return (remaining / 1000) + "s";
                    } else {
                        ghostScaredMap.remove(ghost);
                    }
                }
                return "Disembodied";
            }
        }
        return "Died";
    }

    private boolean ghostMatches(Block ghost, String type) {
        if (type.equals("orange") && ghost.image == orangeGhostImage) return true;
        if (type.equals("pink") && ghost.image == pinkGhostImage) return true;
        if (type.equals("red") && ghost.image == redGhostImage) return true;
        if (type.equals("scared") && ghost.image == blueGhostImage) return true;
        return false;
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (ghostScaredMap.containsKey(ghost) && ghostScaredMap.get(ghost).isActive()) {
                    ghosts.remove(ghost);
                    ghostScaredMap.remove(ghost);
                    score += 300; // 3x multiplier for scared ghost score
                } else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                }
                break;
            }

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // Handle clone movement and collision
        PacmanClone cloneToRemove = null;
        for (PacmanClone clone : pacmanClones) {
            clone.move();

            // Check collision with walls
            for (Block wall : walls) {
                if (collision(clone, wall)) {
                    cloneToRemove = clone;
                    break;
                }
            }

            if (cloneToRemove == null) {
                // Check collision with ghosts
                for (Block ghost : ghosts) {
                    if (collision(clone, ghost)) {
                        ghostScaredMap.put(ghost, new GhostScaredStatus());
                        cloneToRemove = clone;
                        break;
                    }
                }
            }

            // Check boundary collision
            if (cloneToRemove == null && (clone.x < 0 || clone.x + clone.width > boardWidth ||
                    clone.y < 0 || clone.y + clone.height > boardHeight)) {
                cloneToRemove = clone;
            }
        }
        if (cloneToRemove != null) {
            pacmanClones.remove(cloneToRemove);
        }

        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Check power food plus collision
        PowerFoodPlus plusEaten = null;
        for (PowerFoodPlus plus : powerFoodsPlus) {
            if (collision(pacman, plus)) {
                plusEaten = plus;
                hasPowerFoodPlusSkill = true;
                score += 10; 
            }
        }
        powerFoodsPlus.remove(plusEaten);

        if (foods.isEmpty() && powerFoodsPlus.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private boolean collision(PacmanClone clone, Block block) {
        return  clone.x < block.x + block.width &&
                clone.x + clone.width > block.x &&
                clone.y < block.y + block.height &&
                clone.y + clone.height > block.y;
    }

    private boolean collision(Block block, PowerFoodPlus plus) {
        return  block.x < plus.x + plus.width &&
                block.x + block.width > plus.x &&
                block.y < plus.y + plus.height &&
                block.y + block.height > plus.y;
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        pacmanClones.clear();
        hasPowerFoodPlusSkill = false;
        
        for (Block ghost : ghosts) {
            ghost.reset();
            ghostScaredMap.remove(ghost);
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }
        else if (e.getKeyCode() == KeyEvent.VK_Q && hasPowerFoodPlusSkill) {
            Image cloneImage = null;
            if (pacman.direction == 'U') {
                cloneImage = pacmanUpImage;
            } else if (pacman.direction == 'D') {
                cloneImage = pacmanDownImage;
            } else if (pacman.direction == 'L') {
                cloneImage = pacmanLeftImage;
            } else if (pacman.direction == 'R') {
                cloneImage = pacmanRightImage;
            }
            
            PacmanClone clone = new PacmanClone(pacman.x, pacman.y, pacman.direction, cloneImage);
            pacmanClones.add(clone);
            hasPowerFoodPlusSkill = false; // Only one clone per powerfoodplus
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
}
