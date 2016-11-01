package timeydesktop;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.style.theme.LightDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

public class TimeyEngine {

	private static TimeyEngine instance = null;
	public static List<WorkItem> WorkItems = new ArrayList<WorkItem>();
	public static TimeyOptions Options = new TimeyOptions();
	public static boolean IsTracking = false;
	public static boolean ReminderPopupClosed = false;
	public static WorkItem TrackedItem = new WorkItem();
	public static int NumAlarams = 0;
	private static Timer trackTimer = new Timer();
	public static Instant TrackingStartTime = new Instant(Long.MIN_VALUE);
	public static Instant TrackingLastSync = new Instant(Long.MIN_VALUE);
	public static TimeyConfig config = new TimeyConfig();
	public static String SessionKey = null;
	public static String TimeyBase = "http://timey.it";
	public static String ApiBase = TimeyBase + "/PHP/";
	public static long IDTimeLog = -1;

	protected TimeyEngine() {
		// Exists only to defeat instantiation.
	}

	public static TimeyEngine getInstance() {
		if (instance == null) {
			instance = new TimeyEngine();
		}
		return instance;
	}

	public void StopTracking() {
		TimeyLog.LogInfo("StopTracking");
		TrackedItem = new WorkItem();
		NumAlarams = 0;
		IDTimeLog = -1;
		TrackingStartTime = new Instant(Long.MIN_VALUE);
		TrackingLastSync = new Instant(Long.MIN_VALUE);
		IsTracking = false;
		trackTimer.cancel();
		try {
			if (config.getShowNoTrackingNotifications()) {
				TimeyEngine.ReminderPopupClosed = true;
				TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						ShowNoTrackingReminderPopup();
					}
				};

				trackTimer = new Timer();
				try {
					trackTimer.scheduleAtFixedRate(timerTask, config.getNoTrackingNotificationDelay() * 1000,
							config.getNoTrackingNotificationDelay() * 1000);
				} catch (IOException e) {
					TimeyLog.LogException("Failed to start track remainder popup", e);
				}
			}
		} catch (IOException e) {
			TimeyLog.LogException("Failed to stop tracking", e);
		}

		TimeyDesktop.PopulateTrackMenu();
	}

	public void KeepTracking() {
		TimeyLog.LogInfo("Keep Tracking");
		NumAlarams = NumAlarams + 1;
		IsTracking = true;
	}

	public void StartTracking(String idWorkItem) {
		TimeyLog.LogInfo("Start Tracking");
		for (Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext();) {
			final WorkItem item = i.next();
			if (item.idworkItem.equals(idWorkItem)) {
				TrackedItem = item;
			}
		}

		try {
			config.setLastTracked(TrackedItem.idworkItem);
		} catch (IOException e) {
			TimeyLog.LogException("StartTracking: Failed to get configuration", e);
		}
		TimeyLog.LogInfo("Now Tracking: " + TrackedItem.nameWorkItem + " id: " + TrackedItem.idworkItem);
		TrackingStartTime = new Instant();
		TrackingLastSync = new Instant();
		ShowTrackStartPopup(TrackedItem.nameWorkItem);
		IsTracking = true;

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				ShowReminderPopup();
			}
		};

		trackTimer = new Timer();
		try {
			trackTimer.scheduleAtFixedRate(timerTask, config.getTrackNotificationDelay() * 1000,
					config.getTrackNotificationDelay() * 1000);
		} catch (IOException e) {
			TimeyLog.LogException("Failed to start task timer", e);
		}
		TimeyDesktop.PopulateTrackMenu();
	}

	public String GetLatestVersion() {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpGet request = new HttpGet(ApiBase + "version.php");

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

	public String OpenSession() {
		try {
			TimeyLog.LogInfo("Opening Session");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "session_createSession.php");

			// Request parameters and other properties.
			String requestJSON = "{\"username\":\"" + config.getUsername() + "\",\"password\":\"" + config.getPassword()
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
					SessionKey = responseData;
					return responseData;
				}

			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to open Timey session", ex);
		}
		return null;
	}
	
	public boolean RefreshSession() {
		try {
			TimeyLog.LogInfo("Refresing Session");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "session_refreshSession.php");

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

	public void Sync() {
		// Refresh or open session
		if(!RefreshSession())
		{
			OpenSession();
		}
		
		// Proceed with syncing
		if (IsTracking) {
			Interval timeSinceLastSync = new Interval(TrackingLastSync, new Instant());
			Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
			TimeyLog.LogFine("Time Since Last Sync: " + (double) timeSinceLastSync.toDurationMillis() / (double) 1000);
			TimeyLog.LogFine("Time Since Last Start: " + (double) timeSinceStart.toDurationMillis() / (double) 1000);
			if (UploadTrackedTime()) {
				TrackingLastSync = new Instant();
			}

		} else {
			TimeyLog.LogFine("Not tracking time");
		}
		WorkItems = GetWorkItems();
	}

	public List<WorkItem> GetWorkItems() {
		try {
			// System.out.println("Getting work items");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "workItem_getAll.php");

			// Request parameters and other properties.
			StringEntity params = new StringEntity("{\"sessionkey\":\"" + SessionKey + "\"} ");
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

	public boolean UploadTrackedTime() {

		Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
		long duration = timeSinceStart.toDuration().getStandardSeconds();
		duration = duration + TrackedItem.GetDurationLong();

		Interval timeSinceLastSync = new Interval(TrackingLastSync, new Instant());
		long durationSinceLastSync = timeSinceLastSync.toDuration().getStandardSeconds();

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update work item main
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////

		try {
			TimeyLog.LogInfo("Uploading tracked time (Main)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "workItem_update.php");

			// Request parameters and other properties.
			String requestJSON = "{" + "\"idworkItem\":\"" + TrackedItem.idworkItem + "\"," + "\"duration\":" + "\""
					+ Long.toString(duration) + "\"" + "," + "\"sessionkey\":\"" + SessionKey + "\"" + "} ";
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
				TimeyLog.LogFine("Response JSON: " + jsonData);

			}
		} catch (Exception ex) {
			TimeyLog.LogException("Failed to update work item time", ex);
			return false;
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update lap tracking
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try {

			TimeyLog.LogInfo("Uploading tracked time (LAP)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "timeLog_updateCreate.php");

			// Request parameters and other properties.
			String requestJSON = "{" + "\"idworkItem\":\"" + TrackedItem.idworkItem + "\"," + "\"durationLap\":" + "\""
					+ Long.toString(durationSinceLastSync) + "\"" + "," + "\"idTimeLog\":" + "\""
					+ Long.toString(IDTimeLog) + "\"" + "," + "\"sessionkey\":\"" + SessionKey + "\"" + "} ";
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
				if (IDTimeLog < 0) {
					// BAD SOLUTION FOR DESERIALIZING, BUT LETS USE FOR NOW
					String timeIdString = jsonData.replace("[{\"myid\":\"", "").replace("\"}]", "");
					IDTimeLog = Long.parseLong(timeIdString);
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

	public void ShowReminderPopup() {
		TimeyLog.LogInfo("Showing reminder Popup. Item: " + TimeyEngine.TrackedItem.nameWorkItem);
		Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
		int TimeStartSec = (int) Math.round((double) timeSinceStart.toDurationMillis() / (double) 1000);
		// First we define the style/theme of the window.
		// Note how we override the default values
		INotificationStyle style = new DarkDefaultNotification().withWidth(400) // Optional
				.withAlpha(0.9f) // Optional
				;

		// Now lets build the notification
		new NotificationBuilder().withStyle(style) // Required. here we set the
													// previously set style
				.withTitle("Timey") // Required.
				.withMessage("Click message if still working on \"" + TimeyEngine.TrackedItem.nameWorkItem + "\" ("
						+ TimeStartSec + " s).") // Optional
				// .withIcon(new
				// ImageIcon(QuickStart.class.getResource("/twinkle.png"))) //
				// Optional. You could also use a String path
				.withDisplayTime(10000) // Optional
				.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the
													// center of the screen
				.withListener(new NotificationEventAdapter() { // Optional
					public void closed(NotificationEvent event) {
						// System.out.println("closed notification with UUID " +
						// event.getId());
						TimeyEngine.getInstance().StopTracking();
					}

					public void clicked(NotificationEvent event) {
						// System.out.println("clicked notification with UUID "
						// + event.getId());
						TimeyEngine.getInstance().KeepTracking();
					}
				}).showNotification(); // this returns a UUID that you can use
										// to identify events on the listener
	}

	public void ShowNoTrackingReminderPopup() {
		if (TimeyEngine.ReminderPopupClosed) {
			
			TimeyLog.LogInfo("Showing tracking reminder popup");
			
			INotificationStyle style = new LightDefaultNotification().withWidth(400) // Optional
					.withAlpha(0.9f) // Optional
					;

			new NotificationBuilder().withStyle(style) // Required. here we set
														// the
														// previously set style
					.withTitle("Timey") // Required.
					.withMessage("Currently not tracking time.") // Optional
					.withDisplayTime(4000) // Optional
					.withPosition(Positions.SOUTH_EAST) // Optional. Show it at
														// the
														// center of the screen
					.withListener(new NotificationEventAdapter() { // Optional
						public void closed(NotificationEvent event) {
							TimeyEngine.ReminderPopupClosed = true;
						}

						public void clicked(NotificationEvent event) {
							TimeyEngine.ReminderPopupClosed = true;
						}
					}).showNotification();
		} else {
			TimeyLog.LogInfo("Last reminder not closed. Not showing new reminder.");
		}
	}

	public void ShowTrackStartPopup(String taskName) {
		TimeyLog.LogInfo("Showing start tracking popup. Item: " + taskName);

		INotificationStyle style = new DarkDefaultNotification().withWidth(400) // Optional
				.withAlpha(0.9f) // Optional
				;

		// Now lets build the notification
		new NotificationBuilder().withStyle(style) // Required. here we set the
													// previously set style
				.withTitle("Timey") // Required.
				.withMessage("Now tracking item: " + taskName) // Optional
				// .withIcon(new
				// ImageIcon(QuickStart.class.getResource("/twinkle.png"))) //
				// Optional. You could also use a String path
				.withDisplayTime(2000) // Optional
				.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the
													// center of the screen
				.showNotification(); // this returns a UUID that you can use to
										// identify events on the listener
	}

	public void CloseApplication() {
		TimeyLog.LogInfo("Closing application");
		System.exit(0);
	}

	public void OpenReportPage() {
		TimeyLog.LogInfo("Opening report page");
		String url = TimeyEngine.TimeyBase + "/goto.html#?page=reporting.html&sessionkey=" + TimeyEngine.SessionKey;
		TimeyLog.LogInfo("Navigating to: " + url);
		try {
			java.awt.Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			TimeyLog.LogException("Failed to open report page", e);
		} catch (URISyntaxException e) {
			TimeyLog.LogException("Failed to open report page", e);
		}

	}
}
