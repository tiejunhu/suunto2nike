package com.oldhu.suunto2nike;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oldhu.suunto2nike.suunto.moveslink.MovesLinkUploader;
import com.oldhu.suunto2nike.suunto.moveslink2.MovesLink2Uploader;

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
				uploadMovesLink2(false);			
			}
			if (args[0].equals("dev")) {
				uploadMovesLink2(true);
			}
		} else {
			uploadMovesLink();			
			uploadMovesLink2(false);			
		}
	}

	private static void uploadMovesLink() throws IOException, Exception
	{
		log.info("Uploading MovesLink ...");
		MovesLinkUploader mlf = MovesLinkUploader.getInstance();
		if (!mlf.checkIfEnvOkay()) {
			return;
		}
		mlf.uploadXMLFiles();
		log.info("Upload MovesLink done.");
	}

	private static void uploadMovesLink2(boolean dev) throws IOException, Exception
	{
		log.info("Uploading MovesLink2 ...");
		MovesLink2Uploader ml2f = new MovesLink2Uploader(dev);
		if (!ml2f.checkIfEnvOkay()) {
			return;
		}
		ml2f.uploadXMLFiles();
		log.info("Upload MovesLink2 done.");
	}

}
