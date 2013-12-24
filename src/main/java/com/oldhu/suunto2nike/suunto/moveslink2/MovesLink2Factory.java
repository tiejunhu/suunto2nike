package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.suunto.moveslink.MovesLinkFactory;

public class MovesLink2Factory
{
	private static MovesLink2Factory _factory = new MovesLink2Factory();
	private static Log log = LogFactory.getLog("MovesLink2Factory");

	public static MovesLink2Factory getInstance()
	{
		return _factory;
	}

	private MovesLink2Factory()
	{

	}

	private File getDataFolder()
	{
		String userHome = System.getProperty("user.home");
		File folder = new File(new File(userHome), "AppData/Roaming/Suunto/Moveslink2");
		return folder;
	}
	
	private File getNikeUserPropertiesFile()
	{
		return new File(getDataFolder(), "nikeuser.properties");
	}

	public boolean checkIfEnvOkay() throws IOException
	{
		File folder = getDataFolder();
		if (!folder.exists()) {
			log.error("Cannot find moveslink2 data folder");
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moveslink2 data folder");
		}

		File nikeplusUser = getNikeUserPropertiesFile();
		if (!nikeplusUser.exists()) {
			NikePlusProperties.getInstance().createNikePlusUserProperties(nikeplusUser);
		}
		return true;
	}
	
	public void uploadXMLFiles() throws Exception
	{
		File folder = getDataFolder();
		File[] files = folder.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.startsWith("log-")) {
				log.info("Analyzing " + fileName);
				XMLParser parser = new XMLParser(file);
			}
		}
	}
}
