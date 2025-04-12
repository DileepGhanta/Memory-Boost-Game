
//Java AWT classes
import java.awt.*;
import java.awt.event.*;
//Java Database connection classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;



class SQLDB { // Databse connection object
    public static Connection conn = null;
    public static Statement stmt = null;
    public static ResultSet rset = null;

    public static void connect(String dbpath) {
        try {
            // Loading SQlite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Establishing connection with Database
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbpath);
            // Creting statment for execution of querires
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execute(String query) {
        try {
            // Executing query through statement object
            rset = stmt.executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Login form class
class Login extends WindowAdapter implements ActionListener { // Creating frame
    Frame f = new Frame("Login");

    // Creating frame components
    Label l1 = new Label("Email");
    Label l2 = new Label("Password");
    TextField t1 = new TextField();
    TextField t2 = new TextField();
    Button b1 = new Button("Login");
    Button b2 = new Button("Reset");
    Dialog d = new Dialog(f, "Login", true);

    // Constructor to configure window frame
    Login() { // Configuring Window frame
        f.setTitle("Login"); // Setting window title
        f.setSize(300, 300); // Setting window size
        f.setVisible(true); // Displaying window frame
        f.setLayout(new GridLayout(3, 2)); // Setting Grid layout

        // Adding components to frame
        f.add(l1);
        f.add(t1);
        f.add(l2);
        f.add(t2);
        f.add(b1);
        f.add(b2);

        // Adding action listener to buttons
        b1.addActionListener(this);
        b2.addActionListener(this);

        // Adding window listener to frame
        f.addWindowListener(this);
    }

    // Handling Button events
    public void actionPerformed(ActionEvent ae) { // Handling Login button event
        System.out.println(ae.getActionCommand());
        if (ae.getActionCommand().equals("Login")) { // Connect to Database
            SQLDB.connect("D:\\Java\\Java-Project\\javaapp.db");
            // Execute SQL query
            String q = "select * from users";
            q = q + " where email='" + t1.getText() + "' and password='" + t2.getText() + "';";
            SQLDB.execute(q);
            // Fetching results of query
            String msg = "";
            try {
                if (SQLDB.rset.next())
                    msg = "Login success...";
                else
                    msg = "Invalid user name or password";
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            // Displaying Dialog box
        
            d.setLayout(new FlowLayout());
            d.setTitle("Login");
            d.addWindowListener(this);
            d.add(new Label(msg, Label.CENTER));
            d.setLocationRelativeTo(f);
            d.setSize(300, 100);
            d.setVisible(true);
        }
        // Handling Reset button event
        else if (ae.getActionCommand().equals("Reset")) {
            t1.setText("");
            t2.setText("");
        }
    }

    // Handling window closing events
    public void windowClosing(WindowEvent we) {
        we.getWindow().dispose();
    }

}


// Driver Class
public class JavaApp { // main function
    public static void main(String[] args) { // Creating Login class object
        new Login();
    }
}
