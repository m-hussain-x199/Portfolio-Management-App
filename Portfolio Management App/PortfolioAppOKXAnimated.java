import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PortfolioAppOKXAnimated extends JFrame {
    // Data classes:
    static class User implements Serializable {
        String username, password;
        HashMap<String, Holding> portfolio = new HashMap<>();
        java.util.List<Transaction> history = new ArrayList<>();
        Stack<Transaction> undoStack = new Stack<>();
        User(String u, String p) { username=u; password=p; }
    }
    static class Holding implements Serializable {
        String ticker; int quantity;
        Holding(String t, int q) { ticker=t; quantity=q; }
        public String toString() { return ticker + ": " + quantity + " shares"; }
    }
    static class Transaction implements Serializable {
        enum Type { BUY, SELL }
        Type type; String ticker; int qty; double price; ZonedDateTime date;
        static final ZoneId PAK_ZONE = ZoneId.of("Asia/Karachi");
        static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a z");
        Transaction(Type type, String ticker, int qty, double price) {
            this.type = type; this.ticker = ticker; this.qty = qty; this.price = price;
            this.date = ZonedDateTime.now(PAK_ZONE);
        }
        public String toString() {
            return String.format("%s %d×%s @ %.2f on %s", type, qty, ticker, price, date.format(FORMATTER));
        }
    }
    private HashMap<String, User> users = new HashMap<>();
    private User currentUser = null;
    private final String DATA_FILE = "portfolio_data.ser";
    private final Font mainFont = new Font("Segoe UI", Font.PLAIN, 20);
    private final Font bigFont = new Font("Segoe UI", Font.BOLD, 28);
    private final Color bgColor = new Color(13,17,28);
    private final Color cardColor = new Color(27,33,48, 238);
    private final Color borderColor = new Color(48, 120, 255, 90);
    private final Color accent1 = new Color(48, 156, 255);
    private final Color accent2 = new Color(0, 232, 190);
    private final Color failRed = new Color(255, 80, 120);

    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    private LoginPanel loginPanel = new LoginPanel();
    private RegisterPanel registerPanel = new RegisterPanel();
    private MenuPanel menuPanel = new MenuPanel();
    private PortfolioPanel portfolioPanel = new PortfolioPanel();
    private TradePanel buyPanel = new TradePanel(true);
    private TradePanel sellPanel = new TradePanel(false);
    private HistoryPanel historyPanel = new HistoryPanel();

    public static void main(String[] args) {
        UIManager.put("control", new Color(20,22,36));
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", new Color(23,25,34));
        UIManager.put("nimbusBlueGrey", new Color(18,22,32));
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(() -> new PortfolioAppOKXAnimated().setVisible(true));
    }

    public PortfolioAppOKXAnimated() {
        super("OKX Portfolio");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 640);
        setMinimumSize(new Dimension(700, 520));
        setLocationRelativeTo(null);

        loadData();
        setContentPane(new AnimatedBGPanel());

        cardPanel.setOpaque(false);
        cardPanel.setLayout(cardLayout);
        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");
        cardPanel.add(menuPanel, "menu");
        cardPanel.add(portfolioPanel, "portfolio");
        cardPanel.add(buyPanel, "buy");
        cardPanel.add(sellPanel, "sell");
        cardPanel.add(historyPanel, "history");
        getContentPane().setLayout(null);
        getContentPane().add(cardPanel);
        cardPanel.setBounds(0,0,getWidth(),getHeight());
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                cardPanel.setBounds(0,0,getWidth(),getHeight());
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { saveData(); }
        });
        cardLayout.show(cardPanel, "login");
    }

    // Animation:
    class AnimatedBGPanel extends JPanel {
        private float waveOffset = 0;
        private javax.swing.Timer timer;
        public AnimatedBGPanel() {
            setLayout(null);
            setBackground(bgColor);
            timer = new javax.swing.Timer(16, e -> { waveOffset += 0.015f; repaint(); });
            timer.start();
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            int w = getWidth(), h = getHeight();
            // Main bg
            g2.setPaint(new GradientPaint(0,0, bgColor, w,h, new Color(25,32,48)));
            g2.fillRect(0,0,w,h);
            // Sine waves (blue/green)
            drawWave(g2, 0.8f, 90, 1.3f, accent1, 0.6f);
            drawWave(g2, 0.5f, 130, 0.7f, accent2, 0.36f);
            drawDots(g2, accent2, 0.19f);
            g2.dispose();
        }
        private void drawWave(Graphics2D g2, float alpha, int yOffset, double freq, Color color, float thickness) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setStroke(new BasicStroke((float)(thickness * 8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int w = getWidth(), h = getHeight();
            Path2D.Float path = new Path2D.Float();
            int points = 180;
            for (int i = 0; i <= points; i++) {
                float x = i * w / (float)points;
                float y = (float)(Math.sin(waveOffset + i * freq * Math.PI / 90) * 22 + h - yOffset);
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }
            g2.setPaint(new GradientPaint(0, h - yOffset - 40, color.brighter(), w, h - yOffset + 10, color.darker()));
            g2.draw(path);
        }
        private void drawDots(Graphics2D g2, Color color, float alpha) {
            int w = getWidth(), h = getHeight();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            for (int i = 0; i < 13; i++) {
                int r = 13 + (i % 3) * 7;
                int x = (int)(Math.abs(Math.sin(waveOffset + i)) * w * 0.83 + 42 - r/2);
                int y = (int)(Math.abs(Math.cos(waveOffset*0.7 + i*1.2)) * h * 0.55 + 70 - r/2);
                g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(x+r/2, y+r/2), r/2,
                    new float[]{0f, 1f},
                    new Color[]{new Color(color.getRed(), color.getGreen(), color.getBlue(), 110), new Color(0,0,0,0)}
                ));
                g2.fillOval(x, y, r, r);
            }
        }
    }

    // Card Panel base class:
    abstract class CardPanel extends JPanel {
        public CardPanel(String title) {
            setOpaque(false);
            setLayout(new BorderLayout());
            JLabel label = new JLabel(title, SwingConstants.LEFT);
            label.setFont(bigFont);
            label.setForeground(accent1);
            label.setBorder(new EmptyBorder(28, 46, 20, 0));
            add(label, BorderLayout.NORTH);
        }
        protected JPanel makeCard(Component c, int w, int h) {
            JPanel p = new JPanel(new GridBagLayout()) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
                    g2.setPaint(cardColor);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(), 32, 32);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2, 32,32);
                    g2.dispose();
                }
            };
            p.setOpaque(false);
            p.setPreferredSize(new Dimension(w,h));
            p.setBorder(new EmptyBorder(38, 44, 38, 44));
            p.add(c);
            return p;
        }
        protected JButton okxButton(String text, Color...grad) {
            JButton btn = new JButton(text);
            btn.setFont(mainFont.deriveFont(Font.BOLD, 22f));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorder(new EmptyBorder(12, 36, 12, 36));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setUI(new OKXButtonUI(grad.length>0?grad[0]:accent1, grad.length>1?grad[1]:accent2));
            return btn;
        }
        protected JTextField okxField(int cols) {
            JTextField tf = new JTextField(cols);
            tf.setFont(mainFont.deriveFont(20f));
            tf.setBackground(new Color(29,34,49));
            tf.setForeground(Color.WHITE);
            tf.setCaretColor(accent1);
            tf.setBorder(new CompoundBorder(
                new OKXFieldBorder(accent1, 2f, 18), new EmptyBorder(12,18,12,18)
            ));
            tf.setSelectionColor(accent2);
            return tf;
        }
        protected JPasswordField okxPass(int cols) {
            JPasswordField tf = new JPasswordField(cols);
            tf.setFont(mainFont.deriveFont(20f));
            tf.setBackground(new Color(29,34,49));
            tf.setForeground(Color.WHITE);
            tf.setCaretColor(accent1);
            tf.setBorder(new CompoundBorder(
                new OKXFieldBorder(accent1, 2f, 18), new EmptyBorder(12,18,12,18)
            ));
            tf.setSelectionColor(accent2);
            return tf;
        }
    }
    static class OKXButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private final Color grad1, grad2;
        OKXButtonUI(Color g1, Color g2) { grad1=g1; grad2=g2; }
        public void paint(Graphics g, JComponent c) {
            AbstractButton b=(AbstractButton)c;
            Graphics2D g2=(Graphics2D)g.create();
            int w=c.getWidth(), h=c.getHeight();
            GradientPaint gp=new GradientPaint(0,0, grad1, w, h, grad2);
            if(b.getModel().isPressed()) gp=new GradientPaint(0,0, grad2, w,h, grad1);
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,w,h,22,22);
            g2.setColor(new Color(0,0,0,60));
            g2.drawRoundRect(0,0,w-1,h-1,22,22);
            g2.dispose();
            super.paint(g,c);
        }
    }
    static class OKXFieldBorder extends AbstractBorder {
        private final Color col; private final float thick; private final int rad;
        OKXFieldBorder(Color c, float t, int r) { col=c; thick=t; rad=r; }
        public void paintBorder(Component c, Graphics g, int x,int y,int w,int h) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setColor(col); g2.setStroke(new BasicStroke(thick));
            g2.drawRoundRect(x+1,y+1,w-3,h-3,rad,rad);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(14,14,14,14);}
        public Insets getBorderInsets(Component c,Insets i){return getBorderInsets(c);}
    }

    // Login Panel:
    class LoginPanel extends CardPanel {
        JTextField username = okxField(16);
        JPasswordField password = okxPass(16);
        JLabel error = new JLabel(" ");
        public LoginPanel() {
            super("Sign In");
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            error.setFont(mainFont.deriveFont(Font.BOLD, 17f)); error.setForeground(failRed);
            error.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel uLab = new JLabel("Username"); uLab.setFont(mainFont.deriveFont(20f)); uLab.setForeground(accent2);
            JLabel pLab = new JLabel("Password"); pLab.setFont(mainFont.deriveFont(20f)); pLab.setForeground(accent2);

            JButton loginBtn = okxButton("Sign In", accent1, accent2);
            JButton regBtn = okxButton("Create Account", accent2, accent1);
            regBtn.addActionListener(e -> cardLayout.show(cardPanel, "register"));

            loginBtn.addActionListener(e -> {
                String u = username.getText().trim();
                String p = new String(password.getPassword());
                if(users.containsKey(u) && users.get(u).password.equals(p)) {
                    currentUser = users.get(u);
                    error.setText(" ");
                    username.setText(""); password.setText("");
                    menuPanel.updateUser();
                    cardLayout.show(cardPanel, "menu");
                } else {
                    error.setText("Invalid credentials.");
                }
            });

            form.add(Box.createVerticalStrut(14));
            form.add(uLab); form.add(username);
            form.add(Box.createVerticalStrut(14));
            form.add(pLab); form.add(password);
            form.add(Box.createVerticalStrut(10));
            form.add(error);
            form.add(Box.createVerticalStrut(34));
            form.add(loginBtn); form.add(Box.createVerticalStrut(20)); form.add(regBtn);

            JPanel box = makeCard(form, 420, 370);
            add(box, BorderLayout.CENTER);
        }
    }

    // Register Panel:
    class RegisterPanel extends CardPanel {
        JTextField username = okxField(16);
        JPasswordField password = okxPass(16);
        JPasswordField confirm = okxPass(16);
        JLabel error = new JLabel(" ");
        public RegisterPanel() {
            super("Create Account");
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            JLabel uLab = new JLabel("Username"); uLab.setFont(mainFont.deriveFont(20f)); uLab.setForeground(accent2);
            JLabel pLab = new JLabel("Password"); pLab.setFont(mainFont.deriveFont(20f)); pLab.setForeground(accent2);
            JLabel cLab = new JLabel("Confirm Password"); cLab.setFont(mainFont.deriveFont(20f)); cLab.setForeground(accent2);
            error.setFont(mainFont.deriveFont(Font.BOLD, 17f)); error.setForeground(failRed); error.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton regBtn = okxButton("Register", accent2, accent1);
            JButton backBtn = okxButton("Back", accent1, accent2);
            backBtn.addActionListener(e -> cardLayout.show(cardPanel, "login"));
            regBtn.addActionListener(e -> {
                String u = username.getText().trim();
                String p = new String(password.getPassword());
                String c = new String(confirm.getPassword());
                if(u.isEmpty()||p.isEmpty()) error.setText("All fields required.");
                else if(users.containsKey(u)) error.setText("Username taken.");
                else if(!p.equals(c)) error.setText("Passwords don't match.");
                else {
                    users.put(u, new User(u,p));
                    error.setForeground(accent2);
                    error.setText("Registered! Go login.");
                    username.setText(""); password.setText(""); confirm.setText("");
                    javax.swing.Timer t = new javax.swing.Timer(1100, ev -> cardLayout.show(cardPanel,"login")); t.setRepeats(false); t.start();
                }
            });

            form.add(Box.createVerticalStrut(8));
            form.add(uLab); form.add(username);
            form.add(Box.createVerticalStrut(8));
            form.add(pLab); form.add(password);
            form.add(Box.createVerticalStrut(8));
            form.add(cLab); form.add(confirm);
            form.add(Box.createVerticalStrut(9));
            form.add(error);
            form.add(Box.createVerticalStrut(32));
            form.add(regBtn); form.add(Box.createVerticalStrut(18)); form.add(backBtn);

            JPanel box = makeCard(form, 430, 430);
            add(box, BorderLayout.CENTER);
        }
    }

    // Menu Panel:
    class MenuPanel extends CardPanel {
        JLabel userLabel = new JLabel();
        public MenuPanel() {
            super("Dashboard");
            userLabel.setFont(mainFont.deriveFont(Font.BOLD, 22f));
            userLabel.setForeground(accent1.brighter());
            userLabel.setBorder(new EmptyBorder(0, 48, 0, 0));
            add(userLabel, BorderLayout.CENTER);

            JPanel btns = new JPanel();
            btns.setOpaque(false);
            btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));
            JButton viewBtn = okxButton("View Portfolio", accent1, accent2);
            JButton buyBtn = okxButton("Buy", accent2, accent1);
            JButton sellBtn = okxButton("Sell", accent1, accent2);
            JButton histBtn = okxButton("Transactions", accent2, accent1);
            JButton undoBtn = okxButton("Undo Last", accent1, accent2);
            JButton logoutBtn = okxButton("Logout", failRed, accent2);

            btns.add(Box.createVerticalStrut(14));
            btns.add(viewBtn); btns.add(Box.createVerticalStrut(14));
            btns.add(buyBtn); btns.add(Box.createVerticalStrut(14));
            btns.add(sellBtn); btns.add(Box.createVerticalStrut(14));
            btns.add(histBtn); btns.add(Box.createVerticalStrut(14));
            btns.add(undoBtn); btns.add(Box.createVerticalStrut(18));
            btns.add(logoutBtn);
            JPanel box = makeCard(btns, 370, 430);
            add(box, BorderLayout.EAST);

            viewBtn.addActionListener(e -> { portfolioPanel.refresh(); cardLayout.show(cardPanel, "portfolio"); });
            buyBtn.addActionListener(e -> { buyPanel.refresh(); cardLayout.show(cardPanel, "buy"); });
            sellBtn.addActionListener(e -> { sellPanel.refresh(); cardLayout.show(cardPanel, "sell"); });
            histBtn.addActionListener(e -> { historyPanel.refresh(); cardLayout.show(cardPanel, "history"); });
            undoBtn.addActionListener(e -> {
                if(currentUser!=null && !currentUser.undoStack.isEmpty()) {
                    Transaction last = currentUser.undoStack.pop();
                    if(last.type==Transaction.Type.BUY) {
                        Holding h = currentUser.portfolio.get(last.ticker);
                        h.quantity -= last.qty;
                        if(h.quantity<=0) currentUser.portfolio.remove(last.ticker);
                    } else {
                        Holding h = currentUser.portfolio.getOrDefault(last.ticker, new Holding(last.ticker,0));
                        h.quantity += last.qty;
                        currentUser.portfolio.put(last.ticker, h);
                    }
                    currentUser.history.remove(last);
                    JOptionPane.showMessageDialog(MenuPanel.this, "Undid: "+last, "Undo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(MenuPanel.this, "Nothing to undo.", "Undo", JOptionPane.WARNING_MESSAGE);
                }
            });
            logoutBtn.addActionListener(e -> { currentUser=null; cardLayout.show(cardPanel,"login"); });
        }
        public void updateUser() {
            if(currentUser!=null) userLabel.setText("Welcome, " + currentUser.username + "!");
        }
    }

    // Portfolio Panel:
    class PortfolioPanel extends CardPanel {
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JLabel empty = new JLabel("Portfolio is empty. Buy some stocks to get started!", SwingConstants.CENTER);
        JButton backBtn = okxButton("Back", accent2, accent1);
        public PortfolioPanel() {
            super("Your Portfolio");
            list.setFont(mainFont.deriveFont(19f));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(420, 220));
            scroll.setBorder(null);
            empty.setFont(mainFont.deriveFont(17f));
            empty.setForeground(new Color(140,160,200));
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(scroll, BorderLayout.CENTER);
            center.add(empty, BorderLayout.SOUTH);

            JPanel box = makeCard(center, 500, 320);
            add(box, BorderLayout.CENTER);

            JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
            nav.setOpaque(false);
            nav.add(backBtn);
            add(nav, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> cardLayout.show(cardPanel, "menu"));
        }
        public void refresh() {
            model.clear();
            if(currentUser==null||currentUser.portfolio.isEmpty()) {
                empty.setVisible(true);
                list.setVisible(false);
            } else {
                java.util.List<String> tickers = new ArrayList<>(currentUser.portfolio.keySet());
                Collections.sort(tickers);
                for(String t:tickers) model.addElement(currentUser.portfolio.get(t).toString());
                empty.setVisible(false);
                list.setVisible(true);
            }
        }
    }

    // Buy/Sell Panel:
    class TradePanel extends CardPanel {
        boolean isBuy;
        JTextField tickerField = this.okxField(9);
        JTextField qtyField = this.okxField(9);
        JTextField priceField = this.okxField(9);
        JLabel msgLabel = new JLabel(" ");
        JButton execBtn = okxButton("Buy", accent2, accent1);
        JButton backBtn = okxButton("Back", accent1, accent2);
        public TradePanel(boolean buy) {
            super(buy ? "Buy Stock" : "Sell Stock");
            isBuy = buy;
            execBtn.setText(buy ? "Buy" : "Sell");
            msgLabel.setFont(mainFont.deriveFont(Font.BOLD, 16f));
            msgLabel.setForeground(failRed);

            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            JLabel tLab = new JLabel("Stock or Crypto (e.g. AAPL, BTC):");
            tLab.setFont(mainFont.deriveFont(19f)); tLab.setForeground(accent2);
            JLabel qLab = new JLabel("Quantity:");
            qLab.setFont(mainFont.deriveFont(19f)); qLab.setForeground(accent2);
            JLabel pLab = new JLabel("Price ($):");
            pLab.setFont(mainFont.deriveFont(19f)); pLab.setForeground(accent2);

            form.add(Box.createVerticalStrut(6));
            form.add(tLab); form.add(tickerField);
            form.add(Box.createVerticalStrut(10));
            form.add(qLab); form.add(qtyField);
            form.add(Box.createVerticalStrut(10));
            form.add(pLab); form.add(priceField);
            form.add(Box.createVerticalStrut(10));
            form.add(msgLabel);
            form.add(Box.createVerticalStrut(18));
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
            btnRow.setOpaque(false);
            btnRow.add(execBtn); btnRow.add(backBtn);
            form.add(btnRow);

            JPanel box = makeCard(form, 420, 430);
            add(box, BorderLayout.CENTER);

            execBtn.addActionListener(e -> performTrade());
            backBtn.addActionListener(e -> cardLayout.show(cardPanel,"menu"));
        }
        public void refresh() {
            tickerField.setText(""); qtyField.setText(""); priceField.setText(""); msgLabel.setText(" ");
        }
        private void performTrade() {
            String t = tickerField.getText().trim().toUpperCase();
            String qStr = qtyField.getText().trim(), pStr = priceField.getText().trim();
            int q; double price;
            if(currentUser==null) return;
            if(t.isEmpty()||qStr.isEmpty()||pStr.isEmpty()) { msgLabel.setText("All fields required."); return; }
            try { q = Integer.parseInt(qStr); price = Double.parseDouble(pStr); }
            catch(Exception ex) { msgLabel.setText("Quantity and price must be valid numbers."); return; }
            if(isBuy) {
                Holding h = currentUser.portfolio.get(t);
                if(h==null) currentUser.portfolio.put(t, new Holding(t,q));
                else h.quantity += q;
                Transaction tx = new Transaction(Transaction.Type.BUY, t,q,price);
                currentUser.history.add(tx);
                currentUser.undoStack.push(tx);
                msgLabel.setForeground(accent2);
                msgLabel.setText("Bought "+q+"×"+t+" @ $"+String.format("%.2f", price));
            } else {
                Holding h = currentUser.portfolio.get(t);
                if(h==null) { msgLabel.setForeground(failRed); msgLabel.setText("You do not have any "+t); return; }
                if(q > h.quantity) { msgLabel.setForeground(failRed); msgLabel.setText("Not enough shares."); return; }
                h.quantity -= q;
                if(h.quantity==0) currentUser.portfolio.remove(t);
                Transaction tx = new Transaction(Transaction.Type.SELL, t,q,price);
                currentUser.history.add(tx);
                currentUser.undoStack.push(tx);
                msgLabel.setForeground(accent1);
                msgLabel.setText("Sold "+q+"×"+t+" @ $"+String.format("%.2f", price));
            }
        }
    }

    // History Panel:
    class HistoryPanel extends CardPanel {
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JLabel empty = new JLabel("No transactions yet.", SwingConstants.CENTER);
        JButton backBtn = okxButton("Back", accent2, accent1);
        public HistoryPanel() {
            super("Transactions");
            list.setFont(mainFont.deriveFont(17f));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(520, 260));
            scroll.setBorder(null);
            empty.setFont(mainFont.deriveFont(16f));
            empty.setForeground(new Color(120,128,160));
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(scroll, BorderLayout.CENTER);
            center.add(empty, BorderLayout.SOUTH);

            JPanel box = makeCard(center, 550, 340);
            add(box, BorderLayout.CENTER);

            JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 0));
            nav.setOpaque(false);
            nav.add(backBtn);
            add(nav, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> cardLayout.show(cardPanel, "menu"));
        }
        public void refresh() {
            model.clear();
            if(currentUser==null||currentUser.history.isEmpty()) {
                empty.setVisible(true);
                list.setVisible(false);
            } else {
                for(Transaction tx:currentUser.history) model.addElement(tx.toString());
                empty.setVisible(false);
                list.setVisible(true);
            }
        }
    }

    // Save / load user data to / from a file:
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            users = (HashMap<String, User>) ois.readObject();
        } catch (Exception e) { e.printStackTrace(); }
    }
}