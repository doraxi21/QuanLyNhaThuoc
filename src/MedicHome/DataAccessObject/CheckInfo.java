package MedicHome.DataAccessObject;
import java.sql.*;

public class CheckInfo {
    public boolean checklogin(String user,String maNV, String ViTri, String pass){
        boolean ketqua=false;

        try{
            String url = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=QuanLyMedicHome;encrypt=true;trustServerCertificate=true;";
            Connection connect = DriverManager.getConnection(url,"sa","hieudz");

            String sql = "SELECT * FROM TKNV WHERE TenNhanVien=? AND MaNhanVien=? AND ViTri=? AND MatKhau=?";


            PreparedStatement ps=connect.prepareStatement(sql);
            ps.setString(1,user);
            ps.setString(2,maNV);
            ps.setString(3,ViTri);
            ps.setString(4,pass);

            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                ketqua=true;  //tim thay nhan vien khop
            }
            connect.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        return ketqua;
    }
}
