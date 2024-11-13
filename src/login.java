import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class login extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public login() {
        setTitle("Login Ke bengkel");
        setSize(300, 175);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        add(panel);
        placeComponents(panel);

        btnLogin.addActionListener((ActionEvent e) -> {
            login();
        });
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        txtUsername = new JTextField(20);
        txtUsername.setBounds(100, 20, 165, 25);
        panel.add(txtUsername);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        txtPassword = new JPasswordField(20);
        txtPassword.setBounds(100, 50, 165, 25);
        panel.add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(10, 80, 255, 25);
        panel.add(btnLogin);
    }

    private void login() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (Connection conn = databaseconnect.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (SQLException e) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new login().setVisible(true);
        });
    }
}
