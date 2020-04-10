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
    private static TreeMap<Integer,String> cities = null;
    private static TreeMap<Integer,String> countries = null;
    
    
    // <editor-fold defaultstate="collapsed" desc="Appointment methods">
    public static boolean Insert(Appointment appt, String user) throws SQLException //insert method for appointments
    {
        try{         
            int userID = -1, custID = -1;
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT customer.customerId FROM customer WHERE customerName = ?;");
            prepstatement.setString(1,appt.getName());
            results = prepstatement.executeQuery();
            if(results == null) throw new NullPointerException();            
            if(results.next()) {
                custID = results.getInt("customerId"); 
            }
            results = null;
            prepstatement = conn.prepareStatement("SELECT user.userId FROM user WHERE userName = ?;");
            prepstatement.setString(1,user);
            results = prepstatement.executeQuery();
            if(results == null) throw new NullPointerException();
            if(results.next()) {
                userID = results.getInt("userId");
            }
            results = null;
            prepstatement = conn.prepareStatement("INSERT INTO appointment (customerId, userId, "
                    + "title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdate, lastUpdateBy) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
            prepstatement.setInt(1,custID);
            prepstatement.setInt(2,userID);
            prepstatement.setString(3,appt.getTitle());
            prepstatement.setString(4,appt.getDesc());
            prepstatement.setString(5,appt.getLoc());
            prepstatement.setString(6,appt.getContact());
            prepstatement.setString(7,appt.getType());
            prepstatement.setString(8,appt.getURL());
            prepstatement.setTimestamp(9, Timestamp.valueOf(appt.getStart().atZone(ZoneId.of(ZoneId.systemDefault().toString())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            prepstatement.setTimestamp(10, Timestamp.valueOf(appt.getEnd().atZone(ZoneId.of(ZoneId.systemDefault().toString())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));
            prepstatement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));  //createDate timestamp
            prepstatement.setString(12, user);                                      //on Insert, create == update, this value not to be changed in Update function
            prepstatement.setTimestamp(13, new Timestamp(System.currentTimeMillis()));  //lastUpdate timestamp
            prepstatement.setString(14, user);                                      //on Insert, create == update
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
    
    public static boolean Delete(Integer apptID) throws SQLException //delete method for appointments
    {
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("DELETE FROM appointment WHERE appointmentId = ?;");
            prepstatement.setInt(1, apptID);
            prepstatement.execute();
            return true;
        }
        catch(SQLException e){
            return false;
        }
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    
    public static boolean Update(Appointment appt, String user) throws SQLException //update method for appointments
    {
        try{
            int userID = -1, custID = -1;
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT customer.customerId FROM customer WHERE customerName = ?;");
            prepstatement.setString(1,appt.getName());
            results = prepstatement.executeQuery();
            if(results == null) throw new NullPointerException();            
            if(results.next()) {
                custID = results.getInt("customerId"); 
            }
            results = null;
            prepstatement = conn.prepareStatement("SELECT user.userId FROM user WHERE userName = ?;");
            prepstatement.setString(1,user);
            results = prepstatement.executeQuery();
            if(results == null) throw new NullPointerException();
            if(results.next()) {
                userID = results.getInt("userId");
            }
            results = null;
            prepstatement = conn.prepareStatement("UPDATE appointment "
                    + "SET customerId = ?, userId = ?, title = ?, description = ?, location = ?, contact = ?, type = ?, url = ?, start = ?, end = ?, lastUpdate = ?, lastUpdateBy = ?"
                    + "WHERE appointmentId = ?;");
            prepstatement.setInt(1, custID);
            prepstatement.setInt(2, userID);
            prepstatement.setString(3, appt.getTitle());
            prepstatement.setString(4, appt.getDesc());
            prepstatement.setString(5, appt.getLoc());
            prepstatement.setString(6, appt.getContact());
            prepstatement.setString(7, appt.getType());
            prepstatement.setString(8, appt.getURL());
            prepstatement.setTimestamp(9, Timestamp.valueOf(appt.getStart().atZone(ZoneId.of(ZoneId.systemDefault().toString())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));  //from WGU code repo
            prepstatement.setTimestamp(10, Timestamp.valueOf(appt.getEnd().atZone(ZoneId.of(ZoneId.systemDefault().toString())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()));  //from WGU code repo
            prepstatement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(12,user);
            prepstatement.setInt(13, appt.getApptID());
            int rows = prepstatement.executeUpdate();
            return true;
        }
        catch(SQLException e){
            return false;
        }
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Customer methods">
    public static boolean Insert(Customer cust, String user) throws SQLException{
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT * FROM address "
                    + "WHERE address = ? AND address2 = ? AND cityId = (SELECT cityId FROM city WHERE city = ?) AND postalCode = ? AND phone = ?;");

                    /*"INSERT INTO address "
                    + "(address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES "
                    + "SELECT * FROM (?,?,(SELECT cityId FROM city WHERE city = ?),?,?,?,?,?,?) AS temp "
                    + "WHERE NOT EXISTS(SELECT address, address, cityId, postalCode, phone FROM address "
                    + "WHERE address = ? AND address2 = ? AND cityId = ? AND postalCode = ? AND phone = ?) LIMIT 1;");*/
                    //+ "VALUES (?,?,(SELECT cityId FROM city WHERE city = ?),?,?,?,?,?,?);");
            prepstatement.setString(1, cust.getAddr()[0]);
            prepstatement.setString(2, cust.getAddr()[1]);
            prepstatement.setString(3, cust.getCity());
            prepstatement.setString(4, cust.getZip());
            prepstatement.setString(5, cust.getPhone());
            results = prepstatement.executeQuery();
            
            if(!results.first()){
                prepstatement = conn.prepareStatement("INSERT INTO address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) "
                        + "VALUES (?,?,(SELECT cityId FROM city WHERE city = ?),?,?,?,?,?,?);");
                prepstatement.setString(1, cust.getAddr()[0]);
                prepstatement.setString(2, cust.getAddr()[1]);
                prepstatement.setString(3, cust.getCity());
                prepstatement.setString(4, cust.getZip());
                prepstatement.setString(5, cust.getPhone());
                prepstatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));  //createDate timestamp
                prepstatement.setString(7, user);                                          //on Insert, create == update, this value not to be changed in Update function
                prepstatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));  //lastUpdate timestamp
                prepstatement.setString(9, user);
                int rows = prepstatement.executeUpdate();
                System.out.println(rows);
            }

            prepstatement = conn.prepareStatement("INSERT INTO customer "
                    + "(customerName,addressId,active,createDate,createdBy,lastUpdate,lastUpdateBy) VALUES "
                    + "(?,(SELECT addressId FROM address WHERE address = ? AND address2 = ? AND phone = ?),?,?,?,?,?);");
            prepstatement.setString(1, cust.getName());
            prepstatement.setString(2, cust.getAddr()[0]);
            prepstatement.setString(3, cust.getAddr()[1]);
            prepstatement.setString(4, cust.getPhone());
            prepstatement.setBoolean(5, cust.getActive());
            prepstatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(7, user);
            prepstatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(9, user);
            prepstatement.execute();
            return true;
        }
        catch(SQLException e){System.out.println(e.getMessage()); return false;}
        finally{            
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
        }
    }    
    
    public static boolean Update(Customer cust, String user)throws SQLException{
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            /*prepstatement = conn.prepareStatement("UPDATE customer, address "
                    + "SET customer.customerName = ?, customer.addressId = (SELECT addressId FROM address WHERE address LIKE ? AND address2 LIKE ? AND phone LIKE ?), "
                    + "customer.active = ?, customer.lastUpdate = ?, customer.lastUpdateBy = ?, "
                    + "address.address = ?, address.address2 = ?, address.cityId = (SELECT cityId FROM city WHERE city = ?), address.postalCode = ?, "
                    + "address.phone = ?, address.lastUpdate = ?, address.lastUpdateBy = ?;");*/
            
            prepstatement = conn.prepareStatement("SELECT * FROM address "
                    + "WHERE address = ? AND address2 = ? AND cityId = (SELECT cityId FROM city WHERE city = ?) AND postalCode = ? AND phone = ?;");

                    /*"INSERT INTO address "
                    + "(address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES "
                    + "SELECT * FROM (?,?,(SELECT cityId FROM city WHERE city = ?),?,?,?,?,?,?) AS temp "
                    + "WHERE NOT EXISTS(SELECT address, address, cityId, postalCode, phone FROM address "
                    + "WHERE address = ? AND address2 = ? AND cityId = ? AND postalCode = ? AND phone = ?) LIMIT 1;");*/
                    //+ "VALUES (?,?,(SELECT cityId FROM city WHERE city = ?),?,?,?,?,?,?);");
            prepstatement.setString(1, cust.getAddr()[0]);
            prepstatement.setString(2, cust.getAddr()[1]);
            prepstatement.setString(3, cust.getCity());
            prepstatement.setString(4, cust.getZip());
            prepstatement.setString(5, cust.getPhone());
            results = prepstatement.executeQuery();
            if(!results.next()){
                prepstatement = conn.prepareStatement("UPDATE address AS addr INNER JOIN address AS a ON addr.addressId = a.addressId "
                        + "SET addr.address = ?, addr.address2 = ?, addr.cityId = (SELECT cityId FROM city WHERE city = ?), addr.postalCode = ?, "
                        + "addr.phone = ?, addr.lastUpdate = ?, addr.lastUpdateBy = ? WHERE addr.addressId = ?;");              //update address            
                prepstatement.setString(1, cust.getAddr()[0]);
                prepstatement.setString(2, cust.getAddr()[1]);
                prepstatement.setString(3, cust.getCity());
                prepstatement.setString(4, cust.getZip());
                prepstatement.setString(5, cust.getPhone());            
                prepstatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                prepstatement.setString(7, user);
                prepstatement.setInt(8, cust.getAddrID());
                int rows = prepstatement.executeUpdate();
            }
            prepstatement = conn.prepareStatement("UPDATE customer AS cust INNER JOIN customer AS c ON cust.customerId = c.customerId "
                    + "SET cust.customerName = ?, cust.addressId = (SELECT addressId FROM address WHERE address = ? AND address2 = ? AND phone = ?), "
                    + "cust.active = ?, cust.lastUpdate = ?, cust.lastUpdateBy = ? WHERE cust.customerId = ?;");              //update customer
            prepstatement.setString(1, cust.getName());
            prepstatement.setString(2, cust.getAddr()[0]);
            prepstatement.setString(3, cust.getAddr()[1]);
            prepstatement.setString(4, cust.getPhone());
            prepstatement.setBoolean(5, cust.getActive());
            prepstatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(7, user);
            prepstatement.setInt(8,cust.getID());
            int rows = prepstatement.executeUpdate();
            return true;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return false;
        }
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    
    public static boolean Delete(Customer cust) throws SQLException{
        try{
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("DELETE FROM customer WHERE customerId = ?;");
            prepstatement.setInt(1, cust.getID());
            int rows = prepstatement.executeUpdate();
            return true;
        }
        catch(SQLException e){
            return false;
        }
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="map returning methods"> 
    public static TreeMap GetAppointments(String user, LocalDateTime start, LocalDateTime end) throws SQLException
    {
        try{
            map = new TreeMap<>();
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT appointment.*, customer.customerName, address.phone "
                    + "FROM appointment INNER JOIN "
                    + "(customer INNER JOIN address ON customer.addressId = address.addressId) "
                    + "ON appointment.customerId = customer.customerId INNER JOIN user ON appointment.userId = user.userId "
                    + "WHERE appointment.userId = (SELECT userId FROM user WHERE userName = ?) "
                    + "AND appointment.start >= ? "
                    + "AND appointment.end <= ? ;");
            prepstatement.setString(1,user);
            prepstatement.setTimestamp(2, Timestamp.valueOf(start));
            prepstatement.setTimestamp(3, Timestamp.valueOf(end));
            results = prepstatement.executeQuery();
            while(results.next()){
                Appointment appt = new Appointment(results.getInt("appointmentId"),results.getInt("userId"),results.getString("customerName"),results.getString("title"),results.getString("description"),
                        results.getString("location"), results.getString("contact"),results.getString("type"),results.getString("url"),
                        results.getTimestamp("start").toLocalDateTime().atZone(ZoneId.of("UTC")),results.getTimestamp("end").toLocalDateTime().atZone(ZoneId.of("UTC")));
                map.put(appt.getApptID(),appt);
            }
            return map;
        }
        catch(SQLException e){System.out.println(e.getMessage());return null;}
        finally{
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    
    public static TreeMap GetAppointments() throws SQLException
    {
        try{
            map = new TreeMap<>();
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT appointment.*, customer.customerName, address.phone, user.userName "
                    + "FROM appointment INNER JOIN "
                    + "(customer INNER JOIN address ON customer.addressId = address.addressId) "
                    + "ON appointment.customerId = customer.customerId INNER JOIN user ON appointment.userId = user.userId;");
            results = prepstatement.executeQuery();
            while(results.next()){
                Appointment appt = new Appointment(results.getInt("appointmentId"),results.getInt("userId"),results.getString("userName"),
                        results.getString("customerName"),results.getString("title"),results.getString("description"),
                        results.getString("location"), results.getString("contact"),results.getString("type"),results.getString("url"),
                        results.getTimestamp("start").toLocalDateTime().atZone(ZoneId.of("UTC")),results.getTimestamp("end").toLocalDateTime().atZone(ZoneId.of("UTC")));
                map.put(appt.getApptID(),appt);
            }
            return map;
        }
        catch(SQLException e){System.out.println(e.getMessage());return null;}
        finally{
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
                Customer cust =  new Customer(results.getInt("customerId"),results.getString("customerName"),results.getInt("addressId"),
                results.getString("address"),results.getString("address2"),results.getBoolean("active"),results.getString("postalCode"),
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
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    
    public static TreeMap GetCiCo() throws SQLException{
        try{
            cities = new TreeMap<>();
            countries = new TreeMap<>();
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT city.cityId, city.city, country.countryId, country.country "
                    + "FROM city INNER JOIN country ON city.countryId = country.countryId;");
            results = prepstatement.executeQuery();
            if(results == null) return null;
            while(results.next()){
                cities.put(results.getInt("cityId"), results.getString("city"));
                countries.put(results.getInt("countryId"),results.getString("country"));                
            }
            TreeMap<Integer,TreeMap> arr = new TreeMap<>();
            arr.put(0,cities);
            arr.put(1,countries);
            return arr;
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            return null;
        }
        finally{
            cities = null;
            countries = null;
            if(results != null) results.close();
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
            ds = null;
        }
    }
    // </editor-fold>
    
    // password method for login form
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
            ds = null;
        }
        return pass;
    }
    
    // method to repopulate a purged DB with default information
    public static void PurgeAddr() throws SQLException{
        try{    
            ds = DataSource.getInstance();
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("INSERT INTO country (country, createDate, createdBy, lastUpdate,lastUpdateBy) VALUES"
                                                + "(?,?,?,?,?),(?,?,?,?,?);");
            prepstatement.setString(1, "USA");
            prepstatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(3, "test");
            prepstatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(5, "test");
            prepstatement.setString(6, "England");
            prepstatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(8, "test");
            prepstatement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(10, "test");
            prepstatement.executeUpdate();
            prepstatement = conn.prepareStatement("INSERT INTO city (city, countryId, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES"
                                                + "(?,(SELECT countryId FROM country WHERE country = ?),?,?,?,?),"
                                                + "(?,(SELECT countryId FROM country WHERE country = ?),?,?,?,?),"
                                                + "(?,(SELECT countryId FROM country WHERE country = ?),?,?,?,?);");
            prepstatement.setString(1, "Phoenix");
            prepstatement.setString(2, "USA");
            prepstatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(4, "test");
            prepstatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(6, "test");
            prepstatement.setString(7, "New York");
            prepstatement.setString(8, "USA");
            prepstatement.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(10, "test");
            prepstatement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(12, "test");
            prepstatement.setString(13, "London");
            prepstatement.setString(14, "England");
            prepstatement.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(16, "test");
            prepstatement.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(18, "test");
            prepstatement.executeUpdate();
            prepstatement = conn.prepareStatement("INSERT INTO user (userName, password, active, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES"
                                                + "(?,?,?,?,?,?,?)");
            prepstatement.setString(1, "test");
            prepstatement.setString(2, "test");
            prepstatement.setBoolean(3, true);
            prepstatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(5, "test");
            prepstatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            prepstatement.setString(7, "test");
            prepstatement.executeUpdate();
            prepstatement = conn.prepareStatement("SELECT * FROM address;");
            results = prepstatement.executeQuery();
            while(results.next()){System.out.println(results.getString("addressId") + " " + results.getString("address") + " " + results.getString("phone"));}
            
        }
        catch(SQLException e){System.out.println(e.getMessage());}
        finally{
            if(prepstatement != null) prepstatement.close();
            if(conn != null) conn.close();
        }
    }
    


}
