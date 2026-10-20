import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.*;



public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private static final int TILE_SIZE = 25;
    private final int boardWidth;
    private final int boardHeight;

    private class Tile {
        int x, y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Tile snakeHead;
    private LinkedList<Tile> snakeBody; 
    private Tile food;
    private Timer gameLoop;
    private int dx = 1, dy = 0;
    private boolean gameOver = false, gameStarted = false;
    private final Random random = new Random();

    public SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.gray);
        setFocusable(true);
        addKeyListener(this);
        setupGame();

        //By passing "this", it is telling the Timer that SnakeGame will handle each timer tick by executing its "actionPerformed" method.
        gameLoop = new Timer(140, this); //creates a timer object

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameStarted && withinStartButton(e.getX(), e.getY())) {
                    startGame();
                }
            }
        });
    }

    private void setupGame() {
        snakeHead = new Tile(5, 5);
        snakeBody = new LinkedList<>(); 
        placeFood();
    }

    private boolean withinStartButton(int x, int y) {
        int btnWidth = 300, btnHeight = 100;
        int btnX = (boardWidth - btnWidth) / 2;
        int btnY = (boardHeight - btnHeight) / 2;
        return x >= btnX && x <= btnX + btnWidth && y >= btnY && y <= btnY + btnHeight;
    }

    private void startGame() {
        gameStarted = true;
        requestFocusInWindow();
        gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        drawGrid(g);
        if (!gameStarted) {
            drawStartButton(g);
        } else {
            drawFood(g);
            drawSnake(g);
            if (gameOver) drawGameOver(g);
            else drawScore(g);
        }
    }
    // ****************  draw methods  *******************
    private void drawGrid(Graphics g) {
        g.setColor(Color.lightGray);
        for (int i = 0; i < boardWidth / TILE_SIZE; i++) {
            //g.drawLine(x1, y1, x2, y2)
            // 0 is starting point of x and y coordinates
            g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, boardHeight); //draws vertical line on the grid
            g.drawLine(0, i * TILE_SIZE, boardWidth, i * TILE_SIZE); //draws horizontal line on the grid
        }
    }

    private void drawStartButton(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int btnWidth = 300, btnHeight = 100;
        int btnX = (boardWidth - btnWidth) / 2;
        int btnY = (boardHeight - btnHeight) / 2;

        g2.setColor(Color.white);
        g2.fillRoundRect(btnX, btnY, btnWidth, btnHeight, 50, 50);

        g2.setColor(new Color(76, 175, 80));
        g2.setStroke(new BasicStroke(6));
        g2.drawRoundRect(btnX, btnY, btnWidth, btnHeight, 50, 50);

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String startText = "START";
        FontMetrics fm = g2.getFontMetrics();
        int textX = btnX + (btnWidth - fm.stringWidth(startText)) / 2;
        int textY = btnY + fm.getAscent() + 20;
        g2.drawString(startText, textX, textY);

        g2.dispose();
    }

    private void drawFood(Graphics g) {
        g.setColor(Color.orange);
        g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawSnake(Graphics g) {
        g.setColor(Color.green);
        g.fillRect(snakeHead.x * TILE_SIZE, snakeHead.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        for (Tile part : snakeBody) {
            g.fillRect(part.x * TILE_SIZE, part.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawGameOver(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boxWidth = 400, boxHeight = 150;
        int boxX = (boardWidth - boxWidth) / 2;
        int boxY = (boardHeight - boxHeight) / 2;

        g2.setColor(Color.white);
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 30, 30);

        g2.setColor(Color.red);
        g2.setStroke(new BasicStroke(6));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 30, 30);

        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2.getFontMetrics();
        int textX = boxX + (boxWidth - fm.stringWidth(gameOverText)) / 2;
        int textY = boxY + fm.getAscent() + 20;
        g2.drawString(gameOverText, textX, textY);

        String finalScoreText = "Final Score: " + score();
        textX = boxX + (boxWidth - fm.stringWidth(finalScoreText)) / 2;
        textY += 50;
        g2.drawString(finalScoreText, textX, textY);

        g2.dispose();
    }

    // snakeBody also holds the head, so subtract 1 to count only the food eaten
    private int score() {
        return Math.max(0, snakeBody.size() - 1);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score(), 10, 20);
    }
    // ****************  draw methods  *******************


    private void placeFood() {
        int maxX = boardWidth / TILE_SIZE;
        int maxY = boardHeight / TILE_SIZE;
        food = new Tile(random.nextInt(maxX), random.nextInt(maxY));
    }

    private void moveSnake() {

        Tile newHead = new Tile(snakeHead.x + dx, snakeHead.y + dy);

        if (outOfBounds(newHead) || selfCollision(newHead)) {
            gameOver = true;
            gameLoop.stop();
            return; 
        }
        snakeBody.addFirst(newHead); 
        snakeHead = newHead; 

        if (collidesWith(food)) {
            placeFood();
        } else {
           
             if (snakeBody.size() > 1) { 
            snakeBody.removeLast();
            }
        }
    }

    private boolean collidesWith(Tile other) {
        return snakeHead.x == other.x && snakeHead.y == other.y;
    }

    private boolean outOfBounds(Tile newHead) {
        return newHead.x < 0 || newHead.x >= boardWidth / TILE_SIZE || 
        newHead.y < 0 || newHead.y >= boardHeight / TILE_SIZE;
    }

    private boolean selfCollision(Tile newHead) {
        for (Tile part : snakeBody) {
            if (newHead.x == part.x && newHead.y == part.y) {
                return true; 
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver) {
            moveSnake();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> { if (dy == 0) { dx = 0; dy = -1; } }
            case KeyEvent.VK_DOWN -> { if (dy == 0) { dx = 0; dy = 1; } }
            case KeyEvent.VK_LEFT -> { if (dx == 0) { dx = -1; dy = 0; } }
            case KeyEvent.VK_RIGHT -> { if (dx == 0) { dx = 1; dy = 0; } }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame(700, 700);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
