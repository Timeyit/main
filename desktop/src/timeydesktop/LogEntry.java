package timeydesktop;

import org.joda.time.DateTime;

public class LogEntry {

	public LogEntry(String logLevel, String logMessage)
	{
		LogLevel = logLevel;
		LogMessage = logMessage;
		Timestamp = DateTime.now();
		LogID = -1;
	}
	
	public LogEntry(String logLevel, String logMessage, DateTime timestamp)
	{
		LogLevel = logLevel;
		LogMessage = logMessage;
		Timestamp = timestamp;
		LogID = -1;
	}
	
	public LogEntry(int logID, String logLevel, String logMessage, DateTime timestamp)
	{
		LogLevel = logLevel;
		LogMessage = logMessage;
		Timestamp = timestamp;
		LogID = logID;
	}
	
	public int LogID;
	public String LogLevel;
	public String LogMessage;
	public DateTime Timestamp;
}
