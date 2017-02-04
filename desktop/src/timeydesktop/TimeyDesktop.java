package timeydesktop;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class TimeyDesktop {

	static PopupMenu popup = new PopupMenu();
	static MenuItem menuItemTimeyOnline = new MenuItem("Timey Online");
	static Menu menuSettings = new Menu("Settings");
    static Menu menuTrack = new Menu("Track");
    static MenuItem menuItemStopRestart = new MenuItem("Track: <no history>");
    static MenuItem menuItemForceSync = new MenuItem("Force Sync");
    static MenuItem menuItemLogs = new MenuItem("View Logs");
    static MenuItem menuItemLogOut = new MenuItem("Log Out");
    static MenuItem menuItemExit = new MenuItem("Exit");
    static Timer timeySyncTimer = new Timer();
    static TrayIcon trayIcon = null;
    
    static ActionListener listenerStopTracking;
    
	public static void main(String[] args) throws IOException {
		
		TimeyLog.LogInfo("Starting Timey");
		TimeyEngine.TimeyDBHelper.Initialize();
		// AA the text
		System.setProperty("swing.aatext", "true");
		
		CheckVersion();
		HandleLogin();
		
		TimeyEngine.getInstance().Sync();
		
		if (SystemTray.isSupported() && true) {
		    // get the SystemTray instance
		    SystemTray tray = SystemTray.getSystemTray();
		    // load an image
		    
		    
		    Image image = Toolkit.getDefaultToolkit().getImage(TimeyDesktop.class.getClassLoader().getResource("timey_logo_square.png"));
		    // create a action listener to listen for default action executed on the tray icon
		    
		    ActionListener listenerTimeyOnline = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().OpenReportPage();
		        }
		    };
		    
		    listenerStopTracking = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().StopTracking();
		        }
		    };
		    
		    ActionListener listenerForceSync = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().Sync();
		        }
		    };
		    
		    ActionListener listenerLogs = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	LogViewer.createAndShowGUI();
		        }
		    };
		    
		    ActionListener listenerExit = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().CloseApplication();
		        }
		    };
		    
		    ActionListener listenerLogOut = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	LogOut();
		        }
		    };
		    
		    // ---- MenuItem -> Timey Website
		    menuItemTimeyOnline.addActionListener(listenerTimeyOnline);
		    popup.add(menuItemTimeyOnline);
		    
		    // ---- MenuItem -> Settings
		    popup.add(menuSettings);
		    
		    // ---- MenuItem -> Force Sync
		    menuItemForceSync.addActionListener(listenerForceSync);
		    menuSettings.add(menuItemForceSync);
		    
		    // ---- MenuItem -> Logs
		    menuItemLogs.addActionListener(listenerLogs);
		    menuSettings.add(menuItemLogs);
		    
		    // ---- MenuItem -> Log Out
		    menuItemLogOut.addActionListener(listenerLogOut);
		    menuSettings.add(menuItemLogOut);
		    
		    // ---- MenuItem -> Exit
		    menuItemExit.addActionListener(listenerExit);
		    menuSettings.add(menuItemExit);
		    
		    // ---- Menu Item -> Track
		    popup.add(menuTrack);
		    popup.add(menuItemStopRestart);
		    // Populate submenu
		    PopulateTrackMenu();
		    
		    
		    
		    
		    // construct a TrayIcon
		    trayIcon = new TrayIcon(image, "Timey", popup);
		    // set the TrayIcon properties
		    //trayIcon.addActionListener(listenerExit);
		    // ...
		    // add the tray image
		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println(e);
		    }
		    // ...
		} else {
		    // disable tray option in your application or
		    // perform other actions
		    
		}
		
		TimerTask timerTask = new TimerTask() {
	        @Override
	        public void run() {
	            /* The interface UpdateIndicatorsReceiver has an updateIndicators method */
	            TimeyEngine.getInstance().Sync();
	            PopulateTrackMenu();
	            
	            // ...
	    		// some time later
	    		// the application state has changed - update the image
	    		if (trayIcon != null) {
	    		    //trayIcon.setImage(updatedImage);
	    		}
	    		// ...
	        }
	    };
	    
		timeySyncTimer.scheduleAtFixedRate(
				timerTask,
				TimeyEngine.config.getSyncDelay() * 1000,
				TimeyEngine.config.getSyncDelay() * 1000
				);
		
		// Start by stopping the current tracking to trigger notifications.
		TimeyEngine.getInstance().StopTracking();
	}
	
	public static void PopulateTrackMenu()
	{
		TimeyLog.LogFine("Populating TrackMenu");
		
		if(TimeyAPIHelper.RefreshSession())
		{
			popup.remove(menuItemStopRestart);
			if(TimeyEngine.IsTracking)
			{
				menuItemStopRestart = new MenuItem("Stop Tracking");
				menuItemStopRestart.addActionListener(listenerStopTracking);
				popup.add(menuItemStopRestart);
			}
			else
			{
				TimeyConfig config = new TimeyConfig();
				try {
					final String idtotrack = config.getLastTracked();
					TimeyLog.LogFine("Id last tracked: " + idtotrack);
					// See if id exists.
					String workItemName = null; 
					for(Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext(); ) {
			        	final WorkItem item = i.next();
			        	if(item.idworkItem.equals(idtotrack))
			        	{
			        		workItemName = item.nameWorkItem;
			        	}
					}
					
					if(workItemName != null)
					{
						TimeyLog.LogFine("Name last tracked: " + workItemName);
						menuItemStopRestart = new MenuItem("Track: " + workItemName);
						ActionListener listenerTrackItem = new ActionListener() {
					        public void actionPerformed(ActionEvent e) {
					        	TimeyLog.LogFine("Id last tracked (starting): " + idtotrack);
					        	TimeyEngine.getInstance().StartTracking(idtotrack);
					        }
					    };
						menuItemStopRestart.addActionListener(listenerTrackItem);
						popup.add(menuItemStopRestart);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			menuTrack.removeAll();
			for(Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext(); ) {
	        	final WorkItem item = i.next();
	        	
	        	ActionListener listenerTrackItem = new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
			        	TimeyEngine.getInstance().StartTracking(item.idworkItem);
			        }
			    };
			    
	        	MenuItem trackItem = new MenuItem(item.nameWorkItem + " (" + item.duration + ")");
	        	trackItem.addActionListener(listenerTrackItem);
	        	menuTrack.add(trackItem);
	        }
		}
		
	}
	
	public static void LogOut()
	{
		TimeyLog.LogInfo("Logging Out");
		try
		{
	
			TimeyEngine.config.setPassword("");
			TimeyEngine.config.setUsername("");
			TimeyEngine.SessionKey = null;
			HandleLogin();
		}
		catch(IOException ex)
		{
			TimeyLog.LogSevere("Error logging out. Force exit");
			System.exit(0);
		}
	}
	
	public static void CheckVersion()
	{
		TimeyLog.LogInfo("Checking version");
		try
		{
			TimeyConfig properties = new TimeyConfig();
			// Verify that we have the right version of the api
			String serverVersion = TimeyAPIHelper.GetLatestVersion();
			TimeyLog.LogInfo("Server version = " + serverVersion + ". Desktop version = " + properties.getVersion() + ".");
			if(!properties.getVersion().equals(serverVersion))
			{
				String message = "There is a new Timey version available. Download now?";
			    String title = "Download new version?";
			    // display the JOptionPane showConfirmDialog
			    int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
			    if (reply == JOptionPane.YES_OPTION)
			    {
			    	try {
						Desktop.getDesktop().browse(new URI("http://www.timey.it"));
					} catch (URISyntaxException e) {
						TimeyLog.LogException("Error opening timey homepage", e);
					}
			    }
			}
		}
		catch(IOException ex)
		{
			TimeyLog.LogException("Error checking latest version. Exiting", ex);
			System.exit(0);
		}
	}
	
	public static void HandleLogin()
	{
		
		try
		{
			TimeyConfig properties = new TimeyConfig();
			int tries = 0;
			while(TimeyEngine.SessionKey == null)
			{
				TimeyLog.LogInfo("Handle Login. Attempt #" + tries);
				tries++;
				if(tries > 3)
				{
					TimeyLog.LogSevere("Tried to log in 3 times. Exiting.");
					System.exit(0);
				}
				
				TimeyAPIHelper.OpenSession();
				if(TimeyEngine.SessionKey == null)
				{
					try {
						UIManager.setLookAndFeel(
						        UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException e) {
						TimeyLog.LogException("Error applying theme.", e);
					} catch (InstantiationException e) {
						TimeyLog.LogException("Error applying theme.", e);
					} catch (IllegalAccessException e) {
						TimeyLog.LogException("Error applying theme.", e);
					} catch (UnsupportedLookAndFeelException e) {
						TimeyLog.LogException("Error applying theme.", e);
					}
					
					// Prompt for username & password
					JPanel panel = new JPanel(new BorderLayout(5, 5));
	
				    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
				    label.add(new JLabel("Username: ", SwingConstants.RIGHT));
				    label.add(new JLabel("Password: ", SwingConstants.RIGHT));
				    panel.add(label, BorderLayout.WEST);
	
				    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
				    JTextField username = new JTextField();
				    controls.add(username);
				    JPasswordField password = new JPasswordField();
				    controls.add(password);
				    panel.add(controls, BorderLayout.CENTER);
				    	
				    int reply = JOptionPane.showConfirmDialog(null, panel, "Timey Login", JOptionPane.OK_CANCEL_OPTION);
				    if (reply == JOptionPane.YES_OPTION) {
				    	String uname = username.getText();
					    String pwd = new String(password.getPassword());
					    pwd = Helper.md5("mysalt" + pwd);
					    properties.setUsername(uname);
					    properties.setPassword(pwd);
			        }
			        else {
			        	TimeyLog.LogSevere("Error logging in. Exiting");
			        	System.exit(0);
			        }
				    
				}
			}
			PopulateTrackMenu();
		}
		catch(IOException ex)
		{
			TimeyLog.LogSevere("Error logging in. Exiting");
			System.exit(0);
		}
		
	}
	
}
