package com.shop.db;

import java.sql.*;
import java.util.*;
import com.shop.mod.Item;

public class Dao {
    private String url = "jdbc:mysql://localhost:3306/shop_db?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8";
    private String user = "root";
    private String pass = "1234";

    public Connection getConn() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }

    // 登入驗證
    public boolean login(String username, String password) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection c = getConn(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 註冊帳號
    public void register(String username, String password) throws Exception {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection c = getConn(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, password);
            p.executeUpdate();
        }
    }

    // 新增商品 (對應您的 products 表)
    public void addItem(String name, double price, int stock) throws Exception {
        String sql = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setDouble(2, price);
            p.setInt(3, stock);
            p.executeUpdate();
        }
    }

    // 存入訂單
    public void saveOrder(String names, double total, int totalQty) throws Exception {
        String sql = "INSERT INTO orders (item_names, total_price, total_qty) VALUES (?, ?, ?)";
        try (Connection c = getConn(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, names);
            p.setDouble(2, total);
            p.setInt(3, totalQty);
            p.executeUpdate();
        }
    }

    // 取得商品清單
    public List<Item> listItems() throws Exception {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection c = getConn(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Item(rs.getInt("id"), rs.getString("name"), rs.getDouble("price")));
            }
        }
        return list;
    }

    // 刪除商品
    public void delItem(int id) throws Exception {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }
}
