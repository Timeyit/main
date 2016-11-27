package timeydesktop;

import java.util.List;

public interface ITimeyDB
{
	boolean Initialize();
	List<TimeLog> GetAllTimeLogs();
	List<TimeLog> GetUnsyncedTimeLogs();
	List<TimeLog> GetSyncedTimeLogs();
	boolean UpdateTimeLog(TimeLog timeLog);
	boolean AddTimeLog(TimeLog timeLog);
	boolean DeleteTimeLog(int LogID);
}
