package com.oldhu.suunto2nike;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SuuntoXMLParser
{
	private static Log log = LogFactory.getLog("SuuntoXMLParser");
	private final Pattern durationPattern = Pattern.compile("(\\d+):(\\d+):(\\d+)\\.?(\\d*)");

	private Element moves;
	private File xmlFile;

	public SuuntoXMLParser(File xmlFile) throws Exception
	{
		log.debug("Parsing " + xmlFile.getName());
		this.xmlFile = xmlFile;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document document = dBuilder.parse(xmlFile);

		NodeList movesCountList = document.getElementsByTagName("MovesCount");
		if (movesCountList.getLength() != 1) {
			throw new Exception("not valid moves count xml file, MovesCount node count: " + movesCountList.getLength());
		}
		Element movesCount = (Element) movesCountList.item(0);

		NodeList movesList = movesCount.getElementsByTagName("Moves");
		if (movesList.getLength() != 1) {
			throw new Exception("not valid moves count xml file, Moves node count: " + movesList.getLength());
		}
		moves = (Element) movesList.item(0);
	}

	public SuuntoMove[] parse() throws Exception
	{
		NodeList moveList = moves.getElementsByTagName("Move");
		if (moveList.getLength() == 0) {
			log.debug("No moves data in " + xmlFile.getName());
			return null;
		}
		
		ArrayList<SuuntoMove> suuntoMoves = new ArrayList<SuuntoMove>();
		log.debug(moveList.getLength() + " move elements in this file");

		for (int i = 0; i < moveList.getLength(); ++i) {
			try {
				Element move = (Element) moveList.item(i);
				SuuntoMove suuntoMove = new SuuntoMove();
				Element header = (Element) move.getElementsByTagName("Header").item(0);
				parseHeader(header, suuntoMove);
				Element samples = (Element) move.getElementsByTagName("Samples").item(0);
				parseSamples(samples, suuntoMove);	
				suuntoMoves.add(suuntoMove);
			} catch (Exception e) {
				log.info("Data invalid in the no. " + (i + 1) + " of the moves");
			}
			
		}
		
		if (suuntoMoves.size() == 0) {
			return null;
		}
		
		SuuntoMove[] moves = new SuuntoMove[suuntoMoves.size()];
		suuntoMoves.toArray(moves);

		return moves;
	}

	private void parseSamples(Element samples, SuuntoMove suuntoMove)
	{
		String distanceStr = getChildElementValue(samples, "Distance");
		String heartRateStr = getChildElementValue(samples, "HR");
		int currentSum = 0;
		for (String distance : distanceStr.split(" ")) {
			if (distance.trim().isEmpty()) {
				continue;
			}
			currentSum += Integer.parseInt(distance);
			suuntoMove.addDistanceSample(Integer.toString(currentSum));
		}
		for (String heartRate : heartRateStr.split(" ")) {
			if (heartRate.trim().isEmpty()) {
				continue;
			}
			suuntoMove.addHeartRateSample(heartRate);
		}
	}

	private void parseHeader(Element header, SuuntoMove suuntoMove) throws ParseException
	{
		suuntoMove.setCalories(Integer.parseInt(getChildElementValue(header, "Calories")));
		suuntoMove.setDistance(Integer.parseInt(getChildElementValue(header, "Distance")));
		suuntoMove.setStartTime(getChildElementValue(header, "Time"));
		
		String durationStr = getChildElementValue(header, "Duration");
		Matcher matcher = durationPattern.matcher(durationStr);
		if (matcher.matches()) {
			int hour = Integer.parseInt(matcher.group(1));
			int minute = Integer.parseInt(matcher.group(2));
			int second = Integer.parseInt(matcher.group(3));
			int ms = 0;
			if (!matcher.group(4).isEmpty()) {
				ms = Integer.parseInt(matcher.group(4));
			}
			ms = (hour * 3600 + minute * 60 + second) * 1000 + ms;
			suuntoMove.setDuration(ms);
		}
	}
	
	private String getChildElementValue(Element parent, String elementName)
	{
		Element child = (Element) parent.getElementsByTagName(elementName).item(0);
		return child.getTextContent();
	}
}
