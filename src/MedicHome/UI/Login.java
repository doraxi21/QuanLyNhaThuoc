package MedicHome.UI;

import MedicHome.DataAccessObject.CheckInfo;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Login extends JFrame {
    private JTextField txtUser, txtMaNV;
    private JPasswordField txtPass;
    private JComboBox<String> cbViTri, cbCa;
    private JLabel lbCa;

    public Login() {
        setTitle("Login - Medic Home");
        setSize(500, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

    //1. Background
        BackgroundPanel mainPanel = new BackgroundPanel("login.jpg");
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

    //2. Top
        JLabel title = new JLabel("Đăng nhập", JLabel.CENTER);
        title.setForeground(Color.BLUE);
        title.setFont(new Font("Times New Roman", Font.BOLD, 30));
        title.setBorder(new EmptyBorder(30, 0, 30, 0));
        mainPanel.add(title, BorderLayout.NORTH);

    //3. Center
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        txtUser = new JTextField(20);
        txtUser.setPreferredSize(new Dimension(0, 30));

        txtMaNV = new JTextField(20);
        txtMaNV.setPreferredSize(new Dimension(0, 30));

        txtPass = new JPasswordField(20);
        txtPass.setPreferredSize(new Dimension(0, 30));

        cbViTri = new JComboBox<>(new String[]{"Nhân viên bán", "Nhân viên kho", "Quản lý"});

        cbCa = new JComboBox<>(new String[]{"Sáng (1)", "Chiều (2)", "Tối (3)"});
        lbCa = new JLabel("Ca:");


        int y = 0;   //trục y |

        addItem(center, new JLabel("Tên Đăng Nhập:"), txtUser, y++);
        addItem(center, new JLabel("Mã Nhân Viên:"), txtMaNV, y++);
        addItem(center, new JLabel("Vị Trí:"), cbViTri, y++);
        addItem(center, lbCa, cbCa, y++);
        addItem(center, new JLabel("Mật Khẩu:"), txtPass, y++);

        //On||OFF mật khẩu
        JCheckBox showPass = new JCheckBox("Hiện mật khẩu");
        showPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPass.setOpaque(false);
        showPass.addActionListener(e -> txtPass.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = y++;
        gbc.anchor = GridBagConstraints.WEST;
        center.add(showPass, gbc);

        mainPanel.add(center, BorderLayout.CENTER);

        //Quản lý -> skip ca
        cbViTri.addActionListener(e -> {
            boolean show = !"Quản lý".equals(cbViTri.getSelectedItem());
            lbCa.setVisible(show); cbCa.setVisible(show);
        });

    //4. Bottom
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 0, 30, 0));

        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBackground(Color.BLUE); btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(140, 35));

        bottom.add(btnLogin);

        mainPanel.add(bottom, BorderLayout.SOUTH);

        // event login
        btnLogin.addActionListener(e -> xulyLogin());
    }

    // hàm thêm các item
    private void addItem(JPanel p, JComponent label, JComponent input, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridy = y;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        // ép kiểu label
        if (label instanceof JLabel) {
            JLabel labelDaEpKieu = (JLabel) label;
            labelDaEpKieu.setFont(new Font("Arial", Font.BOLD, 13));
        }
        p.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        p.add(input, gbc);
    }

    private void xulyLogin() {
        String u = txtUser.getText().trim();
        String id = txtMaNV.getText().trim();
        String p = new String(txtPass.getPassword());
        String role = (String) cbViTri.getSelectedItem();
        String ca = (String) cbCa.getSelectedItem();

        if (u.isEmpty() || id.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (new CheckInfo().checklogin(u, id, role, p)) {
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            this.dispose();
            if ("Quản lý".equals(role)) new QuanLy().setVisible(true);
            else if ("Nhân viên kho".equals(role)) new KHO(id, u, ca).setVisible(true);
            else new NhanVien(id, u, ca).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new Login().setVisible(true));
    }
}

//Background
class BackgroundPanel extends JPanel {
    private Image bg;
    public BackgroundPanel(String file) {
        try { bg = ImageIO.read(getClass().getResource("/img/" + file)); } catch (IOException e) { e.printStackTrace(); }
    }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
    }
}