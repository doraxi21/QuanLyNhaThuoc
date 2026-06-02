package MedicHome.UI;

import MedicHome.DataAccessObject.Thuoc;
import MedicHome.DataAccessObject.ThuocDAO;
import MedicHome.DataAccessObject.LichSuDAO;
import MedicHome.UI.Dialog.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class NhanVien extends JFrame {


    private JTabbedPane tabPane;

    // luu info nhan vien de ghi de ca va bao cao
    private String maNV_Current = "nhanvien"; // thiet lap mac dinh
    private String tenCa_Current = "Sáng"; //login se thay the theo info
    private String tenNV_Current ="Nhan Vien";
    public NhanVien(String maNV,String tenNV, String caLamViec) {
        setTitle("Quản Lý Bán Hàng - Medic Home");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // kiem tra ca nao
        this.maNV_Current=maNV;
        this.tenNV_Current=tenNV;
        if (caLamViec != null) {
            if (caLamViec.contains("1")) tenCa_Current = "Sáng";  // ca lam phai co 1, 2, 3 de check
            else if (caLamViec.contains("2")) tenCa_Current = "Chiều";
            else if (caLamViec.contains("3")) tenCa_Current = "Tối";
        }

    //1.Top
        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBackground(new Color(30, 144, 255));
        pnTop.setPreferredSize(new Dimension(0, 60));
        pnTop.setBorder(new EmptyBorder(7, 20, 10, 20));
        //ten nha thuoc
        JLabel lbTenStore = new JLabel("Medic Home of Doraxi");
        lbTenStore.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 40));
        lbTenStore.setForeground(Color.WHITE);
         //ca
        JPanel pnRig = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnRig.setOpaque(false);
        JLabel lbCa = new JLabel("Ca: " + tenCa_Current + "  |");
        lbCa.setForeground(Color.WHITE);
        lbCa.setFont(new Font("Bradley Hand ITC", Font.BOLD, 25));
        //nut menu
        JButton btmenu = new JButton();
        try {
            ImageIcon settingIcon = new ImageIcon(getClass().getResource("/img/setting.png"));
            Image scale = settingIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            btmenu.setIcon(new ImageIcon(scale));
        } catch (Exception e) {
            btmenu.setText("MENU");
        }
        btmenu.setPreferredSize(new Dimension(60, 50));
        btmenu.setBackground(Color.WHITE);
        btmenu.setBorderPainted(false);
        btmenu.setContentAreaFilled(false);
        btmenu.setFocusPainted(false);
        btmenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu popupMenu = new JPopupMenu();  //tao menu item
        //cac nut chuc nang cua menu
        JMenuItem menuHangHoa = new JMenuItem("Hàng hoá");
        menuHangHoa.setFont(new Font("Arial", Font.PLAIN, 14));
        menuHangHoa.addActionListener(e -> new HangHoaDialog(this,false).setVisible(true));

        JMenuItem menuLSBan = new JMenuItem("Lịch sử bán");
        menuLSBan.setFont(new Font("Arial", Font.PLAIN, 14));
        menuLSBan.addActionListener(e -> new LichSuBanDialog(this).setVisible(true));

        JMenuItem menuBaoCao = new JMenuItem("Báo cáo & Chốt ca");
        menuBaoCao.setFont(new Font("Arial", Font.PLAIN, 14));
        menuBaoCao.addActionListener(e -> new BaoCaoDialog(this, maNV_Current,tenNV_Current,tenCa_Current).setVisible(true));


        JMenuItem menuLogout = new JMenuItem("Đăng xuất");
        menuLogout.setFont(new Font("Arial", Font.BOLD, 14));
        menuLogout.setForeground(Color.RED);
        menuLogout.addActionListener(e -> {
            this.dispose();
            new Login().setVisible(true);
        });

        popupMenu.add(menuHangHoa);
        popupMenu.add(menuLSBan);
        popupMenu.add(menuBaoCao);
        popupMenu.addSeparator();
        popupMenu.add(menuLogout);

        btmenu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                popupMenu.show(e.getComponent(), 0, e.getComponent().getHeight());
            }
        });

        pnRig.add(lbCa);
        pnRig.add(btmenu);

        pnTop.add(lbTenStore, BorderLayout.WEST);
        pnTop.add(pnRig, BorderLayout.EAST);

        add(pnTop, BorderLayout.NORTH);

    //2. Center
        tabPane = new JTabbedPane();   //khung tab
        tabPane.setFont(new Font("Arial", Font.PLAIN, 14));
        addNewTab();                   //goi ham them tab
        tabPane.addTab(" + ", new JPanel());

        tabPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = tabPane.indexAtLocation(e.getX(), e.getY()); //lấy vị trí vừa ấn
                if (index != -1 && tabPane.getTitleAt(index).trim().equals("+")) { //nếu nút vừa ấn là dấu cộng -> new tab
                    addNewTab();
                }}});
        add(tabPane, BorderLayout.CENTER);
    }
    // tao tab hoa don moi
    private int hoaDonCounter = 1; //đếm hóa đơn
    private void addNewTab() {
        String title = "Hóa đơn " + hoaDonCounter++;
        BanHangPanel content = new BanHangPanel();
        // neu getTabCount >= 2 tab thi phai chen vo giua tab hiện tại voi tab "+"
        int index = tabPane.getTabCount() > 0 ? tabPane.getTabCount() - 1 : 0;
        if (tabPane.getTabCount() == 0) index = 0;
        tabPane.insertTab(title, null, content, null, index); //tuong tu nhu addTab nhung chi dinh vi tri (index)
        tabPane.setTabComponentAt(index,createTabComponent(title));
        tabPane.setSelectedIndex(index);  //tao tab moi se chuyen luon sang tab do
    }
    // close tab
    private JPanel createTabComponent(String title) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnl.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        JButton btnClose = new JButton("x");   //thêm nút tắt tab hóa đơn
        btnClose.setPreferredSize(new Dimension(17, 17));
        btnClose.setBorder(BorderFactory.createEtchedBorder());
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(Color.RED);

        btnClose.addActionListener(e -> {
            int i = tabPane.indexOfTabComponent(pnl);  //tim vi tri nut x cua tab nao
            if (i != -1 && JOptionPane.showConfirmDialog(this, "Đóng đơn này? Dữ liệu chưa lưu sẽ mất!", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                tabPane.remove(i);
                if (tabPane.getTabCount() == 1) {  //neu la tab cuoi reset bo dem
                    hoaDonCounter=1;
                    addNewTab();
                }
            }
        });
        pnl.add(lblTitle);
        pnl.add(btnClose);
        return pnl;
    }

    //test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new NhanVien("NV001","hieunt","1").setVisible(true);
        });
    }


    // == Giao diện nhập thuốc ==

    public class BanHangPanel extends JPanel {
        JTextField txtMa, txtTen, txtLoai, txtGia, txtSL, txtNCC, txtNSX, txtHSD;
        JComboBox<String> cbDonVi;
        DefaultTableModel tableModel;
        JTable table;
        String currentNote = "";
        JLabel lblTongTienThanhToan;
        DecimalFormat df = new DecimalFormat("###,###");
        JTextField txtGiamGia, txtVAT;
        public BanHangPanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            //tao border voi title
            JPanel pnInputContainer = new JPanel(new BorderLayout());
            JPanel pnNhapLieu = new JPanel(new GridBagLayout());
            pnNhapLieu.setBorder(BorderFactory.createTitledBorder("Thông tin thuốc"));
            //Giao dien nhap thuoc
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.0; int i = 0;

            gbc.gridx=0;
            gbc.gridy=i;
            gbc.insets=new Insets(5,20,5,5);
            pnNhapLieu.add(createLabel("Mã SP:"), gbc);

            gbc.gridx=1;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,0);
            txtMa=new JTextField(15);
            txtMa.setPreferredSize(new Dimension(200,30));
            pnNhapLieu.add(txtMa, gbc);

            gbc.gridx=2;
            gbc.gridy=i;
            gbc.gridwidth=2;
            gbc.insets=new Insets(5,50,5,20);
            JPanel pnRightTop=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            pnRightTop.setPreferredSize(new Dimension(280,30));
            txtNSX=new JTextField();
            txtNSX.setPreferredSize(new Dimension(85,30));
            txtHSD=new JTextField();
            txtHSD.setPreferredSize(new Dimension(85,30));
            pnRightTop.add(createLabel("NSX:           "));
            pnRightTop.add(txtNSX); pnRightTop.add(createLabel("     Hạn: "));
            pnRightTop.add(txtHSD);
            pnNhapLieu.add(pnRightTop, gbc);

            gbc.gridwidth=1;

            i++;
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.insets=new Insets(5,20,5,5);
            pnNhapLieu.add(createLabel("Tên:"), gbc);
            gbc.gridx=1;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,0);
            txtTen=new JTextField(15);
            txtTen.setPreferredSize(new Dimension(200,30));
            pnNhapLieu.add(txtTen, gbc);
            gbc.gridx=2;
            gbc.gridy=i;
            gbc.insets=new Insets(5,50,5,5);
            pnNhapLieu.add(createLabel("Đơn vị:"), gbc);
            gbc.gridx=3;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,20);
            cbDonVi=new JComboBox<>(new String[]{"Hộp","Vỉ","Viên"});
            cbDonVi.setPreferredSize(new Dimension(200,30));
            cbDonVi.setBackground(Color.WHITE); pnNhapLieu.add(cbDonVi, gbc);

            i++;
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.insets=new Insets(5,20,5,5);
            pnNhapLieu.add(createLabel("Loại:"), gbc);
            gbc.gridx=1;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,0);
            txtLoai=new JTextField(15);
            txtLoai.setPreferredSize(new Dimension(200,30));
            pnNhapLieu.add(txtLoai, gbc);
            gbc.gridx=2;
            gbc.gridy=i;
            gbc.insets=new Insets(5,50,5,5);
            pnNhapLieu.add(createLabel("Giá bán:"), gbc);
            gbc.gridx=3;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,20);
            txtGia=new JTextField(15);
            txtGia.setPreferredSize(new Dimension(200,30));
            pnNhapLieu.add(txtGia, gbc);

            i++;
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.insets=new Insets(5,20,5,5);
            pnNhapLieu.add(createLabel("NCC:"), gbc);
            gbc.gridx=1;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,0);
            txtNCC=new JTextField(15);
            txtNCC.setPreferredSize(new Dimension(200,30));
            pnNhapLieu.add(txtNCC, gbc);
            gbc.gridx=2;
            gbc.gridy=i;
            gbc.insets=new Insets(5,50,5,5);
            pnNhapLieu.add(createLabel("Số lượng:"), gbc);
            gbc.gridx=3;
            gbc.gridy=i;
            gbc.insets=new Insets(5,0,5,20);
            txtSL=new JTextField(15);
            txtSL.setPreferredSize(new Dimension(200,30));
            txtSL.setFont(new Font("Arial", Font.BOLD, 14));
            pnNhapLieu.add(txtSL, gbc);

            i++;
            JPanel pnButton=new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
            JButton btnThem=new JButton("Thêm thuốc");
            btnThem.setBackground(new Color(0,153,76));
            btnThem.setContentAreaFilled(false);
            btnThem.setOpaque(true);
            btnThem.setForeground(Color.WHITE);
            JButton btnLamMoi=new JButton("Làm mới");
            btnLamMoi.setBackground(new Color(255,165,0));
            btnLamMoi.setContentAreaFilled(false);
            btnLamMoi.setOpaque(true);
            btnLamMoi.setForeground(Color.WHITE);
            JButton btnNote=new JButton("Ghi chú");
            btnNote.setBackground(new Color(255,204,0));
            pnButton.add(btnThem);
            pnButton.add(btnLamMoi);
            pnButton.add(btnNote);
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.gridwidth=4;
            gbc.insets=new Insets(10,0,10,0);
            pnNhapLieu.add(pnButton, gbc);
            pnInputContainer.add(pnNhapLieu, BorderLayout.CENTER);
            add(pnInputContainer, BorderLayout.NORTH);

            //Bảng dữ liệu thuốc được thêm -> bán
            String[] cols = {"Mã", "Tên thuốc", "Đơn vị", "SL", "Đơn giá", "Thành tiền", "Ghi chú", "Xóa"};
            tableModel = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return c==7;
                }    // chỉ đc phép tương tác với ô xóa
            };

            table = new JTable(tableModel);
            table.setRowHeight(30);
            table.getColumn("Xóa").setCellRenderer(new ButtonRenderer());
            table.getColumn("Xóa").setCellEditor(new ButtonEditor(table));
            table.getColumn("Xóa").setMaxWidth(50);
            add(new JScrollPane(table), BorderLayout.CENTER);

            //Thanh toan
            JPanel pnPay = new JPanel(new GridBagLayout());
            pnPay.setPreferredSize(new Dimension(300,0));
            pnPay.setBorder(BorderFactory.createMatteBorder(0,1,0,0,Color.LIGHT_GRAY));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(10,10,10,10);
            JPanel pKey = new JPanel(new GridLayout(1,2,10,0));
            g.gridy=0;
            pnPay.add(pKey, g);
            JLabel lbT = new JLabel("THANH TOÁN", JLabel.CENTER); lbT.setFont(new Font("Arial", Font.BOLD, 22));g.gridy=1; pnPay.add(lbT, g);
            JPanel info = new JPanel(new GridLayout(3,2,5,10));
            info.add(new JLabel("Tổng SL:")); info.add(new JLabel("0", JLabel.RIGHT));
            info.add(new JLabel("Giảm giá (VNĐ):"));
            txtGiamGia = new JTextField("0");
            info.add(txtGiamGia);

            info.add(new JLabel("VAT (%):"));
            txtVAT = new JTextField("0");
            info.add(txtVAT);

            //event tính tổng tiền ( vat, giảm giá)
            KeyAdapter eventTinhTien = new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    tinhTongTien();
                }
            };
            txtGiamGia.addKeyListener(eventTinhTien);
            txtVAT.addKeyListener(eventTinhTien);
            g.gridy=2; pnPay.add(info, g);
            g.gridy=3; pnPay.add(new JSeparator(), g);
            g.gridy=4; pnPay.add(new JLabel("THÀNH TIỀN:", JLabel.CENTER), g);
            lblTongTienThanhToan = new JLabel("0 VNĐ", JLabel.CENTER); lblTongTienThanhToan.setFont(new Font("Arial", Font.BOLD, 24)); lblTongTienThanhToan.setForeground(Color.RED);
            g.gridy=5; pnPay.add(lblTongTienThanhToan, g);
            JButton btnPay = new JButton("THANH TOÁN"); btnPay.setBackground(Color.RED);btnPay.setContentAreaFilled(false);btnPay.setOpaque(true); btnPay.setForeground(Color.WHITE); btnPay.setPreferredSize(new Dimension(0,50));
            g.gridy=6; g.weighty=1.0; g.anchor=GridBagConstraints.SOUTH; pnPay.add(btnPay, g);
            add(pnPay, BorderLayout.EAST);

            // them event note va xoa hang va them thuoc
            btnNote.addActionListener(e -> {
                String n = JOptionPane.showInputDialog("Ghi chú:");
                if(n!=null) currentNote=n;
            });
            btnLamMoi.addActionListener(e -> clearForm());

            btnThem.addActionListener(e -> themThuocVaoBang());

            txtSL.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode()==KeyEvent.VK_ENTER)
                        themThuocVaoBang();
                }});

            //Lấy dữ liệu SQL
            ThuocDAO dao = new ThuocDAO();
            List<Thuoc> khoThuoc = dao.getAllThuoc();
            if(khoThuoc == null) khoThuoc = new ArrayList<>();

            AutoSuggest.attach(txtTen, khoThuoc, t -> fillForm(t), this::clearForm);
            AutoSuggest.attach(txtMa, khoThuoc, t -> fillForm(t), this::clearForm);

            //Thanh toán
            btnPay.addActionListener(e -> {
                if(tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "Giỏ hàng trống!");
                    return;
                }
                // Tính tổng tiền gốc
                long tongTienHangGoc = 0;
                for(int x=0; x<tableModel.getRowCount(); x++) {
                    tongTienHangGoc += Long.parseLong(tableModel.getValueAt(x,5).toString());
                }

                long giamGia = 0;
                try { giamGia = Long.parseLong(txtGiamGia.getText().trim()); } catch (Exception ex) {}

                double vat = 0;
                try { vat = Double.parseDouble(txtVAT.getText().trim()); } catch (Exception ex) {}

                // Tính tổng thực tế khách phải trả
                long sauGiam = tongTienHangGoc - giamGia;
                if (sauGiam < 0) sauGiam = 0;
                long tienThue = (long) (sauGiam * (vat / 100));
                long tongThucTe = sauGiam + tienThue;

                // Tính Tỷ lệ thực thu
                double tyLeThucThu = (double) tongThucTe / tongTienHangGoc;

                // Hiển thị xác nhận
                String msg = "Tổng gốc: " + df.format(tongTienHangGoc) +
                        "\n- Giảm giá: " + df.format(giamGia) +
                        "\n+ VAT: " + vat + "%" +
                        "\n--------------------" +
                        "\nKHÁCH CẦN TRẢ: " + df.format(tongThucTe) + " VNĐ";

                if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    LichSuDAO lsDao = new LichSuDAO();
                    ThuocDAO thuocDao = new ThuocDAO();

                    try {
                        for(int k=0; k<tableModel.getRowCount(); k++) {
                            String ma = tableModel.getValueAt(k,0).toString();
                            String ten = tableModel.getValueAt(k,1).toString();
                            int sl = Integer.parseInt(tableModel.getValueAt(k,3).toString());
                            long thanhTienGoc = Long.parseLong(tableModel.getValueAt(k,5).toString());

                            long tienLuuSQL = (long) (thanhTienGoc * tyLeThucThu); //tiền thực

                            lsDao.luuLichSuBan(ten, ma, sl, tienLuuSQL, maNV_Current, tenNV_Current, tenCa_Current);

                            // Trừ kho
                            thuocDao.truTonKho(ma, sl);
                        }

                        JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                        tableModel.setRowCount(0);
                        txtGiamGia.setText("0");
                        txtVAT.setText("0");
                        tinhTongTien();
                        clearForm();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                    }
                }
            });
        }

        private void fillForm(Thuoc t) {
            txtMa.setText(t.ma);
            txtTen.setText(t.ten);
            txtNSX.setText(t.nsx);
            txtHSD.setText(t.hsd);
            txtLoai.setText(t.loai);
            txtGia.setText((long)t.gia+"");
            txtNCC.setText(t.ncc);
            cbDonVi.setSelectedItem(t.donVi);
            txtSL.setText("");
            txtSL.requestFocus();
        }
        private void clearForm() {
            txtMa.setText("");
            txtTen.setText("");
            txtNSX.setText("");
            txtHSD.setText("");
            txtLoai.setText(""); txtGia.setText(""); txtNCC.setText(""); txtSL.setText("");
            cbDonVi.setSelectedIndex(0);
            currentNote="";
            txtMa.requestFocus();
        }

        private void themThuocVaoBang() {
            try {
                if (txtMa.getText().isEmpty() || txtSL.getText().isEmpty()) return;
                String ma = txtMa.getText();
                int slMua = Integer.parseInt(txtSL.getText());
                long gia = Long.parseLong(txtGia.getText());

                //kiểm tra số lượng tồn
                int tonKho = 0;
                String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
                String user = "sa";
                String pass = "hieudz";

                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    //lấy số lượng thực tế trong kho
                    PreparedStatement ps = conn.prepareStatement("SELECT SOLUONG FROM THUOC WHERE MASP = ?");
                    ps.setString(1, ma);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        tonKho = rs.getInt("SOLUONG");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL khi kiểm tra kho!");
                    return;
                }

                if (slMua > tonKho) {
                    JOptionPane.showMessageDialog(this,
                            "Không đủ hàng! Kho chỉ còn: " + tonKho,
                            "Cảnh báo hết hàng",
                            JOptionPane.WARNING_MESSAGE);

                    txtSL.requestFocus(); // Đưa con trỏ về ô số lượng
                    txtSL.selectAll();    // Bôi đen để nhập lại cho nhanh
                    return;
                }
                tableModel.addRow(new Object[]{
                        txtMa.getText(),
                        txtTen.getText(),
                        cbDonVi.getSelectedItem(),
                        slMua,
                        gia,
                        slMua * gia,
                        currentNote,
                        "X"
                });
                tinhTongTien();
                clearForm();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng và giá đúng định dạng số!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: " + ex.getMessage());
            }
        }
        private void tinhTongTien() {
            long tongHang = 0;
            // Tính tổng tiền hàng từ bảng
            for(int i=0; i<tableModel.getRowCount(); i++)
                tongHang += Long.parseLong(tableModel.getValueAt(i,5).toString());
            try {
                //Lấy giá trị Giảm giá
                long giamGia = 0;
                if (!txtGiamGia.getText().trim().isEmpty()) {
                    giamGia = Long.parseLong(txtGiamGia.getText().trim());
                }
                //Lấy giá trị VAT
                double vat = 0;
                if (!txtVAT.getText().trim().isEmpty()) {
                    vat = Double.parseDouble(txtVAT.getText().trim());
                }
                //(Tổng hàng - Giảm giá) + VAT
                long sauGiam = tongHang - giamGia;
                if (sauGiam < 0) sauGiam = 0; // Không để âm tiền
                long tienThue = (long) (sauGiam * (vat / 100));
                long tongCong = sauGiam + tienThue;
                // 5. Hiển thị
                lblTongTienThanhToan.setText(df.format(tongCong) + " VNĐ");
            } catch (Exception e) {}
        }
        private JLabel createLabel(String t) { return new JLabel(t); }

        //render nut X
        class ButtonRenderer extends JButton implements TableCellRenderer {
            public ButtonRenderer() { setOpaque(true); setText("X");
                setBackground(Color.RED);
                setForeground(Color.WHITE); }
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                return this;
            }
        }
        class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
            JButton b;
            JTable t;
            public ButtonEditor(JTable t) {
                this.t=t;
                b=new JButton("X");
                b.setBackground(Color.RED);
                b.setForeground(Color.WHITE);
                b.addActionListener(this);
            }
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return b; }
            public Object getCellEditorValue() { return "X"; }
            public void actionPerformed(ActionEvent e) { fireEditingStopped(); ((DefaultTableModel)t.getModel()).removeRow(t.getSelectedRow()); tinhTongTien(); }
        }
    }
}
//lop goi y thuoc
class AutoSuggest {
    public interface OnSelection { void onSelect(Thuoc i); }
    public static void attach(JTextField tf, List<Thuoc> list, OnSelection onSelect, Runnable onClear) {
        JPopupMenu menu = new JPopupMenu();
        DefaultListModel<Thuoc> model = new DefaultListModel<>();
        JList<Thuoc> listUI = new JList<>(model);
        listUI.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(listUI);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(Math.max(tf.getWidth(), 300), 150));
        menu.add(scroll);

        tf.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER && listUI.getSelectedValue()!=null) {
                    menu.setVisible(false);
                    onSelect.onSelect(listUI.getSelectedValue());
                }}
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_DOWN || e.getKeyCode()==KeyEvent.VK_UP || e.getKeyCode()==KeyEvent.VK_ENTER) return;
                String text = tf.getText().trim().toLowerCase();
                if(text.isEmpty()) {
                    menu.setVisible(false);
                    onClear.run(); return;
                }
                model.clear();
                for (Thuoc item : list) if (item.ten.toLowerCase().contains(text) || item.ma.toLowerCase().contains(text)) model.addElement(item);
                if (!model.isEmpty()) {
                    scroll.setPreferredSize(new Dimension(Math.max(tf.getWidth(), 300), 150));
                    menu.pack();
                    menu.show(tf, 0, tf.getHeight());
                    listUI.setSelectedIndex(0);
                    tf.requestFocus();
                } else menu.setVisible(false);
            }
        });
        listUI.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (listUI.getSelectedValue() != null) {
                    menu.setVisible(false);
                    onSelect.onSelect(listUI.getSelectedValue());
                }
            }});
    }
}
