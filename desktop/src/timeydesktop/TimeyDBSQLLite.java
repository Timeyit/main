package timeydesktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class TimeyDBSQLLite implements ITimeyDB {

	public static String TimeyDBFile = TimeyConfig.TimeyDirectory + "timeydb_sql.db"; 
	
	public boolean Initialize()
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      //public String LogID;
	  	  //public String WorkItem;
	  	  //public long TimeDifference;
	  	  //public String Timestamp;
	  	  //public boolean IsSynced;
	      String sql = "CREATE TABLE IF NOT EXISTS TimeLog " +
	                   "(LogID          INTEGER  PRIMARY KEY AUTOINCREMENT," +
	                   " WorkItem       TEXT     NOT NULL, " + 
	                   " TimeDifference INT      NOT NULL, " + 
	                   " Timestamp      DATETIME NOT NULL, " + 
	                   " IsSynced       BOOLEAN  NOT NULL " +
	                   ")"; 

	      stmt.executeUpdate(sql);
	      
	      sql = "CREATE TABLE IF NOT EXISTS Logs " +
                  "(LogID          INTEGER  PRIMARY KEY AUTOINCREMENT," +
                  " LogLevel       TEXT     NOT NULL, " + 
                  " LogMessage        TEXT     NOT NULL, " + 
                  " Timestamp      DATETIME NOT NULL " + 
                  ")"; 

	      stmt.executeUpdate(sql);
     
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    System.out.println("Table created successfully");
	    return true;
	}
	
	public List<TimeLog> GetAllTimeLogs()
	{
		return GetTimeLogs("");
	}
	
	public List<TimeLog> GetUnsyncedTimeLogs()
	{
		return GetTimeLogs("WHERE IsSynced=0");
	}
	
	public List<TimeLog> GetSyncedTimeLogs()
	{
		return GetTimeLogs("WHERE IsSynced=1");
	}
	
	private List<TimeLog> GetTimeLogs(String filter)
	{
		List<TimeLog> timeLogs = new ArrayList<TimeLog>();
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM TimeLog " + filter + ";" );
	      while ( rs.next() ) {
	         // Parse SQL Results
	    	 int logID = rs.getInt("LogID");
	         String  workItem = rs.getString("WorkItem");
	         long timeDifference  = rs.getInt("TimeDifference");
	         DateTime  timestamp = new DateTime(rs.getTimestamp("Timestamp"));
	         boolean isSynced = rs.getBoolean("IsSynced");
	         // Create TimeLog item
	         TimeLog timelog = new TimeLog(logID, workItem, timeDifference, timestamp, isSynced);
	         timeLogs.add(timelog);
	         System.out.println( "LogID = " + timelog.LogID );
	         System.out.println( "WorkItem = " + timelog.WorkItem );
	         System.out.println( "TimeDifference = " + timelog.TimeDifference );
	         System.out.println( "Timestamp = " + timelog.Timestamp );
	         System.out.println( "IsSynced = " + timelog.IsSynced );
	         System.out.println();
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return timeLogs;
	    }
	    System.out.println("Operation done successfully");
	    return timeLogs;
	}
	
	public boolean UpdateTimeLog(TimeLog timeLog)
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");
	      
	      int isSyncedInt = (timeLog.IsSynced) ? 1 : 0;
	      Timestamp timeStamp = new Timestamp(timeLog.Timestamp.getMillis());
	      
	      stmt = c.createStatement();
	      String sql = "UPDATE TimeLog SET " +
	      "WorkItem = '" + timeLog.WorkItem + "', " + 
	      "TimeDifference = " + timeLog.TimeDifference + ", " + 
	      "Timestamp = '" + timeStamp + "', " + 
	      "IsSynced = " + isSyncedInt + " " + 
	      " WHERE LogID=" +  timeLog.LogID + ";";
	      stmt.executeUpdate(sql);
	      c.commit();

	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return true;
	    }
	    System.out.println("Operation done successfully");
	    return false;
	}
	
	public boolean AddTimeLog(TimeLog timeLog)
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      DateTime dateTime = new DateTime();
	      Timestamp timeStamp = new Timestamp(dateTime.getMillis());
	      
	      int isSyncedInt = (timeLog.IsSynced) ? 1 : 0;
	      
	      String sql = "INSERT INTO TimeLog (WorkItem,TimeDifference,Timestamp,IsSynced) " +
	                   "VALUES (" + 
	                   		"'" + timeLog.WorkItem + "'" + ", " +
	                   		timeLog.TimeDifference + ", " +
	                   		"'" + timeStamp.toString() + "'" + ", " +
	                   		isSyncedInt +
	                   		");"; 
	      stmt.executeUpdate(sql);

	      stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return false;
	    }
	    System.out.println("Records created successfully");
	    return true;
	}
	
	public boolean DeleteTimeLog(int LogID)
	{
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      String sql = "DELETE from TimeLog where ID=" + LogID + ";";
	      stmt.executeUpdate(sql);
	      c.commit();

	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return false;
	    }
	    System.out.println("Operation done successfully");
	    return true;
	}

	public List<LogEntry> GetLogEntries(String filter) {
		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM Logs " + filter + ";" );
	      while ( rs.next() ) {
	         // Parse SQL Results
	    	 int logID = rs.getInt("LogID");
	         String  logLevel = rs.getString("LogLevel");
	         String  logMessage = rs.getString("LogMessage");
	         DateTime  timestamp = new DateTime(rs.getTimestamp("Timestamp"));
	         // Create TimeLog item
	         LogEntry logEntry = new LogEntry(logID, logLevel, logMessage, timestamp);
	         logEntries.add(logEntry);
	         System.out.println( "LogID = " + logEntry.LogID );
	         System.out.println( "LogLevel = " + logEntry.LogLevel );
	         System.out.println( "LogLevel = " + logEntry.LogLevel );
	         System.out.println( "Timestamp = " + logEntry.Timestamp );
	         System.out.println();
	      }
	      rs.close();
	      stmt.close();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return logEntries;
	    }
	    System.out.println("Operation done successfully");
	    return logEntries;
	}

	@Override
	public boolean AddLogEntry(LogEntry entry) {
		Connection c = null;
	    Statement stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + TimeyDBFile);
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      Timestamp timeStamp = new Timestamp(entry.Timestamp.getMillis());
	      	      
	      String sql = "INSERT INTO Logs (LogLevel,LogMessage,Timestamp) " +
	                   "VALUES (" + 
	                   		"'" + entry.LogLevel + "'" + ", " +
	                   		"'" + entry.LogMessage + "'" + ", " +
	                   		"'" + timeStamp.toString() + "'" +
	                   		");"; 
	      stmt.executeUpdate(sql);

	      stmt.close();
	      c.commit();
	      c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return false;
	    }
	    System.out.println("Records created successfully");
	    return true;
	}
}
