package com.shop.ui;

import javax.swing.*;
import com.shop.db.Dao;
import java.awt.*;

public class LoginWin extends JFrame {
    private JTextField fUser = new JTextField(15);
    private JPasswordField fPass = new JPasswordField(15);
    private Dao db = new Dao();

    public LoginWin() {
        setTitle("EasyShop 登入系統");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        JButton bLogin = new JButton("登入");
        JButton bReg = new JButton("註冊");

        add(new JLabel("帳號:", SwingConstants.CENTER)); add(fUser);
        add(new JLabel("密碼:", SwingConstants.CENTER)); add(fPass);
        add(bReg); add(bLogin);

        // 登入邏輯
        bLogin.addActionListener(e -> {
            try {
                String user = fUser.getText();
                String pass = new String(fPass.getPassword());
                if (db.login(user, pass)) {
                    JOptionPane.showMessageDialog(this, "登入成功！");
                    this.dispose(); // 關閉登入視窗
                    new Win().setVisible(true); // 開啟主視窗
                } else {
                    JOptionPane.showMessageDialog(this, "帳號或密碼錯誤");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "連線失敗: " + ex.getMessage());
            }
        });

        // 註冊邏輯
        bReg.addActionListener(e -> {
            try {
                db.register(fUser.getText(), new String(fPass.getPassword()));
                JOptionPane.showMessageDialog(this, "註冊成功，請登入！");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "註冊失敗 (帳號可能已存在)");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWin().setVisible(true));
    }
}
