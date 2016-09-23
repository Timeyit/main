package timeydesktop;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.style.theme.LightDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.*;


public class TimeyDesktop {

	public static void main(String[] args) throws IOException {
		// AA the text
		System.setProperty("swing.aatext", "true");
		System.out.println("Working Directory = " +
	              System.getProperty("user.dir"));
		
		TrayIcon trayIcon = null;
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
		    
		    ActionListener listenerShowSample = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	TimeyEngine.getInstance().ShowReminderPopup("test task");
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
		    
		    // MenuItem -> Timey Website
		    MenuItem menuItemTimeyOnline = new MenuItem("Timey Online");
		    menuItemTimeyOnline.addActionListener(listenerTimeyOnline);
		    popup.add(menuItemTimeyOnline);
		    
		    // MenuItem -> Show Sample
		    MenuItem menuItemShowSample = new MenuItem("Show Sample");
		    menuItemShowSample.addActionListener(listenerShowSample);
		    popup.add(menuItemShowSample);
		    
		    // MenuItem -> Exit
		    MenuItem menuItemExit = new MenuItem("Exit");
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
		// ...
		// some time later
		// the application state has changed - update the image
		if (trayIcon != null) {
		    //trayIcon.setImage(updatedImage);
		}
		// ...
		
		
	}
	
}
