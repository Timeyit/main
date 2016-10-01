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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class TimeyDesktop {

	static MenuItem menuItemTimeyOnline = new MenuItem("Timey Online");
    static Menu menuTrack = new Menu("Track");
    static MenuItem menuItemForceSync = new MenuItem("Force Sync");
    static MenuItem menuItemLogOut = new MenuItem("Log Out");
    static MenuItem menuItemExit = new MenuItem("Exit");
    static Timer timeySyncTimer = new Timer();
    static TrayIcon trayIcon = null;
    
	public static void main(String[] args) throws IOException {
		
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
		        	try {
						java.awt.Desktop.getDesktop().browse(new URI("http://timey.it"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
		    };
		    
		    ActionListener listenerForceSync = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().Sync();
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
		    // create a popup menu
		    PopupMenu popup = new PopupMenu();
		    // create menu item for the default action
		    
		    // ---- MenuItem -> Timey Website
		    menuItemTimeyOnline.addActionListener(listenerTimeyOnline);
		    popup.add(menuItemTimeyOnline);
		    
		    // ---- Menu Item -> Track
		    popup.add(menuTrack);
		    // Populate submenu
		    PopulateTrackMenu();
		    
		    // ---- MenuItem -> Force Sync
		    menuItemForceSync.addActionListener(listenerForceSync);
		    popup.add(menuItemForceSync);
		    
		 // ---- MenuItem -> Force Sync
		    menuItemLogOut.addActionListener(listenerLogOut);
		    popup.add(menuItemLogOut);
		    
		    // ---- MenuItem -> Exit
		    menuItemExit.addActionListener(listenerExit);
		    popup.add(menuItemExit);
		    
		    
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
	    
		timeySyncTimer.scheduleAtFixedRate(timerTask, TimeyEngine.Options.SyncTime, TimeyEngine.Options.SyncTime);
	}
	
	private static void PopulateTrackMenu()
	{
		menuTrack.removeAll();
		for(Iterator<WorkItem> i = TimeyEngine.WorkItems.iterator(); i.hasNext(); ) {
        	final WorkItem item = i.next();
        	
        	ActionListener listenerTrackItem = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().StartTracking(item.idworkItem);
		        }
		    };
		    
		    //System.out.println(item.nameWorkItem + " - " + item.idworkItem + " - " + item.duration);
        	MenuItem trackItem = new MenuItem(item.nameWorkItem + " (" + item.duration + ")");
        	trackItem.addActionListener(listenerTrackItem);
        	menuTrack.add(trackItem);
            //System.out.println(item.nameWorkItem);
        }
	}
	
	public static void LogOut()
	{
		System.out.println("Logging out");
		try
		{
			TimeyConfig properties = new TimeyConfig();
			// Verify that we have the right version of the api
	
			properties.setPassword("");
			properties.setUsername("");
			TimeyEngine.SessionToken = null;
			HandleLogin();
		}
		catch(IOException ex)
		{
			System.err.println("Error logging out. Exiting");
			System.exit(0);
		}
	}
	
	public static void CheckVersion()
	{
		try
		{
			TimeyConfig properties = new TimeyConfig();
			// Verify that we have the right version of the api
	
			if(!properties.getVersion().equals(TimeyEngine.getInstance().GetLatestVersion()))
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}
		}
		catch(IOException ex)
		{
			System.err.println("Error checking latest version. Exiting");
			System.exit(0);
		}
	}
	
	public static void HandleLogin()
	{
		try
		{
			TimeyConfig properties = new TimeyConfig();
			int tries = 0;
			while(TimeyEngine.SessionToken == null)
			{
				tries++;
				if(tries > 3)
				{
					System.exit(0);
				}
				
				TimeyEngine.getInstance().OpenSession();
				if(TimeyEngine.SessionToken == null)
				{
					// Prompt for username & password
					JPanel panel = new JPanel(new BorderLayout(5, 5));
	
				    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
				    label.add(new JLabel("Username", SwingConstants.RIGHT));
				    label.add(new JLabel("Password", SwingConstants.RIGHT));
				    panel.add(label, BorderLayout.WEST);
	
				    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
				    JTextField username = new JTextField();
				    controls.add(username);
				    JPasswordField password = new JPasswordField();
				    controls.add(password);
				    panel.add(controls, BorderLayout.CENTER);
	
				    int reply = JOptionPane.showConfirmDialog(null, panel, "login", JOptionPane.OK_CANCEL_OPTION);
				    if (reply == JOptionPane.YES_OPTION) {
				    	String uname = username.getText();
					    String pwd = new String(password.getPassword());
					    pwd = md5("mysalt" + pwd);
					    properties.setUsername(uname);
					    properties.setPassword(pwd);
			        }
			        else {
			        	System.err.println("Error logging in. Exiting");
			        	System.exit(0);
			        }
				    
				}
			}
		}
		catch(IOException ex)
		{
			System.err.println("Error logging in. Exiting");
			System.exit(0);
		}
		
	}
	public static String md5(String input) {
		
		String md5 = null;
		
		if(null == input) return null;
		
		try {
			
		//Create MessageDigest object for MD5
		MessageDigest digest = MessageDigest.getInstance("MD5");
		
		//Update input string in message digest
		try {
			digest.update(input.getBytes("UTF-8"), 0, input.length());
			//Converts message digest value in base 16 (hex) 
			md5 = new BigInteger(1, digest.digest()).toString(16);
			while(md5.length() < 32)
			{
				md5 = "0" + md5;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		return md5;
	}
}
