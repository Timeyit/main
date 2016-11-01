package timeydesktop;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TimeyLog {

	public static void LogInfo(String logMessage)
	{
		AddLogEntry(logMessage, "info");
	}
	
	public static void LogWarning(String logMessage)
	{
		AddLogEntry(logMessage, "warning");
	}
	
	public static void LogSevere(String logMessage)
	{
		AddLogEntry(logMessage, "severe");
	}
	
	public static void LogException(String logMessage, Exception ex)
	{
		logMessage = logMessage + ". Exception: " + ex.toString() + " - " +ex.getMessage() + " - " + ex.getStackTrace();
		AddLogEntry(logMessage, "severe");
	}
	
	public static void LogFine(String logMessage)
	{
		AddLogEntry(logMessage, "fine");
	}
	
	private static void AddLogEntry(String logMessage, String logType)
	{
		Logger logger = Logger.getLogger("logfile.log");  
	    FileHandler fh;  

	    try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler(".log");  
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  

	        // the following statement is used to log any messages 
	        if(logType.equals("info"))
	        {
	        	logger.info(logMessage);  
	        }
	        else if(logType.equals("warning"))
	        {
	        	logger.warning(logMessage);
	        }
	        else if(logType.equals("severe"))
	        {
	        	logger.severe(logMessage);
	        }
	        else
	        {
	        	logger.fine(logMessage);
	        }

	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  

	}
}
