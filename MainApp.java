import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

class StyleConstants {
    public static final Color BACKGROUND_COLOR = new Color(240, 245, 250);
    public static final Color PRIMARY_COLOR = new Color(60, 120, 180);
    public static final Color SECONDARY_COLOR = new Color(100, 150, 200);
    public static final Color TEXT_COLOR = new Color(50, 50, 50);
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    public static final Color TABLE_HEADER_BACKGROUND = new Color(90, 140, 190);
    public static final Color TABLE_HEADER_FOREGROUND = Color.WHITE;
    public static final Color TABLE_GRID_COLOR = new Color(200, 215, 230);
    public static final Color HIGHLIGHT_ROW_COLOR = new Color(210, 225, 240);

    private static String getPreferredFontName() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String fontName : fontNames) {
            if ("Segoe UI".equalsIgnoreCase(fontName)) {
                return "Segoe UI";
            }
        }
        return "SansSerif";
    }

    private static final String FONT_NAME = getPreferredFontName();
    public static final Font LABEL_FONT = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font TEXT_FIELD_FONT = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font TABLE_FONT = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font TABLE_HEADER_FONT = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 18);
}

class LeaderboardFrame extends JFrame {
    private String currentUserEmail;

    public LeaderboardFrame(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
        setTitle("Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleConstants.BACKGROUND_COLOR);

        String[] columnNames = { "Rank", "Username", "Score" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFont(StyleConstants.TABLE_FONT);
        table.setRowHeight(25);
        table.setGridColor(StyleConstants.TABLE_GRID_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(StyleConstants.BACKGROUND_COLOR);
        table.setForeground(StyleConstants.TEXT_COLOR);
        table.setSelectionBackground(StyleConstants.SECONDARY_COLOR);
        table.setSelectionForeground(StyleConstants.BUTTON_TEXT_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setBackground(StyleConstants.TABLE_HEADER_BACKGROUND);
        header.setForeground(StyleConstants.TABLE_HEADER_FOREGROUND);
        header.setFont(StyleConstants.TABLE_HEADER_FONT);
        header.setPreferredSize(new Dimension(100, 30));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getViewport().setBackground(StyleConstants.BACKGROUND_COLOR);

        loadLeaderboardData(model);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton);
        closeButton.addActionListener(e -> {
             dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(StyleConstants.BACKGROUND_COLOR);
        bottomPanel.setBorder(new EmptyBorder(5, 0, 10, 0));
        bottomPanel.add(closeButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadLeaderboardData(DefaultTableModel model) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:D:\\Java\\Java-Project\\javaapp.db")) {

            String topQuery = "SELECT email, username, score FROM users ORDER BY score DESC LIMIT 5";
            try (PreparedStatement psTop = conn.prepareStatement(topQuery);
                 ResultSet rsTop = psTop.executeQuery()) {

                int rank = 1;
                while (rsTop.next()) {
                    String username = rsTop.getString("username");
                    double score = rsTop.getDouble("score");
                    model.addRow(new Object[]{rank, username, String.format("%.4f", score)});
                    rank++;
                }
            }

            String rankQuery = "SELECT username, score, " +
                    "(SELECT COUNT(*) + 1 FROM users WHERE score > u.score) AS rank " +
                    "FROM users u WHERE email = ?";
            try (PreparedStatement psRank = conn.prepareStatement(rankQuery)) {
                psRank.setString(1, this.currentUserEmail);
                try (ResultSet rsRank = psRank.executeQuery()) {

                    if (rsRank.next()) {
                        int userRank = rsRank.getInt("rank");
                        String username = rsRank.getString("username");
                        double userScore = rsRank.getDouble("score");

                        boolean userInTop5 = false;
                        for (int i = 0; i < model.getRowCount() && i < 5; i++) {
                           if (model.getValueAt(i, 1).equals(username)) {
                                userInTop5 = true;
                                break;
                            }
                        }

                        if (!userInTop5 && userRank > 5) {
                            model.addRow(new Object[]{"...", "...", "..."});
                            model.addRow(new Object[]{userRank, username, String.format("%.4f", userScore)});
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading leaderboard: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton button) {
        button.setFont(StyleConstants.BUTTON_FONT);
        button.setBackground(StyleConstants.PRIMARY_COLOR);
        button.setForeground(StyleConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false); // Explicitly disable focus painting
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(StyleConstants.SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(StyleConstants.PRIMARY_COLOR);
            }
        });
    }
}

class MemoryBoostGame extends JFrame {
    private JPanel mainPanel;
    private String selectedTheme;
    private ArrayList<String> imagePaths;
    private JButton[] cardButtons;
    private ImageIcon hiddenIcon;
    private int firstIndex = -1, secondIndex = -1;
    private Timer flipTimer;
    private static int numMoves = 0;
    private int matchedPairs = 0;
    private String userEmail;
    private JLabel movesLabel;
    private final int GRID_SIZE = 4;
    private final int CARD_SIZE = 100;

    public MemoryBoostGame(String userEmail) {
        this.userEmail = userEmail;
        numMoves = 0;
        matchedPairs = 0;
        firstIndex = -1;
        secondIndex = -1;

        setTitle("Memory Boost Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleConstants.BACKGROUND_COLOR);

        hiddenIcon = loadImage("Images/hidden.webp", CARD_SIZE, CARD_SIZE);
        if (hiddenIcon == null) {
            JOptionPane.showMessageDialog(this, "Error: Could not load card back image (Images/hidden.webp).",
                    "Image Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        selectTheme();
    }

    private void selectTheme() {
        String[] themes = { "Cricket", "IPL", "Anime" };
        selectedTheme = (String) JOptionPane.showInputDialog(
                this, "Select a Theme:", "Theme Selection",
                JOptionPane.QUESTION_MESSAGE, null, themes, themes[0]);

        if (selectedTheme != null && !selectedTheme.isEmpty()) {
            if (loadImages(selectedTheme)) {
                 initializeGameUI();
            } else {
                 selectTheme();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No theme selected. Exiting.", "Game Cancelled", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

     private boolean loadImages(String theme) {
        imagePaths = new ArrayList<>();
         String folderPath = "Images" + File.separator + theme;

        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
            if (files != null) {
                for (File file : files) {
                    imagePaths.add(file.getAbsolutePath());
                }
            }
        } else {
             System.err.println("Image folder not found or not a directory: " + folder.getAbsolutePath());
        }

        int requiredUniqueImages = (GRID_SIZE * GRID_SIZE) / 2;
        if (imagePaths.size() < requiredUniqueImages) {
            JOptionPane.showMessageDialog(this,
                    "Not enough images found in theme '" + theme + "'.\n" +
                            "Need at least " + requiredUniqueImages + " unique images in\n" + folder.getAbsolutePath(),
                    "Image Error", JOptionPane.ERROR_MESSAGE);
             System.err.println("Error: Found " + imagePaths.size() + " images, need " + requiredUniqueImages + " for a " + GRID_SIZE + "x" + GRID_SIZE + " grid.");
            return false;
        }

         Collections.shuffle(imagePaths);
         imagePaths = new ArrayList<>(imagePaths.subList(0, requiredUniqueImages));
         imagePaths.addAll(new ArrayList<>(imagePaths));
         Collections.shuffle(imagePaths);

        return true;
    }

    private void initializeGameUI() {
        mainPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 5, 5));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(StyleConstants.BACKGROUND_COLOR);

        cardButtons = new JButton[imagePaths.size()];
        CardClickListener listener = new CardClickListener();

        for (int i = 0; i < imagePaths.size(); i++) {
            cardButtons[i] = new JButton(hiddenIcon);
            cardButtons[i].setActionCommand(String.valueOf(i));
            cardButtons[i].setPreferredSize(new Dimension(CARD_SIZE, CARD_SIZE));
            cardButtons[i].addActionListener(listener);
            cardButtons[i].setBorder(BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR));
            cardButtons[i].setBackground(StyleConstants.BACKGROUND_COLOR);
            cardButtons[i].setFocusPainted(false); // Disable focus painting for cards
            mainPanel.add(cardButtons[i]);
        }

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(StyleConstants.BACKGROUND_COLOR);
        movesLabel = new JLabel("Moves: 0");
        movesLabel.setFont(StyleConstants.LABEL_FONT);
        movesLabel.setForeground(StyleConstants.TEXT_COLOR);
        statusPanel.add(movesLabel);

        add(statusPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class CardClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (flipTimer != null && flipTimer.isRunning()) {
                return;
            }

            int index = Integer.parseInt(e.getActionCommand());

            if (!cardButtons[index].isEnabled() || index == firstIndex) {
                return;
            }

            cardButtons[index].setIcon(loadImage(imagePaths.get(index), CARD_SIZE, CARD_SIZE));

            if (firstIndex == -1) {
                firstIndex = index;
                numMoves++;
                updateMovesLabel();
            } else {
                secondIndex = index;
                numMoves++;
                updateMovesLabel();

                flipTimer = new Timer(800, event -> checkMatch());
                flipTimer.setRepeats(false);
                flipTimer.start();
            }
        }
    }

    private void checkMatch() {

        if (imagePaths.get(firstIndex).equals(imagePaths.get(secondIndex))) {
            cardButtons[firstIndex].setEnabled(false);
            cardButtons[secondIndex].setEnabled(false);
             cardButtons[firstIndex].setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
             cardButtons[secondIndex].setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

            matchedPairs++;

            if (matchedPairs == (imagePaths.size() / 2)) {
                showScore();
            }
        } else {
            cardButtons[firstIndex].setIcon(hiddenIcon);
            cardButtons[secondIndex].setIcon(hiddenIcon);
        }

        firstIndex = -1;
        secondIndex = -1;
    }

    private void updateMovesLabel() {
         movesLabel.setText("Moves: " + numMoves);
    }

    private void showScore() {
        int totalCards = imagePaths.size();
        double score = (numMoves > 0) ? (totalCards * 1.0 / numMoves) * 100000 : 0;
        score = Math.round(score * 100000) / 100000.0;

        System.out.println("Game ended. Moves: " + numMoves + " Score: " + score);

        String message = String.format("Game Over!\nYour Score: %.4f\nNumber of Moves: %d", score, numMoves);
        JOptionPane.showMessageDialog(this, message, "Game Completed", JOptionPane.INFORMATION_MESSAGE);

        boolean updated = SQLDB.updateScore(userEmail, score);
         if (updated) {
             System.out.println("High score updated for " + userEmail);
         } else {
             System.out.println("Score not updated (either lower than existing or DB error) for " + userEmail);
         }

        this.dispose();
        new LeaderboardFrame(userEmail);
    }

    private ImageIcon loadImage(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Image file not found: " + path);
            return null;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.err.println("Error loading image '" + path + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

class SQLDB {
    private static final String DB_URL = "jdbc:sqlite:D:/Java/Java-Project/javaapp.db";
    private static Connection conn = null;

    public static boolean connect() {
        close();
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Database connection established.");
            return true;
        } catch (Exception e) {
             System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            conn = null;
            return false;
        }
    }

    public static boolean isUnique(String username, String email) {
         if (conn == null) {
             System.err.println("isUnique check failed: No database connection.");
             return false;
         }
        String query = "SELECT 1 FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            System.err.println("SQL error checking uniqueness: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertUser(String username, String email, String password) {
         if (conn == null) {
             System.err.println("insertUser failed: No database connection.");
             return false;
         }
        String query = "INSERT INTO users (username, email, password, score) VALUES (?, ?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
             System.err.println("SQL error inserting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginValid(String email, String password) {
         if (conn == null) {
             System.err.println("loginValid failed: No database connection.");
             return false;
         }
        String query = "SELECT 1 FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("SQL error during login validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateScore(String email, double newScore) {
        Connection updateConn = null;
        try {
            updateConn = DriverManager.getConnection(DB_URL);
            if (updateConn == null) {
                 System.err.println("updateScore failed: Could not connect to DB.");
                return false;
            }

            updateConn.setAutoCommit(false);
            double currentScore = -1.0;

            String selectQuery = "SELECT score FROM users WHERE email = ?";
            try (PreparedStatement selectStmt = updateConn.prepareStatement(selectQuery)) {
                selectStmt.setString(1, email);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentScore = rs.getDouble("score");
                    } else {
                        System.out.println("User not found with email for score update: " + email);
                        updateConn.rollback();
                        return false;
                    }
                }
            }

            if (newScore > currentScore) {
                String updateQuery = "UPDATE users SET score = ? WHERE email = ?";
                try (PreparedStatement updateStmt = updateConn.prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, newScore);
                    updateStmt.setString(2, email);
                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        updateConn.commit();
                        System.out.println("Score updated successfully for " + email + " to " + newScore);
                        return true;
                    } else {
                        System.err.println("Score update failed unexpectedly for " + email);
                        updateConn.rollback();
                        return false;
                    }
                }
            } else {
                 System.out.println("New score (" + newScore + ") is not higher than current (" + currentScore + ") for " + email + ". No update.");
                 updateConn.rollback();
                return false;
            }

        } catch (SQLException e) {
             System.err.println("SQL transaction failed during score update: " + e.getMessage());
            e.printStackTrace();
            if (updateConn != null) {
                try { updateConn.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            if (updateConn != null) {
                try {
                    updateConn.setAutoCommit(true);
                    updateConn.close();
                 } catch (SQLException e) { }
            }
        }
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
                 System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
             System.err.println("Error closing database resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class SignUp extends JFrame implements ActionListener {
    JTextField usernameField;
    JTextField emailField;
    JPasswordField passwordField;
    JButton signupButton;
    JButton loginButton;

    SignUp() {
        setTitle("Sign Up");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleConstants.BACKGROUND_COLOR);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(StyleConstants.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        styleLabel(userLabel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(userLabel, gbc);

        usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(usernameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        styleLabel(emailLabel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        styleTextField(emailField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(emailField, gbc);

        JLabel passLabel = new JLabel("Password:");
        styleLabel(passLabel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setEchoChar((char) 0);
        styleTextField(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        signupButton = new JButton("Sign Up");
        styleButton(signupButton);
        signupButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(signupButton, gbc);


        loginButton = new JButton("Go to Login");
        styleButton(loginButton);
        loginButton.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);

        add(panel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                SQLDB.close();
                dispose();
            }
        });

        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == signupButton) {
             if (!SQLDB.connect()) {
                showErrorDialog("Database connection failed. Cannot sign up.");
                return;
            }

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showErrorDialog("Please fill in all fields.");
                return;
            }
             if (!email.contains("@") || !email.contains(".")) {
                 showErrorDialog("Please enter a valid email address.");
                 return;
             }

            if (SQLDB.isUnique(username, email)) {
                boolean inserted = SQLDB.insertUser(username, email, password);
                if (inserted) {
                    handleSuccessfulSignup(email);
                } else {
                    showErrorDialog("Failed to create account. Database error.");
                }
            } else {
                showErrorDialog("Username or Email already exists.");
            }

        } else if (ae.getSource() == loginButton) {
            this.dispose();
            new Login();
        }
    }

     private void handleSuccessfulSignup(String userEmail) {
         JOptionPane.showMessageDialog(this,
                 "Signup successful! Click OK to start the game.",
                 "Success",
                 JOptionPane.INFORMATION_MESSAGE);
         this.dispose();
         new MemoryBoostGame(userEmail);
     }

    private void showErrorDialog(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Signup Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleLabel(JLabel label) {
        label.setFont(StyleConstants.LABEL_FONT);
        label.setForeground(StyleConstants.TEXT_COLOR);
    }

    private void styleTextField(JTextField field) {
        field.setFont(StyleConstants.TEXT_FIELD_FONT);
        field.setForeground(StyleConstants.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

     private void styleButton(JButton button) {
        button.setFont(StyleConstants.BUTTON_FONT);
        button.setBackground(StyleConstants.PRIMARY_COLOR);
        button.setForeground(StyleConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false); // Explicitly disable focus painting
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
         button.addMouseListener(new MouseAdapter() {
             public void mouseEntered(MouseEvent evt) {
                 button.setBackground(StyleConstants.SECONDARY_COLOR);
             }
             public void mouseExited(MouseEvent evt) {
                 button.setBackground(StyleConstants.PRIMARY_COLOR);
             }
         });
     }
}

class Login extends JFrame implements ActionListener {
    JTextField emailField;
    JPasswordField passwordField;
    JButton loginButton;
    JButton signupButton;

    Login() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleConstants.BACKGROUND_COLOR);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(StyleConstants.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        styleLabel(emailLabel);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        styleTextField(emailField);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(emailField, gbc);

        JLabel passLabel = new JLabel("Password:");
        styleLabel(passLabel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setEchoChar((char) 0);
        styleTextField(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        styleButton(loginButton);
        loginButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        signupButton = new JButton("Go to Sign Up");
        styleButton(signupButton);
        signupButton.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(signupButton, gbc);

        add(panel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                SQLDB.close();
                dispose();
            }
        });

        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == loginButton) {
             if (!SQLDB.connect()) {
                showErrorDialog("Database connection failed. Cannot login.");
                return;
            }

            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                showErrorDialog("Please enter both email and password.");
                return;
            }

            if (SQLDB.loginValid(email, password)) {
                 handleSuccessfulLogin(email);
            } else {
                showErrorDialog("Invalid Email or Password.");
            }
        } else if (ae.getSource() == signupButton) {
            this.dispose();
            new SignUp();
        }
    }

     private void handleSuccessfulLogin(String userEmail) {
         JOptionPane.showMessageDialog(this,
                 "Login successful! Click OK to start the game.",
                 "Success",
                 JOptionPane.INFORMATION_MESSAGE);
         this.dispose();
         new MemoryBoostGame(userEmail);
     }

    private void showErrorDialog(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleLabel(JLabel label) {
        label.setFont(StyleConstants.LABEL_FONT);
        label.setForeground(StyleConstants.TEXT_COLOR);
    }

    private void styleTextField(JTextField field) {
        field.setFont(StyleConstants.TEXT_FIELD_FONT);
        field.setForeground(StyleConstants.TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleButton(JButton button) {
        button.setFont(StyleConstants.BUTTON_FONT);
        button.setBackground(StyleConstants.PRIMARY_COLOR);
        button.setForeground(StyleConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false); // Explicitly disable focus painting
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.SECONDARY_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
         button.addMouseListener(new MouseAdapter() {
             public void mouseEntered(MouseEvent evt) {
                 button.setBackground(StyleConstants.SECONDARY_COLOR);
             }
             public void mouseExited(MouseEvent evt) {
                 button.setBackground(StyleConstants.PRIMARY_COLOR);
             }
         });
     }
}

public class MainApp {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found, using default.");
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                 System.err.println("Could not set default L&F either.");
            }
        }

        SwingUtilities.invokeLater(() -> {
             new SignUp();
        });

         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             System.out.println("Application shutting down, closing DB connection...");
             SQLDB.close();
             System.out.println("DB connection closed via shutdown hook.");
         }));
    }
}