import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import com.itextpdf.text.Chunk;


public class PemasukanForm extends javax.swing.JFrame {
    private DefaultTableModel tableModel;
    
    public PemasukanForm() {
        initComponents();
        
        // Inisialisasi tabel
        tableModel = new DefaultTableModel(new String[]{"Id", "Tanggal", "Jumlah"}, 0);

        // Atur model ke tabel
        tblRiwayat.setModel(tableModel);
        
        loadData(); // Muat data saat aplikasi dibuka
        
        // Tambahkan listener untuk menangani klik pada tabel
        tblRiwayat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRiwayatMouseClicked(evt);  // Panggil metode yang menangani klik pada tabel
            }
        });
        
    }
    
    
    private java.sql.Connection connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/keuangan_pribadi";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
            return null;
        }
    }


    
    private void loadData() {
    // Kosongkan tabel sebelum memuat data baru
        tableModel.setRowCount(0);

        try (Connection conn = connectToDatabase();
            Statement stmt = conn != null ? conn.createStatement() : null;
            ResultSet rs = stmt != null ? stmt.executeQuery("SELECT * FROM pemasukan") : null) {

        if (rs != null) {
            while (rs.next()) {
                // Ambil data dari ResultSet
                int id = rs.getInt("id"); // Kolom ID
                String tanggal = rs.getString("tanggal");
                String jumlah = rs.getString("jumlah");

                // Tambahkan baris ke tableModel
                tableModel.addRow(new Object[]{id, tanggal, jumlah});
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: Koneksi tidak tersedia.");
        }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void saveData() {
    // Ambil data dari inputan
    Date tanggal = dateChooser.getDate();
    String jumlahText = txtJumlah.getText();

    // Validasi input
    if (tanggal == null || jumlahText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Harap isi semua data dengan benar!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Konversi jumlah ke angka
        double jumlah = Double.parseDouble(jumlahText);

        // Format tanggal ke format database (yyyy-MM-dd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tanggalFormatted = sdf.format(tanggal);

        // Simpan data ke database
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pemasukan (tanggal, jumlah) VALUES (?, ?)")) {

            pstmt.setString(1, tanggalFormatted);
            pstmt.setDouble(2, jumlah);
            pstmt.executeUpdate();

            // Tambahkan data ke tabel GUI
            loadData();

            // Bersihkan input
            dateChooser.setDate(null);
            txtJumlah.setText("");

            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    private void tblRiwayatMouseClicked(java.awt.event.MouseEvent evt) {                                          
    int selectedRow = tblRiwayat.getSelectedRow();
    if (selectedRow != -1) {
        // Ambil data dari baris yang dipilih
        int id = (int) tableModel.getValueAt(selectedRow, 0); // Kolom ID
        String tanggal = tableModel.getValueAt(selectedRow, 1).toString(); // Kolom Tanggal
        String jumlah = tableModel.getValueAt(selectedRow, 2).toString(); // Kolom Jumlah

        // Masukkan data ke form
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(tanggal);
            dateChooser.setDate(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        txtJumlah.setText(jumlah);

        // Simpan ID ke variabel global untuk digunakan saat update
        selectedId = id;
        }
    }

    
    private int selectedId = -1; // ID data yang dipilih dari tabel

    private void editData() {
    // Validasi apakah ada data yang dipilih
    if (selectedId == -1) {
        JOptionPane.showMessageDialog(this, "Pilih data yang ingin diedit terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Ambil data dari form
    Date tanggal = dateChooser.getDate();
    String jumlahText = txtJumlah.getText();

    // Validasi input
    if (tanggal == null || jumlahText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Harap isi semua data dengan benar!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Konversi jumlah ke angka
        double jumlah = Double.parseDouble(jumlahText);

        // Format tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tanggalFormatted = sdf.format(tanggal);

        // Update data di database
        try (Connection conn = connectToDatabase();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE pemasukan SET tanggal = ?, jumlah = ? WHERE id = ?")) { // Perbaiki nama tabel

            pstmt.setString(1, tanggalFormatted);
            pstmt.setDouble(2, jumlah);
            pstmt.setInt(3, selectedId); // Perbaiki urutan parameter
            pstmt.executeUpdate();

            // Refresh tabel
            loadData();

            // Reset form dan ID
            dateChooser.setDate(null);
            txtJumlah.setText("");
            selectedId = -1;

            JOptionPane.showMessageDialog(this, "Data berhasil diedit!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal mengedit data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


     private void hapusData() {
         int selectedRow = tblRiwayat.getSelectedRow(); // Mendapatkan baris yang dipilih

    if (selectedRow >= 0) {
        // Ambil ID dari baris yang dipilih
        int id = (int) tblRiwayat.getValueAt(selectedRow, 0);
        
        // Hapus dari database
        try (Connection conn = connectToDatabase()) {
            String sql = "DELETE FROM pemasukan WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();  // Eksekusi query untuk menghapus data
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Hapus baris dari tabel
                tableModel.removeRow(selectedRow);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus.", "Warning", JOptionPane.WARNING_MESSAGE);
    }
    }
    
    private void exportToPdf() {
    try {
        // Tentukan lokasi dan nama file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Sebagai PDF");
        fileChooser.setSelectedFile(new File("pemasukan.pdf"));
        int userChoice = fileChooser.showSaveDialog(this);

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Buat dokumen PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Tulis judul
            document.add(new Paragraph("Laporan Pemasukan"));
            document.add(Chunk.NEWLINE);

            // Tulis tabel
            PdfPTable table = new PdfPTable(3); // 3 kolom
            table.addCell("ID");
            table.addCell("Tanggal");
            table.addCell("Jumlah");

            // Tulis data ke tabel PDF
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                table.addCell(tableModel.getValueAt(i, 0).toString());
                table.addCell(tableModel.getValueAt(i, 1).toString());
                table.addCell(tableModel.getValueAt(i, 2).toString());
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke PDF!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mengekspor data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnSimpan = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();
        txtJumlah = new javax.swing.JTextField();
        dateChooser = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();
        btnHapus = new javax.swing.JButton();
        btnEksport = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel1.setBackground(new java.awt.Color(102, 204, 255));
        jLabel1.setFont(new java.awt.Font("Forte", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 204, 255));
        jLabel1.setText("Pemasukan Keuangan Anda");
        jPanel1.add(jLabel1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Pilih Tanggal");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, 123, -1));

        jLabel5.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Input Pemasukan");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(247, 54, -1, -1));

        btnSimpan.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });
        jPanel3.add(btnSimpan, new org.netbeans.lib.awtextra.AbsoluteConstraints(223, 95, -1, -1));

        btnEdit.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jPanel3.add(btnEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 95, -1, -1));

        btnKeluar.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });
        jPanel3.add(btnKeluar, new org.netbeans.lib.awtextra.AbsoluteConstraints(593, 95, -1, -1));

        txtJumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJumlahActionPerformed(evt);
            }
        });
        jPanel3.add(txtJumlah, new org.netbeans.lib.awtextra.AbsoluteConstraints(426, 52, 135, -1));
        jPanel3.add(dateChooser, new org.netbeans.lib.awtextra.AbsoluteConstraints(426, 12, 135, -1));

        tblRiwayat.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tblRiwayat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Id", "Tanggal", "Jumlah"
            }
        ));
        jScrollPane1.setViewportView(tblRiwayat);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 147, -1, -1));

        btnHapus.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
        jPanel3.add(btnHapus, new org.netbeans.lib.awtextra.AbsoluteConstraints(413, 95, -1, -1));

        btnEksport.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnEksport.setText("Export");
        btnEksport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEksportActionPerformed(evt);
            }
        });
        jPanel3.add(btnEksport, new org.netbeans.lib.awtextra.AbsoluteConstraints(504, 95, -1, -1));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Gambar/2-900.jpg"))); // NOI18N
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(-290, -150, 1310, 840));

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtJumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJumlahActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        editData();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        saveData();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        // Menutup form PemasukanForm dan membuka MenuUtama
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this, "Apakah Anda yakin ingin kembali ke Menu Utama?", "Konfirmasi Keluar", 
            javax.swing.JOptionPane.YES_NO_OPTION
        );
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            this.dispose();  // Menutup form PemasukanForm
        
            // Membuka MenuUtama
            MenuUtama menuUtama = new MenuUtama();  // Pastikan MenuUtama sudah ada di proyek Anda
            menuUtama.setVisible(true);  // Menampilkan MenuUtama
        }
    }//GEN-LAST:event_btnKeluarActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        hapusData();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnEksportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEksportActionPerformed
        exportToPdf();
    }//GEN-LAST:event_btnEksportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PemasukanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PemasukanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PemasukanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PemasukanForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PemasukanForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEksport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnSimpan;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtJumlah;
    // End of variables declaration//GEN-END:variables
}
