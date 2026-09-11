import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class StartPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final Color TABLE = new Color(22, 78, 58);
    private static final Color BUTTON = new Color(30, 44, 38);
    private static final Color EDGE = new Color(110, 130, 120);
    private static final Color HIGHLIGHT = new Color(255, 210, 80);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(180, 195, 188);

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 44);
    private static final Font HEADING_FONT = new Font("SansSerif", Font.PLAIN, 18);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 26);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private ArrayList<Rectangle> choices = new ArrayList<Rectangle>();
    private Point mouse = new Point(-1, -1);

    public StartPanel() {
        setBackground(TABLE);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
    }

    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(TABLE);
        g2.fillRect(0, 0, width, height);

        g2.setFont(TITLE_FONT);
        g2.setColor(TEXT);
        drawCentered(g2, "FLIP 7", width / 2, height / 2 - 140);

        g2.setFont(HEADING_FONT);
        g2.setColor(MUTED);
        drawCentered(g2, "How many players?", width / 2, height / 2 - 90);

        layoutChoices(width, height);
        for (int i = 0; i < choices.size(); i++) {
            drawChoice(g2, choices.get(i), Game.MIN_PLAYERS + i);
        }

        g2.setFont(BODY_FONT);
        g2.setColor(MUTED);
        int y = height / 2 + 70;
        drawCentered(g2, "First player past " + Game.TARGET_SCORE + " points wins.", width / 2, y + 64);

    }

    private void layoutChoices(int width, int height) {
        int count = Game.MAX_PLAYERS - Game.MIN_PLAYERS + 1;
        int size = 70;
        int gap = 18;
        int totalWidth = count * size + (count - 1) * gap;
        int x = (width - totalWidth) / 2;
        int y = height / 2 - 60;

        choices.clear();
        for (int i = 0; i < count; i++) {
            choices.add(new Rectangle(x + i * (size + gap), y, size, size));
        }
    }

    private void drawChoice(Graphics2D g2, Rectangle bounds, int players) {
        boolean hovered = bounds.contains(mouse);

        g2.setColor(BUTTON);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g2.setColor(hovered ? HIGHLIGHT : EDGE);
        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g2.setFont(BUTTON_FONT);
        g2.setColor(hovered ? HIGHLIGHT : TEXT);
        drawCentered(g2, Integer.toString(players), bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int centerY) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, centerX - fm.stringWidth(text) / 2, centerY + (fm.getAscent() - fm.getDescent()) / 2);
    }

    private void startGame(int players) {
        if (players < Game.MIN_PLAYERS || players > Game.MAX_PLAYERS) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof Frame)) {
            return;
        }
        Frame frame = (Frame) window;
        Panel panel = new Panel(players);
        frame.setCount(players);
        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= KeyEvent.VK_0 + Game.MIN_PLAYERS && code <= KeyEvent.VK_0 + Game.MAX_PLAYERS) {
            startGame(code - KeyEvent.VK_0);
            return;
        }

        char key = e.getKeyChar();
        if (key >= '0' + Game.MIN_PLAYERS && key <= '0' + Game.MAX_PLAYERS) {
            startGame(key - '0');
        }
    }

    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).contains(e.getPoint())) {
                startGame(Game.MIN_PLAYERS + i);
                return;
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        mouse = e.getPoint();
        repaint();
    }

    public void mouseExited(MouseEvent e) {
        mouse = new Point(-1, -1);
        repaint();
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
