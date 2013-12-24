package com.oldhu.suunto2nike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.excel.SuuntoExcelParser;
import com.oldhu.suunto2nike.moveslink.SuuntoMove;
import com.oldhu.suunto2nike.moveslink.SuuntoXMLParser;
import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;

public class App
{
	private static final String NIKEPLUS_PASSWORD = "NIKEPLUS_PASSWORD";
	private static final String NIKEPLUS_EMAIL = "NIKEPLUS_EMAIL";
	private static Log log = LogFactory.getLog("App");

	public static void main(String[] args) throws Exception
	{
		if (!checkIfEnvOkay()) {
			return;
		}

		if (args.length == 1) {
			String excelFile = args[0];
			log.info("Uploading excel file " + excelFile);
			uploadExcelFile(excelFile);
			return;
		}
		uploadXMLFiles();
	}

	private static boolean checkIfEnvOkay() throws IOException
	{
		File folder = getMovesLinkDataFolder();
		if (!folder.exists()) {
			log.error("Cannot find moves link data folder");
			return false;
		}
		if (!folder.canWrite()) {
			log.error("Cannot write to moves link data folder");
		}

		File nikeplusUser = getNikeUserPropertiesFile();
		if (!nikeplusUser.exists()) {
			createNikePlusUserProperties();
		}
		return true;
	}

	private static void createNikePlusUserProperties() throws IOException
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
		
		FileOutputStream fos = new FileOutputStream(getNikeUserPropertiesFile());
		prop.store(fos, "SYSTEM GENERATED");
		fos.close();
	}
	
	private static Properties getNikePlusUserProperties() throws FileNotFoundException, IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream(getNikeUserPropertiesFile()));
		return prop;
	}

	private static File getMovesLinkDataFolder()
	{
		String userHome = System.getProperty("user.home");
		File folder = new File(new File(userHome), "AppData/Roaming/Suunto/Moveslink");
		return folder;
	}

	private static File getNikeUserPropertiesFile()
	{
		return new File(getMovesLinkDataFolder(), "nikeuser.properties");
	}

	private static void uploadXMLFiles() throws Exception
	{
		File folder = getMovesLinkDataFolder();
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
//				System.out.println(formatXML(doc));
			}
		}

		if (docs.size() == 0) {
			log.info("No moves to upload.");
			return;
		}

		Document[] docsArray = new Document[docs.size()];
		docs.toArray(docsArray);
		
		Properties nikePlusUserProperties = getNikePlusUserProperties();

		String nikeEmail = nikePlusUserProperties.getProperty(NIKEPLUS_EMAIL);
		char[] nikePassword = nikePlusUserProperties.getProperty(NIKEPLUS_PASSWORD).toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, docsArray, null);
		for (File file : pendingMovesFolder.listFiles()) {
			file.renameTo(new File(uploadedMovesFolder, file.getName()));
		}
	}

	private static boolean isDuplicated(File newFile, File pendingMovesFolder, File uploadedMovesFolder)
			throws Exception
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

	private static void uploadExcelFile(String fileName) throws Exception
	{
		SuuntoExcelParser parser = new SuuntoExcelParser();
		SuuntoMove move = parser.parse(fileName);
		NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(move);
		Document doc = nikeXml.getXML();

		System.out.println(formatXML(doc));

		Properties nikePlusUserProperties = getNikePlusUserProperties();

		String nikeEmail = nikePlusUserProperties.getProperty(NIKEPLUS_EMAIL);
		char[] nikePassword = nikePlusUserProperties.getProperty(NIKEPLUS_PASSWORD).toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, new Document[] { doc }, null);

	}

	private static String formatXML(Document doc) throws Exception
	{
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
	}

}
