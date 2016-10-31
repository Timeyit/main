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
import org.codehaus.jackson.JsonNode;
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
	public static WorkItem TrackedItem = new WorkItem();
	public static int NumAlarams = 0;
	private static Timer trackTimer = new Timer();
	public static Instant TrackingStartTime = new Instant(Long.MIN_VALUE);
	public static Instant TrackingLastSync = new Instant(Long.MIN_VALUE);
	public static TimeyConfig config = new TimeyConfig();
	public static String SessionToken = null;
	public static String TimeyBase = "http://localhost:1337";
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
		System.out.println("Stop Tracking");
		TrackedItem = new WorkItem();
		NumAlarams = 0;
		IDTimeLog = -1;
		TrackingStartTime = new Instant(Long.MIN_VALUE);
		TrackingLastSync = new Instant(Long.MIN_VALUE);
		IsTracking = false;
		trackTimer.cancel();
		try {
			if (config.getShowNoTrackingNotifications()) {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeyDesktop.PopulateTrackMenu();
	}

	public void KeepTracking() {
		System.out.println("Keep Tracking");
		NumAlarams = NumAlarams + 1;
		IsTracking = true;
	}

	public void StartTracking(String idWorkItem) {
		for (Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext();) {
			final WorkItem item = i.next();
			if (item.idworkItem.equals(idWorkItem)) {
				TrackedItem = item;
			}
		}

		try {
			config.setLastTracked(TrackedItem.idworkItem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Now Tracking: " + TrackedItem.nameWorkItem + " id: " + TrackedItem.idworkItem);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				System.out.println("Got data: " + jsonData);

				return jsonData;
			}
		} catch (Exception ex) {
			// Do nothing for now
		}
		return "0.0";
	}

	public String OpenSession() {
		try {
			System.out.println("Opening Session");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "session_createSession.php");

			// Request parameters and other properties.
			String requestJSON = "{\"username\":\"" + config.getUsername() + "\",\"password\":\"" + config.getPassword()
					+ "\"} ";
			System.out.println("Request JSON: " + requestJSON);
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
					SessionToken = responseData;
					return responseData;
				}

			}
		} catch (Exception ex) {
			System.out.println("Failure...");
		}
		return null;
	}

	public boolean VerifyAPIKey(String apiKey) {
		return false;
	}

	public void Sync() {
		if (IsTracking) {
			Interval timeSinceLastSync = new Interval(TrackingLastSync, new Instant());
			Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
			System.out
					.println("Time Since Last Sync: " + (double) timeSinceLastSync.toDurationMillis() / (double) 1000);
			System.out.println("Time Since Last Start: " + (double) timeSinceStart.toDurationMillis() / (double) 1000);
			if (UploadTrackedTime()) {
				TrackingLastSync = new Instant();
			}

		} else {
			// System.out.println("Not tracking time");
		}
		WorkItems = GetWorkItems();
	}

	public List<WorkItem> GetWorkItems() {
		try {
			// System.out.println("Getting work items");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "workItem_getAll.php");

			// Request parameters and other properties.
			StringEntity params = new StringEntity("{\"sessionkey\":\"" + SessionToken + "\"} ");
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
			System.out.println("Failure...");
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
			System.out.println("Uploading tracked time (Main)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "workItem_update.php");

			// Request parameters and other properties.
			String requestJSON = "{" + 
					"\"idworkItem\":\"" + TrackedItem.idworkItem + "\"," +
					"\"duration\":" + "\"" + Long.toString(duration) + "\"" + "," +
					"\"sessionkey\":\"" + SessionToken + "\"" +
					"} ";
			System.out.println("Request JSON: " + requestJSON);
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
				System.out.println("Response JSON: " + jsonData);
				
			}
		} catch (Exception ex) {
			System.out.println("Failure...");
			return false;
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Update lap tracking
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try {
			System.out.println("Uploading tracked time (LAP)");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(ApiBase + "timeLog_updateCreate.php");

			// Request parameters and other properties.
			String requestJSON = "{" + 
					"\"idworkItem\":\"" + TrackedItem.idworkItem + "\"," +
					"\"durationLap\":" + "\"" + Long.toString(durationSinceLastSync) + "\"" + "," +
					"\"idTimeLog\":" + "\"" + Long.toString(IDTimeLog) + "\"" + "," +
					"\"sessionkey\":\"" + SessionToken + "\"" +
					"} ";
			System.out.println("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String jsonData = IOUtils.toString(instream, "UTF-8");
				if(IDTimeLog < 0)
				{
					// BAD SOLUTION FOR DESERIALIZING, BUT LETS USE FOR NOW
					String timeIdString = jsonData.replace("[{\"myid\":\"", "").replace("\"}]", "");
					IDTimeLog = Long.parseLong(timeIdString); 
				}
				instream.close();
				System.out.println("Response JSON: " + jsonData);
			}
		} catch (Exception ex) {
			System.out.println("Failure...");
			return false;
		}
		
		return true;
	}

	public void ShowReminderPopup() {
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
		// First we define the style/theme of the window.
		// Note how we override the default values
		INotificationStyle style = new LightDefaultNotification().withWidth(400) // Optional
				.withAlpha(0.9f) // Optional
				;

		new NotificationBuilder().withStyle(style) // Required. here we set the
													// previously set style
				.withTitle("Timey") // Required.
				.withMessage("Currently not tracking time.") // Optional
				// .withIcon(new
				// ImageIcon(QuickStart.class.getResource("/twinkle.png"))) //
				// Optional. You could also use a String path
				.withDisplayTime(5000) // Optional
				.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the
													// center of the screen
				.showNotification();
	}

	public void ShowTrackStartPopup(String taskName) {
		// First we define the style/theme of the window.
		// Note how we override the default values
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
		System.out.println("Shutting down.");
		System.exit(0);
	}

	public void OpenReportPage() {

		String url = TimeyEngine.TimeyBase + "/goto.html#?page=reporting.html&sessionkey=" + TimeyEngine.SessionToken;
		System.out.println("Navigating to: " + url);
		try {
			java.awt.Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
