package com.ndcf.spider.crawler.httpclient;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.*;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author bangis
 * 
 */
public class HttpCompoentsUtil {
	private final String _DEFLAUT_CHARSET = "utf-8";
	private CloseableHttpClient httpClient;

	/**
	 * 设置超时，毫秒级别
	 */
	private RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(1000).setConnectTimeout(1000).build();
	private BasicCookieStore cookieStore = new BasicCookieStore();
	{
		try {
			// 保持连接时长
			ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {

				@Override
				public long getKeepAliveDuration(HttpResponse response,
												 HttpContext context) {
					long keepAlive = super.getKeepAliveDuration(response,
							context);
					if (keepAlive == -1) {
						// 如果服务器没有设置keep-alive这个参数，我们就把它设置成5秒
						keepAlive = 5000;
					}
					return keepAlive;
				}

			};
			// 重试机制
			HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
				public boolean retryRequest(IOException exception,
											int executionCount, HttpContext context) {
					if (executionCount >= 5) {
						// 如果已经重试了5次，就放弃
						return false;
					}
					if (exception instanceof InterruptedIOException) {
						// 超时
						return false;
					}
					if (exception instanceof UnknownHostException) {
						// 目标服务器不可达
						return false;
					}
					if (exception instanceof ConnectTimeoutException) {
						// 连接被拒绝
						return false;
					}
					if (exception instanceof SSLException) {
						// ssl握手异常
						return false;
					}
					HttpClientContext clientContext = HttpClientContext
							.adapt(context);
					HttpRequest request = clientContext.getRequest();
					boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
					if (idempotent) {
						// 如果请求是幂等的，就再次尝试
						return true;
					}
					return false;
				}

			};
			CookieSpecProvider easySpecProvider = new CookieSpecProvider() {

				public CookieSpec create(HttpContext context) {

					return new BrowserCompatSpec() {
						@Override
						public void validate(Cookie cookie, CookieOrigin origin)
								throws MalformedCookieException {
							// Oh, I am easy
						}
					};
				}

			};
			SSLContext sslContext;
			sslContext = new SSLContextBuilder().loadTrustMaterial(null,
					new TrustStrategy() {
						// 信任所有
						public boolean isTrusted(X509Certificate[] chain,
												 String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext);

			RegistryBuilder
					.<CookieSpecProvider> create()
					.register(CookieSpecs.BEST_MATCH,
							new BestMatchSpecFactory())
					.register(CookieSpecs.BROWSER_COMPATIBILITY,
							new BrowserCompatSpecFactory())
					.register("easy", easySpecProvider).build();

			RequestConfig globalConfig = RequestConfig.custom()
					.setCookieSpec("easy").build();

			httpClient = HttpClients.custom()
					.setKeepAliveStrategy(keepAliveStrat)
					.setRetryHandler(retryHandler)
					.setDefaultRequestConfig(globalConfig)
					.setDefaultCookieStore(cookieStore)
					// .setSslcontext(sslContext)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTimeOut(int socketTimeOut, int connectTimeOut) {
		requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeOut)
				.setConnectTimeout(connectTimeOut).build();
	}

	public String get(String url) {
		return get(url, _DEFLAUT_CHARSET);
	}

	public String get(String url, String charset) {
		return get(url, null, charset);
	}

	public String get(String url, Map<String, String> headers, String charset) {
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		String useCharset = charset;
		if (charset == null) {
			useCharset = _DEFLAUT_CHARSET;
		}
		HttpGet httpGet = new HttpGet(url);
		if (headers != null) {
			for (String key : headers.keySet()) {
				httpGet.setHeader(key, headers.get(key));
			}
		}
		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet,
					context);
			try {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, useCharset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String post(String url, Map<String, String> params) {
		return post(url, params, null, null);
	}

	public String post(String url, Map<String, String> params,
					   Map<String, String> headers) {
		return post(url, params, headers, null);
	}

	public String post(String url, Map<String, String> params, String charset) {
		return post(url, params, null, charset);
	}

	public String post(String url, Map<String, String> params,
					   Map<String, String> headers, String charset) {
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		String useCharset = charset;
		if (charset == null) {
			useCharset = _DEFLAUT_CHARSET;
		}
		try {
			HttpPost httpPost = new HttpPost(url);
			if (headers != null) {
				for (String key : headers.keySet()) {
					httpPost.setHeader(key, headers.get(key));
				}
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			if (params != null) {
				for (String key : params.keySet()) {
					nvps.add(new BasicNameValuePair(key, params.get(key)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			}
			httpPost.setConfig(requestConfig);
			CloseableHttpResponse response = httpClient.execute(httpPost,
					context);
			try {
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, useCharset);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getCookie(String key) {
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(key)) {
					return c.getValue();
				}
			}
		}
		return null;
	}

	public void printCookies() {
		System.out.println("---查看当前Cookie---");
		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				System.out.print(c.getName() + "         :" + c.getValue());
				System.out.print("	domain:" + c.getDomain());
				System.out.print("	expires:" + c.getExpiryDate());
				System.out.print("	path:" + c.getPath());
				System.out.println("	version:" + c.getVersion());
			}
		}
	}

}
