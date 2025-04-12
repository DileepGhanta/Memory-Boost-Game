import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MemoryCardGame extends JFrame {
    private JPanel mainPanel;
    private String selectedTheme;
    private ArrayList<String> imagePaths;
    private JButton[] cardButtons;
    private ImageIcon hiddenIcon;
    private int firstIndex = -1, secondIndex = -1;
    private Timer timer;
    static int numMoves = 0;
    private int matchedPairs = 0;
    private String userName;

    public MemoryCardGame() {
        setTitle("Memory Card Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load hidden card image
        hiddenIcon = loadImage("Images/hidden.png", 600, 600);
        
        // Ask for user name first
        askUserName();
    }

    private void askUserName() {
        userName = JOptionPane.showInputDialog(this, "Enter your name:", "User Name", JOptionPane.QUESTION_MESSAGE);
        if (userName == null || userName.trim().isEmpty()) {
            userName = "Demo";
        }
        selectTheme();
    }

    private void selectTheme() {
        String[] themes = {"Cricket", "IPL", "Anime", "Memes"};
        selectedTheme = (String) JOptionPane.showInputDialog(
            this, "Select a Theme:", "Theme Selection", 
            JOptionPane.QUESTION_MESSAGE, null, themes, themes[0]
        );

        if (selectedTheme != null) {
            loadImages(selectedTheme);
            initializeGame();
        } else {
            System.exit(0);
        }
    }

    private void loadImages(String theme) {
        imagePaths = new ArrayList<>();
        String folderPath = "Images/" + theme;

        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg"))) {
                        // System.out.println("Loaded Image: " + file.getAbsolutePath());
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }

        if (imagePaths.size() < 8) {
            JOptionPane.showMessageDialog(this, "Not enough images found in " + theme);
            System.exit(0);
        }

        // Duplicate the images for the memory game
        imagePaths = new ArrayList<>(imagePaths.subList(0, 8));
        imagePaths.addAll(new ArrayList<>(imagePaths));
        Collections.shuffle(imagePaths);
    }

    private void initializeGame() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 4));
        int cardWidth = 100;
        int cardHeight = 100;
        cardButtons = new JButton[imagePaths.size()];
        
        for (int i = 0; i < imagePaths.size(); i++) {
            cardButtons[i] = new JButton(hiddenIcon);
            cardButtons[i].setActionCommand(String.valueOf(i));
            cardButtons[i].setPreferredSize(new Dimension(cardWidth, cardHeight));
            cardButtons[i].addActionListener(new CardClickListener());
            mainPanel.add(cardButtons[i]);
        }

        add(mainPanel);
        pack();
        setVisible(true);
    }

    private class CardClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int index = Integer.parseInt(e.getActionCommand());
    
            // Ignore clicks on already matched cards
            if (cardButtons[index].getIcon() != hiddenIcon) {
                return;
            }
    
            // First card selection
            if (firstIndex == -1) {
                firstIndex = index;
                cardButtons[firstIndex].setIcon(loadImage(imagePaths.get(firstIndex), 100, 100));
            }
            // Second card selection
            else if (secondIndex == -1) {
                secondIndex = index;
                cardButtons[secondIndex].setIcon(loadImage(imagePaths.get(secondIndex), 100, 100));
                numMoves+=2;
                // System.out.println(numMoves);
    
                // Delay before checking for match
                timer = new Timer(1000, event -> checkMatch());
                timer.setRepeats(false);
                timer.start();
            }
        }
    }
    

    private void checkMatch() {
        if (imagePaths.get(firstIndex).equals(imagePaths.get(secondIndex))) {
            // Match: Disable cards
            cardButtons[firstIndex].setEnabled(false);
            cardButtons[secondIndex].setEnabled(false);
            matchedPairs++;
    
            if (matchedPairs == (imagePaths.size() / 2)) {
                showScore();
            }
        } else {
            // No match: Flip cards back
            cardButtons[firstIndex].setIcon(hiddenIcon);
            cardButtons[secondIndex].setIcon(hiddenIcon);
        }
    
        // Reset selected indices
        firstIndex = -1;
        secondIndex = -1;
    }
    

    private void showScore() {
        int totalCards = imagePaths.size();
        double score = (totalCards*1.0 /numMoves) * 100000;
        System.out.println(numMoves);
        JOptionPane.showMessageDialog(this, "Game Over, " + userName + "!\nYour Score: " + String.format("%.4f", score), "Game Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private ImageIcon loadImage(String path, int width, int height) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Image not found: " + path);
            return null;
        }

        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoryCardGame());
    }
}
