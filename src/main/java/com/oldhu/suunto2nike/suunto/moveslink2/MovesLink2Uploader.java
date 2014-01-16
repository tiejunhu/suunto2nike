package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusGpxGenerator;
import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class MovesLink2Uploader
{
	private static Logger log = Logger.getLogger(MovesLink2Uploader.class);
	private NikePlusProperties nikePlusProperties;
	private boolean devMode = false;

	public MovesLink2Uploader(boolean dev)
	{
		devMode = dev;
	}

	private File getDataFolder()
	{
		String userHome = System.getProperty("user.home");
		String folderName = "Moveslink2";
		if (devMode) {
			folderName = "Moveslink2-dev";
		}
		if (Util.isWindows()) {
			return new File(new File(userHome), "AppData/Roaming/Suunto/" + folderName);
		}
		if (Util.isMac()) {
			return new File(new File(userHome), "Library/Application Support/Suunto/" + folderName);
		}
		return null;
	}

	public boolean checkIfEnvOkay() throws IOException
	{
		File folder = getDataFolder();
		if (!folder.exists()) {
			log.info("Cannot find MovesLink2 data folder");
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moveslink2 data folder");
		}

		nikePlusProperties = new NikePlusProperties(getDataFolder());

		return true;
	}

	public void uploadXMLFiles() throws Exception
	{
		File folder = getDataFolder();

		File notRunFolder = new File(folder, "NotRun");
		File uploadedMovesFolder = new File(folder, "Uploaded");

		notRunFolder.mkdir();
		uploadedMovesFolder.mkdir();
		
		NikePlus nikePlus = null;

		File[] files = folder.listFiles();
		for (File file : files) {
			String fileName = file.getName().toLowerCase();
			if (fileName.startsWith("log-") && fileName.endsWith(".xml")) {
				if (nikePlus == null) {
					log.info("Login to Nike Plus");
					nikePlus = new NikePlus();
					String nikeEmail = nikePlusProperties.getEmail();
					char[] nikePassword = nikePlusProperties.getPassword().toCharArray();
					nikePlus.login(nikeEmail, nikePassword);
				}
				log.info("Analyzing " + fileName);
				XMLParser parser = new XMLParser(file);
				if (parser.isParseCompleted()) {
					uploadMoveToNike(nikePlus, parser.getSuuntoMove());
					file.renameTo(new File(uploadedMovesFolder, file.getName()));
				} else {
					file.renameTo(new File(notRunFolder, file.getName()));
				}
			}
		}
		
		if (nikePlus != null) {
			nikePlus.endSync();
		}
	}

	private void uploadMoveToNike(NikePlus nikePlus, SuuntoMove suuntoMove) throws Exception
	{
		log.info("Uploading move to Nike Plus");
		NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(suuntoMove);
		Document doc = nikeXml.getXML();

		NikePlusGpxGenerator nikeGpx = new NikePlusGpxGenerator(suuntoMove);
		Document gpx = nikeGpx.getXML();
		
		nikePlus.syncData(doc, gpx);
	}
}
