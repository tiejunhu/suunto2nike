package com.oldhu.suunto2nike.nike;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientNaiveSsl
{

	public static CloseableHttpClient getClient()
	{
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException
				{
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException
				{
				}

				public X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLConnectionSocketFactory ssf = new SSLConnectionSocketFactory(ctx,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpClientBuilder hcb = HttpClientBuilder.create();
			hcb.setSSLSocketFactory(ssf);

			hcb.setSchemePortResolver(new SchemePortResolver() {

				public int resolve(HttpHost host) throws UnsupportedSchemeException
				{
					if (host.getSchemeName().equals("https")) {
						return 443;
					}
					throw new UnsupportedSchemeException(host.getSchemeName());
				}

			});
			
			RequestConfig.Builder rcb = RequestConfig.custom();
			rcb.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
			hcb.setDefaultRequestConfig(rcb.build());

			return hcb.build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
