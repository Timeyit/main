package timeydesktop;

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
