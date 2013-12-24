package com.oldhu.suunto2nike;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.moveslink.MovesLinkFactory;

public class App
{
	private static Log log = LogFactory.getLog("App");

	public static void main(String[] args) throws Exception
	{
		MovesLinkFactory mlf = MovesLinkFactory.getInstance();

		if (!mlf.checkIfEnvOkay()) {
			return;
		}

		// if (args.length == 1) {
		// String excelFile = args[0];
		// log.info("Uploading excel file " + excelFile);
		// uploadExcelFile(excelFile);
		// return;
		// }

		mlf.uploadXMLFiles();
	}

	// private static void uploadExcelFile(String fileName) throws Exception
	// {
	// SuuntoExcelParser parser = new SuuntoExcelParser();
	// SuuntoMove move = parser.parse(fileName);
	// NikePlusXmlGenerator nikeXml = new NikePlusXmlGenerator(move);
	// Document doc = nikeXml.getXML();
	//
	// System.out.println(formatXML(doc));
	//
	// Properties nikePlusUserProperties = getNikePlusUserProperties();
	//
	// String nikeEmail = nikePlusUserProperties.getProperty(NIKEPLUS_EMAIL);
	// char[] nikePassword =
	// nikePlusUserProperties.getProperty(NIKEPLUS_PASSWORD).toCharArray();
	// NikePlus u = new NikePlus();
	// u.fullSync(nikeEmail, nikePassword, new Document[] { doc }, null);
	//
	// }

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
