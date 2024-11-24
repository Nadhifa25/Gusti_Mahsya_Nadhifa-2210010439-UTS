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



public class PengeluaranForm extends javax.swing.JFrame {

     private DefaultTableModel Model;
    
    public PengeluaranForm() {
        initComponents();
        
        // Inisialisasi model tabel
        Model = new DefaultTableModel(new String[]{"ID","Tanggal", "Kategori", "Jumlah"}, 0);
        tblRiwayat.setModel(Model);
        loadData();
        
        // Tambahkan listener untuk menangani klik pada tabel
        tblRiwayat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRiwayatMouseClicked(evt);  // Panggil metode yang menangani klik pada tabel
            }
        });
    }

   private Connection connectToDatabase() {
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
        Model.setRowCount(0);

        try (Connection conn = connectToDatabase();
            Statement stmt = conn != null ? conn.createStatement() : null;
            ResultSet rs = stmt != null ? stmt.executeQuery("SELECT * FROM pengeluaran") : null) {

        if (rs != null) {
            while (rs.next()) {
                // Ambil data dari ResultSet
                int id = rs.getInt("id"); // Kolom ID
                String tanggal = rs.getString("tanggal");
                String kategori = rs.getString("kategori");
                String jumlah = rs.getString("jumlah");

                // Tambahkan baris ke tableModel
                Model.addRow(new Object[]{id, tanggal, kategori, jumlah});
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
    String kategori = comboBoxKategori.getSelectedItem().toString();
    String jumlahText = txtJumlah.getText();

    // Validasi input
    if (tanggal == null || kategori.equals("-Pilih Kategori-") || jumlahText.isEmpty()) {
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
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO pengeluaran (tanggal, kategori, jumlah) VALUES (?, ?, ?)")) {

            pstmt.setString(1, tanggalFormatted);
            pstmt.setString(2, kategori);
            pstmt.setDouble(3, jumlah);
            pstmt.executeUpdate();

            // Tambahkan data ke tabel GUI
            loadData();

            // Bersihkan input
            dateChooser.setDate(null);
            comboBoxKategori.setSelectedIndex(0);
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
        int id = (int) Model.getValueAt(selectedRow, 0); // Kolom ID
        String tanggal = Model.getValueAt(selectedRow, 1).toString(); // Kolom Tanggal
        String kategori = Model.getValueAt(selectedRow, 2).toString(); // Kolom Kategori
        String jumlah = Model.getValueAt(selectedRow, 3).toString(); // Kolom Jumlah

        // Masukkan data ke form
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(tanggal);
            dateChooser.setDate(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboBoxKategori.setSelectedItem(kategori);
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
    String kategori = comboBoxKategori.getSelectedItem().toString();
    String jumlahText = txtJumlah.getText();

    // Validasi input
    if (tanggal == null || kategori.equals("-Pilih Kategori-") || jumlahText.isEmpty()) {
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
                     "UPDATE pengeluaran SET tanggal = ?, kategori = ?, jumlah = ? WHERE id = ?")) {

            pstmt.setString(1, tanggalFormatted);
            pstmt.setString(2, kategori);
            pstmt.setDouble(3, jumlah);
            pstmt.setInt(4, selectedId);
            pstmt.executeUpdate();

            // Refresh tabel
            loadData();

            // Reset form dan ID
            dateChooser.setDate(null);
            comboBoxKategori.setSelectedIndex(0);
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
            String sql = "DELETE FROM pengeluaran WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();  // Eksekusi query untuk menghapus data
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);

                // Hapus baris dari tabel
                Model.removeRow(selectedRow);
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
        fileChooser.setSelectedFile(new File("pengeluaran.pdf"));
        int userChoice = fileChooser.showSaveDialog(this);

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Buat dokumen PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Tulis judul
            document.add(new Paragraph("Laporan Pengeluaran"));
            document.add(Chunk.NEWLINE);

            // Tulis tabel
            PdfPTable table = new PdfPTable(4); // 4 kolom
            table.addCell("ID");
            table.addCell("Tanggal");
            table.addCell("Kategori");
            table.addCell("Jumlah");

            // Tulis data ke tabel PDF
            for (int i = 0; i < Model.getRowCount(); i++) {
                table.addCell(Model.getValueAt(i, 0).toString());
                table.addCell(Model.getValueAt(i, 1).toString());
                table.addCell(Model.getValueAt(i, 2).toString());
                table.addCell(Model.getValueAt(i, 3).toString());
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke PDF!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mengekspor data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}



    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnSimpan = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();
        comboBoxKategori = new javax.swing.JComboBox<>();
        txtJumlah = new javax.swing.JTextField();
        dateChooser = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRiwayat = new javax.swing.JTable();
        btnHapus = new javax.swing.JButton();
        btnEksport = new javax.swing.JButton();
        bg = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel4.setFont(new java.awt.Font("Forte", 0, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(102, 204, 255));
        jLabel4.setText("Pengeluaran Keuangan Anda");
        jPanel1.add(jLabel4);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Pilih Tanggal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 64;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(27, 71, 8, 0);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Kategori Pengeluaran");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 68, 8, 0);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Jumlah Pengeluaran");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 69, 8, 0);
        jPanel2.add(jLabel3, gridBagConstraints);

        btnSimpan.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 65, 0, 0);
        jPanel2.add(btnSimpan, gridBagConstraints);

        btnEdit.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 46, 0, 0);
        jPanel2.add(btnEdit, gridBagConstraints);

        btnKeluar.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 44, 0, 0);
        jPanel2.add(btnKeluar, gridBagConstraints);

        comboBoxKategori.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        comboBoxKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-Pilih Kategori-", "Makanan", "Jajan", "Skincare", "Hiburan", "Lainnya" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 58, 6, 0);
        jPanel2.add(comboBoxKategori, gridBagConstraints);

        txtJumlah.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 94;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 54, 6, 0);
        jPanel2.add(txtJumlah, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 59;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 58, 6, 11);
        jPanel2.add(dateChooser, gridBagConstraints);

        tblRiwayat.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        tblRiwayat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Id", "Tanggal", "Kategori", "Jumlah"
            }
        ));
        tblRiwayat.setPreferredSize(new java.awt.Dimension(150, 75));
        jScrollPane1.setViewportView(tblRiwayat);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.ipadx = 436;
        gridBagConstraints.ipady = 407;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(20, 73, 0, 50);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        btnHapus.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 46, 0, 0);
        jPanel2.add(btnHapus, gridBagConstraints);

        btnEksport.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        btnEksport.setText("Export");
        btnEksport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEksportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 44, 0, 0);
        jPanel2.add(btnEksport, gridBagConstraints);

        bg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Gambar/2-900.jpg"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 9;
        gridBagConstraints.ipadx = 128;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(bg, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        saveData();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        editData();
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
       hapusData();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnEksportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEksportActionPerformed
        exportToPdf();
    }//GEN-LAST:event_btnEksportActionPerformed

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
            java.util.logging.Logger.getLogger(PengeluaranForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PengeluaranForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PengeluaranForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengeluaranForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PengeluaranForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bg;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnEksport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> comboBoxKategori;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRiwayat;
    private javax.swing.JTextField txtJumlah;
    // End of variables declaration//GEN-END:variables
}
