package MedicHome.DataAccessObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LichSuDAO {
    String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
    String user = "sa";
    String pass = "hieudz";

    public void luuLichSuBan(String tenThuoc, String maThuoc, int soLuong, long tien, String maNV,String tenNV, String ca) {
        String sql = "INSERT INTO LSBAN (TenSP, MaSP, SoLuong, Tien, NgayGio, MaNV,TenNV, Ca) VALUES (?, ?, ?, ?, GETDATE(), ?,?, ?)";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenThuoc);
            ps.setString(2, maThuoc);
            ps.setInt(3, soLuong);
            ps.setLong(4, tien);
            ps.setString(5, maNV);
            ps.setString(6, tenNV);
            ps.setString(7,ca);

            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}