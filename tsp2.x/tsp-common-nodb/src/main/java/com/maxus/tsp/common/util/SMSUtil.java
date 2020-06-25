package com.maxus.tsp.common.util;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
public class SMSUtil {

	//private static Logger log = Logger.getLogger(SMSUtil.class);
	private static Logger log = LogManager.getLogger(SMSUtil.class);
	// region Maxus Interface URL

	// get token
	public static final String TOKEN_URL_TEST = "https://c2bt4.maxuscloud.com/oauth/token";
//	public static final String TOKEN_URL_PDU = "https://c2b.saicmaxus.com/oauth/token";
	// send sms
	public static final String SMS_URL_TEST = "https://c2bt4.maxuscloud.com/api/sms/sendsms";
//	public static final String SMS_URL_PDU = "https://c2b.saicmaxus.com/api/sms/sendsms";

	// search user
	public static final String SEARCH_USER_URL_TEST = "https://c2bt4.maxuscloud.com/api/userMark/getUserByMobile";
//	public static final String SEARCH_USER_URL_PDU = "https://c2b.saicmaxus.com/api/userMark/getUserByMobile";
	// register
	public static final String REGISTER_URL_TEST = "https://c2bt4.maxuscloud.com/api/userMark/registUserInfoByThirdPartyAccount";
//	public static final String REGISTER_URL_PDU = "https://c2b.saicmaxus.com/api/userMark/registUserInfoByThirdPartyAccount";

	public static final String APPAPI_URL_TEST = "https://veh.maxuscloud.cn/";
	//public static final String APPAPI_URL_PDU = "https://veh.maxuscloud.com/";
	// endregion Maxus Interface URL

	// region Get Token Post Parameter
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";

	// test(uat) environment value
	public static final String CLIENT_ID_VALUE_TEST = "i03llu4a7rytg7912df2lil60uugfboy";
	public static final String CLIENT_SECRET_VALUE_TEST = "sd7a6q15efxiz6e03um0s1ny67k7vgcw";

	// product environment value
//	public static final String CLIENT_ID_VALUE_PDU = "w2f0gc1kqoskkl0jpz5c3j0u4rc4nb2n";
//	public static final String CLIENT_SECRET_VALUE_PDU = "sb2310uaios9thpl96e0rsf5xg2z5qfe";

	public static final String SCOPE = "scope";
	public static final String GRANT_TYPE = "grant_type";
	public static final String SCOPE_VALUE = "read write";
	public static final String GRANT_TYPE_VALUE = "client_credentials";

	// endregion Get Token Post Parameter

	// region Send SMS Post Parameter

	//private static boolean isPrd = false;
	private static Map<String, String> postDataForToken = new HashMap<String, String>();

	private static String TOKEN_URL;

	private static Token token;

	private static String fromIp;

	private static String smsUrl;

	private static String searchUserUrl;

	private static String registserUrl;



	static {
		postDataForToken.put(CLIENT_ID, CLIENT_ID_VALUE_TEST);
		postDataForToken.put(CLIENT_SECRET, CLIENT_SECRET_VALUE_TEST);
		postDataForToken.put(SCOPE, SCOPE_VALUE);
		postDataForToken.put(GRANT_TYPE, GRANT_TYPE_VALUE);
		fromIp = APPAPI_URL_TEST;
		smsUrl = SMS_URL_TEST;
		searchUserUrl = SEARCH_USER_URL_TEST;
		registserUrl = REGISTER_URL_TEST;

		TOKEN_URL = TOKEN_URL_TEST;
	}

	public static void setSMSUtil(String client_id_value, String client_secret_value, String token_url,
			String sms_url, String search_user_url, String appapi_url, String register_url) {

		postDataForToken.put(CLIENT_ID, client_id_value);
		postDataForToken.put(CLIENT_SECRET, client_secret_value);
		postDataForToken.put(SCOPE, SCOPE_VALUE);
		postDataForToken.put(GRANT_TYPE, GRANT_TYPE_VALUE);
		fromIp = appapi_url;
		smsUrl = sms_url;
		searchUserUrl = search_user_url;
		registserUrl = register_url;
		TOKEN_URL = token_url;
	}




	public static String post(String url) throws IOException, KeyManagementException,
	        NoSuchAlgorithmException {
		return post(url, null);
	}

	public static String post(String url, Map<String, String> postData) throws IOException,
	        KeyManagementException, NoSuchAlgorithmException {
		if (postData != null && !postData.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String key : postData.keySet()) {
				sb.append(key).append("=").append(URLEncoder.encode(postData.get(key), "utf-8")).append("&");
			}
			String paras = sb.toString();
			paras = paras.substring(0, paras.length() - 1);
			url += "?" + paras;
		}
		SSLContext sslcontext = createIgnoreVerifySSL();

		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
		        .register("http", PlainConnectionSocketFactory.INSTANCE)
		        .register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		CloseableHttpClient client = HttpClients.custom().setConnectionManager(connManager).build(); 
//		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
		        .setConnectionRequestTimeout(5000).build();
		post.setConfig(requestConfig);

		HttpResponse response = client.execute(post);
		log.info("post response: " + JSON.toJSONString(response));
		int state = response.getStatusLine().getStatusCode();
		System.out.println("state: " + state);
		if (state == 200) {
			HttpEntity resEntity = response.getEntity();
			String result = EntityUtils.toString(resEntity, "utf-8");
			log.info("result: " + result);
			return result;
		} 
		else if(state == 302) {  
			// String locationUrl=response.getLastHeader("Location").getValue();  
			return null;
			} 
		else {
			return null;
		}
	}

	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
			        String paramString) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
			        String paramString) {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}
}
