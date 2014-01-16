package com.oldhu.suunto2nike;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.oldhu.suunto2nike.suunto.moveslink.MovesLinkUploader;
import com.oldhu.suunto2nike.suunto.moveslink2.MovesLink2Uploader;

public class App
{
	private static Logger log = null;

	public static void main(String[] args) throws Exception
	{
		configDebugLogger(args);
		getLogger();
		if (log.isDebugEnabled()) {
			Util.dumpSystemEnv(log);
		}
		if (args.length >= 1) {
			if (args[0].equals("1")) {
				uploadMovesLink();
			}
			if (args[0].equals("2")) {
				uploadMovesLink2(false);
			}
			if (args[0].equals("dev")) {
				uploadMovesLink2(true);
			}
			if (args[0].equals("-debug")) {
				uploadMovesLink();
				uploadMovesLink2(false);
			}
			return;
		}

		uploadMovesLink();
		uploadMovesLink2(false);
	}
	
	private static Logger getLogger()
	{
		if (log == null) {
			log = Logger.getLogger(App.class);
		}
		return log;
	}

	private static void configDebugLogger(String[] args) throws MalformedURLException
	{
		for (String arg : args) {
			if (arg.equalsIgnoreCase("-debug")) {
				System.setProperty("log4j.configuration", "log4j-debug.properties");
				getLogger().info("Debug logger config done.");
			}
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
