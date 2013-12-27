package com.oldhu.suunto2nike.nike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NikePlusProperties
{
	private static Log log = LogFactory.getLog(NikePlusProperties.class);

	public static final String NIKEPLUS_PASSWORD = "NIKEPLUS_PASSWORD";
	public static final String NIKEPLUS_EMAIL = "NIKEPLUS_EMAIL";

	private File dataFolder;

	private Properties nikeProperties;

	public NikePlusProperties(File folder) throws IOException
	{
		log.info("Checking Nike Plus properties file under " + folder.getAbsolutePath());
		dataFolder = folder;
		File nikeplusUser = getNikeUserPropertiesFile();
		if (!nikeplusUser.exists()) {
			createNikePlusUserProperties(nikeplusUser);
		}
		nikeProperties = getNikePlusUserProperties();
	}

	public String getEmail()
	{
		return nikeProperties.getProperty(NIKEPLUS_EMAIL);
	}

	public String getPassword()
	{
		return nikeProperties.getProperty(NIKEPLUS_PASSWORD);

	}

	private File getNikeUserPropertiesFile()
	{
		return new File(dataFolder, "nikeuser.properties");
	}

	private Properties getNikePlusUserProperties() throws IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream(getNikeUserPropertiesFile()));
		return prop;
	}

	private void createNikePlusUserProperties(File file) throws IOException
	{
		String userName;
		String password;

		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Input your Nike+ login email: ");
			userName = br.readLine();
			System.out.print("Input your Nike+ password: ");
			password = br.readLine();

			try {
				System.out.print("Verifying...");
				NikePlus plus = new NikePlus();
				plus.login(userName, password.toCharArray());
			} catch (Exception e) {
				System.out.println("Failed. Please try again.");
				continue;
			}
			System.out.println("ok.");
			break;
		}

		Properties prop = new Properties();
		prop.put(NIKEPLUS_EMAIL, userName);
		prop.put(NIKEPLUS_PASSWORD, password);

		FileOutputStream fos = new FileOutputStream(file);
		prop.store(fos, "SYSTEM GENERATED");
		fos.close();
	}
}
