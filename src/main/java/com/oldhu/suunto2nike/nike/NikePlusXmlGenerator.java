package com.oldhu.suunto2nike.nike;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.oldhu.suunto2nike.moveslink.SuuntoMove;

public class NikePlusXmlGenerator
{
	private static final String DATE_TIME_FORMAT_NIKE = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String DATE_TIME_FORMAT_SUUNTO = "yyyy-MM-dd HH:mm:ss";

	private SuuntoMove move;

	public NikePlusXmlGenerator(SuuntoMove move)
	{
		this.move = move;
	}

	private Document newDocument() throws Exception
	{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}

	public Document getXML() throws Exception
	{
		Document doc = newDocument();
		Element rootSportsData = appendElement(doc, "sportsData");

		appendRunSummary(rootSportsData);
		appendTemplate(rootSportsData);
		appendGoalType(rootSportsData);
		appendUserInfo(rootSportsData);
		appendStartTimeElement(rootSportsData);
		appendExtendedDataList(rootSportsData);

		return doc;
	}

	private void appendRunSummary(Element parent) throws ParseException
	{
		Element runSummary = appendElement(parent, "runSummary");
		appendRunSummaryStartTime(runSummary);
		appendRunSummaryTotals(runSummary);
		appendElement(runSummary, "battery");
	}

	private String getNikeFormatStartTime() throws ParseException
	{
		SimpleDateFormat dfNike = new SimpleDateFormat(DATE_TIME_FORMAT_NIKE);
		SimpleDateFormat dfSuunto = new SimpleDateFormat(DATE_TIME_FORMAT_SUUNTO);
		Date startTime = dfSuunto.parse(move.getStartTime());
		String startTimeNikeStr = dfNike.format(startTime);
		return String.format("%s:%s", startTimeNikeStr.substring(0, 22), startTimeNikeStr.substring(22));
	}

	private void appendRunSummaryStartTime(Element runSummary) throws ParseException
	{
		appendElement(runSummary, "time", getNikeFormatStartTime());
	}

	private void appendRunSummaryTotals(Element runSummary)
	{
		appendElement(runSummary, "duration", move.getDuration());
		float distance = move.getDistance() / 1000.0f;
		appendElement(runSummary, "distance", String.format("%.4f", distance), "unit", "km");
		appendElement(runSummary, "calories", move.getCalories());
	}

	private void appendTemplate(Element sportsDataElement)
	{
		Element templateElement = appendElement(sportsDataElement, "template");
		appendCDATASection(templateElement, "templateName", "Basic");
	}

	private void appendGoalType(Element sportsDataElement)
	{
		appendElement(sportsDataElement, "goal", null, "type", "", "value", "", "unit", "");
	}

	private void appendUserInfo(Element sportsDataElement)
	{
		Element userInfo = appendElement(sportsDataElement, "userInfo");
		appendElement(userInfo, "empedID", "XXXXXXXXXXX");
		appendElement(userInfo, "weight");

		appendElement(userInfo, "device", "iPod"); // iPod
		appendElement(userInfo, "calibration");
	}

	private void appendStartTimeElement(Element sportsDataElement) throws Exception
	{
		appendElement(sportsDataElement, "startTime", getNikeFormatStartTime());
	}

	private void appendExtendedDataList(Element sportsDataElement)
	{
		StringBuilder sbDistance = new StringBuilder();
		StringBuilder sbSpeed = new StringBuilder();
		StringBuilder sbHeartRate = new StringBuilder();

		for (int i = 0; i < move.getDistanceSamples().size(); ++i) {
			float distance = Float.parseFloat(move.getDistanceSamples().get(i)) / 1000;
			sbDistance.append(String.format("%.4f", distance));
			sbSpeed.append("0.0000");
			sbHeartRate.append(move.getHeartRateSamples().get(i));
			if (i < move.getDistanceSamples().size() - 1) {
				sbDistance.append(", ");
				sbSpeed.append(", ");
				sbHeartRate.append(", ");
			}
		}

		Element extendedDataListElement = appendElement(sportsDataElement, "extendedDataList");
		appendElement(extendedDataListElement, "extendedData", sbDistance, "dataType", "distance", "intervalType",
				"time", "intervalUnit", "s", "intervalValue", "10");
//		appendElement(extendedDataListElement, "extendedData", sbSpeed, "dataType", "speed", "intervalType", "time",
//				"intervalUnit", "s", "intervalValue", "10");
		appendElement(extendedDataListElement, "extendedData", sbHeartRate, "dataType", "heartRate", "intervalType",
				"time", "intervalUnit", "s", "intervalValue", "10");
	}

	private Element appendElement(Node parent, String name)
	{
		return appendElement(parent, name, null);
	}

	private Element appendElement(Node parent, String name, Object data, String... attributes)
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

	private void appendCDATASection(Node parent, String name, Object data)
	{
		Document doc = (parent.getNodeType() == Node.DOCUMENT_NODE) ? (Document) parent : parent.getOwnerDocument();
		Element e = doc.createElement(name);
		parent.appendChild(e);
		e.appendChild(doc.createCDATASection(data.toString()));
	}
}
