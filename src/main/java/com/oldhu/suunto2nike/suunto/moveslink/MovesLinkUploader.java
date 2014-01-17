package com.oldhu.suunto2nike.suunto.moveslink;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class MovesLinkUploader
{
	private static MovesLinkUploader _instance = new MovesLinkUploader();
	private static Logger log = Logger.getLogger(MovesLinkUploader.class);
	private NikePlusProperties nikePlusProperties;

	public static MovesLinkUploader getInstance()
	{
		return _instance;
	}

	private MovesLinkUploader()
	{

	}

	private File getDataFolder()
	{
		File suuntoHome = Util.getSuuntoHome();
		if (suuntoHome == null) {
			return null;
		}
		return new File(suuntoHome, "Moveslink");
	}

	public void uploadXMLFiles() throws Exception
	{
		File folder = getDataFolder();
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
			String fileName = file.getName().toLowerCase();
			if (fileName.startsWith("quest_") && fileName.endsWith(".xml")) {
				log.info("Analyzing " + fileName);
				SuuntoMove[] moves = new XMLParser(file).parse();
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
			SuuntoMove[] moves = new XMLParser(file).parse();
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

		String nikeEmail = nikePlusProperties.getEmail();
		char[] nikePassword = nikePlusProperties.getPassword().toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, docsArray, null);
		for (File file : pendingMovesFolder.listFiles()) {
			file.renameTo(new File(uploadedMovesFolder, file.getName()));
		}
	}

	private boolean isDuplicated(File newFile, File pendingMovesFolder, File uploadedMovesFolder) throws Exception
	{
		SuuntoMove[] newMoves = new XMLParser(newFile).parse();
		File[] files = (File[]) ArrayUtils.addAll(pendingMovesFolder.listFiles(), uploadedMovesFolder.listFiles());
		for (File file : files) {
			SuuntoMove[] moves = new XMLParser(file).parse();
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
			log.info("Cannot find MovesLink data folder at " + folder.getAbsolutePath());
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moves link data folder at " + folder.getAbsolutePath());
		}
		
		nikePlusProperties = new NikePlusProperties(getDataFolder());

		return true;
	}


}
