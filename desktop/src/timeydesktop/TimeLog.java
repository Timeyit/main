package timeydesktop;

import java.util.Random;

import org.joda.time.DateTime;

public class TimeLog {

	public static String Delimiter = ";";
	
	public TimeLog(String workItem, long timeDifference, DateTime timestamp, boolean isSynced)
	{
		Random randomGenerator = new Random();
		LogID = randomGenerator.nextInt(30000);
		WorkItem = workItem;
		TimeDifference = timeDifference;
		Timestamp = timestamp;
		IsSynced = isSynced;
	}
	
	public TimeLog(int logID, String workItem, long timeDifference, DateTime timestamp, boolean isSynced)
	{
		LogID = logID;
		WorkItem = workItem;
		TimeDifference = timeDifference;
		Timestamp = timestamp;
		IsSynced = isSynced;
	}
	
	
	public int LogID;
	public String WorkItem;
	public long TimeDifference;
	public DateTime Timestamp;
	public boolean IsSynced;
	
	public String ToCSVString()
	{
		String retString = 
				LogID + Delimiter +
				WorkItem + Delimiter +
				Long.toString(TimeDifference) + Delimiter + 
				Timestamp + Delimiter + 
				Boolean.toString(IsSynced) + Delimiter;
		return retString;
	}
}
