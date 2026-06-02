package MedicHome.UI.Dialog;

import MedicHome.DataAccessObject.Thuoc;
import MedicHome.DataAccessObject.ThuocDAO;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class HangHoaDialog extends JDialog {
    public HangHoaDialog(JFrame parent, boolean isEditable) {
        super(parent, "Danh sách Hàng Hoá", true);
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("KHO HÀNG HOÁ TỔNG HỢP");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));

        if(isEditable) {
            JLabel lblHint = new JLabel(" (Quản lý: Click đúp để sửa, Chuột phải xóa)");
            lblHint.setForeground(Color.RED);
            pnTop.add(lblTitle); pnTop.add(lblHint);
        } else {
            pnTop.add(lblTitle);
        }
        add(pnTop, BorderLayout.NORTH);

        String[] cols = {"Mã SP", "Tên thuốc", "Loại", "NSX", "Hạn", "NCC", "Số lượng", "Đơn giá"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (!isEditable) return false;
                return column != 0;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(25);

        //quan ly dc cho phep sua
        if (isEditable) {
            model.addTableModelListener(e -> {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    if(row < 0 || col < 0) return;
                    String maSP = model.getValueAt(row, 0).toString();
                    Object val = model.getValueAt(row, col);
                    updateSQL(maSP, col, val);
                }
            });
        //xóa dòng
        JPopupMenu popupMenu=new JPopupMenu();
        JMenuItem del= new JMenuItem("Xóa Thuốc");
        del.setForeground(Color.RED);
        del.setFont(new Font("Arial",Font.BOLD,12));
        popupMenu.add(del);
        //event nut xoa
        del.addActionListener(e->{
            int row=table.getSelectedRow();
            if(row != -1){
                String maSP=table.getValueAt(row,0).toString();
                String tenSP=table.getValueAt(row,1).toString();
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc muốn xóa vĩnh viễn thuốc: " + tenSP + "?",
                        "Cảnh báo xóa", JOptionPane.YES_NO_OPTION);
                if(confirm==JOptionPane.YES_OPTION){
                    if(deleteRow(maSP)){
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(this,"Delete Success");
                    }
                }else{
                    JOptionPane.showMessageDialog(this,"Vui lòng chọn dòng cần xóa");
                }
            }
        });
        //gan event chuot phai vao table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){ showPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e){showPopup(e);}

            private void showPopup(MouseEvent e){
                if(e.isPopupTrigger()){
                    int row=table.rowAtPoint(e.getPoint());
                    if(row!=-1){  // != ko phai dong nao
                        table.setRowSelectionInterval(row ,row);
                        popupMenu.show(e.getComponent(),e.getX(),e.getY());
                    }
                }
            }
        });
        }

        // Load data
        ThuocDAO dao = new ThuocDAO();
        List<Thuoc> list = dao.getAllThuoc();
        if(list != null) {
            for (Thuoc t : list) {
                model.addRow(new Object[]{t.ma, t.ten, t.loai, t.nsx, t.hsd, t.ncc, t.soluong, (long)t.gia});
            }
        }
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void updateSQL(String maSP, int col, Object val) {
        String colName = "";
        switch(col) {
            case 1: colName="TENSP"; break;
            case 2: colName="LOAI"; break;
            case 3: colName="NSX"; break;
            case 4: colName="HSD"; break;
            case 5: colName="NCC"; break;
            case 6: colName="SOLUONG"; break;
            case 7: colName="TIEN"; break;
        }
        if(colName.isEmpty()) return;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;", "sa", "hieudz")) {
            PreparedStatement ps = conn.prepareStatement("UPDATE THUOC SET " + colName + " = ? WHERE MASP = ?");
            if(col==6 || col==7) ps.setLong(1, Long.parseLong(val.toString()));
            else ps.setString(1, val.toString());
            ps.setString(2, maSP);
            ps.executeUpdate();
            System.out.println("Đã cập nhật " + colName);
        } catch(Exception e) { JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage()); }
    }
    private boolean deleteRow(String maSP){
        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
        String user = "sa";
        String pass = "hieudz";

        try (Connection conn = DriverManager.getConnection(dbUrl, user, pass)) {
            String sql = "DELETE FROM THUOC WHERE MASP = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, maSP);
            ps.executeUpdate();
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể xóa! (Có thể thuốc đang tồn tại trong Lịch sử bán/Nhập kho)\nChi tiết lỗi: " + e.getMessage());
            return false;
        }
    }
}