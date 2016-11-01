package timeydesktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public class TimeyConfig {
	
	
	String configurationFilePath = System.getenv("APPDATA") + "\\Timey\\timeyconfiguration.config";
	public TimeyConfig()
	{
		File f = new File(configurationFilePath);
		if(f.exists() && !f.isDirectory()) { 
			
		}
		else
		{
			PrintWriter out = null;
			try {
				out = new PrintWriter(configurationFilePath);
				out.println("version=1.0");
				out.println("username=empty");
				out.println("password=empty");
				out.println("lastTracked=none");
				out.println("trackNotificationDelay=300");
				out.println("showNoTrackingNotifications=true");
				out.println("noTrackingNotificationDelay=300");
				out.println("syncDelay=30");
			} catch (FileNotFoundException e) {
				TimeyLog.LogException("Failed to instansiate TimeyConfig", e);
			} finally
			{
				out.close();
			}
			
		}
	}
	String result = "";
	InputStream inputStream;
	FileOutputStream out;
	
	
	public String getUsername() throws IOException {
		return getProperty("username");
	}
	
	public String getPassword() throws IOException {
		return getProperty("password");
	}
	
	public String getVersion() throws IOException {
		return getProperty("version");
	}
	
	public String getLastTracked() throws IOException {
		return getProperty("lastTracked");
	}
	
	public int getTrackNotificationDelay() throws IOException {
		return Integer.parseInt(getProperty("trackNotificationDelay"));
	}
	
	public boolean getShowNoTrackingNotifications() throws IOException {
		return Boolean.parseBoolean(getProperty("showNoTrackingNotifications"));
	}
	
	public int getNoTrackingNotificationDelay() throws IOException {
		return Integer.parseInt(getProperty("noTrackingNotificationDelay"));
	}
	
	public int getSyncDelay() {
		try {
			return Integer.parseInt(getProperty("syncDelay"));
		} catch (NumberFormatException e) {
			TimeyLog.LogException("TimeyConfig: failed to get syncDelay", e);
			return 30;
		} catch (IOException e) {
			TimeyLog.LogException("TimeyConfig: failed to get syncDelay", e);
			return 30;
		}
	}
	
	public boolean setUsername(String username) throws IOException {
		return setProperty("username", username);
	}
	
	public boolean setPassword(String password) throws IOException {
		return setProperty("password", password);
	}
	
	public boolean setLastTracked(String lastTracked) throws IOException {
		return setProperty("lastTracked", lastTracked);
	}
	
	public boolean setTrackNotificationDelay(int trackNotificationDelay) throws IOException {
		return setProperty("trackNotificationDelay", Integer.toString(trackNotificationDelay));
	}
	
	public boolean setShowNoTrackingNotifications(boolean showNoTrackingNotifications) throws IOException {
		return setProperty("showNoTrackingNotifications", Boolean.toString(showNoTrackingNotifications));
	}
	
	public boolean setNoTrackingNotificationDelay(int noTrackingNotificationDelay) throws IOException {
		return setProperty("noTrackingNotificationDelay", Integer.toString(noTrackingNotificationDelay));
	}
	
	public boolean setSyncDelay(int syncDelay) throws IOException {
		return setProperty("syncDelay", Integer.toString(syncDelay));
	}
	
	public String getProperty(String propertyName) throws IOException {
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
 
			inputStream = new FileInputStream(configurationFilePath);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			
			// get the property value and print it out
			return prop.getProperty(propertyName);
		} catch (Exception e) {
			TimeyLog.LogException("Failed to get configuration property", e);
		} finally {
			inputStream.close();
		}
		return null;
	}
	
	public boolean setProperty(String propertyName, String propertyValue) throws IOException {
		try {
			Properties prop = new Properties();
			String propFileName = "config.properties";
 
			inputStream = new FileInputStream(configurationFilePath);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
			
			inputStream.close();
			
			// get the property value and print it out
			FileOutputStream out = new FileOutputStream(configurationFilePath);
			prop.setProperty(propertyName, propertyValue);
			prop.store(out, null);
			out.close();
			return true;
		} catch (Exception e) {
			TimeyLog.LogException("Failed to set configuration property", e);
		} finally {
			
		}
		return false;
	}
}
