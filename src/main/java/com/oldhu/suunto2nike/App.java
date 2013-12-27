package com.oldhu.suunto2nike;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.oldhu.suunto2nike.suunto.moveslink.MovesLinkFactory;
import com.oldhu.suunto2nike.suunto.moveslink2.MovesLink2Factory;

public class App
{
	private static Log log = LogFactory.getLog("App");

	public static void main(String[] args) throws Exception
	{
		if (args.length == 1) {
			if (args[0].equals("1")) {
				uploadMovesLink();			
			}
			if (args[0].equals("2")) {
				uploadMovesLink2();			
			}
		} else {
			uploadMovesLink();			
			uploadMovesLink2();			
		}
	}

	private static void uploadMovesLink() throws IOException, Exception
	{
		log.info("Uploading MovesLink ...");
		MovesLinkFactory mlf = MovesLinkFactory.getInstance();
		if (!mlf.checkIfEnvOkay()) {
			return;
		}
		mlf.uploadXMLFiles();
		log.info("Upload MovesLink done.");
	}

	private static void uploadMovesLink2() throws IOException, Exception
	{
		log.info("Uploading MovesLink2 ...");
		MovesLink2Factory ml2f = MovesLink2Factory.getInstance();
		if (!ml2f.checkIfEnvOkay()) {
			return;
		}
		ml2f.uploadXMLFiles();
		log.info("Upload MovesLink2 done.");
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
