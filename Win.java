package com.shop.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.shop.db.Dao;
import com.shop.mod.Item;
import java.awt.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Win extends JFrame {
    private JTable tbProduct, tbCart;
    private DefaultTableModel mdProduct, mdCart;
    private Dao db = new Dao();
    private JTextField fName, fPrice; // 後台輸入框
    private JLabel lblTime, lblTotal;
    private double currentTotal = 0;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Win() {
        setTitle("EasyShop 系統 - 前後台整合版");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- 1. 介面配置 ---
        lblTime = new JLabel("目前時間：正在載入...", SwingConstants.RIGHT);
        lblTime.setFont(new Font("微軟正黑體", Font.BOLD, 14));

        // 表格設定
        mdProduct = new DefaultTableModel(new Object[]{"ID", "名稱", "單價"}, 0);
        tbProduct = new JTable(mdProduct);
        mdCart = new DefaultTableModel(new Object[]{"名稱", "單價", "數量", "小計"}, 0);
        tbCart = new JTable(mdCart);

        // --- 2. 按鈕與面板 ---
        // 前台購物按鈕
        JButton bAddToCart = new JButton("加入購物車 ↓");
        JButton bRemoveFromCart = new JButton("從購物車移除");
        JButton bCheckout = new JButton("確認結帳並列印");
        lblTotal = new JLabel("總計：$0", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("微軟正黑體", Font.BOLD, 18));
        lblTotal.setForeground(Color.RED);

        // 後台管理元件
        fName = new JTextField(10);
        fPrice = new JTextField(5);
        JButton bAdd = new JButton("後台：新增商品");
        JButton bDel = new JButton("後台：刪除商品");

        // --- 3. 佈局組合 ---
        // 中間表格區 (上下排列)
        JPanel pTables = new JPanel(new GridLayout(2, 1, 5, 5));
        pTables.add(new JScrollPane(tbProduct));
        pTables.add(new JScrollPane(tbCart));

        // 下方控制區 (兩列)
        JPanel pControl = new JPanel(new GridLayout(2, 1));
        
        // 第一列：前台購物
        JPanel pShop = new JPanel();
        pShop.setBorder(BorderFactory.createTitledBorder("前台結帳區"));
        pShop.add(bAddToCart); pShop.add(bRemoveFromCart); pShop.add(bCheckout); pShop.add(lblTotal);
        
        // 第二列：後台管理
        JPanel pAdmin = new JPanel();
        pAdmin.setBorder(BorderFactory.createTitledBorder("後台管理區"));
        pAdmin.add(new JLabel("名稱:")); pAdmin.add(fName);
        pAdmin.add(new JLabel("單價:")); pAdmin.add(fPrice);
        pAdmin.add(bAdd); pAdmin.add(bDel);

        pControl.add(pShop);
        pControl.add(pAdmin);

        getContentPane().add(lblTime, BorderLayout.NORTH);
        getContentPane().add(pTables, BorderLayout.CENTER);
        getContentPane().add(pControl, BorderLayout.SOUTH);

        // 即時時間
        new Timer(1000, e -> lblTime.setText("目前時間：" + LocalDateTime.now().format(dtf))).start();

        // --- 4. 邏輯功能 ---

        // 加入購物車 (前台)
        bAddToCart.addActionListener(e -> {
            int row = tbProduct.getSelectedRow();
            if (row == -1) return;
            String name = mdProduct.getValueAt(row, 1).toString();
            double price = Double.parseDouble(mdProduct.getValueAt(row, 2).toString());
            
            Integer[] qtys = {1,2,3,4,5,6,7,8,9,10};
            Integer q = (Integer) JOptionPane.showInputDialog(this, "數量", "加入購物車", 
                        JOptionPane.PLAIN_MESSAGE, null, qtys, 1);
            if (q != null) {
                mdCart.addRow(new Object[]{name, price, q, price * q});
                updateTotal();
            }
        });

        // 結帳 (前台)
        bCheckout.addActionListener(e -> {
            if (mdCart.getRowCount() == 0) return;
            StringBuilder sb = new StringBuilder();
            int totalQ = 0;
            for (int i = 0; i < mdCart.getRowCount(); i++) {
                sb.append(mdCart.getValueAt(i, 0)).append("x").append(mdCart.getValueAt(i, 2)).append(" ");
                totalQ += Integer.parseInt(mdCart.getValueAt(i, 2).toString());
            }
            try {
                db.saveOrder(sb.toString(), currentTotal, totalQ);
                tbCart.print(JTable.PrintMode.FIT_WIDTH, new MessageFormat("收據 " + LocalDateTime.now().format(dtf)), null);
                mdCart.setRowCount(0);
                updateTotal();
                JOptionPane.showMessageDialog(this, "結帳完成！");
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // 新增商品 (後台)
        bAdd.addActionListener(e -> {
            try {
                db.addItem(fName.getText(), Double.parseDouble(fPrice.getText()), 100);
                load();
                fName.setText(""); fPrice.setText("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "輸入錯誤"); }
        });

        // 刪除商品 (後台)
        bDel.addActionListener(e -> {
            int row = tbProduct.getSelectedRow();
            if (row != -1) {
                try {
                    db.delItem((int)mdProduct.getValueAt(row, 0));
                    load();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        bRemoveFromCart.addActionListener(e -> {
            int row = tbCart.getSelectedRow();
            if (row != -1) { mdCart.removeRow(row); updateTotal(); }
        });

        load();
    }

    private void updateTotal() {
        currentTotal = 0;
        for (int i = 0; i < mdCart.getRowCount(); i++) currentTotal += (double) mdCart.getValueAt(i, 3);
        lblTotal.setText("總計：$" + String.format("%.0f", currentTotal));
    }

    private void load() {
        try {
            mdProduct.setRowCount(0);
            for (Item i : db.listItems()) mdProduct.addRow(new Object[]{i.id, i.name, i.price});
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Win().setVisible(true));
    }
}
