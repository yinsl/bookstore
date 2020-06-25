package com.maxus.tsp.schedule.baiduhttp;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpCommon {

	private final static int TIME_OUT = 15000;
	
	private RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(TIME_OUT)
			.setConnectTimeout(TIME_OUT)
			.setConnectionRequestTimeout(TIME_OUT)
			.build();
	
	public String httpGet(String url) throws Exception {
		HttpGet get = new HttpGet(url);
		get.setConfig(requestConfig);

		CloseableHttpClient client = HttpClientBuilder.create().build();

		HttpResponse response = client.execute(get);

		StringBuilder sb = new StringBuilder();
		
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();

			InputStream in = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} else {
			sb.append("error");
		}
		return sb.toString();
	}

	public void post(String url, HttpEntity reqEntity) {
		long start = System.currentTimeMillis();
		try {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(reqEntity);
		post.setConfig(requestConfig);
		HttpResponse response = client.execute(post);

		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity resEntity = response.getEntity();
			String message = EntityUtils.toString(resEntity, "utf-8");
			System.out.println(message);
		} else {
			System.out.println("请求失败");
		}
		} catch(Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println((end - start) / 1000 + "秒");
	}

}
