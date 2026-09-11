import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Panel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private static final double CARD_RATIO = 238.0 / 333.0;

    private static final Color TABLE = new Color(22, 78, 58);
    private static final Color ROW = new Color(30, 44, 38);
    private static final Color ROW_EDGE = new Color(110, 130, 120);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(180, 195, 188);
    private static final Color HIGHLIGHT = new Color(255, 210, 80);
    private static final Color HIT_COLOR = new Color(60, 150, 90);
    private static final Color STAY_COLOR = new Color(70, 120, 180);
    private static final Color DISABLED = new Color(90, 100, 95);

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 18);

    private int cnt;
    private Game game1;
    private HashMap<String, BufferedImage> cardImages;

    private Rectangle hitButton = new Rectangle();
    private Rectangle stayButton = new Rectangle();
    private ArrayList<Rectangle> playerRows = new ArrayList<Rectangle>();
    private Point mouse = new Point(-1, -1);

    public Panel(int count) {
        this.cnt = Math.max(Game.MIN_PLAYERS, Math.min(count, Game.MAX_PLAYERS));

        setBackground(TABLE);
        game1 = new Game(cnt);
        game1.init();

        cardImages = loadCardImages();

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    public Game getGame() {
        return game1;
    }

    public int getPlayerCount() {
        return cnt;
    }

    private static String[] cardNames() {
        return new String[]{
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "flip_3", "flip_7", "freeze", "second_chance",
            "plus_2", "plus_4", "plus_6", "plus_8", "plus_10", "times_2"
        };
    }

    private static HashMap<String, BufferedImage> loadCardImages() {
        HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
        String[] names = cardNames();
        for (int i = 0; i < names.length; i++) {
            BufferedImage image = readImage(names[i]);
            if (image != null) {
                images.put(names[i], image);
            } else {
                System.out.println("Missing card image for " + names[i]);
            }
        }
        return images;
    }

    private static BufferedImage readImage(String name) {
        try {
            InputStream in = Panel.class.getResourceAsStream("/cards/" + name + ".png");
            if (in != null) {
                BufferedImage image = ImageIO.read(in);
                in.close();
                return image;
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        String[] folders = {"src/cards", "cards", "../src/cards"};
        for (int i = 0; i < folders.length; i++) {
            File file = new File(folders[i], name + ".png");
            if (file.isFile()) {
                try {
                    return ImageIO.read(file);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        return null;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int width = getWidth();
        int height = getHeight();

        layout(width, height);

        g2.setColor(TABLE);
        g2.fillRect(0, 0, width, height);

        drawHeader(g2, width);

        ArrayList<Player> players = game1.getState();
        for (int i = 0; i < players.size() && i < playerRows.size(); i++) {
            drawPlayer(g2, players.get(i), playerRows.get(i), i);
        }

        drawFooter(g2, width, height);
    }

    private void layout(int width, int height) {
        int margin = 20;
        int buttonWidth = 140;
        int buttonHeight = 46;
        int buttonY = height - buttonHeight - 20;

        stayButton.setBounds(width - margin - buttonWidth, buttonY, buttonWidth, buttonHeight);
        hitButton.setBounds(stayButton.x - 14 - buttonWidth, buttonY, buttonWidth, buttonHeight);

        int top = 70;
        int bottom = buttonY - 16;
        int count = game1.getState().size();
        int gap = 8;
        int rowHeight = Math.max(90, (bottom - top - (count - 1) * gap) / count);

        playerRows.clear();
        for (int i = 0; i < count; i++) {
            playerRows.add(new Rectangle(margin, top + i * (rowHeight + gap), width - margin * 2, rowHeight));
        }
    }

    private void drawHeader(Graphics2D g2, int width) {
        int x = 20;
        BufferedImage logo = cardImages.get("flip_7");
        if (logo != null) {
            int logoHeight = 44;
            int logoWidth = (int) Math.round(logoHeight * CARD_RATIO);
            g2.drawImage(logo, x, 10, logoWidth, logoHeight, null);
            x = x + logoWidth + 10;
        }

        g2.setFont(TITLE_FONT);
        g2.setColor(TEXT);
        g2.drawString("FLIP 7", x, 33);

        g2.setFont(BODY_FONT);
        g2.setColor(MUTED);
        g2.drawString("Round " + game1.getRoundCount() + "   First to " + Game.TARGET_SCORE, x, 52);

        drawRightAligned(g2, "Deck: " + game1.getCardsLeft() + " cards", width - 20, 33);
    }

    private void drawPlayer(Graphics2D g2, Player player, Rectangle row, int index) {
        boolean isCurrent = index == game1.getCurrentPlayerIndex() && !game1.isRoundFinished() && player.isActive();
        boolean targetable = game1.canTarget(player);

        g2.setColor(ROW);
        g2.fillRect(row.x, row.y, row.width, row.height);
        if (isCurrent || targetable) {
            g2.setColor(HIGHLIGHT);
        } else {
            g2.setColor(ROW_EDGE);
        }
        g2.drawRect(row.x, row.y, row.width, row.height);

        int textX = row.x + 12;
        String name = player.getName() + " (" + (index + 1) + ")";
        if (index == game1.getDealerIndex()) {
            name = name + "  DEALER";
        }

        g2.setFont(NAME_FONT);
        if (isCurrent || targetable) {
            g2.setColor(HIGHLIGHT);
        } else {
            g2.setColor(TEXT);
        }
        g2.drawString(name, textX, row.y + 22);

        g2.setFont(BODY_FONT);
        g2.setColor(TEXT);
        g2.drawString("Score: " + player.getScore(), textX, row.y + 44);
        g2.drawString("Hand: " + player.checkTotal(), textX, row.y + 62);

        if (player.hasFlippedSeven() && player.scoresThisRound()) {
            g2.setColor(HIGHLIGHT);
            g2.drawString("FLIP 7!", textX, row.y + 80);
        } else {
            g2.setColor(stateColor(player));
            g2.drawString(player.getState().toUpperCase(), textX, row.y + 80);
        }

        if (game1.isRoundFinished()) {
            g2.setColor(MUTED);
            drawRightAligned(g2, "this round: +" + player.getLastRoundPoints(), row.x + row.width - 12, row.y + 22);
        } else if (targetable) {
            g2.setColor(HIGHLIGHT);
            drawRightAligned(g2, "click to choose", row.x + row.width - 12, row.y + 22);
        }

        int handX = row.x + 200;
        drawHand(g2, player, new Rectangle(handX, row.y + 8, row.width - (handX - row.x) - 12, row.height - 16));
    }

    private Color stateColor(Player player) {
        String state = player.getState();
        if (state.equals(Player.BUSTED)) {
            return new Color(240, 110, 100);
        }
        if (state.equals(Player.FROZEN)) {
            return new Color(130, 200, 240);
        }
        if (state.equals(Player.STAYED)) {
            return new Color(240, 200, 110);
        }
        return new Color(140, 220, 160);
    }

    private void drawHand(Graphics2D g2, Player player, Rectangle area) {
        ArrayList<Card> hand = player.getHand();
        if (hand.isEmpty() || area.width <= 0) {
            return;
        }

        int cardHeight = Math.max(50, Math.min(area.height, 110));
        int cardWidth = (int) Math.round(cardHeight * CARD_RATIO);
        int spacing = cardWidth + 6;
        if (hand.size() > 1) {
            int needed = cardWidth + (hand.size() - 1) * spacing;
            if (needed > area.width) {
                spacing = Math.max(14, (area.width - cardWidth) / (hand.size() - 1));
            }
        }

        int y = area.y + (area.height - cardHeight) / 2;
        for (int i = 0; i < hand.size(); i++) {
            drawCard(g2, hand.get(i), area.x + i * spacing, y, cardWidth, cardHeight);
        }
    }

    private void drawCard(Graphics2D g2, Card card, int x, int y, int width, int height) {
        BufferedImage image = cardImages.get(card.getCardID());
        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
            return;
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);
        g2.setFont(BODY_FONT);
        drawCentered(g2, card.getLabel(), x + width / 2, y + height / 2);
    }

    private void drawFooter(Graphics2D g2, int width, int height) {
        String message = game1.getMessage();
        g2.setFont(BODY_FONT);
        g2.setColor(TEXT);
        if (message != null && message.length() > 0) {
            g2.drawString(message, 20, height - 48);
        }

        g2.setColor(MUTED);
        g2.drawString(hintText(), 20, height - 26);

        boolean canAct = game1.getGameMode().equals(Game.MODE_WAITING_FOR_ACTION);
        drawButton(g2, hitButton, "HIT", HIT_COLOR, canAct);
        drawButton(g2, stayButton, "STAY", STAY_COLOR, canAct);
    }

    private String hintText() {
        if (game1.isGameOver()) {
            return "Game over. Close the window to quit.";
        }
        if (game1.getGameMode().equals(Game.MODE_WAITING_FOR_START)) {
            return "Round over - click anywhere or press SPACE to deal the next round.";
        }
        if (game1.getGameMode().equals(Game.MODE_FREEZE_CHOICE)) {
            return "FREEZE: click the player who is frozen, or press their number key.";
        }
        if (game1.getGameMode().equals(Game.MODE_FLIP3_CHOICE)) {
            return "FLIP 3: click the player who draws three cards, or press their number key.";
        }
        if (game1.getGameMode().equals(Game.MODE_SECOND_CHANCE_CHOICE)) {
            return "SECOND CHANCE: click the player who keeps it, or press their number key.";
        }
        return "HIT draws a card (H or 1).  STAY keeps your hand (S or 2).";
    }

    private void drawButton(Graphics2D g2, Rectangle bounds, String label, Color color, boolean enabled) {
        boolean hovered = enabled && bounds.contains(mouse);

        if (enabled) {
            g2.setColor(color);
        } else {
            g2.setColor(DISABLED);
        }
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (hovered) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(ROW_EDGE);
        }
        g2.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g2.setFont(BUTTON_FONT);
        if (enabled) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(new Color(160, 170, 165));
        }
        drawCentered(g2, label, bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int centerY) {
        if (text == null) {
            return;
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, centerX - fm.stringWidth(text) / 2, centerY + (fm.getAscent() - fm.getDescent()) / 2);
    }

    private void drawRightAligned(Graphics2D g2, String text, int rightX, int baselineY) {
        if (text == null) {
            return;
        }
        g2.drawString(text, rightX - g2.getFontMetrics().stringWidth(text), baselineY);
    }

    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
        Point point = e.getPoint();

        if (game1.isGameOver()) {
            return;
        }
        if (game1.getGameMode().equals(Game.MODE_WAITING_FOR_START)) {
            game1.dealRound();
            repaint();
            return;
        }
        if (game1.isChoosingTarget()) {
            for (int i = 0; i < playerRows.size(); i++) {
                if (playerRows.get(i).contains(point) && game1.canTarget(game1.getState().get(i))) {
                    game1.chooseTarget(i);
                    repaint();
                    return;
                }
            }
            return;
        }
        if (hitButton.contains(point)) {
            game1.hit();
            repaint();
        } else if (stayButton.contains(point)) {
            game1.stay();
            repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        boolean wasOver = hitButton.contains(mouse) || stayButton.contains(mouse);
        mouse = e.getPoint();
        boolean isOver = hitButton.contains(mouse) || stayButton.contains(mouse);
        if (wasOver != isOver) {
            repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (game1.isGameOver()) {
            return;
        }

        if (game1.getGameMode().equals(Game.MODE_WAITING_FOR_START)) {
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                game1.dealRound();
                repaint();
            }
            return;
        }
        if (game1.isChoosingTarget()) {
            if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
                game1.processInput(code - KeyEvent.VK_0);
                repaint();
            }
            return;
        }
        if (code == KeyEvent.VK_H || code == KeyEvent.VK_1) {
            game1.processInput(Game.KEY_HIT);
            repaint();
        } else if (code == KeyEvent.VK_S || code == KeyEvent.VK_2) {
            game1.processInput(Game.KEY_STAY);
            repaint();
        }
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
