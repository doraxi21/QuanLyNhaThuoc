package MedicHome.UI.Dialog;

import MedicHome.UI.Login;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

public class BaoCaoDialog extends JDialog {
    private JLabel lblChenhLech;
    private long tongThuMay = 0;
    private JTextField txtThuc, txtGhiChu;
    private String maNV, tenNV, caLam;

    String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
    String user = "sa", pass = "hieudz";

    public BaoCaoDialog(JFrame parent, String maNV, String tenNV, String ca) {
        super(parent, "Báo cáo cuối ca", true);
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.caLam = ca;

        setSize(450, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

    //1. Top
        JLabel lblTitle = new JLabel("KẾT TOÁN CA");
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.BLUE);
        lblTitle.setBorder(new EmptyBorder(30, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

    //2. Center
        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tongThuMay = getDoanhThuCa();
        String strTongThu = new DecimalFormat("#,###").format(tongThuMay);

        int y = 0;

        addRow(center, gbc, "Mã Nhân viên:", maNV, y++);
        addRow(center, gbc, "Họ tên:", tenNV, y++);
        addRow(center, gbc, "Ca làm việc:", caLam, y++);

        // Kẻ ngang
        center.add(new JSeparator(), setGbc(gbc, y++, 2));

        // Số liệu hệ thống
        addRow(center, gbc, "Tổng thu (Máy):", strTongThu + " VNĐ", y++);

        // Nhập tiền thực tế
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        center.add(new JLabel("Tổng thực (Két):"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtThuc = new JTextField();
        txtThuc.setFont(new Font("Arial", Font.BOLD, 14));
        center.add(txtThuc, gbc);
        y++;

        // Hiển thị chênh lệch
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        center.add(new JLabel("Chênh lệch:"), gbc);

        gbc.gridx = 1;
        lblChenhLech = new JLabel("0 VNĐ");
        lblChenhLech.setFont(new Font("Arial", Font.BOLD, 14));
        lblChenhLech.setForeground(Color.RED);
        center.add(lblChenhLech, gbc);
        y++;

        // Ghi chú
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0;
        center.add(new JLabel("Ghi chú:"), gbc);

        gbc.gridx = 1;
        txtGhiChu = new JTextField();
        center.add(txtGhiChu, gbc);

        // Sự kiện nhập tiền -> Tính lệch ngay lập tức
        txtThuc.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { tinhChenhLech(); }
        });

        add(center, BorderLayout.CENTER);

    // 3. Bottom
        JButton btnGui = new JButton("CHỐT CA & GỬI BÁO CÁO");
        btnGui.setBackground(Color.BLUE);
        btnGui.setForeground(Color.WHITE);
        btnGui.setFont(new Font("Arial", Font.BOLD, 14));
        btnGui.setPreferredSize(new Dimension(0, 50));
        btnGui.setOpaque(true);
        btnGui.setBorderPainted(false);

        btnGui.addActionListener(e -> guiBaoCao());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(10, 20, 20, 20));
        bottom.add(btnGui, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    //tinh tong tien lay tu lich su ban
    private long getDoanhThuCa() {
        long tong = 0;
        String sql = "SELECT SUM(TIEN) FROM LSBAN WHERE MaNV=? AND Ca=? AND CONVERT(DATE, NGAYGIO) = CONVERT(DATE, GETDATE())";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, caLam);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tong = rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tong;
    }

    //ham tinh chenh lech
    private void tinhChenhLech() {
        try {
            String text = txtThuc.getText().replace(",", "").replace(".", "").trim();
            if (text.isEmpty()) {
                lblChenhLech.setText("0 VNĐ");
                return;
            }
            long thuc = Long.parseLong(text);
            long lech = thuc - tongThuMay;

            lblChenhLech.setText(new DecimalFormat("#,###").format(lech) + " VNĐ");
            // Xanh nếu dư/đủ, Đỏ nếu thiếu
            lblChenhLech.setForeground(lech >= 0 ? new Color(0, 153, 76) : Color.RED);
        } catch (Exception e) {}
    }

    //gui bao cao vao sql
    private void guiBaoCao() {
        try {
            String inputTien = txtThuc.getText().replace(",", "").replace(".", "").trim();
            if (inputTien.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tổng tiền thực tế trong két!");
                return;
            }

            long tienThuc = Long.parseLong(inputTien);
            long chenhLech = tienThuc - tongThuMay;

            String sql = "INSERT INTO BAOCAO (MaNV, TenNV, CaLam, NgayBaoCao, TongThuMay, TienThucTe, ChenhLech, GhiChu) " +
                    "VALUES (?, ?, ?, GETDATE(), ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, maNV);
                ps.setString(2, tenNV);
                ps.setString(3, caLam);
                ps.setLong(4, tongThuMay);
                ps.setLong(5, tienThuc);
                ps.setLong(6, chenhLech);
                ps.setString(7, txtGhiChu.getText());

                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Chốt ca thành công!\nHệ thống sẽ đăng xuất.");
                this.dispose();
                Window parent = SwingUtilities.getWindowAncestor(this);
                if(parent != null) parent.dispose();

                new Login().setVisible(true);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tiền thực tế phải là số!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu báo cáo: " + ex.getMessage());
        }
    }

    //Tạo giao diện nhanh
    private void addRow(JPanel p, GridBagConstraints g, String l, String v, int y) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = y; g.weightx = 0.0;
        p.add(new JLabel(l), g);

        g.gridx = 1; g.weightx = 1.0;
        JLabel val = new JLabel(v);
        val.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(val, g);
    }

    private GridBagConstraints setGbc(GridBagConstraints g, int y, int w) {
        g.gridx = 0; g.gridy = y; g.gridwidth = w;
        return g;
    }
}