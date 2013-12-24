package com.oldhu.suunto2nike.excel;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.oldhu.suunto2nike.moveslink.SuuntoMove;

public class SuuntoExcelParser
{
	public SuuntoMove parse(String filePath) throws Exception
	{
		OPCPackage packge = OPCPackage.open(filePath);
		XSSFReader reader = new XSSFReader(packge);
		SuuntoSheetHandler handler = new SuuntoSheetHandler();
		XMLReader parser = fetchSheetParser(handler);
		Iterator<InputStream> iss = reader.getSheetsData();
		if (iss.hasNext()) {
			InputStream is = iss.next();
			InputSource sheetSource = new InputSource(is);
			parser.parse(sheetSource);
			return handler.getSuuntoMove();
		}
		return null;
	}

	private XMLReader fetchSheetParser(ContentHandler handler) throws SAXException
	{
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		parser.setContentHandler(handler);
		return parser;
	}
}
