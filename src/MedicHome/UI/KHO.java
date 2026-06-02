package MedicHome.UI;

import MedicHome.DataAccessObject.Thuoc;
import MedicHome.DataAccessObject.ThuocDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class KHO extends JFrame {
    private String maNV_Current;
    private String tenNV_Current;
    private String caLamViec;

    static final String DB_URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=QuanlyMedicHome;integratedSecurity=true;trustServerCertificate=true;";
    static final String DB_USER = "sa";
    static final String DB_PASS = "hieudz";

    private JTabbedPane tabPane;

    public KHO(String maNV, String tenNV, String ca) {
        this.maNV_Current = maNV;
        this.tenNV_Current = tenNV;
        this.caLamViec = ca;

        setTitle("Quản Lý Kho - Medic Home");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

    //1. Top
        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBackground(new Color(30, 144, 255));
        pnTop.setPreferredSize(new Dimension(0, 60));
        pnTop.setBorder(new EmptyBorder(0, 20, 7, 20));

        JLabel lbLogo = new JLabel("Medic Home Of Doraxi");
        lbLogo.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 40));
        lbLogo.setForeground(Color.WHITE);

        JPanel pnInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnInfo.setOpaque(false);
        JLabel lbUser = new JLabel(tenNV_Current + " (" + maNV + ")  |  Ca: " + ca);
        lbUser.setFont(new Font("Arial", Font.BOLD, 16));
        lbUser.setForeground(Color.WHITE);

        //ICON MENU
        JButton btmenu = new JButton();
        try {
            ImageIcon settingIcon = new ImageIcon(getClass().getResource("/img/setting.png"));
            Image scale = settingIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            btmenu.setIcon(new ImageIcon(scale));
        } catch (Exception e) {
            btmenu.setText("MENU");
        }
        btmenu.setPreferredSize(new Dimension(40, 40));
        btmenu.setBackground(Color.WHITE);
        btmenu.setBorderPainted(false);   //tắt viền
        btmenu.setContentAreaFilled(false);   //làm trong suốt nền
        btmenu.setFocusPainted(false);     //tắt viền focus, đường nét đứt bao quannh khi ấn vô
        btmenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuLogout = new JMenuItem("Đăng xuất");
        menuLogout.setForeground(Color.RED);
        menuLogout.addActionListener(e -> { this.dispose(); new Login().setVisible(true); });
        popupMenu.add(menuLogout);

        btmenu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { popupMenu.show(e.getComponent(), 0, e.getComponent().getHeight()); }
        });

        pnInfo.add(lbUser);
        pnInfo.add(btmenu);
        pnTop.add(lbLogo, BorderLayout.WEST);
        pnTop.add(pnInfo, BorderLayout.EAST);
        add(pnTop, BorderLayout.NORTH);

    //2. TabPane
        tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabPane.addTab("  TỒN KHO HIỆN TẠI  ", new TonKhoPanel());
        tabPane.addTab("  NHẬP HÀNG MỚI  ", new NhapKhoPanel());
        tabPane.addTab("  LỊCH SỬ NHẬP  ", new BaoCaoKhoPanel());
        add(tabPane, BorderLayout.CENTER);
    }

    //TAB 1: TỒN KHO
    class TonKhoPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        public TonKhoPanel() {
            setLayout(new BorderLayout());
            JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRefresh = new JButton("Làm mới");
            btnRefresh.addActionListener(e -> loadData());

            toolBar.add(btnRefresh);
            add(toolBar, BorderLayout.NORTH);

            String[] cols = {"Mã SP", "Tên Thuốc", "Loại", "Đơn vị", "NCC", "NSX", "HSD", "Số lượng", "Giá bán"};
            model = new DefaultTableModel(cols, 0);
            table = new JTable(model);
            table.setRowHeight(35);
            add(new JScrollPane(table), BorderLayout.CENTER);
            loadData();
        }

        void loadData() {
            model.setRowCount(0);  //reset
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT * FROM THUOC";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("MASP"),
                            rs.getString("TENSP"),
                            rs.getString("LOAI"),
                            rs.getString("DONVI"),
                            rs.getString("NCC"),
                            rs.getDate("NSX"),
                            rs.getDate("HSD"),
                            rs.getInt("SOLUONG"),
                            rs.getLong("TIEN")
                    });
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    //TAB 2: NHẬP HÀNG
    class NhapKhoPanel extends JPanel {
        JTextField txtMa, txtTen, txtLoai, txtGiaNhap, txtSLNhap, txtNCC, txtNSX, txtHSD;
        JLabel lblTongTien;
        JComboBox<String> boxDonVi;
        DefaultTableModel tableModel;

        public NhapKhoPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            JPanel pnInput = new JPanel(new GridBagLayout());
            pnInput.setBorder(BorderFactory.createTitledBorder("Nhập thông tin chi tiết"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

            //Hàng 1
            gbc.gridy=0; gbc.gridx=0; pnInput.add(new JLabel("Mã SP:"), gbc);
            gbc.gridx=1; txtMa = new JTextField(12); pnInput.add(txtMa, gbc);
            gbc.gridx=2; pnInput.add(new JLabel("Tên thuốc:"), gbc);
            gbc.gridx=3; txtTen = new JTextField(12); pnInput.add(txtTen, gbc);
            gbc.gridx=4; pnInput.add(new JLabel("Loại:"), gbc);
            gbc.gridx=5; txtLoai = new JTextField(12); pnInput.add(txtLoai, gbc);

            //Hàng 2
            gbc.gridy=1; gbc.gridx=0; pnInput.add(new JLabel("Đơn vị:"), gbc);
            gbc.gridx=1; boxDonVi = new JComboBox<>(new String[]{"Viên","Vỉ","Hộp"}); pnInput.add(boxDonVi, gbc);
            gbc.gridx=2; pnInput.add(new JLabel("Nhà Cung Cấp:"), gbc);
            gbc.gridx=3; txtNCC = new JTextField(12); pnInput.add(txtNCC, gbc);
            gbc.gridx=4; pnInput.add(new JLabel("NSX (yy-mm-dd):"), gbc);
            gbc.gridx=5; txtNSX = new JTextField(LocalDate.now().toString(), 12); pnInput.add(txtNSX, gbc);

            //Hàng 3
            gbc.gridy=2; gbc.gridx=0; pnInput.add(new JLabel("HSD (yy-mm-dd):"), gbc);
            gbc.gridx=1; txtHSD = new JTextField(LocalDate.now().plusYears(2).toString(), 12); pnInput.add(txtHSD, gbc);
            gbc.gridx=2; pnInput.add(new JLabel("Số lượng nhập:"), gbc);
            gbc.gridx=3; txtSLNhap = new JTextField(12); pnInput.add(txtSLNhap, gbc);
            gbc.gridx=4; pnInput.add(new JLabel("Giá nhập:"), gbc);
            gbc.gridx=5; txtGiaNhap = new JTextField(12); pnInput.add(txtGiaNhap, gbc);

            //Nút chức năng
            JPanel pnButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            JButton btnAdd = new JButton("Thêm");
            btnAdd.setBackground(new Color(0, 153, 76));
            btnAdd.setForeground(Color.WHITE);
            btnAdd.setPreferredSize(new Dimension(100, 30));

            JButton btnClear = new JButton("Làm mới");
            btnClear.setPreferredSize(new Dimension(100, 30));

            pnButtons.add(btnAdd);
            pnButtons.add(btnClear);

            gbc.gridy=3; gbc.gridx=0; gbc.gridwidth=6;
            gbc.anchor = GridBagConstraints.CENTER;
            pnInput.add(pnButtons, gbc);

            add(pnInput, BorderLayout.NORTH);

            //Bảng nháp
            String[] cols = {"Mã SP", "Tên", "Loại", "Đơn vị", "NCC", "NSX", "HSD", "SL Nhập", "Giá Nhập", "Thành Tiền"};
            tableModel = new DefaultTableModel(cols, 0);
            add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

            //Bottom panel
            JPanel bot = new JPanel(new BorderLayout());
            lblTongTien = new JLabel("   Tổng tiền: 0 VNĐ");
            lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
            JButton btnSave = new JButton("Nhập Kho");
            btnSave.setBackground(Color.BLUE);
            btnSave.setForeground(Color.WHITE);
            btnSave.setPreferredSize(new Dimension(250, 50));
            btnSave.setFont(new Font("Arial", Font.BOLD, 14));

            bot.add(lblTongTien, BorderLayout.CENTER);
            bot.add(btnSave, BorderLayout.EAST);
            add(bot, BorderLayout.SOUTH);

            //Event các nút
            btnAdd.addActionListener(e -> {
                try {
                    if(txtMa.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã SP!"); return; }
                    long thanhtien = Long.parseLong(txtSLNhap.getText()) * Long.parseLong(txtGiaNhap.getText());
                    tableModel.addRow(new Object[]{
                            txtMa.getText(), txtTen.getText(), txtLoai.getText(), boxDonVi.getSelectedItem(),
                            txtNCC.getText(), txtNSX.getText(), txtHSD.getText(), txtSLNhap.getText(), txtGiaNhap.getText(), thanhtien });
                    tinhTongTien();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Vui lòng kiểm tra lại số lượng và giá!"); }
            });

            btnClear.addActionListener(e -> {
                txtMa.setText("");
                txtTen.setText("");
                txtLoai.setText("");
                txtNCC.setText("");
                txtSLNhap.setText("");
                txtGiaNhap.setText("");
                txtNSX.setText(LocalDate.now().toString());
                txtHSD.setText(LocalDate.now().plusYears(2).toString());  // 2 là số năm hết hạn, <mặc định>
                boxDonVi.setSelectedIndex(0);
            });

            btnSave.addActionListener(e -> luuVaoSQL());
        }

        void tinhTongTien() {
            long sum = 0;
            for(int i=0; i<tableModel.getRowCount(); i++) sum += (long)tableModel.getValueAt(i, 9);
            lblTongTien.setText("Tổng tiền: " + sum + " VNĐ");
        }

        void luuVaoSQL() {
            if(tableModel.getRowCount() == 0) return;
            if(JOptionPane.showConfirmDialog(this, "Xác nhận lưu các phiếu nhập này vào hệ thống?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "INSERT INTO NHAP_KHO (MaSP, TenSP, Loai, DonVi, NCC, NSX, HSD, SoLuongNhap, GiaNhap, TongTienNhap, TrangThai, NgayNhap, MaNV, TenNhanVien, Ca, GhiChu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                for(int i=0; i<tableModel.getRowCount(); i++) {
                    ps.setString(1, tableModel.getValueAt(i,0).toString());
                    ps.setString(2, tableModel.getValueAt(i,1).toString());
                    ps.setString(3, tableModel.getValueAt(i,2).toString());
                    ps.setString(4, tableModel.getValueAt(i,3).toString());
                    ps.setString(5, tableModel.getValueAt(i,4).toString());
                    ps.setDate(6, Date.valueOf(tableModel.getValueAt(i,5).toString()));
                    ps.setDate(7, Date.valueOf(tableModel.getValueAt(i,6).toString()));
                    ps.setInt(8, Integer.parseInt(tableModel.getValueAt(i,7).toString()));
                    ps.setLong(9, Long.parseLong(tableModel.getValueAt(i,8).toString()));
                    ps.setLong(10, (long)tableModel.getValueAt(i,9));
                    ps.setString(11, "Chưa duyệt");
                    ps.setString(12, maNV_Current);
                    ps.setString(13, tenNV_Current);
                    ps.setString(14, caLamViec);
                    ps.setString(15, "Nhập kho mới");
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Đã lưu thành công vào CSDL! Vui lòng chờ Quản lý duyệt.");
                tableModel.setRowCount(0); tinhTongTien();
            } catch (SQLException e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Lỗi SQL: " + e.getMessage()); }
        }
    }

    //TAB 3: LỊCH SỬ NHẬP
    class BaoCaoKhoPanel extends JPanel {
        DefaultTableModel model;

        public BaoCaoKhoPanel() {
            setLayout(new BorderLayout());
            // Header
            JLabel lblTitle = new JLabel("LỊCH SỬ NHẬP HÀNG", JLabel.CENTER);
            lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
            lblTitle.setForeground(new Color(0, 102, 204));
            lblTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
            add(lblTitle, BorderLayout.NORTH);

            model = new DefaultTableModel(new String[]{"Ngày Nhập", "Mã SP", "Tên Thuốc", "SL", "Giá Nhập", "Thành Tiền", "NV", "Trạng Thái"}, 0);
            JTable table = new JTable(model);
            table.setRowHeight(28);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnRefresh = new JButton("Load Data SQL");
            btnRefresh.addActionListener(e -> loadData());

            pnlBot.add(btnRefresh);
            add(pnlBot, BorderLayout.SOUTH);
        }

        void loadData() {
            model.setRowCount(0);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "SELECT NgayNhap, MaSP, TenSP, SoLuongNhap, GiaNhap, TongTienNhap, TenNhanVien, TrangThai FROM NHAP_KHO WHERE MaNV = ? ORDER BY NgayNhap DESC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, maNV_Current);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    model.addRow(new Object[]{rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getLong(5), rs.getLong(6), rs.getString(7), rs.getString(8)});
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KHO("01", "doraxi", "1").setVisible(true));
    }
}