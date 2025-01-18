import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class checkouttest extends JFrame {
    private JComboBox<String> partsComboBox;
    private JTextField searchTextField, diskonTextField, jumlahTextField, totalTextField, uangDiterimaTextField, kembalianTextField;
    private JButton tambahButton, bayarButton, uangPasButton;
    private JTable tempTransactionTable;
    private JLabel lblPilihBarang, lblJumlah, lblDiskon, lblTotal, lblUang, lblKembalian, lblSearch;

    public checkouttest() {
        setTitle("Checkout System");
        setSize(600, 450); // Increased height for new layout
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Dashboard().setVisible(true);
                    dispose();
            }
        });

        // Search field
        lblSearch = new JLabel("Cari Barang:");
        lblSearch.setBounds(20, 5, 150, 15);
        add(lblSearch);

        searchTextField = new JTextField();
        searchTextField.setBounds(20, 20, 150, 25);
        add(searchTextField);

        // Labels
        lblPilihBarang = new JLabel("Pilih Barang:");
        lblPilihBarang.setBounds(20, 45, 150, 15);
        add(lblPilihBarang);

        // GUI Components - First Row
        partsComboBox = new JComboBox<>();
        partsComboBox.setBounds(20, 60, 150, 25);
        add(partsComboBox);

        lblJumlah = new JLabel("Jumlah:");
        lblJumlah.setBounds(180, 45, 100, 15);
        add(lblJumlah);

        jumlahTextField = new JTextField("1"); // Default value
        jumlahTextField.setBounds(180, 60, 50, 25);
        add(jumlahTextField);

        lblDiskon = new JLabel("Diskon:");
        lblDiskon.setBounds(240, 45, 100, 15);
        add(lblDiskon);

        diskonTextField = new JTextField("0"); // Default value
        diskonTextField.setBounds(240, 60, 50, 25);
        add(diskonTextField);

        tambahButton = new JButton("Tambah");
        tambahButton.setBounds(300, 60, 100, 25);
        add(tambahButton);

        // Table
        tempTransactionTable = new JTable(new DefaultTableModel(
            new Object[]{"Part", "Jumlah", "Harga Asli", "Harga Diskon", "Action"}, 0));
        JScrollPane scrollPane = new JScrollPane(tempTransactionTable);
        scrollPane.setBounds(20, 100, 550, 150);
        add(scrollPane);

        // Bottom components
        lblTotal = new JLabel("Total:");
        lblTotal.setBounds(20, 260, 100, 15);
        add(lblTotal);

        totalTextField = new JTextField();
        totalTextField.setBounds(20, 275, 100, 25);
        totalTextField.setEditable(false);
        add(totalTextField);

        lblUang = new JLabel("Masukkan Uang:");
        lblUang.setBounds(130, 260, 100, 15);
        add(lblUang);

        uangDiterimaTextField = new JTextField();
        uangDiterimaTextField.setBounds(130, 275, 100, 25);
        add(uangDiterimaTextField);

        lblKembalian = new JLabel("Kembalian:");
        lblKembalian.setBounds(240, 260, 100, 15);
        add(lblKembalian);

        kembalianTextField = new JTextField();
        kembalianTextField.setBounds(240, 275, 100, 25);
        kembalianTextField.setEditable(false);
        add(kembalianTextField);

        // Uang Pas button
        uangPasButton = new JButton("Uang Pas");
        uangPasButton.setBounds(350, 275, 100, 25);
        add(uangPasButton);

        // Bayar button moved to bottom
        bayarButton = new JButton("Bayar");
        bayarButton.setBounds(240, 310, 100, 25);
        add(bayarButton);

        // Add search functionality
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchParts(); }
            public void removeUpdate(DocumentEvent e) { searchParts(); }
            public void changedUpdate(DocumentEvent e) { searchParts(); }
        });

        // Event Listeners
        tambahButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tambahButtonActionPerformed(evt);
            }
        });

        bayarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bayarButtonActionPerformed(evt);
            }
        });

        uangPasButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                uangPasButtonActionPerformed(evt);
            }
        });

        tempTransactionTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                tempTransactionTableMouseClicked(evt);
            }
        });

        loadParts();
    }

    private void searchParts() {
        partsComboBox.removeAllItems();
        String searchText = searchTextField.getText().toLowerCase();
        
        try (Connection conn = databaseconnect.connect()) {
            String query = "SELECT namaparts FROM parts WHERE LOWER(namaparts) LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                partsComboBox.addItem(rs.getString("namaparts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void uangPasButtonActionPerformed(ActionEvent evt) {
        String totalText = totalTextField.getText();
        if (!totalText.isEmpty()) {
            uangDiterimaTextField.setText(totalText);
            kembalianTextField.setText("0");
        }
    }

    private void loadParts() {
        try (Connection conn = databaseconnect.connect()) {
            String query = "SELECT namaparts FROM parts";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                partsComboBox.addItem(rs.getString("namaparts"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tambahButtonActionPerformed(ActionEvent evt) {
    String namaparts = partsComboBox.getSelectedItem().toString();
    int jumlah = Integer.parseInt(jumlahTextField.getText());
    int diskon = Integer.parseInt(diskonTextField.getText());

    Part part = getPartFromDatabase(namaparts);
    if (part != null && part.getQuantity() >= jumlah) {
        int originalPrice = part.getPrice();
        int discountedPrice = originalPrice - (originalPrice * diskon / 100);

        // Save to temp_transaction table
        saveTempTransaction(namaparts, jumlah, originalPrice, discountedPrice, diskon);

        DefaultTableModel model = (DefaultTableModel) tempTransactionTable.getModel();
        model.addRow(new Object[]{namaparts, jumlah, originalPrice, discountedPrice, "Delete"});

        updatePartQuantity(part.getIdparts(), part.getQuantity() - jumlah);
        
        // Clear input fields
        jumlahTextField.setText("");
        diskonTextField.setText("0");
        
        // Update total setelah menambah
        calculateTotal();
    } else {
        JOptionPane.showMessageDialog(this, "Jumlah tidak mencukupi!");
    }
}
    
    private void saveTempTransaction(String namaparts, int quantity, int originalPrice, int discountedPrice, int discountPercentage) {
        try (Connection conn = databaseconnect.connect()) {
            String query = "INSERT INTO temp_transaction (namaparts, quantity, original_price, discounted_price, discount_percentage) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, namaparts);
            stmt.setInt(2, quantity);
            stmt.setInt(3, originalPrice);
            stmt.setInt(4, discountedPrice);
            stmt.setInt(5, discountPercentage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteTempTransaction(String namaparts, int quantity) {
    try (Connection conn = databaseconnect.connect()) {
        // Mengubah query untuk lebih spesifik
        String query = "DELETE FROM temp_transaction WHERE namaparts = ? AND quantity = ? ORDER BY id ASC LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, namaparts);
        stmt.setInt(2, quantity);
        
        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected == 0) {
            System.out.println("No matching record found to delete");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error deleting temp transaction: " + e.getMessage());
    }
    }

    private void tempTransactionTableMouseClicked(MouseEvent evt) {
    int row = tempTransactionTable.rowAtPoint(evt.getPoint());
    int col = tempTransactionTable.columnAtPoint(evt.getPoint());

    if (col == 4) {
        String partName = tempTransactionTable.getValueAt(row, 0).toString();
        int jumlah = Integer.parseInt(tempTransactionTable.getValueAt(row, 1).toString());

        // Hapus dari database temp_transaction
        deleteTempTransaction(partName, jumlah);

        Part part = getPartFromDatabase(partName);
        if (part != null) {
            updatePartQuantity(part.getIdparts(), part.getQuantity() + jumlah);
        }

        DefaultTableModel model = (DefaultTableModel) tempTransactionTable.getModel();
        model.removeRow(row);
        
        // Update total setelah menghapus
        calculateTotal();
    }
}

    private void bayarButtonActionPerformed(ActionEvent evt) {
        int total = calculateTotal();
        int uangDiterima = Integer.parseInt(uangDiterimaTextField.getText());

        if (uangDiterima >= total) {
            int kembalian = uangDiterima - total;
            kembalianTextField.setText(String.valueOf(kembalian));

            // Move data from temp_transaction to transaction
            moveToTransaction();
            
            // Clear the table
            DefaultTableModel model = (DefaultTableModel) tempTransactionTable.getModel();
            model.setRowCount(0);
            
            JOptionPane.showMessageDialog(this, "Transaksi berhasil!");
        } else {
            JOptionPane.showMessageDialog(this, "Uang tidak cukup!");
        }
    }
    
        private void moveToTransaction() {
        try (Connection conn = databaseconnect.connect()) {
            // First, insert all temp transactions into the transaction table
            String insertQuery = "INSERT INTO transaction (namaparts, quantity, original_price, discounted_price, discount_percentage) " +
                               "SELECT namaparts, quantity, original_price, discounted_price, discount_percentage " +
                               "FROM temp_transaction";
            
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.executeUpdate();

            // Then, clear the temp_transaction table
            String deleteQuery = "DELETE FROM temp_transaction";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage());
        }
    }

    private Part getPartFromDatabase(String partName) {
        try (Connection conn = databaseconnect.connect()) {
            String query = "SELECT * FROM parts WHERE namaparts = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, partName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Part(rs.getInt("idparts"), rs.getString("namaparts"), rs.getInt("price"), rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updatePartQuantity(int idparts, int newQuantity) {
        try (Connection conn = databaseconnect.connect()) {
            String query = "UPDATE parts SET quantity = ? WHERE idparts = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, idparts);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int calculateTotal() {
        int total = 0;
        DefaultTableModel model = (DefaultTableModel) tempTransactionTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (int) model.getValueAt(i, 3);
        }
        totalTextField.setText(String.valueOf(total));
        return total;
    }

    private void saveTransactionToDatabase() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new checkouttest().setVisible(true);
        });
    }

    class Part {
        private int idparts;
        private String name;
        private int price;
        private int quantity;

        public Part(int idparts, String name, int price, int quantity) {
            this.idparts = idparts;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public int getIdparts() { return idparts; }
        public String getName() { return name; }
        public int getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
}
