package MedicHome.UI;

import MedicHome.DataAccessObject.Thuoc;
import MedicHome.DataAccessObject.ThuocDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.List;

public class QuanLy extends JFrame {
    static final String DB_URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=QuanlyMedicHome;integratedSecurity=true;trustServerCertificate=true;";
    static final String DB_USER = "sa";
    static final String DB_PASS = "hieudz";
    static final DecimalFormat df = new DecimalFormat("###,###");   //làm đẹp số 100000-> 100.000

    JTabbedPane TabQuanLy;

    public QuanLy() {
        setTitle("Quản Lý - Medic Home");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

    //Top
        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBackground(new Color(30, 144, 255));
        pnTop.setPreferredSize(new Dimension(0, 60));
        pnTop.setBorder(new EmptyBorder(7, 20, 10, 20));

        JLabel lbTenStore = new JLabel("Medic Home of Doraxi");
        lbTenStore.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 40));
        lbTenStore.setForeground(Color.WHITE);
        pnTop.add(lbTenStore, BorderLayout.WEST);

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
        btmenu.setBorderPainted(false);
        btmenu.setContentAreaFilled(false);
        btmenu.setFocusPainted(false);
        btmenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem("Quản lý Kho")).addActionListener(e -> openTab("Kho Hàng", new PanelHangHoa()));
        popupMenu.add(new JMenuItem("Quản lý Nhân sự")).addActionListener(e -> openTab("Nhân Sự", new PanelNhanSu()));
        popupMenu.add(new JMenuItem("Xếp Ca Làm Việc")).addActionListener(e -> openTab("Xếp Ca", new PanelXepCa()));
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem("Tài Chính")).addActionListener(e -> openTab("Tài Chính", new PanelTaiChinh()));
        popupMenu.add(new JMenuItem("Duyệt Báo Cáo")).addActionListener(e -> openTab("Duyệt Báo Cáo", new PanelBaoCaoNV()));
        popupMenu.addSeparator();
        popupMenu.add(new JMenuItem("Bán Hàng")).addActionListener(e->new NhanVien("admin","admin","").setVisible(true));
        popupMenu.add(new JMenuItem("Kho")).addActionListener(e->new KHO("admin","admin","").setVisible(true));
        JMenuItem itemLogout = new JMenuItem("Đăng xuất");
        itemLogout.setForeground(Color.RED);
        itemLogout.addActionListener(e -> { this.dispose(); new Login().setVisible(true); });
        popupMenu.add(itemLogout);

        btmenu.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { popupMenu.show(e.getComponent(), 0, e.getComponent().getHeight()); }
        });
        pnTop.add(btmenu, BorderLayout.EAST);
        add(pnTop, BorderLayout.NORTH);

    //Center
        TabQuanLy = new JTabbedPane(JTabbedPane.TOP);
        TabQuanLy.setFont(new Font("Arial", Font.BOLD, 14));
        openTab("Kho Hàng", new PanelHangHoa());
        add(TabQuanLy, BorderLayout.CENTER);
    }

    private void openTab(String title, JPanel panelContent) {
        int index = TabQuanLy.indexOfTab(title);
        if (index >= 0) { TabQuanLy.setSelectedIndex(index); return; }
        TabQuanLy.addTab(title, panelContent);
        int newIndex = TabQuanLy.indexOfTab(title);

        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTab.setOpaque(false); pnlTab.add(new JLabel(title));
        JButton btnClose = new JButton("x");
        btnClose.setForeground(Color.RED); btnClose.setBorder(null); btnClose.setContentAreaFilled(false);
        btnClose.addActionListener(e -> { int i = TabQuanLy.indexOfTabComponent(pnlTab); if (i != -1) TabQuanLy.remove(i); });
        pnlTab.add(btnClose);
        TabQuanLy.setTabComponentAt(newIndex, pnlTab);
        TabQuanLy.setSelectedIndex(newIndex);
    }

    //TAB 1: KHO
    class PanelHangHoa extends JPanel {
        DefaultTableModel model; JTable table;
        public PanelHangHoa() {
            setLayout(new BorderLayout());
            JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRefresh = new JButton("Làm mới");
            btnRefresh.addActionListener(e -> loadData());
            JButton btnAdd = new JButton("+ Thêm thuốc mới");
            btnAdd.setBackground(new Color(0, 153, 76));
            btnAdd.setForeground(Color.WHITE);
            btnAdd.addActionListener(e -> ThemThuoc());
            JButton btnDel = new JButton("Xóa thuốc");
            btnDel.setBackground(Color.RED);
            btnDel.setForeground(Color.WHITE);
            btnDel.addActionListener(e -> deleteSelected());

            toolBar.add(btnRefresh);
            toolBar.add(btnAdd);
            toolBar.add(btnDel);
            add(toolBar, BorderLayout.NORTH);

            String[] cols = {"Mã SP", "Tên Thuốc", "Loại", "Đơn vị", "NCC", "NSX", "HSD", "Tồn", "Giá Bán"};
            model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c != 0; } }; // ko cho sửa cột đầu -> khóa chính
            table = new JTable(model);
            table.setRowHeight(30);
            model.addTableModelListener(e -> {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int r = e.getFirstRow();
                    int c = e.getColumn();
                    if(r>=0 && c>=0) updateSQL(model.getValueAt(r, 0).toString(), c, model.getValueAt(r, c));
                }
            });
            add(new JScrollPane(table), BorderLayout.CENTER);
            loadData();
        }

        private void ThemThuoc() {
            JDialog d = new JDialog(QuanLy.this, "Thêm Thuốc Mới", true);
            d.setSize(500, 450);
            d.setLocationRelativeTo(this);
            d.setLayout(new GridLayout(10, 2, 10, 10));
            JTextField[] txts = new JTextField[8];  //mảng chứa thông tin

            String[] labels = {"Mã SP", "Tên Thuốc", "Loại", "NCC", "NSX", "HSD", "Số Lượng", "Giá Bán"};
            JComboBox<String> cbbDonVi = new JComboBox<>(new String[]{"Viên", "Vỉ", "Hộp"});
            for(int i=0; i<8; i++) {
                d.add(new JLabel("  " + labels[i]));
                txts[i] = new JTextField();
                d.add(txts[i]);
            }
            d.add(new JLabel("  Đơn vị:"));
            d.add(cbbDonVi);
            JButton btnSave = new JButton("SAVE");
            btnSave.addActionListener(e -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    String maSP = txts[0].getText().trim();
                    String tenSP = txts[1].getText().trim();
                    String loai = txts[2].getText().trim();
                    String ncc = txts[3].getText().trim();
                    String nsxStr = txts[4].getText().trim();
                    String hsdStr = txts[5].getText().trim();

                    int soLuongNhap = Integer.parseInt(txts[6].getText().trim());
                    long giaBan = Long.parseLong(txts[7].getText().trim());
                    String donVi = cbbDonVi.getSelectedItem().toString();
                    //kiểm tra thuốc tồn tại chưa
                    PreparedStatement psCheck = conn.prepareStatement("SELECT * FROM THUOC WHERE MASP = ?");
                    psCheck.setString(1, maSP);
                    ResultSet rs = psCheck.executeQuery();

                    if (rs.next()) {
                        String dbDonVi = rs.getString("DONVI");
                        String dbNCC = rs.getString("NCC");
                        long dbGia = rs.getLong("TIEN");
                        Date dbNSX = rs.getDate("NSX");
                        Date dbHSD = rs.getDate("HSD");

                        boolean giongNhau = dbDonVi.equals(donVi)
                                && dbNCC.equals(ncc)
                                && dbGia == giaBan
                                && dbNSX.toString().equals(nsxStr)
                                && dbHSD.toString().equals(hsdStr);

                        if (giongNhau) {

                            int confirm = JOptionPane.showConfirmDialog(d,
                                    "Thuốc này đã có trong kho.\nBạn có muốn cộng thêm " + soLuongNhap + " vào số lượng tồn kho?",
                                    "Cộng dồn kho",
                                    JOptionPane.YES_NO_OPTION);

                            if (confirm == JOptionPane.YES_OPTION) {
                                PreparedStatement psUpdate = conn.prepareStatement("UPDATE THUOC SET SOLUONG = SOLUONG + ? WHERE MASP = ?");
                                psUpdate.setInt(1, soLuongNhap);
                                psUpdate.setString(2, maSP);
                                psUpdate.executeUpdate();
                                JOptionPane.showMessageDialog(d, "Đã cập nhật số lượng thành công!");
                                d.dispose();
                                loadData();
                            }
                        } else {
                            //cùng loại thuốc mà khác các thông tin trên thì phải đặt mã khasc
                            JOptionPane.showMessageDialog(d,
                                    "LỖI: Mã thuốc '" + maSP + "' đã tồn tại nhưng thông tin khác nhau!\n" +
                                            "- Trong kho: HSD " + dbHSD + ", Giá " + dbGia + ", Đơn vị " + dbDonVi + "\n" +
                                            "- Bạn nhập: HSD " + hsdStr + ", Giá " + giaBan + ", Đơn vị " + donVi + "\n\n" +
                                            "Vui lòng nhập MÃ SP KHÁC để phân biệt lô hàng.",
                                    "Xung đột data",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    } else {
                        PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO THUOC (MASP, TENSP, LOAI, NCC, NSX, HSD, SOLUONG, TIEN, DONVI) VALUES (?,?,?,?,?,?,?,?,?)");
                        ps.setString(1, maSP);
                        ps.setString(2, tenSP);
                        ps.setString(3, loai);
                        ps.setString(4, ncc);
                        ps.setString(5, nsxStr); // Lưu ý: Database phải nhận chuỗi yyyy-mm-dd
                        ps.setString(6, hsdStr);
                        ps.setInt(7, soLuongNhap);
                        ps.setLong(8, giaBan);
                        ps.setString(9, donVi);

                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(d, "Thêm thuốc mới thành công!");
                        d.dispose();
                        loadData();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(d, "Lỗi nhập liệu: " + ex.getMessage());
                }
            });

            d.add(new JLabel(""));
            d.add(btnSave);
            d.setVisible(true);
        }

        void deleteSelected() {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String ma = model.getValueAt(row, 0).toString();
            if (JOptionPane.showConfirmDialog(this, "Xóa thuốc " + ma + "?", "Xác nhận", 0) == 0) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM THUOC WHERE MASP=?");
                    ps.setString(1, ma);
                    ps.executeUpdate();
                    loadData();
                } catch (Exception e) { JOptionPane.showMessageDialog(this, "Không thể xóa do có lịch sử bán/nhập!"); }  //có khóa ngoại ở LSBAN
            }
        }

        void loadData() {
            model.setRowCount(0);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM THUOC");
                while(rs.next()) model.addRow(new Object[]{rs.getString("MASP"), rs.getString("TENSP"), rs.getString("LOAI"), rs.getString("DONVI"), rs.getString("NCC"), rs.getDate("NSX"), rs.getDate("HSD"), rs.getInt("SOLUONG"), rs.getLong("TIEN")});
            } catch(Exception e) { e.printStackTrace(); }
        }

        void updateSQL(String ma, int col, Object val) {
            String[] cols = {null, "TENSP", "LOAI", "DONVI", "NCC", "NSX", "HSD", "SOLUONG", "TIEN"};
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE THUOC SET " + cols[col] + "=? WHERE MASP=?");
                if (col == 7) ps.setInt(1, Integer.parseInt(val.toString()));
                else if (col == 8) ps.setLong(1, Long.parseLong(val.toString()));
                else ps.setObject(1, val);
                ps.setString(2, ma); ps.executeUpdate();
            } catch(Exception e) { loadData(); }
        }
    }

    //TAB 2: NHÂN SỰ
    class PanelNhanSu extends JPanel {
        DefaultTableModel model; JTable table;
        public PanelNhanSu() {
            setLayout(new BorderLayout());
            JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnAdd = new JButton("Thêm NV");
            btnAdd.addActionListener(e -> AddNV());
            JButton btnDel = new JButton("Xóa NV");
            btnDel.addActionListener(e -> deleteNV());
            tool.add(btnAdd);
            tool.add(btnDel);
            add(tool, BorderLayout.NORTH);
            model = new DefaultTableModel(new String[]{"Mã NV", "Tên NV", "Vị Trí", "Mật Khẩu"}, 0);
            table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER);
            loadData();
        }

        void AddNV() {
            JDialog d = new JDialog(QuanLy.this, "Thêm Nhân Viên", true);
            d.setSize(400, 300);
            d.setLocationRelativeTo(this);
            d.setLayout(new GridLayout(5, 2, 10, 10));
            JTextField tMa = new JTextField();
            JTextField tTen = new JTextField();
            JComboBox<String> cViTri = new JComboBox<>(new String[]{"Nhân viên bán", "Nhân viên kho", "Quản lý"});
            JTextField tPass = new JTextField();
            d.add(new JLabel(" Mã NV:"));
            d.add(tMa);
            d.add(new JLabel(" Tên NV:"));
            d.add(tTen);
            d.add(new JLabel(" Vị trí:"));
            d.add(cViTri);
            d.add(new JLabel(" Mật khẩu:"));
            d.add(tPass);
            JButton bSave = new JButton("SAVE"); bSave.addActionListener(e -> {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO TKNV (MaNhanVien, TenNhanVien, ViTri, MatKhau) VALUES (?,?,?,?)");
                    ps.setString(1, tMa.getText());
                    ps.setString(2, tTen.getText());
                    ps.setString(3, cViTri.getSelectedItem().toString());
                    ps.setString(4, tPass.getText());
                    ps.executeUpdate();
                    conn.createStatement().executeUpdate("INSERT INTO LICHLAM (MaNhanVien, TenNhanVien) VALUES ('"+tMa.getText()+"', N'"+tTen.getText()+"')");
                    d.dispose();
                    loadData();
                } catch(Exception ex) { ex.printStackTrace(); }
            });
            d.add(new JLabel(""));
            d.add(bSave);
            d.setVisible(true);
        }

        void deleteNV() {
            int r = table.getSelectedRow();
            if (r == -1) return;
            String ma = model.getValueAt(r, 0).toString();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                conn.createStatement().executeUpdate("DELETE FROM LICHLAM WHERE MaNhanVien='" + ma + "'");
                conn.createStatement().executeUpdate("DELETE FROM TKNV WHERE MaNhanVien='" + ma + "'");
                loadData();
            } catch (Exception e) { e.printStackTrace(); }
        }

        void loadData() {
            model.setRowCount(0);
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet rs = cn.createStatement().executeQuery("SELECT MaNhanVien, TenNhanVien, ViTri, MatKhau FROM TKNV");
                while(rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            } catch (Exception e) {}
        }
    }

    //TAB 3: XẾP CA
    class PanelXepCa extends JPanel {
        DefaultTableModel model; JTable table;
        public PanelXepCa() {
            setLayout(new BorderLayout());
            JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRef = new JButton("Làm mới");
            btnRef.addActionListener(e -> loadData());
            JButton btnSave = new JButton("Lưu lịch");
            btnSave.addActionListener(e -> saveLich());
            tool.add(btnRef); tool.add(btnSave);
            add(tool, BorderLayout.NORTH);
            model = new DefaultTableModel(new String[]{"Mã NV", "Tên", "T2", "T3", "T4", "T5", "T6", "T7", "CN"}, 0) {
                public boolean isCellEditable(int r, int c) { return c >= 2; }
            };
            table = new JTable(model); add(new JScrollPane(table), BorderLayout.CENTER); loadData();
        }
        void loadData() {
            model.setRowCount(0);
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet r = c.createStatement().executeQuery("SELECT MaNhanVien, TenNhanVien, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday FROM LICHLAM");
                while(r.next()) model.addRow(new Object[]{r.getString(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5), r.getString(6), r.getString(7), r.getString(8), r.getString(9)});
            } catch (Exception e) {}
        }
        void saveLich() {
            try (Connection c = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = c.prepareStatement("UPDATE LICHLAM SET Monday=?, Tuesday=?, Wednesday=?, Thursday=?, Friday=?, Saturday=?, Sunday=? WHERE MaNhanVien=?");
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int col = 2; col <= 8; col++) ps.setString(col - 1, (String)model.getValueAt(i, col));
                    ps.setString(8, (String)model.getValueAt(i, 0));
                    ps.addBatch();
                }
                ps.executeBatch(); JOptionPane.showMessageDialog(this, "Đã lưu!");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    //TAB 4: BÁO CÁO
    class PanelBaoCaoNV extends JPanel {
        CardLayout card = new CardLayout();
        JPanel main = new JPanel(card);
        DefaultTableModel modelLSBan, modelBC, modelKho;
        JTable tblLSBan, tblBC, tblKho;

        public PanelBaoCaoNV() {
            setLayout(new BorderLayout());
            JPanel tool = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            JButton btnLS = new JButton("BÁN HÀNG");
            JButton btnK = new JButton("NHẬP KHO");
            JButton btnFind = new JButton("TRA CỨU NV");
            btnFind.setBackground(new Color(128, 0, 128));
            btnFind.setForeground(Color.WHITE);
            tool.add(btnLS);
            tool.add(btnK);
            tool.add(btnFind);
            add(tool, BorderLayout.NORTH);
            // LS-> lichSu ; K->Kho
            main.add(createLSPanel(), "LS");
            main.add(createKPanel(), "K");
            add(main, BorderLayout.CENTER);
            btnLS.addActionListener(e -> { card.show(main, "LS"); loadDataBanHang(); });
            btnK.addActionListener(e -> { card.show(main, "K"); loadDataKho(); });
            btnFind.addActionListener(e -> showFindDialog());
        }

        private JPanel createLSPanel() {
            //bảng LSban
            JPanel p = new JPanel(new BorderLayout());
            JPanel pTop = new JPanel(new BorderLayout());
            modelLSBan = new DefaultTableModel(new String[]{"STT", "Ngày Giờ", "Mã SP", "Tên Thuốc", "SL", "Tiền", "NV", "Ca"}, 0);
            tblLSBan = new JTable(modelLSBan);
            pTop.add(new JScrollPane(tblLSBan), BorderLayout.CENTER);
            pTop.add(createMiniToolBar("LSBAN", modelLSBan, tblLSBan), BorderLayout.SOUTH);
            //bảng baocao thu
            JPanel pBot = new JPanel(new BorderLayout());
            modelBC = new DefaultTableModel(new String[]{"STT", "Ngày", "Mã NV", "Tên NV", "Ca", "Thu Máy", "Thực Tế", "Chênh", "Ghi Chú"}, 0);
            tblBC = new JTable(modelBC);
            pBot.add(new JScrollPane(tblBC), BorderLayout.CENTER);
            pBot.add(createMiniToolBar("BAOCAO", modelBC, tblBC), BorderLayout.SOUTH);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pTop, pBot);
            split.setDividerLocation(350);
            p.add(split, BorderLayout.CENTER);
            return p;
        }

        private JPanel createKPanel() {
            JPanel p = new JPanel(new BorderLayout());
            modelKho = new DefaultTableModel(new String[]{"STT", "Ngày", "Mã SP", "Tên", "SL", "Giá Nhập", "Tổng", "NV", "Duyệt"}, 0) {
                public boolean isCellEditable(int r, int c) { return c == 8; }
            };
            tblKho = new JTable(modelKho);
            tblKho.getColumn("Duyệt").setCellRenderer(new ButtonRenderer());
            tblKho.getColumn("Duyệt").setCellEditor(new ButtonEditor(tblKho));
            p.add(new JScrollPane(tblKho), BorderLayout.CENTER);
            p.add(createMiniToolBar("NHAP_KHO", modelKho, tblKho), BorderLayout.SOUTH);
            return p;
        }

        //TRA CỨU NHÂN VIÊN
        private void showFindDialog() {
            JDialog d = new JDialog(QuanLy.this, "Tra cứu lịch sử nhân viên", true);
            d.setSize(900, 500);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField tMa = new JTextField(8);
            JTextField tTen = new JTextField(12);
            JComboBox<String> cCa = new JComboBox<>(new String[]{"Tất cả", "Sáng", "Chiều", "Tối", "Đêm"});

            top.add(new JLabel("Mã NV:")); top.add(tMa);
            top.add(new JLabel("Tên NV:")); top.add(tTen);
            top.add(new JLabel("Ca:")); top.add(cCa);
            JButton bSearch = new JButton("TÌM KIẾM"); top.add(bSearch);
            d.add(top, BorderLayout.NORTH);

            // Bảng kết quả
            DefaultTableModel mRes = new DefaultTableModel(new String[]{"Ngày", "Mã NV", "Tên NV", "Ca", "Tổng Thu", "Ghi Chú"}, 0);
            d.add(new JScrollPane(new JTable(mRes)), BorderLayout.CENTER);

            bSearch.addActionListener(e -> {
                mRes.setRowCount(0);
                String sql = "SELECT * FROM BAOCAO WHERE 1=1";
                //nối điều kiện tùy theo thông tin nhập
                if(!tMa.getText().isEmpty()) sql += " AND MaNV LIKE '%"+tMa.getText()+"%'";
                if(!tTen.getText().isEmpty()) sql += " AND TenNV LIKE N'%"+tTen.getText()+"%'";
                if(!cCa.getSelectedItem().toString().equals("Tất cả")) sql += " AND CaLam = N'" + cCa.getSelectedItem() + "'";

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    ResultSet rs = conn.createStatement().executeQuery(sql);
                    boolean found = false;
                    while(rs.next()) {
                        found = true;
                        mRes.addRow(new Object[]{rs.getTimestamp("NgayBaoCao"), rs.getString("MaNV"), rs.getString("TenNV"), rs.getString("CaLam"), df.format(rs.getLong("TongThuMay")), rs.getString("GhiChu")});
                    }
                    if(!found) JOptionPane.showMessageDialog(d, "Không tìm thấy dữ liệu phù hợp!");
                } catch(Exception ex) { ex.printStackTrace(); }
            });
            d.setVisible(true);
        }

        // THANH CÔNG CỤ XÓA / LÀM MỚI
        private JPanel createMiniToolBar(String tableName, DefaultTableModel model, JTable table) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnRef = new JButton("Làm mới");
            JButton btnDel = new JButton("Xóa");

            btnRef.addActionListener(e -> {
                if(tableName.equals("LSBAN") || tableName.equals("BAOCAO")) loadDataBanHang(); else loadDataKho();
            });

            btnDel.addActionListener(e -> {
                int r = table.getSelectedRow();
                if(r == -1) return;
                String stt = model.getValueAt(r, 0).toString();
                String colSTT = "STT";
                if(JOptionPane.showConfirmDialog(this, "Xóa dòng = " + stt + "?", "Xác nhận", 0) == 0) {
                    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                        conn.createStatement().executeUpdate("DELETE FROM " + tableName + " WHERE " + colSTT + "=" + stt);
                        if(tableName.equals("LSBAN") || tableName.equals("BAOCAO")) loadDataBanHang(); else loadDataKho();
                    } catch(Exception ex) { ex.printStackTrace(); }
                }
            });
            p.add(btnRef);
            p.add(btnDel);
            return p;
        }

        private void loadDataBanHang() {
            modelLSBan.setRowCount(0);
            modelBC.setRowCount(0);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet rs1 = conn.createStatement().executeQuery("SELECT STT, NGAYGIO, MASP, TENSP, SOLUONG, TIEN, MaNV, Ca FROM LSBAN ORDER BY NGAYGIO DESC");
                while(rs1.next()) {
                    modelLSBan.addRow(new Object[]{
                            rs1.getInt(1),
                            rs1.getTimestamp(2),
                            rs1.getString(3),
                            rs1.getString(4),
                            rs1.getInt(5),
                            df.format(rs1.getLong(6)),
                            rs1.getString(7),
                            rs1.getString(8)
                    });
                }
                String sqlBC = "SELECT STT, NgayBaoCao, MaNV, TenNV, CaLam, TongThuMay, TienThucTe, ChenhLech, GhiChu FROM BAOCAO ORDER BY NgayBaoCao DESC";

                ResultSet rs2 = conn.createStatement().executeQuery(sqlBC);
                while(rs2.next()) {
                    modelBC.addRow(new Object[]{
                            rs2.getInt(1), // Lấy cột STT
                            rs2.getTimestamp(2),
                            rs2.getString(3),
                            rs2.getString(4),
                            rs2.getString(5),
                            df.format(rs2.getLong(6)),
                            df.format(rs2.getLong(7)),
                            rs2.getLong(8),
                            rs2.getString(9)
                    });
                }
            } catch(Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
            }
        }
        private void loadDataKho() {
            modelKho.setRowCount(0);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT STT, NgayNhap, MaSP, TenSP, SoLuongNhap, GiaNhap, TongTienNhap, MaNV, TrangThai FROM NHAP_KHO ORDER BY NgayNhap DESC");
                while(rs.next()) {
                    //kiem tra cột duyệt hay chưa
                    String tt = rs.getString(9);
                    modelKho.addRow(new Object[]{rs.getInt(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4), rs.getInt(5), df.format(rs.getLong(6)), df.format(rs.getLong(7)), rs.getString(8), tt.equals("Đã duyệt") ? "Đã xong" : "DUYỆT"});
                }
            } catch(Exception e) {}
        }

        private void xuLyDuyet(int row) {
            int key = (int) modelKho.getValueAt(row, 0); // Lấy STT phiếu

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sqlGetNhap = "SELECT * FROM NHAP_KHO WHERE STT = ?";
                PreparedStatement psGet = conn.prepareStatement(sqlGetNhap);
                psGet.setInt(1, key);
                ResultSet rs = psGet.executeQuery();

                if (rs.next()) {
                    // Lưu toàn bộ thông tin nhập vào biến
                    String maNhap = rs.getString("MaSP").trim();
                    String tenNhap = rs.getString("TenSP").trim();
                    String loaiNhap = rs.getString("Loai").trim();
                    String dvNhap = rs.getString("DonVi").trim();
                    String nccNhap = rs.getString("NCC").trim();
                    Date nsxNhap = rs.getDate("NSX");
                    Date hsdNhap = rs.getDate("HSD");
                    long giaNhap = rs.getLong("GiaNhap");
                    int slNhap = rs.getInt("SoLuongNhap");

                    PreparedStatement psCheck = conn.prepareStatement("SELECT * FROM THUOC WHERE MASP = ?");
                    psCheck.setString(1, maNhap);
                    ResultSet rsKho = psCheck.executeQuery();

                    if (rsKho.next()) {
                        //TH1: thuốc đã có
                        // Lấy thông tin hiện tại trong kho
                        String tenSP = rsKho.getString("TENSP").trim();
                        String loai = rsKho.getString("LOAI").trim();
                        String donvi = rsKho.getString("DONVI").trim();
                        String ncc = rsKho.getString("NCC").trim();
                        Date nsx = rsKho.getDate("NSX");
                        Date hsd= rsKho.getDate("HSD");
                        long gia = rsKho.getLong("TIEN");

                        // So sánh tìm điểm khác biệt
                        StringBuilder canhBao = new StringBuilder();  //chuỗi có thể thay đổi nối nhanh hơn
                        boolean coLech = false;      //cờ kiểm tra

                        if (!tenSP.equalsIgnoreCase(tenNhap)) { canhBao.append("- Tên thuốc: Kho(" + tenSP + ") # Nhập(" + tenNhap + ")\n"); coLech = true; }
                        if (!loai.equalsIgnoreCase(loaiNhap)) { canhBao.append("- Loại: Kho(" + loai + ") # Nhập(" + loaiNhap + ")\n"); coLech = true; }
                        if (!donvi.equalsIgnoreCase(dvNhap)) { canhBao.append("- Đơn vị: Kho(" + donvi + ") # Nhập(" + dvNhap + ")\n"); coLech = true; }
                        if (!ncc.equalsIgnoreCase(nccNhap)) { canhBao.append("- NCC: Kho(" + ncc + ") # Nhập(" + nccNhap + ")\n"); coLech = true; }
                        if (gia != giaNhap) { canhBao.append("- Giá: Kho(" + df.format(gia) + ") # Nhập(" + df.format(giaNhap) + ")\n"); coLech = true; }
                        if(!nsx.toString().equals(nsxNhap.toString())) {canhBao.append("- NSX: Kho(" + nsx + ") # Nhập(" + nsxNhap + ")\n");coLech = true; }
                        if (!hsd.toString().equals(hsdNhap.toString())) { canhBao.append("- HSD: Kho(" + hsd + ") # Nhập(" + hsdNhap + ")\n"); coLech = true; }

                        if (coLech) {
                            String msg = "PHÁT HIỆN DỮ LIỆU KHÔNG KHỚP!\n" + canhBao.toString() +
                                    "\n Từ chối phiếu này.";

                            int choice = JOptionPane.showConfirmDialog(this, msg, "Xung đột dữ liệu", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                            if (choice == JOptionPane.YES_OPTION) {
                                tuChoiPhieu(conn, key);
                            }
                        } else {
                            // trùng hết thông tin trừ số lượng
                            if (JOptionPane.showConfirmDialog(this, "Duyệt nhập kho?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                String sqlUpdateSL = "UPDATE THUOC SET SOLUONG=SOLUONG+? WHERE MASP=?";
                                PreparedStatement ps = conn.prepareStatement(sqlUpdateSL);
                                ps.setInt(1, slNhap);
                                ps.setString(2, maNhap);
                                ps.executeUpdate();
                                pheDuyetThanhCong(conn, key);
                            }
                        }
                    } else {
                        // thuốc mới tinh
                        if (JOptionPane.showConfirmDialog(this, "Duyệt đơn mới?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            String sqlInsert = "INSERT INTO THUOC (MASP, TENSP, LOAI, DONVI, NCC, NSX, HSD, SOLUONG, TIEN) VALUES (?,?,?,?,?,?,?,?,?)";
                            PreparedStatement ps = conn.prepareStatement(sqlInsert);
                            ps.setString(1, maNhap);
                            ps.setString(2, tenNhap);
                            ps.setString(3, loaiNhap);
                            ps.setString(4, dvNhap);
                            ps.setString(5, nccNhap);
                            ps.setDate(6, nsxNhap);
                            ps.setDate(7, hsdNhap);
                            ps.setInt(8, slNhap);
                            ps.setLong(9, giaNhap);
                            ps.executeUpdate();
                            pheDuyetThanhCong(conn, key);
                        }
                    }
                    loadDataKho(); // Tải lại bảng
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage());
            }
        }
        private void pheDuyetThanhCong(Connection conn, int key) throws SQLException {
            conn.createStatement().executeUpdate("UPDATE NHAP_KHO SET TrangThai = N'Đã duyệt' WHERE STT=" + key);
            JOptionPane.showMessageDialog(this, "Đã duyệt phiếu nhập!");
        }
        private void tuChoiPhieu(Connection conn, int key) throws SQLException {
            conn.createStatement().executeUpdate("UPDATE NHAP_KHO SET TrangThai = N'Không duyệt' WHERE STT=" + key);
            JOptionPane.showMessageDialog(this, "Đã từ chối phiếu nhập!");
        }


        class ButtonRenderer extends JButton implements TableCellRenderer {
            public ButtonRenderer() { setOpaque(true); }
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                setText((v == null) ? "" : v.toString());
                setBackground(v.equals("DUYỆT") ? Color.GREEN : Color.LIGHT_GRAY); return this;
            }
        }
        class ButtonEditor extends DefaultCellEditor {
            JButton b; boolean isPushed; int row;
            public ButtonEditor(JTable t) { super(new JCheckBox()); b = new JButton(); b.addActionListener(e -> fireEditingStopped()); }
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { row=r; b.setText(v.toString()); isPushed=true; return b; }
            public Object getCellEditorValue() { if(isPushed && b.getText().equals("DUYỆT")) xuLyDuyet(row); isPushed=false; return b.getText(); }
        }
    }
    //Tab 5: Tài Chính
    class PanelTaiChinh extends JPanel {
        DefaultTableModel modelSummary;
        DefaultTableModel modelThu;
        DefaultTableModel modelChi;
        JTextField txtVon;
        JTextField txtNgayLoc;
        JLabel lblKetLuan;

        public PanelTaiChinh() {
            setLayout(new BorderLayout());
            JPanel pnlTopContainer = new JPanel(new BorderLayout());

            JPanel pnInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
            pnInput.setBorder(BorderFactory.createTitledBorder("Tính toán tài chính"));

            pnInput.add(new JLabel("Ngày tính (yy-mm-dd):"));
            txtNgayLoc = new JTextField(10);
            txtNgayLoc.setText(java.time.LocalDate.now().toString()); // Tự động điền ngày hôm nay
            txtNgayLoc.setFont(new Font("Arial", Font.BOLD, 14));
            pnInput.add(txtNgayLoc);

            pnInput.add(new JLabel(" |   Vốn đầu ca (VNĐ):"));
            txtVon = new JTextField("0", 12);
            txtVon.setFont(new Font("Arial", Font.BOLD, 14));
            pnInput.add(txtVon);

            JButton btnTinh = new JButton("TÍNH TOÁN DOANH THU");
            btnTinh.setBackground(new Color(0, 102, 204));
            btnTinh.setForeground(Color.WHITE);
            pnInput.add(btnTinh);

            // --- BẢNG TÍNH---
            modelSummary = new DefaultTableModel(new String[]{"NGÀY", "VỐN (A)", "DOANH THU (B)", "CHI PHÍ (C)", "TỔNG (A+B-C)"}, 0);
            JTable tblSummary = new JTable(modelSummary);
            tblSummary.setRowHeight(50);
            tblSummary.setFont(new Font("Arial", Font.BOLD, 16));

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for(int i=0; i<5; i++) tblSummary.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

            JScrollPane scSummary = new JScrollPane(tblSummary);
            scSummary.setPreferredSize(new Dimension(0, 80));

            pnlTopContainer.add(pnInput, BorderLayout.NORTH);
            pnlTopContainer.add(scSummary, BorderLayout.CENTER);

            add(pnlTopContainer, BorderLayout.NORTH);

            // --- HIỂN THỊ 2 BẢNG THU CHI---
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setResizeWeight(0.5);

            // Bảng THU (LSBAN) - Lọc theo ngày
            JPanel pnlThu = new JPanel(new BorderLayout());
            pnlThu.setBorder(BorderFactory.createTitledBorder(null, "DOANH THU BÁN HÀNG TRONG NGÀY", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), new Color(0, 153, 76)));
            modelThu = new DefaultTableModel(new String[]{"Giờ", "Tên Thuốc", "SL", "Thành Tiền", "NV"}, 0);
            pnlThu.add(new JScrollPane(new JTable(modelThu)), BorderLayout.CENTER);

            // Bảng CHI (NHAP_KHO) - Lọc theo ngày
            JPanel pnlChi = new JPanel(new BorderLayout());
            pnlChi.setBorder(BorderFactory.createTitledBorder(null, "CHI PHÍ NHẬP HÀNG TRONG NGÀY", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), Color.RED));
            modelChi = new DefaultTableModel(new String[]{"Giờ nhập", "Tên Thuốc", "SL", "Tổng Chi", "Trạng Thái"}, 0);
            pnlChi.add(new JScrollPane(new JTable(modelChi)), BorderLayout.CENTER);

            splitPane.setLeftComponent(pnlThu);
            splitPane.setRightComponent(pnlChi);
            add(splitPane, BorderLayout.CENTER);

            // --- KẾT LUẬN LÃI LỖ ---
            lblKetLuan = new JLabel("$$$", JLabel.CENTER);
            lblKetLuan.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 22));
            lblKetLuan.setForeground(Color.BLUE);
            lblKetLuan.setBorder(new EmptyBorder(10,0,10,0));
            add(lblKetLuan, BorderLayout.SOUTH);

            //Event nút tính
            btnTinh.addActionListener(e -> tinhToanTaiChinh());
        }

        private void tinhToanTaiChinh() {
            // Xóa dữ liệu cũ trên bảng
            modelSummary.setRowCount(0);
            modelThu.setRowCount(0);
            modelChi.setRowCount(0);

            String ngayCanLoc = txtNgayLoc.getText().trim(); // Lấy ngày từ ô nhập

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                long von = 0;
                try { von = Long.parseLong(txtVon.getText().trim()); } catch (Exception ex) {}

                long tongThu = 0, tongChi = 0;

                //TÍNH TỔNG THU
                // CAST(NGAYGIO AS DATE) cắt bỏ phần giờ phút, chỉ so sánh ngày
                String sqlSumThu = "SELECT SUM(TIEN) FROM LSBAN WHERE CAST(NGAYGIO AS DATE) = ?";
                PreparedStatement psThu = conn.prepareStatement(sqlSumThu);
                psThu.setString(1, ngayCanLoc);
                ResultSet rsSumThu = psThu.executeQuery();
                if (rsSumThu.next()) tongThu = rsSumThu.getLong(1);

                //TÍNH TỔNG CHI
                String sqlSumChi = "SELECT SUM(TongTienNhap) FROM NHAP_KHO WHERE TrangThai = N'Đã duyệt' AND CAST(NgayNhap AS DATE) = ?";
                PreparedStatement psChi = conn.prepareStatement(sqlSumChi);
                psChi.setString(1, ngayCanLoc);
                ResultSet rsSumChi = psChi.executeQuery();
                if (rsSumChi.next()) tongChi = rsSumChi.getLong(1);

                //Tính Tổng tiền cuối cùng
                long tongTien= von + tongThu - tongChi;
                modelSummary.addRow(new Object[]{ngayCanLoc, df.format(von), df.format(tongThu), df.format(tongChi), df.format(tongTien)});

                //Hiển thị Lãi/Lỗ
                long laiLo = tongTien - von;
                if (tongTien >= von) {
                    lblKetLuan.setText("KẾT QUẢ NGÀY " + ngayCanLoc + ": LÃI " + df.format(tongTien - von) + " VNĐ");
                    lblKetLuan.setForeground(new Color(0, 153, 0));
                } else {
                    lblKetLuan.setText("KẾT QUẢ NGÀY " + ngayCanLoc + ": LỖ/ÂM VỐN " + df.format(von - tongTien) + " VNĐ");
                    lblKetLuan.setForeground(Color.RED);
                }

                // bảng LSBAN
                String sqlListThu = "SELECT NGAYGIO, TENSP, SOLUONG, TIEN, TenNV FROM LSBAN WHERE CAST(NGAYGIO AS DATE) = ? ORDER BY NGAYGIO DESC";
                PreparedStatement psListThu = conn.prepareStatement(sqlListThu);
                psListThu.setString(1, ngayCanLoc);
                ResultSet rsListThu = psListThu.executeQuery();
                while(rsListThu.next()) {
                    modelThu.addRow(new Object[]{
                            rsListThu.getTimestamp(1),
                            rsListThu.getString(2),
                            rsListThu.getInt(3),
                            df.format(rsListThu.getLong(4)),
                            rsListThu.getString(5)
                    });
                }

                //bảng Nhap_Kho
                String sqlListChi = "SELECT NgayNhap, TenSP, SoLuongNhap, TongTienNhap, TrangThai FROM NHAP_KHO WHERE TrangThai = N'Đã duyệt' AND CAST(NgayNhap AS DATE) = ? ORDER BY NgayNhap DESC";
                PreparedStatement psListChi = conn.prepareStatement(sqlListChi);
                psListChi.setString(1, ngayCanLoc);
                ResultSet rsListChi = psListChi.executeQuery();
                while(rsListChi.next()) {
                    modelChi.addRow(new Object[]{
                            rsListChi.getTimestamp(1),
                            rsListChi.getString(2),
                            rsListChi.getInt(3),
                            df.format(rsListChi.getLong(4)),
                            rsListChi.getString(5)
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage() + "\nHãy nhập đúng định dạng ngày yy-mm-dd (Ví dụ: 2025-12-31)");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLy().setVisible(true));
    }
}