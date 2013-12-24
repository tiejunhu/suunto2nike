package com.oldhu.suunto2nike.moveslink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;

public class MovesLinkFactory
{
	private static MovesLinkFactory _factory = new MovesLinkFactory();
	private static Log log = LogFactory.getLog("MovesLinkFactory");

	public static MovesLinkFactory getInstance()
	{
		return _factory;
	}

	private MovesLinkFactory()
	{

	}

	private File getDataFolder()
	{
		String userHome = System.getProperty("user.home");
		File folder = new File(new File(userHome), "AppData/Roaming/Suunto/Moveslink");
		return folder;
	}

	private Properties getNikePlusUserProperties() throws FileNotFoundException, IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream(getNikeUserPropertiesFile()));
		return prop;
	}

	private File getNikeUserPropertiesFile()
	{
		return new File(getDataFolder(), "nikeuser.properties");
	}

	public void uploadXMLFiles() throws Exception
	{
		File folder = MovesLinkFactory.getInstance().getDataFolder();
		File noMovesFolder = new File(folder, "NoMoves");
		File duplicateMovesFolder = new File(folder, "Duplicates");
		File pendingMovesFolder = new File(folder, "Pending");
		File uploadedMovesFolder = new File(folder, "Uploaded");

		noMovesFolder.mkdir();
		duplicateMovesFolder.mkdir();
		pendingMovesFolder.mkdir();
		uploadedMovesFolder.mkdir();

		File[] files = folder.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.startsWith("Quest_")) {
				log.info("Analyzing " + fileName);
				SuuntoMove[] moves = new SuuntoXMLParser(file).parse();
				if (moves == null) {
					log.info("There's no moves in " + file.getName());
					file.renameTo(new File(noMovesFolder, file.getName()));
					continue;
				}

				if (isDuplicated(file, pendingMovesFolder, uploadedMovesFolder)) {
					log.info(file.getName() + " duplicates existing moves");
					file.renameTo(new File(duplicateMovesFolder, file.getName()));
					continue;
				}
				log.info("Moving file into pending folder: " + file.getName());
				file.renameTo(new File(pendingMovesFolder, file.getName()));
			}
		}

		log.info("Uploading all files in pending folder.");
		ArrayList<Document> docs = new ArrayList<Document>();

		for (File file : pendingMovesFolder.listFiles()) {
			SuuntoMove[] moves = new SuuntoXMLParser(file).parse();
			for (int i = 0; i < moves.length; ++i) {
				SuuntoMove move = moves[i];
				NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(move);
				Document doc = nikeXml.getXML();
				docs.add(doc);
				// System.out.println(formatXML(doc));
			}
		}

		if (docs.size() == 0) {
			log.info("No moves to upload.");
			return;
		}

		Document[] docsArray = new Document[docs.size()];
		docs.toArray(docsArray);

		Properties nikePlusUserProperties = getNikePlusUserProperties();

		String nikeEmail = nikePlusUserProperties.getProperty(NikePlusProperties.NIKEPLUS_EMAIL);
		char[] nikePassword = nikePlusUserProperties.getProperty(NikePlusProperties.NIKEPLUS_PASSWORD).toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, docsArray, null);
		for (File file : pendingMovesFolder.listFiles()) {
			file.renameTo(new File(uploadedMovesFolder, file.getName()));
		}
	}

	private boolean isDuplicated(File newFile, File pendingMovesFolder, File uploadedMovesFolder) throws Exception
	{
		SuuntoMove[] newMoves = new SuuntoXMLParser(newFile).parse();
		File[] files = (File[]) ArrayUtils.addAll(pendingMovesFolder.listFiles(), uploadedMovesFolder.listFiles());
		for (File file : files) {
			SuuntoMove[] moves = new SuuntoXMLParser(file).parse();
			if (newMoves.length != moves.length) {
				continue;
			}
			boolean isDuplicated = true;
			for (int i = 0; i < newMoves.length; ++i) {
				if (!newMoves[i].equals(moves[i])) {
					isDuplicated = false;
					break;
				}
			}
			if (isDuplicated) {
				log.debug(newFile.getName() + " is duplicated with " + file.getName());
				return true;
			}
		}
		return false;
	}

	public boolean checkIfEnvOkay() throws IOException
	{
		File folder = getDataFolder();
		if (!folder.exists()) {
			log.error("Cannot find moves link data folder");
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moves link data folder");
		}

		File nikeplusUser = getNikeUserPropertiesFile();
		if (!nikeplusUser.exists()) {
			NikePlusProperties.getInstance().createNikePlusUserProperties(nikeplusUser);
		}
		return true;
	}


}
