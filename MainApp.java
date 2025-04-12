import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class MainApp {
    static Connection con;
    static Statement stmt;

    public static void main(String[] args) {
        connectDB("javaapp.db");
        new Signup(); // Start with signup window
    }

    static void connectDB(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (uname TEXT PRIMARY KEY, pwd TEXT NOT NULL)");
            System.out.println("Connected to database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean execute(String query) {
        try {
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void insert(String query) {
        try {
            stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Signup {
        JFrame f;
        JTextField t1;
        JPasswordField p1;

        public Signup() {
            f = new JFrame("Signup");
            f.setSize(300, 200);
            f.setLayout(null);

            JLabel l1 = new JLabel("Username:");
            l1.setBounds(20, 20, 80, 25);
            f.add(l1);

            t1 = new JTextField();
            t1.setBounds(110, 20, 150, 25);
            f.add(t1);

            JLabel l2 = new JLabel("Password:");
            l2.setBounds(20, 60, 80, 25);
            f.add(l2);

            p1 = new JPasswordField();
            p1.setBounds(110, 60, 150, 25);
            f.add(p1);

            JButton b1 = new JButton("Signup");
            b1.setBounds(100, 100, 100, 30);
            f.add(b1);

            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String user = t1.getText();
                    String pass = new String(p1.getPassword());

                    if (user.isEmpty() || pass.isEmpty()) {
                        JOptionPane.showMessageDialog(f, "Please fill all fields.");
                        return;
                    }

                    String checkQuery = "SELECT * FROM users WHERE uname = '" + user + "'";
                    if (execute(checkQuery)) {
                        JOptionPane.showMessageDialog(f, "User already exists!");
                    } else {
                        String insertQuery = "INSERT INTO users(uname, pwd) VALUES('" + user + "', '" + pass + "')";
                        insert(insertQuery);
                        JOptionPane.showMessageDialog(f, "Signup Successful!");
                        f.dispose();
                        new Login();
                    }
                }
            });

            f.setVisible(true);
        }
    }

    static class Login {
        JFrame f;
        JTextField t1;
        JPasswordField p1;

        public Login() {
            f = new JFrame("Login");
            f.setSize(300, 200);
            f.setLayout(null);

            JLabel l1 = new JLabel("Username:");
            l1.setBounds(20, 20, 80, 25);
            f.add(l1);

            t1 = new JTextField();
            t1.setBounds(110, 20, 150, 25);
            f.add(t1);

            JLabel l2 = new JLabel("Password:");
            l2.setBounds(20, 60, 80, 25);
            f.add(l2);

            p1 = new JPasswordField();
            p1.setBounds(110, 60, 150, 25);
            f.add(p1);

            JButton b1 = new JButton("Login");
            b1.setBounds(100, 100, 100, 30);
            f.add(b1);

            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String user = t1.getText();
                    String pass = new String(p1.getPassword());

                    if (user.isEmpty() || pass.isEmpty()) {
                        JOptionPane.showMessageDialog(f, "Please fill all fields.");
                        return;
                    }

                    String loginQuery = "SELECT * FROM users WHERE uname = '" + user + "' AND pwd = '" + pass + "'";
                    if (execute(loginQuery)) {
                        JOptionPane.showMessageDialog(f, "Login Successful!");
                        f.dispose();
                        // You can launch your game here
                        JFrame g = new JFrame("Welcome");
                        g.setSize(200, 100);
                        g.add(new JLabel("Game/Portal Loaded"), SwingConstants.CENTER);
                        g.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(f, "Invalid username or password.");
                    }
                }
            });

            f.setVisible(true);
        }
    }
}
