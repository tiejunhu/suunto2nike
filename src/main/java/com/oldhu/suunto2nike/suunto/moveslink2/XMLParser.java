package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLParser
{
	private static Log log = LogFactory.getLog(XMLParser.class);
	private File xmlFile;

	private Document getDocument(File xmlFile) throws Exception
	{
		BufferedReader in = new BufferedReader(new FileReader(xmlFile));
		String firstLine = in.readLine();
		if (!firstLine.trim().toLowerCase().equals("<?xml version=\"1.0\" encoding=\"utf-8\"?>")) {
			in.close();
			throw new Exception("File format not correct: " + xmlFile.getName());
		}

		StringBuilder sb = new StringBuilder();
		sb.append(firstLine);
		sb.append("<data>");
		while (in.ready()) {
			sb.append(in.readLine());
		}
		in.close();
		sb.append("</data>");
		InputStream stream = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(stream);

	}

	private boolean pareseHeader(Element header)
	{
		int moveType = Integer.parseInt(getChildElementValue(header, "ActivityType"));
		if (moveType != 3) {
			log.info("    not a running move");
			return false;
		}
		return true;
	}

	public XMLParser(File xmlFile) throws Exception
	{
		log.debug("Parsing " + xmlFile.getName());
		this.xmlFile = xmlFile;
		Document doc = getDocument(xmlFile);
		Element header = (Element) doc.getElementsByTagName("header").item(0);
		pareseHeader(header);

	}
	
	private String getChildElementValue(Element parent, String elementName)
	{
		Element child = (Element) parent.getElementsByTagName(elementName).item(0);
		return child.getTextContent();
	}
}
