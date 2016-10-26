package com.oldhu.suunto2nike.nike;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.oldhu.suunto2nike.Util;
import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class NikePlusGpxGenerator
{
	private SuuntoMove move;

	public NikePlusGpxGenerator(SuuntoMove move)
	{
		this.move = move;
	}

	public Document getXML() throws Exception
	{
		if (!move.getTrackPoints().iterator().hasNext()) {
			return null;
		}
		
		Document doc = newDocument();
		Element gpxElement = Util.appendElement(doc, "gpx", null, "xmlns", "http://www.topografix.com/GPX/1/1",
				"creator", "NikePlus", "version", "1.1");

		// trk
		Element trk = Util.appendElement(gpxElement, "trk");
		Util.appendCDATASection(trk, "name", "4c888a06");
		Util.appendCDATASection(trk, "desc", "workout");

		// trkseg
		Element trkSeg = Util.appendElement(trk, "trkseg");
		for (SuuntoMove.TrackPoint trackPoint : move.getTrackPoints()) {
			Element trkptNike = Util.appendElement(trkSeg, "trkpt", null, "lat", trackPoint.getLatitude(), "lon",
					trackPoint.getLongitude());
			Util.appendElement(trkptNike, "ele", trackPoint.getElevation());
			Util.appendElement(trkptNike, "time", trackPoint.getTime());
		}
		return doc;
	}

	private Document newDocument() throws Exception
	{

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}
}
