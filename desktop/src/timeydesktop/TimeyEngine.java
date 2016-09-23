package timeydesktop;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

public class TimeyEngine {

	private static TimeyEngine instance = null;

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
	}
	
	public void KeepTracking()
	{
		System.out.println("Keep Tracking");
	}
	
	public void StartTracking(String taskName)
	{
		System.out.println("Start Tracking");
	}
	
	public String GetAPIKey(String username, String password)
	{
		return "";
	}
	
	public boolean VerifyAPIKey(String apiKey)
	{
		return false;
	}
	
	public List<WorkItem> GetWorkItems()
	{
		try
		{
			System.out.println("Getting work items");
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("http://timey.it/PHP/workItem_getAll.php");

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("userName", "Simon"));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
			    InputStream instream = entity.getContent();
			    String jsonData = IOUtils.toString(instream, "UTF-8"); 
			    instream.close();
			    System.out.println("Got data: " + jsonData);
			    ObjectMapper mapper = new ObjectMapper();
			    List<WorkItem> workItems = mapper.readValue(jsonData, new TypeReference<List<WorkItem>>(){});
			    System.out.println("Got : " + workItems.size() + " work items" );
			    return workItems;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Failure...");
		}
		return new ArrayList<WorkItem>();
	}
	
	public void ShowReminderPopup(String taskName)
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
						.withMessage("Click message if still working on \"Create web design\".") // Optional
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
	
	public void CloseApplication()
	{
		System.out.println("Shutting down.");
		System.exit(0);
	}
}
