import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import java.io.File;
import java.io.IOException;

public class TransactionHistory extends JFrame {
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JSpinner dateSpinnerFrom, dateSpinnerTo;
    private JButton printPDFButton;

    public TransactionHistory() {
        setTitle("Riwayat Transaksi");
        setSize(800, 600); // Adjusted size to accommodate new components
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // Initialize table model with columns
        String[] columns = {
            "ID", "Tanggal", "Nama Parts", "Jumlah", 
            "Harga Asli", "Harga Diskon", "Diskon (%)", "Action"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only allow editing of the Action column
            }
        };

        // Initialize table
        transactionTable = new JTable(tableModel);
        scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBounds(20, 100, 750, 420); // Adjusted position to fit new components
        add(scrollPane);

        // Initialize date spinners
        SpinnerDateModel modelFrom = new SpinnerDateModel();
        SpinnerDateModel modelTo = new SpinnerDateModel();
        dateSpinnerFrom = new JSpinner(modelFrom);
        dateSpinnerTo = new JSpinner(modelTo);

        JSpinner.DateEditor editorFrom = new JSpinner.DateEditor(dateSpinnerFrom, "yyyy-MM-dd");
        JSpinner.DateEditor editorTo = new JSpinner.DateEditor(dateSpinnerTo, "yyyy-MM-dd");
        dateSpinnerFrom.setEditor(editorFrom);
        dateSpinnerTo.setEditor(editorTo);

        // Set bounds
        dateSpinnerFrom.setBounds(20, 20, 120, 25);
        dateSpinnerTo.setBounds(160, 20, 120, 25);

        add(dateSpinnerFrom);
        add(dateSpinnerTo);

        // Button to print PDF
        printPDFButton = new JButton("Cetak PDF");
        printPDFButton.setBounds(670, 20, 100, 30);
        add(printPDFButton);

        printPDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printPDF();
            }
        });

        // Add mouse listener for delete button
        transactionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = transactionTable.columnAtPoint(e.getPoint());
                int row = transactionTable.rowAtPoint(e.getPoint());

                if (column == 7) { // Action column
                    int response = JOptionPane.showConfirmDialog(
                        null,
                        "Apakah anda yakin ingin menghapus transaksi ini?",
                        "Konfirmasi",
                        JOptionPane.YES_NO_OPTION
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        deleteTransaction(row);
                    }
                }
            }
        });

        // Load initial data
        loadTransactions();
    }

    private void drawCell(PDPageContentStream contentStream, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x + 2, y);
        contentStream.showText(text != null ? text : "");
        contentStream.endText();
    }

    private void printPDF() {
        Date fromDate = (Date) dateSpinnerFrom.getValue();
        Date toDate = (Date) dateSpinnerTo.getValue();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih rentang tanggal.");
            return;
        }

        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan PDF");
        fileChooser.setSelectedFile(new File("LaporanTransaksi.pdf"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        // Show save dialog
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Get selected file
        File fileToSave = fileChooser.getSelectedFile();
        
        // Add .pdf extension if not present
        if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
        }

        try (Connection conn = databaseconnect.connect()) {
            String query = "SELECT * FROM transaction WHERE transaction_date BETWEEN ? AND ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, dateFormat.format(fromDate));
            stmt.setString(2, dateFormat.format(toDate));
            ResultSet rs = stmt.executeQuery();

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(250, 750);
            contentStream.showText("Laporan Transaksi");
            contentStream.endText();

            // Table configuration
            float margin = 50;
            float y = 700;
            float rowHeight = 20;
            float[] columnWidths = {40, 90, 90, 50, 80, 80, 80};
            String[] headers = {"ID", "Tanggal", "Nama Parts", "Qty", "Harga", "Diskon", "Total"};

            // Draw headers
            float x = margin;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            for (int i = 0; i < headers.length; i++) {
                drawCell(contentStream, x, y, headers[i]);
                x += columnWidths[i];
            }

            // Draw content
            y -= rowHeight;
            float tableTopY = y;
            int totalHarga = 0;
            contentStream.setFont(PDType1Font.HELVETICA, 9);

            while (rs.next()) {
                if (y < 100) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    y = 700;
                }

                x = margin;
                drawCell(contentStream, x, y, rs.getString("id"));
                x += columnWidths[0];
                drawCell(contentStream, x, y, rs.getString("transaction_date"));
                x += columnWidths[1];
                drawCell(contentStream, x, y, rs.getString("namaparts"));
                x += columnWidths[2];
                drawCell(contentStream, x, y, String.valueOf(rs.getInt("quantity")));
                x += columnWidths[3];
                drawCell(contentStream, x, y, String.format("%,d", rs.getInt("original_price")));
                x += columnWidths[4];
                drawCell(contentStream, x, y, String.format("%,d", rs.getInt("discounted_price")));
                x += columnWidths[5];
                
                int total = rs.getInt("discounted_price");
                drawCell(contentStream, x, y, String.format("%,d", total));
                totalHarga += total;
                
                y -= rowHeight;
            }

            float tableBottomY = y;

            // Draw total at bottom
            x = margin;
            for (int i = 0; i < columnWidths.length - 1; i++) {
                x += columnWidths[i];
            }
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
            drawCell(contentStream, x, y, String.format("Total: %,d", totalHarga));

            contentStream.close();
            document.save(fileToSave);
            document.close();

            JOptionPane.showMessageDialog(this, "PDF berhasil disimpan di: " + fileToSave.getPath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void loadTransactions() {
        tableModel.setRowCount(0); // Clear existing data

        try (Connection conn = databaseconnect.connect()) {
            String query = "SELECT * FROM transaction ORDER BY transaction_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    dateFormat.format(rs.getTimestamp("transaction_date")),
                    rs.getString("namaparts"),
                    rs.getInt("quantity"),
                    rs.getInt("original_price"),
                    rs.getInt("discounted_price"),
                    rs.getString("discount_percentage"),
                    "Delete"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }
    }

    private void deleteTransaction(int row) {
        int transactionId = (int) tableModel.getValueAt(row, 0);
        String partName = (String) tableModel.getValueAt(row, 2);
        int quantity = (int) tableModel.getValueAt(row, 3);

        try (Connection conn = databaseconnect.connect()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // First, restore the quantity to parts table
                String updatePartsQuery = "UPDATE parts SET quantity = quantity + ? WHERE namaparts = ?";
                PreparedStatement updatePartsStmt = conn.prepareStatement(updatePartsQuery);
                updatePartsStmt.setInt(1, quantity);
                updatePartsStmt.setString(2, partName);
                updatePartsStmt.executeUpdate();

                // Then, delete the transaction
                String deleteQuery = "DELETE FROM transaction WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, transactionId);
                deleteStmt.executeUpdate();

                conn.commit(); // Commit transaction
                tableModel.removeRow(row);
                JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus");

            } catch (SQLException e) {
                conn.rollback(); // Rollback in case of error
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage());
        }
    }

    // Method to refresh the table
    public void refreshTable() {
        loadTransactions();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TransactionHistory().setVisible(true);
        });
    }
}