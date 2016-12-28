package univision.com.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class Configuration {

	public static HashMap<String, String> config = new HashMap<String, String>();
	String path = System.getProperty("user.dir") + File.separator + "resources" + File.separator ;
	String configFileName = path + "crawlerconfiguration.properties";
	
	public HashMap<String, String> getConfigPropValues() throws IOException {
		
		// get the property value
		config.put("URL", EnvirommentManager.getInstance().getProperty("URL"));
		config.put("USER", EnvirommentManager.getInstance().getProperty("USER"));
		config.put("PASSWORD",   EnvirommentManager.getInstance().getProperty("PASSWORD"));
		config.put("DRIVERCLASS",  EnvirommentManager.getInstance().getProperty("DRIVERCLASS"));
		config.put("LINKSNAME", path+EnvirommentManager.getInstance().getProperty("LINKSNAME"));
		config.put("LINKSPROPNAME", path+EnvirommentManager.getInstance().getProperty("LINKSPROPNAME"));
		
		return config;
	}
	
	public HashMap<String, String> getAdoPropValues() throws IOException {

		Properties prop = readProp(configFileName);
		// get the property value
		
		config.put("URL", prop.getProperty("URL"));
		config.put("USER", prop.getProperty("USER"));
		config.put("PASSWORD",  prop.getProperty("PASSWORD"));
		config.put("DRIVERCLASS",  prop.getProperty("DRIVERCLASS"));
		config.put("LINKSPATH", prop.getProperty("LINKSNAME"));
		config.put("LINKSPROPPATH", prop.getProperty("LINKSPROPNAME"));
		
		return config;
	}
	
	
	public HashMap<String, String> getCsvPropValues() throws IOException {
    	
		Properties prop = readProp(configFileName);
		// get the property value
		
		config.put("LINKSPATH", prop.getProperty("LINKSPATH"));
		config.put("LINKSPROPPATH", prop.getProperty("LINKSPROPPATH"));
		return config;
	}
	private Properties readProp(String propName) throws IOException
	{
		Properties prop = new Properties();
		String propFileName = propName;

		FileInputStream inputStream = new FileInputStream(propFileName);
		prop.load(inputStream);
		
		return prop;

	}
}
