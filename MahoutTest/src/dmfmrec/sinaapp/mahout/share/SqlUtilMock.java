package dmfmrec.sinaapp.mahout.share;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlUtilMock
{
    // keep the same value
    public static String dbDriver = "com.mysql.jdbc.Driver";

    // change this for sae
    public static String dbUrl = "jdbc:mysql://localhost:3306/app_dm302?useUnicode=true&characterEncoding=utf-8";

    public static String user = "root";

    public static String password = "";

    private static SqlUtilMock instance = null;

    private SqlUtilMock()
    {
    }

    public static SqlUtilMock getInstance()
    {
        if (instance == null)
        {
            instance = new SqlUtilMock();
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
