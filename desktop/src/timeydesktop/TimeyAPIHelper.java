package timeydesktop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.Instant;
import org.joda.time.Interval;

public class TimeyAPIHelper {

	public static String GetLatestVersion() {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpGet request = new HttpGet(TimeyEngine.ApiBase + "version.php");

			// Execute and get the response.
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String jsonData = IOUtils.toString(instream, "UTF-8");
				instream.close();
				TimeyLog.LogFine("Got data: " + jsonData);

				return jsonData;
			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to get latest Timey version", ex);
		}
		return "0.0";
	}
	
	public static String OpenSession() {
		try {
			TimeyLog.LogInfo("Opening Session");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(TimeyEngine.ApiBase + "session_createSession.php");

			// Request parameters and other properties.
			String requestJSON = "{\"username\":\"" + TimeyEngine.config.getUsername() + "\",\"password\":\"" + TimeyEngine.config.getPassword()
					+ "\"} ";
			TimeyLog.LogFine("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String responseData = IOUtils.toString(instream, "UTF-8");
				instream.close();
				System.out.println("Response: " + responseData);
				if (responseData.contains("fail")) {
					return null;
				} else {
					TimeyEngine.SessionKey = responseData;
					return responseData;
				}

			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to open Timey session", ex);
		}
		return null;
	}
	
	public static boolean RefreshSession() {
		try {
			TimeyLog.LogInfo("Refresing Session");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(TimeyEngine.ApiBase + "session_refreshSession.php");

			// Request parameters and other properties.
			String requestJSON = "{\"sessionkey\":\"" + TimeyEngine.SessionKey + "\"} ";
			TimeyLog.LogFine("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String responseData = IOUtils.toString(instream, "UTF-8");
				instream.close();
				System.out.println("Response: " + responseData);
				if (responseData.contains("OK")) {
					return true;
				} else {
					return false;
				}

			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to refresh Timey session", ex);
		}
		return false;
	}

	
	public static List<WorkItem> GetWorkItems() {
		try {
			// System.out.println("Getting work items");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(TimeyEngine.ApiBase + "workItem_getAll.php");

			// Request parameters and other properties.
			StringEntity params = new StringEntity("{\"sessionkey\":\"" + TimeyEngine.SessionKey + "\"} ");
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String jsonData = IOUtils.toString(instream, "UTF-8");
				instream.close();
				// System.out.println("Got data: " + jsonData);
				ObjectMapper mapper = new ObjectMapper();
				List<WorkItem> workItems = mapper.readValue(jsonData, new TypeReference<List<WorkItem>>() {
				});
				// System.out.println("Got : " + workItems.size() + " work
				// items" );
				return workItems;
			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to get work items", ex);
		}
		return new ArrayList<WorkItem>();
	}

	public static boolean UploadTrackedTime() {

		Interval timeSinceStart = new Interval(TimeyEngine.TrackingStartTime, new Instant());
		long durationSinceStartTracking = timeSinceStart.toDuration().getStandardSeconds();
		
		long currentDuration = TimeyEngine.TrackedItem.GetDurationLong();
		long unsyncedDuration = 0;
		List<TimeLog> unsyncedTimeLogs = TimeyEngine.TimeyDBHelper.GetUnsyncedTimeLogs();
		// Get all unsynced TimeLogs (never miss a beat)
		for(TimeLog timeLog : unsyncedTimeLogs)
		{
			unsyncedDuration = unsyncedDuration + timeLog.TimeDifference;
		}
		
		long duration = currentDuration + unsyncedDuration;
		//Interval timeSinceLastSync = new Interval(TimeyEngine.TrackingLastSync, new Instant());
		//long durationSinceLastSync = timeSinceLastSync.toDuration().getStandardSeconds();

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update work item main
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		try {
			TimeyLog.LogInfo("Uploading tracked time (Main)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(TimeyEngine.ApiBase + "workItem_update.php");

			// Request parameters and other properties.
			String requestJSON = "{" + "\"idworkItem\":\"" + TimeyEngine.TrackedItem.idworkItem + "\"," + "\"duration\":" + "\""
					+ Long.toString(duration) + "\"" + "," + "\"sessionkey\":\"" + TimeyEngine.SessionKey + "\"" + "} ";
			TimeyLog.LogFine("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String jsonData = IOUtils.toString(instream, "UTF-8");
				instream.close();
				
				if(jsonData.equals("OK"))
				{
					// If update is ok. Marked items as synced
					for(TimeLog timeLog : unsyncedTimeLogs)
					{
						timeLog.IsSynced = true;
						TimeyEngine.TimeyDBHelper.UpdateTimeLog(timeLog);
					}
				}
				else
				{
					TimeyLog.LogFine("Failed to update work item. Response JSON: " + jsonData);
				}
			}
			
			
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to update work item time", ex);
			return false;
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update lap tracking
		// TODO: Use the database records for this instead
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try {

			TimeyLog.LogInfo("Uploading tracked time (LAP)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(TimeyEngine.ApiBase + "timeLog_updateCreate.php");

			// Request parameters and other properties.
			String requestJSON = "{" + "\"idworkItem\":\"" + TimeyEngine.TrackedItem.idworkItem + "\"," + "\"durationLap\":" + "\""
					+ Long.toString(durationSinceStartTracking) + "\"" + "," + "\"idTimeLog\":" + "\""
					+ Long.toString(TimeyEngine.IDTimeLog) + "\"" + "," + "\"sessionkey\":\"" + TimeyEngine.SessionKey + "\"" + "} ";
			TimeyLog.LogFine("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String jsonData = IOUtils.toString(instream, "UTF-8");
				if (TimeyEngine.IDTimeLog < 0) {
					// BAD SOLUTION FOR DESERIALIZING, BUT LETS USE FOR NOW
					String timeIdString = jsonData.replace("[{\"myid\":\"", "").replace("\"}]", "");
					TimeyEngine.IDTimeLog = Long.parseLong(timeIdString);
				}
				instream.close();
				TimeyLog.LogFine("Response JSON: " + jsonData);
			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to update lap time tracking", ex);
			return false;
		}

		return true;
	}

}
