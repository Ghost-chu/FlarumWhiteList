package org.maxgamer.quickshop.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Engine from QSRR
public class Database
{
  private DatabaseCore core;
  
  public Database(DatabaseCore core)
    throws Database.ConnectionException
  {
    try
    {
      try
      {
        if (!core.getConnection().isValid(10)) {
          throw new ConnectionException("Database doesn not appear to be valid!");
        }
      }
      catch (AbstractMethodError localAbstractMethodError) {}
      this.core = core;
    }
    catch (SQLException e)
    {
      throw new ConnectionException(e.getMessage());
    }
  }
  
  public DatabaseCore getCore()
  {
    return this.core;
  }
  
  public Connection getConnection()
  {
    return this.core.getConnection();
  }
  
  public void execute(String query, Object... objs)
  {
    BufferStatement bs = new BufferStatement(query, objs);
    this.core.queue(bs);
  }
  
  public boolean hasTable(String table)
    throws SQLException
  {
    ResultSet rs = getConnection().getMetaData().getTables(null, null, "%", null);
    while (rs.next()) {
      if (table.equalsIgnoreCase(rs.getString("TABLE_NAME")))
      {
        rs.close();
        return true;
      }
    }
    rs.close();
    return false;
  }
  
  public void close()
  {
    this.core.close();
  }
  
  public boolean hasColumn(String table, String column)
    throws SQLException
  {
    if (!hasTable(table)) {
      return false;
    }
    String query = "SELECT * FROM " + table + " LIMIT 0,1";
    try
    {
      PreparedStatement ps = getConnection().prepareStatement(query);
      ResultSet rs = ps.executeQuery();
      if (rs.next())
      {
        rs.getString(column);
        
        return true;
      }
    }
    catch (SQLException e)
    {
      return false;
    }
    return false;
  }
  
  public static class ConnectionException
    extends Exception
  {
    private static final long serialVersionUID = 8348749992936357317L;
    
    public ConnectionException(String msg)
    {
      super();
    }
  }
}
