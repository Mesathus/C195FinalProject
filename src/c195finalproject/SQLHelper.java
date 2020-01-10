/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;
import java.sql.*;
import java.lang.StringBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
/**
 *
 * @author Mesa
 */
public class SQLHelper{
    private static DataSource ds;
    //private static DataSource data = null;
    private static Connection conn = null;
    private static PreparedStatement prepstatement = null;
    private static ResultSet results = null;
    /*
        Inserting appointments requires
    */
    public static boolean Insert() throws SQLException //insert method for appointments
    {
        try{            
            ds.getInstance();
            return true;
        }
        catch(Exception e){
            e.getMessage();
            return false;
        }
        finally{
            ds = null;
        }
    }
    
    public static boolean Delete() throws SQLException //delete method for appointments
    {return false;}
    
    public static boolean Update() throws SQLException //update method for appointments
    {return false;}
    
    public static StringBuilder GetPass(String inputName) throws SQLException
    {
        StringBuilder pass = new StringBuilder();
        try{
            ds = DataSource.getInstance();
            System.out.println(ds);
            conn = ds.getMDS().getConnection();
            prepstatement = conn.prepareStatement("SELECT password from user WHERE userName = ?");
            prepstatement.setString(1, inputName);
            results = prepstatement.executeQuery();
            if(results.next()) pass.insert(0,results.getString(1));
            return pass;
        }
        catch(SQLException e)
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
