package com.oldhu.suunto2nike.suunto.moveslink2;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class XMLParser
{
	private static Logger log = Logger.getLogger(XMLParser.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private static double PositionConstant = 57.2957795131;
	private SuuntoMove suuntoMove = new SuuntoMove();
	private boolean parseCompleted = false;

	public boolean isParseCompleted()
	{
		return parseCompleted;
	}

	public SuuntoMove getSuuntoMove()
	{
		return suuntoMove;
	}

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

	private boolean pareseHeader(Element header) throws ParseException
	{
		int moveType = Integer.parseInt(Util.getChildElementValue(header, "ActivityType"));
		if (moveType != 3 && moveType != 93 && moveType != 82) {
			log.info("    not a running move");
			return false;
		}
		int distance = Integer.parseInt(Util.getChildElementValue(header, "Distance"));
		if (distance == 0) {
			log.info("    distance zero");
			return false;
		}

		suuntoMove.setDistance(distance);
		suuntoMove.setDuration((int) (Util.doubleFromString(Util.getChildElementValue(header, "Duration"))
				.doubleValue() * 1000));
		suuntoMove.setCalories(Util.kiloCaloriesFromKilojoules(Util.doubleFromString(Util.getChildElementValue(header,
				"Energy"))));

		String dateTime = Util.getChildElementValue(header, "DateTime");
		suuntoMove.setStartTime(dateFormat.parse(dateTime));

		return true;
	}

	public XMLParser(File xmlFile) throws Exception
	{
		log.debug("Parsing " + xmlFile.getName());
		Document doc = getDocument(xmlFile);
		Element header = (Element) doc.getElementsByTagName("header").item(0);
		if (pareseHeader(header)) {
			Element samples = (Element) doc.getElementsByTagName("Samples").item(0);
			parseSamples(samples);
		}
	}

	private boolean parseSamples(Element samples) throws ArgumentOutsideDomainException
	{
		NodeList sampleList = samples.getElementsByTagName("Sample");

		ArrayList<Double> timeList = new ArrayList<Double>();
		ArrayList<Double> hrList = new ArrayList<Double>();
		ArrayList<Double> distanceList = new ArrayList<Double>();

		double pausedTime = 0.0;
		double pauseStartTime = 0.0;
		boolean inPause = true;

		for (int i = 0; i < sampleList.getLength(); ++i) {
			Element sample = (Element) sampleList.item(i);

			String pause = Util.getChildElementValue(sample, "Events", "Pause", "State");
			if (pause != null) {
				double time = Util.doubleFromString(Util.getChildElementValue(sample, "Time"));
				if (pause.equalsIgnoreCase("false")) {
					if (inPause) {
						pausedTime += time - pauseStartTime;
						inPause = false;
					}
				} else if (pause.equalsIgnoreCase("true")) {
					pauseStartTime = time;
					inPause = true;
				}
			}

			if (inPause)
				continue;

			String sampleType = Util.getChildElementValue(sample, "SampleType");

			if (sampleType == null)
				continue;

			if (sampleType.equalsIgnoreCase("periodic")) {
				String distanceStr = Util.getChildElementValue(sample, "Distance");
				if (distanceStr != null) {
					timeList.add(Util.doubleFromString(Util.getChildElementValue(sample, "Time")) - pausedTime);
					hrList.add(Util.doubleFromString(Util.getChildElementValue(sample, "HR")));					
					distanceList.add(Util.doubleFromString(distanceStr));
				}
				continue;
			}

			if (sampleType.toLowerCase().contains("gps")) {
				double lat = Util.doubleFromString(Util.getChildElementValue(sample, "Latitude")) * PositionConstant;
				double lon = Util.doubleFromString(Util.getChildElementValue(sample, "Longitude")) * PositionConstant;
				int ele = Util.doubleFromString(Util.getChildElementValue(sample, "GPSAltitude")).intValue();
				String utc = Util.getChildElementValue(sample, "UTC");
				suuntoMove.addTrackPoint(lat, lon, ele, utc);
			}
		}

		double[] timeArray = new double[timeList.size()];
		double[] hrArray = new double[hrList.size()];
		double[] distanceArray = new double[distanceList.size()];

		populateTimeArray(timeArray, timeList);
		populateHRArray(hrArray, hrList, timeArray);
		populateDistanceArray(distanceArray, distanceList);

		PolynomialSplineFunction timeToHR = generateTimeToHRSplineFunction(timeArray, hrArray);
		PolynomialSplineFunction timeToDistance = generateTimeToDistanceSplineFunction(timeArray, distanceArray);

		double t = 0;
		while (t < suuntoMove.getDuration()) {
			t += 10 * 1000;
			int hr = (int) interpolate(timeToHR, t);
			int distance = (int) interpolate(timeToDistance, t);
			suuntoMove.addHeartRateSample(hr);
			suuntoMove.addDistanceSample(distance);
		}

		parseCompleted = true;

		return true;
	}

	private double interpolate(PolynomialSplineFunction spline, double x) throws ArgumentOutsideDomainException
	{
		try {
			return spline.value(x);
		} catch (ArgumentOutsideDomainException aode) {
			double[] knots = spline.getKnots();
			return spline.value(knots[(x < knots[0]) ? 0 : spline.getN() - 1]);
		}
	}

	private PolynomialSplineFunction generateTimeToDistanceSplineFunction(double[] timeArray, double[] distanceArray)
	{
		SplineInterpolator interpolator = new SplineInterpolator();
		return interpolator.interpolate(timeArray, distanceArray);
	}

	private void populateDistanceArray(double[] distanceArray, ArrayList<Double> distanceList)
	{
		for (int i = 0; i < distanceList.size(); ++i) {
			distanceArray[i] = distanceList.get(i).doubleValue();
		}
	}

	// HR = sample data * 60
	private void populateHRArray(double[] hrArray, ArrayList<Double> hrList, double[] timeArray)
	{
		for (int i = 0; i < hrList.size(); ++i) {
			hrArray[i] = hrList.get(i).doubleValue() * 60;
		}
	}

	private void populateTimeArray(double[] timeArray, ArrayList<Double> timeList)
	{
		for (int i = 0; i < timeList.size(); ++i) {
			timeArray[i] = timeList.get(i).doubleValue() * 1000;
		}
	}

	private PolynomialSplineFunction generateTimeToHRSplineFunction(double[] timeArray, double[] hrArray)
	{
		SplineInterpolator interpolator = new SplineInterpolator();
		return interpolator.interpolate(timeArray, hrArray);
	}

}
