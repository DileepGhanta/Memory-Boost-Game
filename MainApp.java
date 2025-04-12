    import java.awt.*;
    import java.awt.event.*;
    import java.sql.*;

    class SQLDB {
        public static Connection conn = null;
        public static Statement stmt = null;

        public static void connect(String dbpath) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
                stmt = conn.createStatement();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static boolean isUnique(String username, String email) {
            ResultSet rs = null;
            try {
                String query = "SELECT * FROM users WHERE username='" + username + "' OR email='" + email + "';";
                rs = stmt.executeQuery(query);
                return !rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
            }
        }

        public static boolean insertUser(String username, String email, String password) {
            try {
                String query = "INSERT INTO users (username, email, password, score) VALUES ('" + username + "', '" + email + "', '" + password + "', 0);";
                int rowsAffected = stmt.executeUpdate(query);
                return rowsAffected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static boolean loginValid(String email, String password) {
            try {
                String query = "SELECT * FROM users WHERE email='" + email + "' AND password='" + password + "';";
                ResultSet rs = stmt.executeQuery(query);
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        public static void close() {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    class SignUp extends Frame implements ActionListener {
        Label l1 = new Label("Username:");
        Label l2 = new Label("Email:");
        Label l3 = new Label("Password:");
        TextField t1 = new TextField();
        TextField t2 = new TextField();
        TextField t3 = new TextField();
        Button signupBtn = new Button("Signup");
        Button loginBtn = new Button("Login");

        SignUp() {
            Font labelFont = new Font("Arial", Font.PLAIN, 14);
            l1.setFont(labelFont);
            l2.setFont(labelFont);
            l3.setFont(labelFont);
            
            setTitle("Signup");
            setSize(350, 200);
            setLayout(new GridLayout(4, 2, 10, 10));
            setLocationRelativeTo(null);

            add(l1); 
            add(t1);
            add(l2); 
            add(t2);
            add(l3); 
            add(t3);
            add(signupBtn);
            add(loginBtn);

            signupBtn.setPreferredSize(new Dimension(100, 35));
            signupBtn.setBackground(new Color(0, 120, 215)); // Blue color
            signupBtn.setForeground(Color.WHITE);
            signupBtn.setFont(new Font("Arial", Font.BOLD, 14));

            loginBtn.setPreferredSize(new Dimension(100, 35));
            loginBtn.setBackground(new Color(0, 120, 215)); // Blue color
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setFont(new Font("Arial", Font.BOLD, 14));

            signupBtn.addActionListener(this);
            loginBtn.addActionListener(this);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    SQLDB.close();
                    dispose();
                    System.exit(0);
                }
            });

            setVisible(true);
        }

        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == signupBtn) {
                SQLDB.connect("D:\\Java\\Java-Project\\javaapp.db");
                String username = t1.getText().trim();
                String email = t2.getText().trim();
                String password = t3.getText().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showMessage("Please fill all fields",false);
                    return;
                }

                if (SQLDB.isUnique(username, email)) {
                    boolean inserted = SQLDB.insertUser(username, email, password);
                    if (inserted) {
                        showMessage("Signup successful",true);
                        t1.setText(""); t2.setText(""); t3.setText("");
                    } else {
                        showMessage("Failed to create account",false);
                    }
                } else {
                    showMessage("Username or Email already exists",false);
                }
            } else if (ae.getSource() == loginBtn) {
                SQLDB.close();
                this.dispose();
                new Login();
            }
        }

        private void showMessage(String msg, boolean closeFrameOnOk) {
            Dialog d = new Dialog(this, "Message", true);
            d.setLayout(new FlowLayout());
            d.add(new Label(msg));

            d.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    d.dispose();
                    if (closeFrameOnOk) {
                        dispose(); // Close login window
                    }
                }
            });

            d.setSize(250, 100);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }
    }

    class Login extends Frame implements ActionListener {
        Label l1 = new Label("Email:");
        Label l2 = new Label("Password:");
        TextField t1 = new TextField();
        TextField t2 = new TextField();
        Button loginBtn = new Button("Login");
        Button signupBtn = new Button("Signup");

        Login() {
            Font labelFont = new Font("Arial", Font.PLAIN, 14);
            l1.setFont(labelFont);
            l2.setFont(labelFont);

            t1.setPreferredSize(new Dimension(100, 30));
            t2.setPreferredSize(new Dimension(100, 30));

            setTitle("Login");
            setSize(350, 200);
            setLayout(new GridLayout(3, 2, 10, 10));
            setLocationRelativeTo(null);

            add(l1);
            add(t1);
            add(l2);
            add(t2);
            add(loginBtn);
            add(signupBtn);

            signupBtn.setPreferredSize(new Dimension(100, 35));
            signupBtn.setBackground(new Color(0, 120, 215)); // Blue color
            signupBtn.setForeground(Color.WHITE);
            signupBtn.setFont(new Font("Arial", Font.BOLD, 14));

            loginBtn.setPreferredSize(new Dimension(100, 35));
            loginBtn.setBackground(new Color(0, 120, 215)); // Blue color
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setFont(new Font("Arial", Font.BOLD, 14));

            loginBtn.addActionListener(this);
            signupBtn.addActionListener(this);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    SQLDB.close();
                    dispose();
                    System.exit(0);
                }
            });

            setVisible(true);
        }

        public void actionPerformed(ActionEvent ae) {
            SQLDB.connect("D:\\Java\\Java-Project\\javaapp.db");

            if (ae.getSource() == loginBtn) {
                String email = t1.getText().trim();
                String password = t2.getText().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    showMessage("Please enter credentials", false);
                    return;
                }

                boolean valid = SQLDB.loginValid(email, password);
                if (valid)
                    showMessage("Login successful", true);
                else
                    showMessage("Invalid Email or Password", false);

            } else if (ae.getSource() == signupBtn) {
                SQLDB.close();
                this.dispose();
                new SignUp();
            }
        }

        // 🔄 Modified to handle success case
        private void showMessage(String msg, boolean closeFrameOnOk) {
            Dialog d = new Dialog(this, "Message", true);
            d.setLayout(new FlowLayout());
            d.add(new Label(msg));

            d.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    d.dispose();
                    if (closeFrameOnOk) {
                        dispose(); // Close login window
                    }
                }
            });

            d.setSize(250, 100);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }
    }

    public class MainApp {
        public static void main(String[] args) {
            new SignUp();
        }
    }
