/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;
import java.sql.*;
import java.time.*;
import java.util.TreeMap;
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
    private LocalDateTime currDateTime;
    private static TreeMap<Integer,Object> map = null;
    /*
        Inserting appointments requires
    */    
    // <editor-fold defaultstate="collapsed" desc="Appointment methods">
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
            if(results.next()) custID = results.getInt(1);
            if(results.next()) userID = results.getInt(1);
            results = null;            
            prepstatement = conn.prepareStatement("INSERT INTO appointment (customerId, userId, "
                    + "title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            prepstatement.setInt(2,custID);
            prepstatement.setInt(3,userID);
            prepstatement.setString(4,title);
            prepstatement.setString(5,description);
            prepstatement.setString(6,location);
            prepstatement.setString(7,contact);
            prepstatement.setString(8,type);
            prepstatement.setString(9,url);
            prepstatement.setTimestamp(10, start);
            prepstatement.setTimestamp(11,end);
            prepstatement.setTimestamp(12, new Timestamp(System.currentTimeMillis()));  //createDate timestamp
            prepstatement.setString(13, userName);                                      //on Insert, create == update, this value not to be changed in Update function
            prepstatement.setTimestamp(14, new Timestamp(System.currentTimeMillis()));  //lastUpdate timestamp
            prepstatement.setString(15, userName);                                      //on Insert, create == update
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
            System.out.println("User/Customer ID lookup returned an empty result set.");
            return false;
        }
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }  
    
    public static boolean Delete(Appointment appt) throws SQLException //delete method for appointments
    {
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("DELETE FROM appointment WHERE appointmentId = ?;");
            prepstatement.setInt(1, appt.getID());
            prepstatement.execute();
            return true;
        }
        catch(SQLException e){
            return false;
        }
    }
    
    public static boolean Update(Appointment appt) throws SQLException //update method for appointments
    {
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("UPDATE appointment "
                    + "SET customerName = ?, "
                    + "WHERE appointmentId = ?;");
            prepstatement.setInt(1, appt.getID());
            prepstatement.executeUpdate();
            return true;
        }
        catch(SQLException e){
            return false;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Customer methods">
    public static boolean Insert(Customer cust, String user) throws SQLException{
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("INSERT INTO customer "
                    + "(customerName,addressId,active,createDate,createdBy,lastUpdate,lastUpdateBy) VALUES "
                    + "(?,?,?,?,?,?,?);");
            prepstatement.setString(2,cust.getName());
            prepstatement.setBoolean(4, cust.getActive());
            prepstatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(6, user);
            prepstatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(8, user);
            prepstatement.execute();
            prepstatement = conn.prepareStatement("INSERT INTO address "
                    + "(address, address2, postalCode, phone) VALUES (?,?,?,?)");
            prepstatement.setString(1, cust.getAddr()[0]);
            prepstatement.setString(2, cust.getAddr()[1]);
            prepstatement.setString(3, cust.getZip());
            prepstatement.setString(4, cust.getPhone());
            prepstatement.execute();
            prepstatement = conn.prepareStatement("INSERT INTO city ");
        }
        catch(SQLException e){System.out.println(e.getMessage());}
        finally{            
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
        }
        
        return false;
    }
    
    // </editor-fold>
    
    
    public static TreeMap GetAppointments(String user) throws SQLException
    {
        try{
            map = new TreeMap<>();
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT appointment.*, customer.customerName, address.phone "
                    + "FROM appointment INNER JOIN "
                    + "(customer INNER JOIN address ON customer.addressId = address.addressId) "
                    + "ON appointment.customerId = customer.customerId "
                    + "WHERE appointment.userId = (SELECT userId FROM user WHERE userName LIKE ?;);");
            prepstatement.setString(1,user);
            results = prepstatement.executeQuery();
            while(results.next()){
                Appointment appt = new Appointment(results.getInt("userId"),results.getString("customerName"),results.getString("title"),results.getString("description"),
                        results.getString("location"), results.getString("contact"),results.getString("url"),
                        results.getTimestamp("start").toLocalDateTime(),results.getTimestamp("end").toLocalDateTime());
                map.put(appt.getID(),appt);
            }
            return map;
        }
        catch(SQLException e){System.out.println(e.getMessage());return null;}
        finally{
            map = null;
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    
    public static TreeMap GetCustomers() throws SQLException
    {
        try{
            map = new TreeMap<>();
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT customer.*, address.*, city.city, country.country"
                    + " FROM customer INNER JOIN "
                    + "(address INNER JOIN "
                    + "(city INNER JOIN country ON city.countryId = country.countryId) "
                    + "ON address.cityId = city.cityId)"
                    + "ON customer.addressId = address.addressId;");
            results = prepstatement.executeQuery();
            while(results.next()){
                //int custID, String custName, String add1, String add2, String postCode, String phone, String city, String country
                Customer cust =  new Customer(results.getInt("customerId"),results.getString("customerName"),results.getBoolean("active"),results.getString("address"),results.getString("address2"),results.getString("postalCode"),
                results.getString("phone"),results.getString("city"),results.getString("country"));
                map.put(cust.getID(), cust);
            }
            return map;
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
            return null;
        }
        finally{
            map = null;
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
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
            pass = null;
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
    
    /* Code for comparing Int values in streams
        Iterator<Integer> i1 = num.iterator();
        Iterator<Integer> i2 = infinite.iterator();        
        while(i1.hasNext()){
            Integer num1 = i1.next();
            Integer num2 = i2.next();
            if(!num1.equals(num2)){System.out.println(num2);break;}
        }
     */
}
