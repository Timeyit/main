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
	
	
	String configurationFilePath = "timeyconfiguration.config";
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
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	
	public boolean setUsername(String username) throws IOException {
		return setProperty("username", username);
	}
	
	public boolean setPassword(String password) throws IOException {
		return setProperty("password", password);
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
			System.out.println("Exception: " + e);
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
			System.out.println("Exception: " + e);
		} finally {
			
		}
		return false;
	}
}