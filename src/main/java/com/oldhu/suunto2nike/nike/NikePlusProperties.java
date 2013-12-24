package com.oldhu.suunto2nike.nike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NikePlusProperties
{
	private static NikePlusProperties _instance = new NikePlusProperties();
	private static Log log = LogFactory.getLog("NikePlusProperties");
	public static final String NIKEPLUS_PASSWORD = "NIKEPLUS_PASSWORD";
	public static final String NIKEPLUS_EMAIL = "NIKEPLUS_EMAIL";

	public static NikePlusProperties getInstance()
	{
		return _instance;
	}

	private NikePlusProperties()
	{

	}
	
	public void createNikePlusUserProperties(File file) throws IOException
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
