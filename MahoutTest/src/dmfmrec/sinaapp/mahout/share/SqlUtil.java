package dmfmrec.sinaapp.mahout.share;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlUtil
{

    // keep the same value
    public static String dbDriver = "com.mysql.jdbc.Driver";

    // change this for sae
    public static String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_dm302?useUnicode=true&characterEncoding=utf-8";

    public static String user = "4mj5oz1jyy";

    public static String password = "3lwyk0lh015y4i1kzy0j0yih1zk3x4wzh0kzm3y5";

    private static SqlUtil instance = null;

    private SqlUtil()
    {
    }

    public static SqlUtil getInstance()
    {
        if (instance == null)
        {
            instance = new SqlUtil();
            try
            {
                Class.forName(dbDriver);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public Connection getConnection()
    {
        Connection con = null;
        try
        {
            con = DriverManager.getConnection(dbUrl, user, password);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return con;
    }
}
