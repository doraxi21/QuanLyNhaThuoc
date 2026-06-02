package MedicHome.DataAccessObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThuocDAO {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "hieudz";

    public List<Thuoc> getAllThuoc() {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM THUOC ORDER BY LOAI ASC, TENSP ASC";

        try ( Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery() )
        {

            while (rs.next()) {

                String ma = rs.getString("MASP");
                String ten = rs.getString("TENSP");
                String donVi = rs.getString("DONVI");
                String loai = rs.getString("LOAI");
                String ncc = rs.getString("NCC");
                String nsx = rs.getString("NSX");
                String hsd = rs.getString("HSD");
                int soluong=rs.getInt("SOLUONG");
                long gia = rs.getLong("TIEN");

                list.add(new Thuoc(ma, ten, donVi, loai, ncc, nsx, hsd, soluong,(double) gia));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi lấy dữ liệu: " + e.getMessage());
        }
        return list;
    }

    public void truTonKho(String maSP, int soLuongBan) {
        String sql = "UPDATE THUOC SET SOLUONG = SOLUONG - ? WHERE MASP = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, soLuongBan);
            ps.setString(2, maSP);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//class -> lay data tu sql -> ghi de vo list co cac kieu du lieu trong THUOC