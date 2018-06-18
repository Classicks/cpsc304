import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.*;

import net.proteanit.sql.DbUtils;
import package1.Connections;

public class customer {
    private JTextArea text_proname;
    private JTextArea text_proprice;
    private JTextArea text_probrand;
    private JTextArea text_proID;
    private JTextArea text_sellerID;
    private JTextArea text_sellername;
    private JTextField price_low;
    private JTextField price_high;
    private JButton findCheapestProductButton;
    private JButton findMostExpensiveProductButton;
    private JTextField rate_proID;
    private JTextField rate_score;
    private JButton rateByIDButton;
    private JTextField order_proID;
    private JButton putOrderButton;
    public JPanel CustomerUI;
    private JTextField search_proName;
    private JTextField search_sellerName;
    private JButton searchButton;
    private JTextField search_rating;
    private JButton findRatingButton;
    private JTable tableSearch;
    private String customerID;

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public customer() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String SELECT = "SELECT P.producthas_name, P.producthas_brand, P.producthas_price, P.producthas_id, P.seller_id, ";
                String FROM = " FROM producthas P";
                String WHERE = "";
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs;
                    String proName = search_proName.getText();
                    String low = price_low.getText();
                    String high = price_high.getText();
                    String sellerName = search_sellerName.getText();

                    WHERE = getLikePName(WHERE, proName);
                    WHERE = setPriceRange(WHERE, low, high);

                    if (!search_sellerName.getText().isEmpty()){
                        SELECT += "S.seller_name";
                        FROM += ", seller S ";
                        if (WHERE.isEmpty()) {
                            WHERE = " WHERE P.seller_id = S.seller_id AND S.seller_name LIKE %" + search_sellerName + "%";

                        }
                        else {
                            WHERE = WHERE + " AND P.seller_id = S.seller_id AND S.seller_name LIKE %" + search_sellerName + "%";
                        }
                    }

                    rs =stmt.executeQuery(SELECT + FROM + WHERE);
                    tableSearch.setModel(DbUtils.resultSetToTableModel(rs));
                    JOptionPane.showMessageDialog(null, "Search success!");
                }catch(SQLException ex){
                    System.out.println("Search failed (Combo): " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Search failure.");
                }
            }

            private String getLikePName(String where, String proName) {
                if (!proName.isEmpty()){
                    if (where.isEmpty()) {
                        where = " WHERE P.producthas_name LIKE %" + proName + "%";
                    }
                    else {
                        where += " AND P.producthas_name LIKE %" + proName + "%";
                    }
                }
                return where;
            }

            private String setPriceRange(String where, String low, String high) {
                if (!(low.isEmpty() && high.isEmpty())){
                    if (where.isEmpty()) {
                        where = " WHERE P.producthas_price <= " + high + " AND P.producthas_price >= " + low;
                    }
                    else {
                        where += " AND P.producthas_price <= " + high + " AND P.producthas_price >= " + low;
                    }
                }
                else if (!low.isEmpty()){
                    if (where.isEmpty()) {
                        where = "WHERE P.producthas_price >= " + low;
                    }
                    else {
                        where += " AND P.producthas_price >= " + low;
                    }
                }
                else if (!high.isEmpty()) {
                    if (where.isEmpty()) {
                        where = "WHERE P.producthas_price <= " + high;
                    }
                    else {
                        where += " AND P.producthas_price >= " + high;
                    }
                }
                return where;
            }
        });

        findRatingButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT AVG(R.rating) " +
                            "FROM Rating R " +
                            "WHERE R.ProductID = " +
                            search_rating.getText());


                    tableSearch.setModel(DbUtils.resultSetToTableModel(rs));
                }
                catch (SQLException ex){
                    System.out.println("Search failed (Rating): " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Search failure.");
                }
           }
        });

        findCheapestProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT P.producthas_name, P.producthas_brand, P.producthas_price, P.producthas_id, P.seller_id, " +
                                                            "FROM producthas P " +
                                                            "WHERE P.producthas_price IN (SELECT MIN(P1.producthas_price) " +
                                                                                            "FROM producthas P1)");

                    tableSearch.setModel(DbUtils.resultSetToTableModel(rs));
                }
                catch (SQLException ex){
                    System.out.println("Search failed (Cheapest): " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Search failure.");
                }
            }
        });
        findMostExpensiveProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT P.producthas_name, P.producthas_brand, P.producthas_price, P.producthas_id, P.seller_id, " +
                                                            "FROM producthas P " +
                                                            "WHERE P.producthas_price IN (SELECT MAX(P1.producthas_price) " +
                                                                                            "FROM producthas P1)");

                    tableSearch.setModel(DbUtils.resultSetToTableModel(rs));
                }
                catch (SQLException ex){
                    System.out.println("Search failed (Most Expensive): " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Search failure.");
                }
            }
        });
        rateByIDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    ResultSet rs;
                    rs = stmt.executeQuery("SELECT R.*, FROM rate R, WHERE R.customer_id = " + customerID + " AND R.producthas_id = " + rate_proID.getText());
                    if (rs.wasNull()) {
                        rs = stmt.executeQuery("SELECT R.* FROM rate R");

                        rs.moveToInsertRow();
                        rs.updateFloat(1, Float.parseFloat(rate_score.getText()));
                        rs.updateInt(2, Integer.parseInt(customerID));
                        rs.updateInt(3, Integer.parseInt(rate_proID.getText()));
                        rs.moveToCurrentRow();
                        JOptionPane.showMessageDialog(null, "Item rated!");
                    }
                    else {
                        rs.updateFloat(1, Float.parseFloat(rate_score.getText()));
                        JOptionPane.showMessageDialog(null, "Rating updated!");
                    };

                }
                catch (SQLException ex) {
                    System.out.println("Rating failed : " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Rating failed.");
                }
            }
        });
        putOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection con = Connections.getConnection();
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT P.* FROM PUTORDER");
                    int rows = rs.getRow() + 1;
                    java.util.Date today = new java.util.Date();
                    java.sql.Date sqlToday = new java.sql.Date(today.getTime());
                    int rowCount = stmt.executeUpdate("INSERT INTO PUTORDER VALUES (" + rows + ", " +
                            null + ", " + null + ", " + sqlToday + ", " + "PayPal, " + customerID + ", " + order_proID.getText() +")");
                }
                catch (SQLException ex) {
                    System.out.println("Order not made : " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Ordering failed.");
                }
            }
        });



    }
}
