import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class login extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public login() {
        setTitle("Login Ke bengkel");
        setSize(300, 300); // Ukuran ditingkatkan agar sesuai dengan gambar yang diskalakan
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

        // Tambahkan gambar di atas text field
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/gambar/auth.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel imgLabel = new JLabel(scaledIcon);
        imgLabel.setBounds(100, 10, 100, 100); // Atur posisi dan ukuran gambar
        panel.add(imgLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 120, 80, 25);
        panel.add(userLabel);

        txtUsername = new JTextField(20);
        txtUsername.setBounds(100, 120, 165, 25);
        panel.add(txtUsername);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 150, 80, 25);
        panel.add(passwordLabel);

        txtPassword = new JPasswordField(20);
        txtPassword.setBounds(100, 150, 165, 25);
        panel.add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(10, 190, 255, 25);
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
                // Buka halaman Dashboard
                new Dashboard().setVisible(true);
                dispose(); // Tutup jendela login
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new login().setVisible(true);
        });
    }
}
