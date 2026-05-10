import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Flip Card — Memory Matching Game
 * Pure Java Swing, no external libraries.
 */
public class FlipCardGame extends JFrame {

    // ── Palette ──────────────────────────────────────
    static final Color BG       = new Color(139, 186, 115);
    static final Color GREEN    = new Color(95,  175,  60);
    static final Color GREEN_DK = new Color(68,  140,  40);
    static final Color YELLOW   = new Color(240, 175,  50);
    static final Color YELLOW_DK= new Color(195, 135,  25);
    static final Color RED      = new Color(224,  95,  85);
    static final Color RED_DK   = new Color(185,  60,  52);
    static final Color CARD_G   = new Color(75,  160,  50);   // card back easy
    static final Color CARD_Y   = new Color(210, 155,  35);   // card back medium
    static final Color CARD_R   = new Color(195,  75,  68);   // card back hard
    static final Color WHITE    = Color.WHITE;
    static final Color DARK_TXT = new Color(40,  70,  30);

    // ── Symbols (18 pairs max) ────────────────────────
    static final String[] SYMBOLS = {
        "★","♦","♥","♠","♣","☀","☽","☂","♪","✿","⊕","∞","π","∆","§","Ω","@","#"
    };
    static final Color[] SYM_COLORS = {
        new Color(220,150, 20), new Color( 80,130,220), new Color(210, 60, 80),
        new Color( 50, 50, 50), new Color(190, 60,180), new Color(220,160,  0),
        new Color( 70,100,180), new Color( 50,130,210), new Color(160, 60,200),
        new Color(200, 80,160), new Color( 60,180,120), new Color( 60,100,200),
        new Color( 50,160,200), new Color(180, 80, 60), new Color(100, 60,160),
        new Color( 80, 80, 80), new Color(180, 60,100), new Color( 60,140, 60)
    };

    // ── Persistence (session) ─────────────────────────
    static final int[] bestTimes  = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    static final int[] bestMoves  = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    // ── Routing ──────────────────────────────────────
    private final CardLayout  cl   = new CardLayout();
    private final JPanel      root = new JPanel(cl);

    // ─────────────────────────────────────────────────
    public FlipCardGame() {
        super("Flip Card");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        root.add(new MenuPanel(),      "MENU");
        root.add(new DifficultyPanel(),"DIFF");
        add(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void navigate(String screen) { cl.show(root, screen); }

    void launchGame(int diff) {
        // Remove stale game panel
        for (Component c : root.getComponents())
            if ("GAME".equals(c.getName())) { root.remove(c); break; }
        GamePanel gp = new GamePanel(diff);
        gp.setName("GAME");
        root.add(gp, "GAME");
        cl.show(root, "GAME");
        revalidate();
    }

    // ══════════════════════════════════════════════════
    //  MENU SCREEN
    // ══════════════════════════════════════════════════
    class MenuPanel extends JPanel {
        MenuPanel() {
            setBackground(BG);
            setPreferredSize(new Dimension(460, 500));
            setLayout(null);

            // Background suit symbols
            addSuitDecorations();

            // Title
            JLabel title = shadowLabel("Flip Card", 52, WHITE);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            title.setBounds(0, 20, 460, 70);
            add(title);

            // Card fan
            CardFanPanel fan = new CardFanPanel();
            fan.setBounds(110, 95, 240, 130);
            add(fan);

            // Buttons
            RoundButton start = new RoundButton("▶   START", GREEN,  GREEN_DK);
            RoundButton menu  = new RoundButton("⚙   MENU",  YELLOW, YELLOW_DK);
            RoundButton exit  = new RoundButton("✕   EXIT",  RED,    RED_DK);

            start.setBounds(110, 260, 240, 56);
            menu .setBounds(110, 332, 240, 56);
            exit .setBounds(110, 404, 240, 56);

            start.addActionListener(e -> navigate("DIFF"));
            menu .addActionListener(e -> showHowToPlay());
            exit .addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(FlipCardGame.this,
                    "Exit the game?", "Exit", JOptionPane.YES_NO_OPTION) == 0)
                    System.exit(0);
            });

            add(start); add(menu); add(exit);
        }

