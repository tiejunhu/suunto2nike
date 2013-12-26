package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.oldhu.suunto2nike.nike.NikePlus;
import com.oldhu.suunto2nike.nike.NikePlusProperties;
import com.oldhu.suunto2nike.nike.NikePlusXmlGenerator;
import com.oldhu.suunto2nike.suunto.SuuntoMove;
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
	
	private Properties getNikePlusUserProperties() throws FileNotFoundException, IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream(getNikeUserPropertiesFile()));
		return prop;
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
				if (parser.isParseCompleted()) {
					uploadMoveToNike(parser.getSuuntoMove());
				}		
			}
		}
	}

	private void uploadMoveToNike(SuuntoMove suuntoMove) throws Exception
	{
		NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(suuntoMove);
		Document doc = nikeXml.getXML();
		Properties nikePlusUserProperties = getNikePlusUserProperties();
		String nikeEmail = nikePlusUserProperties.getProperty(NikePlusProperties.NIKEPLUS_EMAIL);
		char[] nikePassword = nikePlusUserProperties.getProperty(NikePlusProperties.NIKEPLUS_PASSWORD).toCharArray();
		NikePlus u = new NikePlus();
		u.fullSync(nikeEmail, nikePassword, new Document[] { doc } , null);
	}
}
