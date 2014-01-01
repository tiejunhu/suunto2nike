package com.oldhu.suunto2nike;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
		return (int) (kj / 4186);
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
}
