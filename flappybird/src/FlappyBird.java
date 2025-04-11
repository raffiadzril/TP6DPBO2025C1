import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.sampled.*;


public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int frameWidth = 360;
    int frameHeight = 640;

    Image backgroundImage;
    Image birdImage;
    Image lowerPipeImage;
    Image upperPipeImage;

    int playerStartPosX = frameWidth / 8;
    int playerStartPosY = frameHeight / 2;
    int playerWidth = 34;
    int playerHeight = 24;
    Player player;

    ArrayList<Pipe> pipes;
    int pipeStartPosX = frameWidth;
    int pipeWidth = 64;
    int pipeHeight = 512;

    Timer gameLoop;
    Timer pipesCooldown;
    int gravity = 1;

    boolean gameOver = false;
    JLabel scoreLabel;
    int score = 0;
    boolean passedPipe = false;

    Clip backgroundClip;


    public FlappyBird() {
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImage = new ImageIcon(getClass().getResource("assets/background1.png")).getImage();
        birdImage = new ImageIcon(getClass().getResource("assets/bird1.png")).getImage();
        lowerPipeImage = new ImageIcon(getClass().getResource("assets/lowerPipe1.png")).getImage();
        upperPipeImage = new ImageIcon(getClass().getResource("assets/upperPipe1.png")).getImage();

        player = new Player(playerStartPosX, playerStartPosY, playerWidth, playerHeight, birdImage);
        pipes = new ArrayList<>();

        pipesCooldown = new Timer(1500, e -> placePipes());
        pipesCooldown.start();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        try {
            InputStream is = getClass().getResourceAsStream("/assets/ARCADE_N.TTF");
            Font arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(24f);
            scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(arcadeFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24)); // fallback
        }


        scoreLabel.setForeground(Color.WHITE);
        setLayout(null);
        scoreLabel.setBounds(20, 20, 400, 40);
        add(scoreLabel);
        playBackgroundMusic();


    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, frameWidth, frameHeight, null);
        g.drawImage(player.getImage(), player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight(), null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.getImage(), pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight(), null);
        }

        if (gameOver) {
            // Load game over image (papan kayu)
            Image gameOverImg = new ImageIcon(getClass().getResource("assets/gameover.png")).getImage();
            int imgWidth = 300;
            int imgHeight = 150;
            int imgX = (frameWidth - imgWidth) / 2;
            int imgY = (frameHeight - imgHeight) / 2 - 30;

            // Draw background image (papan kayu)
            g.drawImage(gameOverImg, imgX, imgY, imgWidth, imgHeight, null);

            try {
                // Load custom font from assets
                InputStream is = getClass().getResourceAsStream("/assets/ARCADE_N.TTF");
                Font arcadeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18f);
                g.setFont(arcadeFont);
                Font arcadeFontTitle = arcadeFont.deriveFont(18f);
                Font arcadeFontScore = arcadeFont.deriveFont(12f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(arcadeFont);

                // Draw "GAME OVER" text in the center of the image
                g.setFont(arcadeFontTitle);
                g.setColor(Color.WHITE);
                String gameOverText = "GAME OVER";
                int gameOverTextX = imgX + (imgWidth - g.getFontMetrics().stringWidth(gameOverText)) / 2;
                int gameOverTextY = imgY + imgHeight / 2 - 10;
                g.drawString(gameOverText, gameOverTextX, gameOverTextY);

                // Draw score text below "GAME OVER"
                g.setFont(arcadeFontScore);
                String scoreText = "Score: " + score;
                int scoreTextX = imgX + (imgWidth - g.getFontMetrics().stringWidth(scoreText)) / 2;
                int scoreTextY = gameOverTextY + 30;
                g.drawString(scoreText, scoreTextX, scoreTextY);

            } catch (Exception e) {
                e.printStackTrace(); // Untuk debug jika font gagal diload
            }
        }



    }

    public void move() {
        if (!gameOver) {
            player.setVelocityY(player.getVelocityY() + gravity);
            player.setPosY(player.getPosY() + player.getVelocityY());
            player.setPosY(Math.max(player.getPosY(), 0));

            Rectangle playerRect = new Rectangle(player.getPosX(), player.getPosY(), player.getWidth(), player.getHeight());

            for (int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                pipe.setPosX(pipe.getPosX() + pipe.getVelocityX());

                Rectangle pipeRect = new Rectangle(pipe.getPosX(), pipe.getPosY(), pipe.getWidth(), pipe.getHeight());

                if (pipeRect.intersects(playerRect)) {
                    gameOver = true;
                    gameLoop.stop();
                    pipesCooldown.stop();
                    repaint();
                    stopBackgroundMusic();
                    return;
                }

                if (i % 2 != 0 && !pipe.isHasScored() && pipe.getPosX() + pipe.getWidth() < player.getPosX()) {
                    score++;
                    scoreLabel.setText("Score: " + score);
                    pipe.setHasScored(true);  // Supaya tidak menambah skor lagi
                }

                if (pipe.getPosX() + pipe.getWidth() < player.getPosX()) {
                    passedPipe = false;
                }
            }

            if (player.getPosY() + player.getHeight() >= frameHeight) {
                gameOver = true;
                gameLoop.stop();
                pipesCooldown.stop();
                stopBackgroundMusic();
                repaint();
            }
        }
    }

    public void placePipes() {
        int openingSpace = 150;
        int randomPosY = (int) (-pipeHeight / 2 - Math.random() * (pipeHeight / 2));

        Pipe upperPipe = new Pipe(pipeStartPosX, randomPosY, pipeWidth, pipeHeight, upperPipeImage);
        pipes.add(upperPipe);

        int lowerPipeY = randomPosY + pipeHeight + openingSpace;
        Pipe lowerPipe = new Pipe(pipeStartPosX, lowerPipeY, pipeWidth, pipeHeight, lowerPipeImage);
        pipes.add(lowerPipe);
    }
    public void playBackgroundMusic() {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/assets/bgm1.wav"));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioIn);

            FloatControl gainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-10.0f); // Volume

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.setFramePosition(0); // reset ke awal
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            player.setVelocityY(-10);
        } else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void restartGame() {
        player.setPosY(playerStartPosY);
        player.setVelocityY(0);
        pipes.clear();
        score = 0;
        scoreLabel.setText("Score: 0");
        gameOver = false;
        gameLoop.start();
        pipesCooldown.start();
        playBackgroundMusic();
    }
}
