package com.oldhu.suunto2nike.suunto.excel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.oldhu.suunto2nike.suunto.SuuntoMove;

public class SuuntoSheetHandler extends DefaultHandler
{
	private String lastContents;
	private String currentCell;

	private char distanceChar;
	private char heartRateChar;

	private SuuntoMove suuntoMove;

	public SuuntoMove getSuuntoMove()
	{
		return suuntoMove;
	}

	public SuuntoSheetHandler() {
		suuntoMove = new SuuntoMove();
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
	{
		if (localName.equals("c")) {
			currentCell = attributes.getValue("r");
		}
		lastContents = "";
	}

	public void endElement(String uri, String localName, String name) throws SAXException
	{
		if (localName.equals("v")) {

			if (currentCell.equals("B3")) {
				suuntoMove.setStartTime(lastContents);
			} else if (currentCell.equals("C3")) {
				suuntoMove.setDuration((int) (Float.parseFloat(lastContents) * 1000));
			} else if (currentCell.equals("D3")) {
				suuntoMove.setCalories(Integer.parseInt(lastContents));
			} else if (currentCell.equals("E3")) {
				suuntoMove.setDistance(Integer.parseInt(lastContents));
			}

			if (lastContents.equals("Distance")) {
				distanceChar = currentCell.charAt(0);
			}

			if (lastContents.equals("HeartRate")) {
				heartRateChar = currentCell.charAt(0);
			}

			if (currentCell.charAt(0) == distanceChar) {
				if (Integer.parseInt(currentCell.substring(1)) >= 3) {
					suuntoMove.addDistanceSample(lastContents);
				}
			} else if (currentCell.charAt(0) == heartRateChar) {
				if (Integer.parseInt(currentCell.substring(1)) >= 3) {
					suuntoMove.addHeartRateSample(lastContents);
				}
			}
			lastContents = "";
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		lastContents += new String(ch, start, length);
	}

}