        void addSuitDecorations() {
            // Painted via paintComponent override
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0, 0, 0, 18));
            g2.setFont(new Font("Dialog", Font.BOLD, 90));
            g2.drawString("♣", 10,  160);
            g2.drawString("♦", 340, 320);
            g2.drawString("♥", 15,  470);
            g2.drawString("♠", 360, 90);
        }
    }

    // ══════════════════════════════════════════════════
    //  DIFFICULTY SCREEN
    // ══════════════════════════════════════════════════
    class DifficultyPanel extends JPanel {
        DifficultyPanel() {
            setBackground(BG);
            setPreferredSize(new Dimension(460, 500));
            setLayout(null);

            // Back
            IconButton back = new IconButton("◀", new Color(100, 148, 72));
            back.setBounds(10, 10, 42, 42);
            back.addActionListener(e -> navigate("MENU"));
            add(back);

            // Title
            JLabel title = shadowLabel("Select Difficulty", 30, WHITE);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            title.setBounds(0, 8, 460, 48);
            add(title);

            // Difficulty cards
            addDiffCard("★  EASY",   "8 Pairs  (16 Cards)",  GREEN,  CARD_G,  new Color(175,230,145), 0,  70);
            addDiffCard("★  MEDIUM","12 Pairs  (24 Cards)",  YELLOW, CARD_Y,  new Color(255,220,140), 1, 215);
            addDiffCard("★  HARD",  "18 Pairs  (36 Cards)",  RED,    CARD_R,  new Color(255,180,168), 2, 360);
        }

        void addDiffCard(String name, String desc, Color bg, Color cardBg,
                         Color miniColor, int diff, int y) {
            JPanel card = new JPanel(null) {
                boolean hovered = false;
                { addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { launchGame(diff); }
                }); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }

                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hovered ? bg.brighter() : bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setBounds(24, y, 412, 120);

            // Name
            JLabel nm = new JLabel(name);
            nm.setFont(new Font("Arial", Font.BOLD, 22));
            nm.setForeground(WHITE);
            nm.setBounds(16, 18, 260, 34);
            card.add(nm);

            // Desc
            JLabel dl = new JLabel(desc);
            dl.setFont(new Font("Arial", Font.PLAIN, 13));
            dl.setForeground(new Color(255, 255, 255, 180));
            dl.setBounds(16, 54, 260, 22);
            card.add(dl);

            // Best time badge
            if (bestTimes[diff] != Integer.MAX_VALUE) {
                JLabel bt = new JLabel("🏆  Best: " + fmt(bestTimes[diff]));
                bt.setFont(new Font("Arial", Font.BOLD, 12));
                bt.setForeground(WHITE);
                bt.setBounds(16, 80, 200, 20);
                card.add(bt);
            }

            // Mini grid preview
            MiniGrid mg = new MiniGrid(diff, miniColor);
            mg.setBounds(270, 12, 128, 96);
            card.add(mg);

            add(card);
        }
    }

    // ══════════════════════════════════════════════════
    //  GAME SCREEN
    // ══════════════════════════════════════════════════
    class GamePanel extends JPanel {
        final int diff, totalPairs, cols, rows;
        final Color theme, cardBack;

        CardButton[] cards;
        int flip1 = -1, flip2 = -1;
        boolean locked = false;
        int moves = 0, matched = 0, elapsed = 0;

        javax.swing.Timer gameTimer;
        JLabel lblTimer, lblMoves;
        JLabel lblBest;

        GamePanel(int diff) {
            this.diff       = diff;
            this.totalPairs = diff == 0 ? 8  : diff == 1 ? 12 : 18;
            this.cols       = diff == 0 ? 4  : 6;
            this.rows       = diff == 0 ? 4  : diff == 1 ? 4 : 6;
            this.theme      = diff == 0 ? GREEN  : diff == 1 ? YELLOW  : RED;
            this.cardBack   = diff == 0 ? CARD_G : diff == 1 ? CARD_Y  : CARD_R;

            buildUI();
            dealCards();
            startClock();
        }

        void buildUI() {
            setBackground(BG);
            setLayout(new BorderLayout(0, 6));
            int W = diff == 0 ? 430 : 530;
            int H = diff == 2 ? 620 : 520;
            setPreferredSize(new Dimension(W, H));

            // ── Top bar ──
            JPanel top = new JPanel(null);
            top.setOpaque(false);
            top.setPreferredSize(new Dimension(W, 52));

            IconButton back = new IconButton("◀", new Color(90, 130, 60));
            back.setBounds(8, 6, 42, 40);
            back.addActionListener(e -> { gameTimer.stop(); navigate("DIFF"); });
            top.add(back);

            // Difficulty badge
            BadgeLabel diff_badge = new BadgeLabel(
                diff == 0 ? "EASY" : diff == 1 ? "MEDIUM" : "HARD", theme);
            diff_badge.setBounds(58, 10, 90, 32);
            top.add(diff_badge);

            lblTimer = new JLabel("⏱  00:00", SwingConstants.CENTER);
            lblTimer.setFont(new Font("Arial", Font.BOLD, 14));
            lblTimer.setForeground(DARK_TXT);
            styleInfoLabel(lblTimer);
            lblTimer.setBounds(160, 11, 106, 30);
            top.add(lblTimer);

            lblMoves = new JLabel("Moves: 0", SwingConstants.CENTER);
            lblMoves.setFont(new Font("Arial", Font.BOLD, 14));
            lblMoves.setForeground(DARK_TXT);
            styleInfoLabel(lblMoves);
            lblMoves.setBounds(272, 11, 106, 30);
            top.add(lblMoves);

            // Restart button
            IconButton restart = new IconButton("↺", new Color(90, 130, 60));
            restart.setBounds(W - 54, 8, 40, 36);
            restart.addActionListener(e -> { gameTimer.stop(); launchGame(diff); });
            top.add(restart);

            add(top, BorderLayout.NORTH);

            // ── Card grid ──
            JPanel grid = new JPanel(new GridLayout(rows, cols, 6, 6));
            grid.setOpaque(false);
            int pad = diff == 0 ? 20 : 12;
            grid.setBorder(BorderFactory.createEmptyBorder(4, pad, 4, pad));
            add(grid, BorderLayout.CENTER);
            // Store ref for dealing
            this.putClientProperty("grid", grid);

            // ── Bottom best time ──
            JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            bot.setOpaque(false);
            String best = bestTimes[diff] == Integer.MAX_VALUE ? "--:--" : fmt(bestTimes[diff]);
            lblBest = new JLabel("  🏆  Best Time: " + best + "  ");
            lblBest.setFont(new Font("Arial", Font.BOLD, 14));
            lblBest.setForeground(WHITE);
            lblBest.setBackground(new Color(60, 110, 38));
            lblBest.setOpaque(true);
            lblBest.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 85, 22), 2),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
            // Rounded via custom component would be ideal but label works
            bot.add(lblBest);
            add(bot, BorderLayout.SOUTH);
        }

        void dealCards() {
            JPanel grid = (JPanel) getClientProperty("grid");
            grid.removeAll();

            List<Integer> deck = new ArrayList<>();
            for (int i = 0; i < totalPairs; i++) { deck.add(i); deck.add(i); }
            Collections.shuffle(deck);

            cards = new CardButton[totalPairs * 2];
            for (int i = 0; i < cards.length; i++) {
                int idx = i;
                int sym = deck.get(i);
                cards[i] = new CardButton(SYMBOLS[sym], SYM_COLORS[sym], cardBack);
                cards[i].addActionListener(e -> onFlip(idx));
                grid.add(cards[i]);
            }
            grid.revalidate();
            grid.repaint();
        }

        void startClock() {
            gameTimer = new javax.swing.Timer(1000, e -> {
                elapsed++;
                lblTimer.setText("⏱  " + fmt(elapsed));
            });
            gameTimer.start();
        }

        void onFlip(int idx) {
            if (locked) return;
            CardButton card = cards[idx];
            if (card.isRevealed() || card.isMatched()) return;
            if (flip1 == idx) return;

            card.reveal();

            if (flip1 == -1) {
                flip1 = idx;
            } else {
                flip2 = idx;
                moves++;
                lblMoves.setText("Moves: " + moves);
                locked = true;

                boolean isMatch = cards[flip1].symText.equals(cards[flip2].symText);
                int f1 = flip1, f2 = flip2;
                flip1 = -1; flip2 = -1;

                new javax.swing.Timer(600, e -> {
                    ((javax.swing.Timer) e.getSource()).stop();
                    if (isMatch) {
                        cards[f1].setMatched(); cards[f2].setMatched();
                        matched++;
                        if (matched == totalPairs) onWin();
                    } else {
                        cards[f1].flipBack(); cards[f2].flipBack();
                    }
                    locked = false;
                }).start();
            }
        }

        void onWin() {
            gameTimer.stop();
            boolean newBest = elapsed < bestTimes[diff];
            if (newBest) bestTimes[diff] = elapsed;
            if (moves < bestMoves[diff]) bestMoves[diff] = moves;
            lblBest.setText("  🏆  Best Time: " + fmt(bestTimes[diff]) + "  ");

            // Win dialog
            JDialog dlg = new JDialog(FlipCardGame.this, "🎉 You Won!", true);
            dlg.getContentPane().setBackground(BG);
            dlg.setLayout(new BorderLayout());

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(BG);
            body.setBorder(BorderFactory.createEmptyBorder(22, 36, 24, 36));

            body.add(centeredLabel("🎉", 56, WHITE));
            body.add(Box.createVerticalStrut(6));
            body.add(centeredLabel("You Won!", 34, WHITE, Font.BOLD));
            body.add(Box.createVerticalStrut(10));

            String info = newBest ? "🏆 New Best Time!" : "";
            if (!info.isEmpty()) body.add(centeredLabel(info, 14, new Color(255,240,120), Font.BOLD));

            body.add(Box.createVerticalStrut(6));
            body.add(centeredLabel("Time: " + fmt(elapsed) + "   |   Moves: " + moves, 15, WHITE, Font.PLAIN));
            body.add(centeredLabel("Best: " + fmt(bestTimes[diff]) + " in " + bestMoves[diff] + " moves", 13, new Color(210,240,190), Font.PLAIN));
            body.add(Box.createVerticalStrut(22));

            RoundButton again = new RoundButton("▶  Play Again", GREEN, GREEN_DK);
            again.setAlignmentX(CENTER_ALIGNMENT);
            again.setMaximumSize(new Dimension(220, 52));
            again.addActionListener(e -> { dlg.dispose(); launchGame(diff); });
            body.add(again);
            body.add(Box.createVerticalStrut(10));

            RoundButton harder = null;
            if (diff < 2) {
                harder = new RoundButton("⬆  Try " + (diff==0?"Medium":"Hard"), YELLOW, YELLOW_DK);
                harder.setAlignmentX(CENTER_ALIGNMENT);
                harder.setMaximumSize(new Dimension(220, 52));
                int nextDiff = diff + 1;
                harder.addActionListener(e -> { dlg.dispose(); launchGame(nextDiff); });
                body.add(harder);
                body.add(Box.createVerticalStrut(10));
            }

            RoundButton menuBtn = new RoundButton("⌂  Main Menu", new Color(100,150,75), new Color(70,110,50));
            menuBtn.setAlignmentX(CENTER_ALIGNMENT);
            menuBtn.setMaximumSize(new Dimension(220, 52));
            menuBtn.addActionListener(e -> { dlg.dispose(); navigate("MENU"); });
            body.add(menuBtn);

            dlg.add(body);
            dlg.pack();
            dlg.setLocationRelativeTo(FlipCardGame.this);

            new javax.swing.Timer(400, e -> { ((javax.swing.Timer)e.getSource()).stop(); dlg.setVisible(true); }).start();
        }

        // Helpers
        void styleInfoLabel(JLabel l) {
            l.setBackground(WHITE);
            l.setOpaque(true);
            l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(170, 200, 150), 1),
                BorderFactory.createEmptyBorder(0, 4, 0, 4)));
        }
    }

    // ══════════════════════════════════════════════════
    //  CARD BUTTON
    // ══════════════════════════════════════════════════
    static class CardButton extends JButton {
        final String symText;
        final Color  symColor;
        final Color  backColor;
        boolean revealed = false;
        boolean matched  = false;

        CardButton(String sym, Color symColor, Color back) {
            this.symText  = sym;
            this.symColor = symColor;
            this.backColor = back;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void reveal()     { revealed = true;  repaint(); }
        void flipBack() { revealed = false; repaint(); }
        void setMatched() { matched  = true;  revealed = true; repaint(); }
        boolean isRevealed() { return revealed; }
        boolean isMatched()  { return matched;  }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int arc = Math.min(14, Math.min(w, h) / 5);

            if (revealed || matched) {
                // Face
                Color face = matched ? new Color(225, 255, 215) : Color.WHITE;
                Color border = matched ? new Color(80, 175, 60) : new Color(195, 195, 195);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(1, 3, w - 2, h - 2, arc, arc);

                g2.setColor(face);
                g2.fillRoundRect(0, 0, w - 1, h - 3, arc, arc);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(matched ? 2.5f : 1.5f));
                g2.drawRoundRect(0, 0, w - 2, h - 4, arc, arc);

                // Symbol
                int fontSize = Math.min(w, h) * 52 / 100;
                g2.setFont(new Font("Dialog", Font.BOLD, Math.max(10, fontSize)));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(symText)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - 2;
                g2.setColor(matched ? new Color(55, 155, 40) : symColor);
                g2.drawString(symText, tx, ty);

            } else {
                // Card back
                Color dark = backColor.darker();

                // Outer shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(1, 3, w - 2, h - 2, arc, arc);

                // 3D base
                g2.setColor(dark);
                g2.fillRoundRect(0, 3, w - 1, h - 2, arc, arc);

                // Main face
                g2.setColor(getModel().isRollover() ? backColor.brighter() : backColor);
                g2.fillRoundRect(0, 0, w - 1, h - 4, arc, arc);

                // Star (hidden symbol)
                int fs = Math.min(w, h) * 50 / 100;
                g2.setFont(new Font("Dialog", Font.BOLD, Math.max(8, fs)));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(0, 0, 0, 35));
                String star = "★";
                int tx = (w - fm.stringWidth(star)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - 2;
                g2.drawString(star, tx, ty);
            }
            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════
    //  UTILITY COMPONENTS
    // ══════════════════════════════════════════════════

    /** Rounded button with shadow */
    static class RoundButton extends JButton {
        final Color base, dark;
        RoundButton(String text, Color base, Color dark) {
            super(text);
            this.base = base; this.dark = dark;
            setFont(new Font("Arial", Font.BOLD, 19));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(240, 56));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(); boolean press = getModel().isArmed();
            int oy = press ? 2 : 0;
            g2.setColor(dark);
            g2.fillRoundRect(0, 4, w, h - 4, 30, 30);
            g2.setColor(press ? base.darker() : base);
            g2.fillRoundRect(0, oy, w, h - 4, 30, 30);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont()); 
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                oy + (h - 4 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Small square icon button */
    static class IconButton extends JButton {
        final Color col;
        IconButton(String text, Color col) {
            super(text); this.col = col;
            setFont(new Font("Arial", Font.BOLD, 15));
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isArmed() ? col.darker() : col);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                (getHeight()-fm.getHeight())/2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Colored text badge */
    static class BadgeLabel extends JLabel {
        final Color col;
        BadgeLabel(String text, Color col) {
            super(text, SwingConstants.CENTER); this.col = col;
            setFont(new Font("Arial", Font.BOLD, 13)); setForeground(Color.WHITE); setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(col); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose(); super.paintComponent(g);
        }
    }

    /** Mini card grid preview on difficulty screen */
    static class MiniGrid extends JPanel {
        final Color color; final int cols, rows;
        MiniGrid(int diff, Color color) {
            this.color = color;
            this.cols  = diff == 0 ? 4 : 6;
            this.rows  = diff == 0 ? 4 : diff == 1 ? 4 : 6;
            setOpaque(false);
            setLayout(new GridLayout(rows, cols, 2, 2));
            for (int i = 0; i < rows * cols; i++) add(new MiniCell(color));
        }
    }
    static class MiniCell extends JPanel {
        final Color c; MiniCell(Color c) { this.c = c; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
            g2.dispose();
        }
    }

    /** Animated card fan decoration on menu */
    static class CardFanPanel extends JPanel {
        static final Color[] COLORS = {new Color(80,130,210), new Color(70,165,70), new Color(220,95,82)};
        static final int[]   ANGLES = {-22, -4, 14};
        CardFanPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() - 10;
            for (int i = 0; i < COLORS.length; i++) {
                double rad = Math.toRadians(ANGLES[i]);
                g2.translate(cx, cy);
                g2.rotate(rad);
                // Shadow
                g2.setColor(new Color(0,0,0,40));
                g2.fillRoundRect(-32, -90, 64, 88, 14, 14);
                // Card
                g2.setColor(COLORS[i]);
                g2.fillRoundRect(-33, -93, 64, 88, 14, 14);
                // Highlight
                g2.setColor(new Color(255,255,255,45));
                g2.fillRoundRect(-33, -93, 64, 30, 14, 14);
                g2.rotate(-rad);
                g2.translate(-cx, -cy);
            }
            g2.dispose();
        }
    }

    // ── Shared helpers ────────────────────────────────
    static JLabel shadowLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = fm.getAscent();
                g2.setColor(new Color(0, 0, 0, 55));
                g2.drawString(getText(), tx + 2, ty + 3);
                g2.setColor(getForeground());
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }

    static JLabel centeredLabel(String text, int size, Color color) {
        return centeredLabel(text, size, color, Font.BOLD);
    }
    static JLabel centeredLabel(String text, int size, Color color, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Arial", style, size));
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(360, size + 12));
        return l;
    }

    static String fmt(int s) { return String.format("%02d:%02d", s / 60, s % 60); }

    void showHowToPlay() {
        String msg =
            "HOW TO PLAY\n\n" +
            "1.  Click any card to flip it face-up.\n" +
            "2.  Click a second card — find its match!\n" +
            "3.  Matched pairs stay revealed.\n" +
            "4.  Unmatched pairs flip back after 0.6s.\n" +
            "5.  Match ALL pairs to win! 🏆\n\n" +
            "DIFFICULTY:\n" +
            "  ★  EASY   —  4×4  (16 cards, 8 pairs)\n" +
            "  ★  MEDIUM —  6×4  (24 cards, 12 pairs)\n" +
            "  ★  HARD   —  6×6  (36 cards, 18 pairs)\n\n" +
            "Tip: Beat your best time on each difficulty!";
        JOptionPane.showMessageDialog(this, msg, "How to Play",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Entry point ───────────────────────────────────
    public static void main(String[] args) {
        // Use system look-and-feel for better font rendering
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(FlipCardGame::new);
    }
}
