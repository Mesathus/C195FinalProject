
package c195finalproject;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
/**
 *
 * @author Mesa
 */
public class DataSource {
    private static final String sqlConnect = "jdbc:mysql://3.227.166.251:3306/U062a2";
    private static DataSource ds;
    private final static String user = "U062a2";
    private final static String pass = "53688672962";
    private final MysqlDataSource myDS = new MysqlDataSource();
    
    private DataSource(){
        myDS.setURL(sqlConnect);
        myDS.setUser(user);
        myDS.setPassword(pass);
    }
    public static DataSource getInstance(){
        if(ds == null) ds = new DataSource();
        return ds;
    }
    public MysqlDataSource getMDS(){
        return myDS;
    }
    
}
