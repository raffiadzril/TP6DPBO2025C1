import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class App {
    static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showMainMenu();
        });
    }

    public static void showMainMenu() {
        frame = new JFrame("Flappy Bird - Menu");
        frame.setSize(360, 640);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(null);

        JLabel background = new JLabel(new ImageIcon(App.class.getResource("assets/background1.png")));
        background.setBounds(0, 0, 360, 640);
        frame.add(background);

        JButton startButton = new JButton(new ImageIcon(App.class.getResource("assets/start_button1.png")));
        startButton.setBounds(0, 250, 350, 100);
        startButton.setContentAreaFilled(false);
        startButton.setBorderPainted(false);
        background.add(startButton);

        startButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            startGame   ();
        });

        frame.setVisible(true);
    }

    public static void startGame() {
        FlappyBird flappyBird = new FlappyBird();
        frame.setContentPane(flappyBird);
        frame.revalidate();
        flappyBird.requestFocus();
    }
}
