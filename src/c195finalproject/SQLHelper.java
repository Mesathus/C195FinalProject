/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;
import java.sql.*;
import java.lang.StringBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.time.*;
/**
 *
 * @author Mesa
 */

/**
 * Table layouts
 * city
 *      cityId INT 10
 *      city VARCHAR 50
 *      countryId INT 10
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 * country
 *      countryId INT 10
 *      country VARCHAR 50
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 * address
 *      addressId INT 10
 *      address VARCHAR 50
 *      address2 VARCHAR 50
 *      cityId INT 10
 *      postalCode VARCHAR 10
 *      phone VARCHAR 20
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 * customer
 *      customerId INT 10
 *      customerName VARCHAR 45
 *      addressId INT 10
 *      active TINYINT 1
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 * appointment
 *      appointmentId INT 10
 *      customerId INT 10
 *      userId INT
 *      title VARCHAR 255
 *      description TEXT
 *      location TEXT
 *      contact TEXT
 *      type TEXT
 *      url VARCHAR 255
 *      start DATETIME
 *      end DATETIME
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 * user
 *      userId INT
 *      userName VARCHAR 50
 *      password VARCHAR 50
 *      active TINYINT
 *      createDate DATETIME
 *      createdBy VARCHAR 40
 *      lastUpdate TIMESTAMP
 *      lastUpdateBy VARCHAR 40
 */


public class SQLHelper{
    private static DataSource ds;
    private static Connection conn = null;
    private static PreparedStatement prepstatement = null;
    private static ResultSet results = null;
    /*
        Inserting appointments requires
    */    
    public static boolean Insert(String userName, String custName,String title, String description, String location, String contact, String type, String url, Timestamp start, Timestamp end) throws SQLException //insert method for appointments
    {
        try{            
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT customer.customerID, user.userID FROM customer, user WHERE userName = ? OR customerName = ?");
            prepstatement.setString(1,userName);
            prepstatement.setString(2,custName);
            results = prepstatement.executeQuery();
            if(results == null) throw new NullPointerException();
            int userID = 0, custID = 0;
            if(results.next()) userID = results.getInt(1);
            if(results.next()) custID = results.getInt(1);
            results = null;            
            prepstatement = conn.prepareStatement("INSERT INTO appointment (appointmentId, customerId, userId, "
                    + "title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            prepstatement.setInt(2,userID);
            prepstatement.setInt(3,custID);
            prepstatement.setString(4,title);
            prepstatement.setString(5,description);
            prepstatement.setString(6,location);
            prepstatement.setString(7,contact);
            prepstatement.setString(8,type);
            prepstatement.setString(9,url);
            prepstatement.setTimestamp(10, start);
            prepstatement.setTimestamp(11,end);
            prepstatement.executeUpdate();
            //add appointmentId(PK), customerId, userId, createDate, createdBy, lastUpdate, lastUpdateBy
            return true;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return false;
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
            System.out.println("User/Customer ID lookup returned a null result set.");
            return false;
        }
        finally{
            prepstatement = null;
            conn = null;
            ds = null;
        }
    }  
    
    public static boolean Delete() throws SQLException //delete method for appointments
    {
        
        return false;
    }
    
    public static boolean Update() throws SQLException //update method for appointments
    {return false;}
    
    //Methods for new customers
    public static boolean Insert(String name, Boolean active, String address1, String address2, String postalCode, String phone, String city, String country) throws SQLException{
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("INSERT INTO ");
        }
        catch(Exception e){}
        finally{}
        
        return false;
    }
    
    
    public static StringBuilder GetPass(String inputName) throws SQLException
    {
        StringBuilder pass = new StringBuilder();
        try{
            ds = DataSource.getInstance();
            //System.out.println(ds);
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT password from user WHERE userName = ?");
            prepstatement.setString(1, inputName);
            results = prepstatement.executeQuery();
            if(results.next()) pass.insert(0,results.getString(1));
            return pass;
        }
        catch(SQLException|NullPointerException e)
        {
            System.out.println(e.getMessage());
        }
        finally{
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
        }
        return pass;
    }

/*static class DataSource {
    private final static String sqlConnect = "jdbc:mysql://3.227.166.251:3306/U062a2";
    private final static String user = "U062a2";
    private final static String pass = "53688672962";
    private final MysqlDataSource myDS = new MysqlDataSource();
    
    private DataSource(){
        myDS.setURL(sqlConnect);
        myDS.setUser(user);
        myDS.setPassword(pass);
    }
    DataSource getInstance(){
        if(ds == null) ds = new DataSource();
        return ds;
    }
    public MysqlDataSource getMDS(){
        return myDS;
    }    
}    */
}
