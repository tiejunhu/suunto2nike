package com.oldhu.suunto2nike.nike;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class NikePlus
{

	// TODO: Tidy up exception-handling... java 7?
	public static Properties nikePlusProperties;
	static {
		nikePlusProperties = new Properties();
		InputStream in = null;
		try {
			in = NikePlus.class.getResourceAsStream("/nikeplus.properties");
			nikePlusProperties.load(in);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
	}

	private static final String URL_LOGIN_DOMAIN = "secure-nikeplus.nike.com";
	private static final String URL_LOGIN = String.format("https://%s/login/loginViaNike.do?mode=login",
			URL_LOGIN_DOMAIN);

	// private static final String URL_LOGIN = String.format(
	// "https://api.nike.com/nsl/v2.0/user/login?client_id=%s&client_secret=%s&app=%s",
	// nikePlusProperties.getProperty("NIKEPLUS_CLIENT_ID"),
	// nikePlusProperties.getProperty("NIKEPLUS_CLIENT_SECRET"),
	// nikePlusProperties.getProperty("NIKEPLUS_APP"));
	private static final String URL_DATA_SYNC = "https://api.nike.com/v2.0/me/sync?access_token=%s";
	private static final String URL_DATA_SYNC_COMPLETE_ACCESS_TOKEN = "https://api.nike.com/v2.0/me/sync/complete";

	private static final String USER_AGENT = "NPConnect";

	private static final int URL_DATA_SYNC_SUCCESS = 200;

	private static final Logger log = Logger.getLogger(NikePlus.class);

	private String _accessToken;

	public NikePlus()
	{
	}

	private UrlEncodedFormEntity generateFormNVPs(String... nvps) throws UnsupportedEncodingException
	{
		int length = nvps.length;
		if ((length % 2) != 0)
			throw new IllegalArgumentException(String.format("Odd number of name-value pairs: %d", length));

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (int i = 0; i < length;) {
			formparams.add(new BasicNameValuePair(nvps[i++], nvps[i++]));
		}
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

		return formEntity;
	}

	private SetCookie createCookie(String key, String value)
	{
		SetCookie cookie = new BasicClientCookie(key, value);
		cookie.setPath("/");
		cookie.setDomain(URL_LOGIN_DOMAIN);
		return cookie;
	}

	/**
	 * Logins into Nike+, setting the access_token, expires_in, refresh_token
	 * and pin
	 * 
	 * @param login
	 *            The login String (email address).
	 * @param password
	 *            The login password.
	 * @return Nike+ pin.
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws UnsupportedEncodingException
	 */
	public void login(String login, char[] password) throws IOException, MalformedURLException,
			ParserConfigurationException, SAXException, UnsupportedEncodingException
	{
		CookieStore cookieStore = new BasicCookieStore();
		cookieStore.addCookie(createCookie("app", nikePlusProperties.getProperty("NIKEPLUS_APP")));
		cookieStore.addCookie(createCookie("client_id", nikePlusProperties.getProperty("NIKEPLUS_CLIENT_ID")));
		cookieStore.addCookie(createCookie("client_secret", nikePlusProperties.getProperty("NIKEPLUS_CLIENT_SECRET")));

		CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
		try {

			HttpPost post = new HttpPost(URL_LOGIN);
			post.addHeader("user-agent", USER_AGENT);
			post.setEntity(generateFormNVPs("email", login, "password", new String(password)));

			HttpClientContext httpClientContext = HttpClientContext.create();
			CloseableHttpResponse response = client.execute(post, httpClientContext);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				for (Cookie cookie : httpClientContext.getCookieStore().getCookies()) {
					if (cookie.getName().equals("access_token")) {
						_accessToken = cookie.getValue();
					}
				}
			}
			if (_accessToken == null)
				throw new IllegalArgumentException(
						"Unable to authenticate with nike+. Please check email and password.");
			log.info("access token is " + _accessToken);

		} finally {
			client.close();
		}
	}

	/**
	 * Calls fullSync converting the File objects to Document.
	 * 
	 * @param pin
	 *            Nike+ pin.
	 * @param runXml
	 *            Nike+ workout xml.
	 * @param gpxXml
	 *            Nike+ gpx xml.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public void fullSync(String nikeEmail, char[] nikePassword, File runXml, File gpxXml)
			throws ParserConfigurationException, SAXException, IOException, MalformedURLException,
			NoSuchAlgorithmException, KeyManagementException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		fullSync(nikeEmail, nikePassword, db.parse(runXml), ((gpxXml != null) ? db.parse(gpxXml) : null));
	}

	private void fullSync(String nikeEmail, char[] nikePassword, Document runXml, Document gpxXml)
			throws KeyManagementException, MalformedURLException, NoSuchAlgorithmException, IOException,
			ParserConfigurationException, SAXException
	{
		fullSync(nikeEmail, nikePassword, new Document[] { runXml }, ((gpxXml != null) ? new Document[] { gpxXml }
				: null));
	}

	/**
	 * Does a full synchronisation cycle (check-pin-status, sync, end-sync) with
	 * nike+ for the given credentials and xml document(s).
	 * 
	 * @param pin
	 *            Nike+ pin.
	 * @param runXml
	 *            Nike+ workout xml array.
	 * @param gpxXml
	 *            Nike+ gpx xml array.
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public void fullSync(String nikeEmail, char[] nikePassword, Document[] runXml, Document[] gpxXml)
			throws IOException, MalformedURLException, ParserConfigurationException, SAXException,
			NoSuchAlgorithmException, KeyManagementException
	{

		log.info("Uploading to Nike+...");
		log.info("Authenticating...");
		login(nikeEmail, nikePassword);

		try {
			log.info("Syncing data...");

			boolean error = false;
			int activitiesLength = runXml.length;

			for (int i = 0; i < activitiesLength; ++i) {
				log.info(String.format("   Syncing: %d", (i + 1)));

				Document gpx = (gpxXml != null) ? gpxXml[i] : null;

				error |= (!syncData(runXml[i], gpx));
			}

			if (error)
				throw new RuntimeException(
						"There was a problem uploading to nike+.  Please try again later, if the problem persists contact me with details of the activity-id or tcx file.");
		} finally {
			log.info("Ending sync...");
			endSync();
		}
	}

	public boolean syncData(Document runXml, Document gpxXml) throws ClientProtocolException, IOException,
			IllegalStateException, SAXException, ParserConfigurationException
	{
		CloseableHttpClient client = HttpClientNaiveSsl.getClient();

		try {
			HttpPost post = new HttpPost(String.format(URL_DATA_SYNC, _accessToken));
			post.addHeader("user-agent", USER_AGENT);
			post.addHeader("appid", "NIKEPLUSGPS");

			// Add run data to the request.
			MultipartEntityBuilder meb = MultipartEntityBuilder.create();
			meb.setStrictMode();
			meb.addBinaryBody("runXML", documentToString(runXml).getBytes(), ContentType.DEFAULT_BINARY, "runXML.xml");

			if (gpxXml != null) {
				meb.addBinaryBody("gpxXML", documentToString(gpxXml).getBytes(), ContentType.DEFAULT_BINARY,
						"gpxXML.xml");
			}

			post.setEntity(meb.build());

			HttpResponse response = client.execute(post);
			return (URL_DATA_SYNC_SUCCESS == response.getStatusLine().getStatusCode());

		} finally {
			client.close();
		}
	}

	public Document endSync() throws ClientProtocolException, IOException, IllegalStateException, SAXException,
			ParserConfigurationException
	{
		CloseableHttpClient client = HttpClientNaiveSsl.getClient();

		try {
			HttpPost post = new HttpPost(String.format("%s?%s", URL_DATA_SYNC_COMPLETE_ACCESS_TOKEN,
					generateHttpParameter("access_token", _accessToken)));
			post.addHeader("user-agent", USER_AGENT);
			post.addHeader("appId", "NIKEPLUSGPS");
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Document outDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entity.getContent());
				EntityUtils.consume(entity);
				outDoc.normalize();
				log.debug(documentToString(outDoc));
				return outDoc;
			} else
				throw new NullPointerException("Http response empty");
		} finally {
			client.close();
		}
	}

	private static String generateHttpParameter(String key, String val) throws UnsupportedEncodingException
	{
		return String.format("%s=%s", URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(val, "UTF-8"));
	}

	private static String documentToString(Document doc)
	{
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			Writer outWriter = new StringWriter();
			StreamResult result = new StreamResult(outWriter);
			transformer.transform(source, result);
			return outWriter.toString();
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

}
