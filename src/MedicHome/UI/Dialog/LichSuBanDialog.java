package MedicHome.UI.Dialog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LichSuBanDialog extends JDialog {
    public LichSuBanDialog(JFrame parent) {
        super(parent, "Lịch sử bán hàng", true);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("LỊCH SỬ BÁN HÀNG TRONG NGÀY", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(10,0,10,0));
        add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Thời gian", "Tên thuốc", "Mã SP", "Số lượng", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;", "sa", "hieudz");
             PreparedStatement ps = conn.prepareStatement("SELECT NGAYGIO, TENSP, MASP, SOLUONG, TIEN FROM LSBAN WHERE CAST(NGAYGIO AS DATE) = CAST(GETDATE() AS DATE) ORDER BY NGAYGIO DESC");
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                model.addRow(new Object[]{rs.getTimestamp("NGAYGIO"), rs.getString("TENSP"), rs.getString("MASP"), rs.getInt("SOLUONG"), rs.getLong("TIEN")});
            }
        } catch(Exception e) { e.printStackTrace(); }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}