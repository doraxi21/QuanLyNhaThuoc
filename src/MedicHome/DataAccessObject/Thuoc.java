package MedicHome.DataAccessObject;

public class Thuoc {
    public String ma, ten, donVi, loai, ncc, nsx, hsd;
    public double gia;
    public int soluong;

    public Thuoc(String ma, String ten, String donVi, String loai, String ncc, String nsx, String hsd, int soluong,double gia) {
        this.ma = ma;
        this.ten = ten;
        this.donVi = donVi;
        this.loai = loai;
        this.ncc = ncc;
        this.nsx = nsx;
        this.hsd = hsd;
        this.soluong=soluong;
        this.gia = gia;
    }

    //ghi de dia chi bo nho thanh ten thuoc + ma sp
    @Override
    public String toString() {
        return ten + " (" + ma + ")";
    }
}


//class -> kieu du lieu doi chieu lay tu sql