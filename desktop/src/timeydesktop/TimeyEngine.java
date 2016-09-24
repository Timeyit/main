package timeydesktop;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.Instant;
import org.joda.time.Interval;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.*;
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
	protected TimeyEngine() {
		// Exists only to defeat instantiation.
	}

	public static TimeyEngine getInstance() {
		if (instance == null) {
			instance = new TimeyEngine();
		}
		return instance;
	}
	
	public void StopTracking()
	{
		System.out.println("Stop Tracking");
		TrackedItem = new WorkItem();
		NumAlarams = 0;
		TrackingStartTime = new Instant(Long.MIN_VALUE);
		TrackingLastSync = new Instant(Long.MIN_VALUE);
		IsTracking = false;
		trackTimer.cancel();
	}
	
	public void KeepTracking()
	{
		System.out.println("Keep Tracking");
		NumAlarams = NumAlarams + 1;
		IsTracking = true;
	}
	
	public void StartTracking(String idWorkItem)
	{
		
		for(Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext(); ) {
        	final WorkItem item = i.next();
        	if(item.idworkItem == idWorkItem)
        	{
        		TrackedItem = item;
        	}
		}
		
		System.out.println("Now Tracking: " + TrackedItem.nameWorkItem);
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
	    
		trackTimer.scheduleAtFixedRate(timerTask, TimeyEngine.Options.AlarmTimeMin1*60000, TimeyEngine.Options.AlarmTimeMin1*60000);
	}
	
	public String GetAPIKey(String username, String password)
	{
		return "";
	}
	
	public boolean VerifyAPIKey(String apiKey)
	{
		return false;
	}
	
	public void Sync()
	{
		if(IsTracking)
		{
			Interval timeSinceLastSync = new Interval(TrackingLastSync, new Instant());
			Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
			System.out.println("Time Since Last Sync: " + (double)timeSinceLastSync.toDurationMillis()/(double)1000);
			System.out.println("Time Since Last Start: " + (double)timeSinceStart.toDurationMillis()/(double)1000);
			if(UploadTrackedTime())
			{
				TrackingLastSync = new Instant();
			}
			
		}
		else
		{
			System.out.println("Not tracking time");
		}
		WorkItems = GetWorkItems();
	}
	
	public List<WorkItem> GetWorkItems()
	{
		try
		{
			System.out.println("Getting work items");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("http://timey.it/PHP/workItem_getAll.php");

			// Request parameters and other properties.
			StringEntity params =new StringEntity("{\"user_username\":\"" + Options.Username + "\"} ");
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);
	        
			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
			    InputStream instream = entity.getContent();
			    String jsonData = IOUtils.toString(instream, "UTF-8"); 
			    instream.close();
			    //System.out.println("Got data: " + jsonData);
			    ObjectMapper mapper = new ObjectMapper();
			    List<WorkItem> workItems = mapper.readValue(jsonData, new TypeReference<List<WorkItem>>(){});
			    //System.out.println("Got : " + workItems.size() + " work items" );
			    return workItems;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Failure...");
		}
		return new ArrayList<WorkItem>();
	}
	
	public boolean UploadTrackedTime()
	{
		Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
		long duration = timeSinceStart.toDuration().getStandardSeconds();
		duration = duration + TrackedItem.GetDurationLong(); 
		try
		{
			System.out.println("Uploading tracked time");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("http://timey.it/PHP/workItem_update.php");

			// Request parameters and other properties.
			String requestJSON = "{\"idworkItem\":\"" + TrackedItem.idworkItem + "\",\"duration\":" + Long.toString(duration) + "} ";
			System.out.println("Request JSON: " + requestJSON);
			StringEntity params = new StringEntity(requestJSON);
			httppost.addHeader("content-type", "application/json");
			httppost.setEntity(params);
	        
			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
			    InputStream instream = entity.getContent();
			    String jsonData = IOUtils.toString(instream, "UTF-8"); 
			    instream.close();
			    System.out.println("Response JSON: " + jsonData);
			    return true;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Failure...");
		}
		return false;
	}
	
	public void ShowReminderPopup()
	{
		Interval timeSinceStart = new Interval(TrackingStartTime, new Instant());
		int TimeStartSec = (int) Math.round((double)timeSinceStart.toDurationMillis()/(double)1000);
		// First we define the style/theme of the window.
				// Note how we override the default values
				INotificationStyle style = new DarkDefaultNotification()
						.withWidth(400) // Optional
						.withAlpha(0.9f) // Optional
				;

				// Now lets build the notification
				new NotificationBuilder()
						.withStyle(style) // Required. here we set the previously set style
						.withTitle("Timey") // Required.
						.withMessage("Click message if still working on \"" + TimeyEngine.TrackedItem + "\" (" + TimeStartSec + " s).") // Optional
						//.withIcon(new ImageIcon(QuickStart.class.getResource("/twinkle.png"))) // Optional. You could also use a String path
						.withDisplayTime(10000) // Optional
						.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the center of the screen
						.withListener(new NotificationEventAdapter() { // Optional
							public void closed(NotificationEvent event) {
								System.out.println("closed notification with UUID " + event.getId());
								TimeyEngine.getInstance().StopTracking();
							}

							public void clicked(NotificationEvent event) {
								System.out.println("clicked notification with UUID " + event.getId());
								TimeyEngine.getInstance().KeepTracking();
							}
						})
						.showNotification(); // this returns a UUID that you can use to identify events on the listener
	}
	
	public void ShowTrackStartPopup(String taskName)
	{
		// First we define the style/theme of the window.
				// Note how we override the default values
				INotificationStyle style = new DarkDefaultNotification()
						.withWidth(400) // Optional
						.withAlpha(0.9f) // Optional
				;

				// Now lets build the notification
				new NotificationBuilder()
						.withStyle(style) // Required. here we set the previously set style
						.withTitle("Timey") // Required.
						.withMessage("Now tracking item: " + taskName) // Optional
						//.withIcon(new ImageIcon(QuickStart.class.getResource("/twinkle.png"))) // Optional. You could also use a String path
						.withDisplayTime(2000) // Optional
						.withPosition(Positions.SOUTH_EAST) // Optional. Show it at the center of the screen
						.showNotification(); // this returns a UUID that you can use to identify events on the listener
	}
	
	public void CloseApplication()
	{
		System.out.println("Shutting down.");
		System.exit(0);
	}
}
