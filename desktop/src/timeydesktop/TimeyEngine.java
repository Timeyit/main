package timeydesktop;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
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
	public static boolean IsTracking = false;
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
	public static ITimeyDB TimeyDBHelper = new TimeyDBSQLLite();

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
				TimerTask timerTask = new TimerTask() {
					@Override
					public void run() {
						ShowNoTrackingReminderPopup();
					}
				};

				trackTimer = new Timer();
				try {
					trackTimer.scheduleAtFixedRate(
							timerTask, 
							config.getNoTrackingNotificationDelay() * 1000,
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
		trackTimer.cancel();
		trackTimer = new Timer();
		try {
			trackTimer.scheduleAtFixedRate(timerTask, config.getTrackNotificationDelay() * 1000,
					config.getTrackNotificationDelay() * 1000);
		} catch (IOException e) {
			TimeyLog.LogException("Failed to start task timer", e);
		}
		TimeyDesktop.PopulateTrackMenu();
	}

	public void Sync() {
		// Refresh or open session
		if (!TimeyAPIHelper.RefreshSession()) {
			TimeyAPIHelper.OpenSession();
		}

		// Proceed with syncing
		if (IsTracking) {
			Interval timeSinceLastSync = new Interval(TrackingLastSync, new Instant());
			Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
			long timeSinceLastSyncLong = timeSinceLastSync.toDurationMillis() / 1000;
			long timeSinceStartLong = timeSinceStart.toDurationMillis() / 1000;
			TimeyLog.LogFine("Time Since Last Sync: " + timeSinceLastSyncLong);
			TimeyLog.LogFine("Time Since Last Start: " + timeSinceStartLong);
			TrackingLastSync = new Instant();
			
			 
			TimeLog timeLog = new TimeLog(
					TimeyEngine.TrackedItem.idworkItem,
					timeSinceLastSyncLong,
					DateTime.now(),
					false
					);
			TimeyDBHelper.AddTimeLog(timeLog);
			
			if (!TimeyAPIHelper.UploadTrackedTime()) {
				TimeyLog.LogSevere("Failed to Upload Tracked Time");
			}

		} else {
			TimeyLog.LogFine("Not tracking time");
		}
		WorkItems = TimeyAPIHelper.GetWorkItems();
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
				.withDisplayTime(15000) // Optional
				.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the
													// center of the screen
				.withListener(new NotificationEventAdapter() { // Optional
					public void closed(NotificationEvent event) {
						TimeyEngine.getInstance().StopTracking();
					}

					public void clicked(NotificationEvent event) {
						TimeyEngine.getInstance().KeepTracking();
					}
				}).showNotification(); // this returns a UUID that you can use
										// to identify events on the listener
	}

	public void ShowNoTrackingReminderPopup() {
		TimeyLog.LogInfo("Showing tracking reminder popup");

		INotificationStyle style = new LightDefaultNotification()
				.withWidth(400)
				.withAlpha(0.9f)
				;

		new NotificationBuilder()
				.withStyle(style).withTitle("Timey")
				.withMessage("Currently not tracking time.")
				.withDisplayTime(4000)
				.withPosition(Positions.SOUTH_EAST)
				.showNotification();
	}

	public void ShowTrackStartPopup(String taskName) {
		TimeyLog.LogInfo("Showing start tracking popup. Item: " + taskName);

		INotificationStyle style = new DarkDefaultNotification()
				.withWidth(400)
				.withAlpha(0.9f)
				;

		// Now lets build the notification
		new NotificationBuilder()
				.withStyle(style)
				.withTitle("Timey")
				.withMessage("Now tracking item: " + taskName)
				.withDisplayTime(3000)
				.withPosition(Positions.SOUTH_EAST)
				.showNotification();
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
