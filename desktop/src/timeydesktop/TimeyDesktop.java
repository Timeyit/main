package timeydesktop;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Timer;


public class TimeyDesktop {

	static MenuItem menuItemTimeyOnline = new MenuItem("Timey Online");
    static Menu menuTrack = new Menu("Track");
    static MenuItem menuItemForceSync = new MenuItem("Force Sync");
    static MenuItem menuItemExit = new MenuItem("Exit");
    static Timer timeySyncTimer = new Timer();
    static TrayIcon trayIcon = null;
    
	public static void main(String[] args) throws IOException {
		
		TimeyEngine.getInstance().Sync();
		
		// AA the text
		System.setProperty("swing.aatext", "true");
		
		if (SystemTray.isSupported() && true) {
		    // get the SystemTray instance
		    SystemTray tray = SystemTray.getSystemTray();
		    // load an image
		    
		    
		    Image image = Toolkit.getDefaultToolkit().getImage("src/resources/timey_logo_square.png");
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
		        	TimeyEngine.getInstance().StartTracking(item.nameWorkItem);
		        }
		    };
		    
        	MenuItem trackItem = new MenuItem(item.nameWorkItem);
        	trackItem.addActionListener(listenerTrackItem);
        	menuTrack.add(trackItem);
            //System.out.println(item.nameWorkItem);
        }
	}
}
