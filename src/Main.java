import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            login login = new login();
            login.setVisible(true);
        });
    }
}
