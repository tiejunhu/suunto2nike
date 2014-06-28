package com.oldhu.suunto2nike;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util
{

	public static String formatXML(Document doc) throws Exception
	{
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		return result.getWriter().toString();
	}

	public static int kiloCaloriesFromKilojoules(double kj)
	{
		return (int) (kj / 4184);
	}

	public static String getChildElementValue(Element parent, String... elementNames)
	{
		for (int i = 0; i < elementNames.length; ++i) {
			String elementName = elementNames[i];
			NodeList nodeList = parent.getElementsByTagName(elementName);
			if (nodeList.getLength() != 1)
				return null;
			Element child = (Element) nodeList.item(0);
			if (i == elementNames.length - 1) {
				return child.getTextContent();
			}
			parent = child;
		}
		return null;
	}

	public static Double doubleFromString(String str)
	{
		if (str == null) {
			return Double.valueOf(0);
		}
		return Double.valueOf(str);
	}

	public static boolean isWindows()
	{
		String OS = System.getProperty("os.name").toLowerCase();
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac()
	{
		String OS = System.getProperty("os.name").toLowerCase();
		return (OS.indexOf("mac") >= 0);
	}

	public static Element appendElement(Node parent, String name)
	{
		return appendElement(parent, name, null);
	}

	public static Element appendElement(Node parent, String name, Object data, String... attributes)
	{
		Document doc = (parent.getNodeType() == Node.DOCUMENT_NODE) ? (Document) parent : parent.getOwnerDocument();
		Element e = doc.createElement(name);
		parent.appendChild(e);

		if (data != null)
			e.appendChild(doc.createTextNode(data.toString()));

		for (int i = 0; i < attributes.length; ++i)
			e.setAttribute(attributes[i++], attributes[i]);

		return e;
	}

	public static void appendCDATASection(Node parent, String name, Object data)
	{
		Document doc = (parent.getNodeType() == Node.DOCUMENT_NODE) ? (Document) parent : parent.getOwnerDocument();
		Element e = doc.createElement(name);
		parent.appendChild(e);
		e.appendChild(doc.createCDATASection(data.toString()));
	}

	public static void dumpSystemEnv(Logger log)
	{
		Properties prop = System.getProperties();
		for (Entry<Object, Object> entry : prop.entrySet()) {
			log.debug("System property -- " + entry.getKey() + " : " + entry.getValue());
		}
		log.debug("System env -- APPDATA : " + System.getenv("APPDATA"));
	}

	public static File getSuuntoHome()
	{
		if (Util.isWindows()) {
			String appData = System.getenv("APPDATA");
			return new File(new File(appData), "Suunto");
		}
		if (Util.isMac()) {
			String userHome = System.getProperty("user.home");
			return new File(new File(userHome), "Library/Application Support/Suunto/");
		}
		return null;
	}

	public static String isToString(final InputStream is, final int bufferSize)
	{
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try {
			final Reader in = new InputStreamReader(is, "UTF-8");
			try {
				for (;;) {
					int rsz = in.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			} finally {
				in.close();
			}
		} catch (UnsupportedEncodingException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		}
		return out.toString();
	}
}
