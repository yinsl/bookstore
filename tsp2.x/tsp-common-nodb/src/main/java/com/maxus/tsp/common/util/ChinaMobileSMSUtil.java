package com.maxus.tsp.common.util;
/**
 * 中国移动短信服务
 * @author lzgea
 *
 */

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChinaMobileSMSUtil {

	private static final Logger logger = LogManager.getLogger(ChinaMobileSMSUtil.class);
	private static String sendMessageUrl = "http://ctyun.st-shanghai.cn:8084/api/http/SendSmsMessage";
	private static final String USER_NAME = "username";
	private static final String PASSWORD = "password";
	private static final String DEST_ADDRESS = "destAddr";
	private static final String CONTENT = "content";
	private static final String REQ_REPORT = "reqReport";
	private static final String SOURCE_ADDRESS = "sourceAddr";
	private static final String SEND_TIME = "sendTime";
	private static String chinaMobileUserName = "sqdt";
	private static String chinaMobilePWD = "sqdt";

	
	public static void setChinaMobileProperties(String sms_url, String username, String password){
		sendMessageUrl = sms_url;
		chinaMobilePWD = password;
		chinaMobileUserName = username;
	}
	public static boolean sendSmsMessage(String destAddr, String content) {
		boolean result = false;
		StringBuilder sendUrl = new StringBuilder();
		sendUrl.append(sendMessageUrl).append("?").append(USER_NAME).append("=").append(chinaMobileUserName)
				.append("&").append(PASSWORD).append("=").append(chinaMobilePWD).append("&").append(DEST_ADDRESS).append("=")
				.append(destAddr).append("&").append(CONTENT).append("=").append(content);
		try {
			result = ChinaMobileSMSUtil.post(sendUrl.toString(), destAddr);
		} catch (Exception e) {
			logger.error("向SIM:{} 发送唤醒短信时发生异常！原因:{}", destAddr, ThrowableUtil.getErrorInfoFromThrowable(e));
		}
		return result;
	}

	private static boolean post(String url, String destAddr)
			throws NoSuchAlgorithmException, KeyManagementException, ClientProtocolException, IOException {
		SSLContext sslcontext = createIgnoreVerifySSL();
		boolean returnRes = false;
		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build();
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000).build();
		post.setConfig(requestConfig);

		HttpResponse response = client.execute(post);
		int state = response.getStatusLine().getStatusCode();
		if (state == 200) {
			HttpEntity resEntity = response.getEntity();
			String returnEntity = EntityUtils.toString(resEntity, "utf-8");
			logger.info("(SIM:{})收到移动短信回复：{}", destAddr, returnEntity);
			if (returnEntity.contains("\"code\":200")){
				returnRes = true;
			}
		} 
		return returnRes;
	}

	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

//	public static void main(String[] args) {
//		boolean result = ChinaMobileSMSUtil.sendSmsMessage("1440076020591", "0000");
//		System.out.println("call result:"+result);
//	}
}
