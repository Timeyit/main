package timeydesktop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeyLog {

	public static void LogInfo(String logMessage)
	{
		LogEntry entry = new LogEntry("info", logMessage);
		AddLogEntry(entry);
		TimeyEngine.TimeyDBHelper.AddLogEntry(entry);
	}
	
	public static void LogWarning(String logMessage)
	{
		LogEntry entry = new LogEntry("warning", logMessage);
		AddLogEntry(entry);
		TimeyEngine.TimeyDBHelper.AddLogEntry(entry);
	}
	
	public static void LogSevere(String logMessage)
	{
		LogEntry entry = new LogEntry("severe", logMessage);
		AddLogEntry(entry);
		TimeyEngine.TimeyDBHelper.AddLogEntry(entry);
	}
	
	public static void LogException(String logMessage, Exception ex)
	{
		logMessage = logMessage + ". Exception: " + ex.toString() + " - " +ex.getMessage() + " - " + ex.getStackTrace();
		LogEntry entry = new LogEntry("severe", logMessage);
		AddLogEntry(entry);
		TimeyEngine.TimeyDBHelper.AddLogEntry(entry);
	}
	
	public static void LogFine(String logMessage)
	{
		LogEntry entry = new LogEntry("fine", logMessage);
		AddLogEntry(entry);
		TimeyEngine.TimeyDBHelper.AddLogEntry(entry);
	}
	
	private static void AddLogEntry(LogEntry entry)
	{
		// This block configure the logger with handler and formatter  
        String dateTimestr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String datestr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String logfileFolderPath = System.getenv("APPDATA") + "\\Timey\\";
        File logfileFolder = new File(logfileFolderPath);
        String logfilePath = logfileFolderPath + "Timeylog_" + datestr + ".log";
        File logfile = new File(logfilePath);
        
        String msg = dateTimestr + " - " + entry.LogLevel + " - " + entry.LogMessage + "\n";
        System.out.print(msg);
        try {
        	if(!logfileFolder.exists())
            {
        		logfileFolder.mkdir();
            }
        	
        	if(!logfile.exists())
            {
        		logfile.createNewFile();
            }
        	
            Files.write(Paths.get(logfilePath), msg.getBytes(), StandardOpenOption.APPEND);
            
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
	}
}
