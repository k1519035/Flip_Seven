import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Frame extends JFrame {
    private static final int PREFERRED_WIDTH = 1280;
    private static final int PREFERRED_HEIGHT = 820;
    private static final int MIN_WIDTH = 900;
    private static final int MIN_HEIGHT = 620;

    private int playerCount;

    public Frame(String framename, JPanel panel) {
        super(framename);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setSize(windowSize());
        setLocationRelativeTo(null);
        setContentPane(panel);
        setVisible(true);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    public void setCount(int n) {
        playerCount = n;
    }

    public int getCount() {
        return playerCount;
    }

    private static Dimension windowSize() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(PREFERRED_WIDTH, (int) (screen.width * 0.9));
        int height = Math.min(PREFERRED_HEIGHT, (int) (screen.height * 0.9));
        return new Dimension(Math.max(width, MIN_WIDTH), Math.max(height, MIN_HEIGHT));
    }
}
