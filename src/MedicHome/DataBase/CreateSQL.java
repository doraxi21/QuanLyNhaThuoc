package MedicHome.DataBase;
import java.sql.*;

public class CreateSQL {
    public static void main(String[] args){

        String url="jdbc:sqlserver://localhost\\SQLEXPRESS;" +
                "databaseName=QuanlyMedicHome;" +
                "integratedSecurity=true;" +
                "trustServerCertificate=true";
        try (Connection conect= DriverManager.getConnection(url)){
            System.out.println("Connect Success");
            createThuoc(conect);
            createTKNV(conect);
            createLichLam(conect);
            createLSBan(conect);
            BAOCAO(conect);
            NHAPKHO(conect);

        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void createThuoc(Connection con) throws SQLException{
        Statement stmt = con.createStatement();
        String query = "CREATE TABLE THUOC (" +
                "STT INT IDENTITY(1,1),"+
                "MASP NVARCHAR(50) PRIMARY KEY," +
                "TENSP NVARCHAR(100)," +
                "LOAI NVARCHAR(50)," +
                "DONVI NVARCHAR(50)," +
                "NCC NVARCHAR(100),"+
                "NSX DATE," +
                "HSD DATE," +
                "SOLUONG INT," +
                "TIEN BIGINT)";
        stmt.execute(query);
        System.out.println("Create TableThuoc Success");
    }

    public static void createLichLam(Connection con) throws SQLException{
        Statement stmt = con.createStatement();
        String query="CREATE TABLE LICHLAM ("+
                "STT INT IDENTITY(1,1),"+
                "MaNhanVien NVARCHAR(50),"+
                "TenNhanVien NVARCHAR(50),"+
                "Monday NVARCHAR(50),"+
                "Tuesday NVARCHAR(50),"+
                "Wednesday NVARCHAR(50),"+
                "Thursday NVARCHAR(50),"+
                "Friday NVARCHAR(50),"+
                "Saturday NVARCHAR(50),"+
                "Sunday NVARCHAR(50))";
        stmt.execute(query);
        System.out.println("Create TableLichLam Success");
    }

    public static void createLSBan(Connection con) throws SQLException{
        Statement stmt = con.createStatement();
        String query= "CREATE TABLE LSBAN(" +
                "STT INT IDENTITY(1,1) PRIMARY KEY," +
                "TENSP NVARCHAR(100)," +
                "MASP NVARCHAR(50)," +
                "NGAYGIO DATETIME," +
                "SOLUONG INT," +
                "TIEN BIGINT,"+
                "MaNV NVARCHAR(50)," +
                "TenNV NVARCHAR(50),"+
                "Ca NVARCHAR(20)," +
                "CONSTRAINT FK_LSBAN_THUOC FOREIGN KEY (MASP) REFERENCES THUOC(MASP))";
        stmt.execute(query);
        System.out.println("Create TableLSBan Success");
    }

    public static void createTKNV(Connection con) throws SQLException{
        Statement stmt = con.createStatement();
        String query= "CREATE TABLE TKNV(" +
                "STT INT IDENTITY(1,1)," +
                "TenNhanVien NVARCHAR(50)," +
                "MaNhanVien NVARCHAR(50) PRIMARY KEY," +
                "ViTri NVARCHAR(50)," +
                "MatKhau NVARCHAR(50))";
        stmt.execute(query);
        System.out.println("Create TableTKNV Success");
        //thêm tài khoản mặc định
        String sqlAdmin = "INSERT INTO TKNV (TenNhanVien, MaNhanVien, ViTri, MatKhau) VALUES ('admin', 'admin', N'Quản Lý', '123')";
        stmt.executeUpdate(sqlAdmin);
    }

    public  static void BAOCAO(Connection con) throws SQLException{
        Statement stmt=con.createStatement();
        String query= "CREATE TABLE BAOCAO ("+
                "STT INT IDENTITY(1,1) PRIMARY KEY,"+
                "MaNV NVARCHAR(50),"+
                "TenNV NVARCHAR(100),"+
                "CaLam NVARCHAR(20),"+
                "NgayBaoCao DATETIME DEFAULT GETDATE(),"+
                "TongThuMay BIGINT,"+
                "TienThucTe BIGINT,"+
                "ChenhLech BIGINT,"+
                "GhiChu NVARCHAR(200))";
        stmt.execute(query);
        System.out.println("Create TableBAOCAO Success");
    }

    public static void NHAPKHO(Connection con) throws SQLException{
        Statement stmt=con.createStatement();

        String query="CREATE TABLE NHAP_KHO(" +
                "STT INT IDENTITY(1,1) PRIMARY KEY," +
                "MaSP NVARCHAR(50)," +
                "TenSP NVARCHAR(100)," +
                "Loai NVARCHAR(50)," +
                "DonVi NVARCHAR(20)," +
                "NCC NVARCHAR(100)," +
                "NSX DATE," +
                "HSD DATE," +
                "SoLuongNhap INT," +
                "GiaNhap BIGINT," +
                "TongTienNhap BIGINT," +
                "TrangThai NVARCHAR(50)," +
                "NgayNhap DATETIME DEFAULT GETDATE()," +
                "MaNV NVARCHAR(50)," +
                "TenNhanVien NVARCHAR(50)," +
                "Ca NVARCHAR(20)," +
                "GhiChu NVARCHAR(200))";
        stmt.execute(query);
        System.out.println("Create TableNhapKho Success");
    }
}