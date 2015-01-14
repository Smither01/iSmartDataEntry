package com.guogu.ismartdataentry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.util.Log;

/**
 *@ClassName:     HttpManager.java
 * @Description:   TODO
 * @author xieguangwei
 * @date 2014-12-4
 * 
 */
public class HttpManager {
	public static final String TAG = "HttpManager";
	private volatile static HttpManager mInstance;
	private HttpClient client;
	private HttpManager(){
		client = getHttpClient();
	};
	
	public static HttpManager getInstance(){
		if( mInstance == null){
			synchronized(HttpManager.class){
				if(mInstance == null){
					mInstance = new HttpManager();
				}
			}
		}
		return mInstance;
	}
	
	private HttpClient getHttpClient(){
		HttpParams params = new BasicHttpParams();
        //设置基本参数
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setUseExpectContinue(params, true);
        //超时设置
        /*从连接池中取连接的超时时间*/
        ConnManagerParams.setTimeout(params, 2000);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(256));
        ConnManagerParams.setMaxTotalConnections(params, 512);
        /*连接超时*/
        HttpConnectionParams.setConnectionTimeout(params, 4000);
        /*请求超时*/
        HttpConnectionParams.setSoTimeout(params, 8000);
        //设置HttpClient支持HTTp和HTTPS两种模式
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        //使用线程安全的连接管理来创建HttpClient
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
        
        HttpClient httpClient = new DefaultHttpClient(conMgr, params);
        return httpClient;
	}
	
	public  void SendPostRequest(String url,final Map<String,String> param,final AsyncHttpResponseHandler handler)
	{

		AsyncTask<String, Map<String, String>, String> task = new AsyncTask<String,Map<String,String>, String>(){
            private Throwable error;
            private int statusCode;
			@Override
			protected String doInBackground(String... params) {
				List<NameValuePair> p = new ArrayList<NameValuePair>();  
				if(param != null){
					Set<String> keys = param.keySet();
					Iterator<String> it = keys.iterator();
					while(it.hasNext()){
						String key = it.next();
						String value = param.get(key);
						NameValuePair pair = new BasicNameValuePair(key, value);
						p.add(pair);
					}
				}
				Log.i(TAG, "sending post http request to "+params[0] + "param:"+param.toString());
				HttpPost httpPost = new HttpPost(params[0]); 
				InputStream is = null;
				BufferedReader br =null;
				try {
					HttpEntity h = new UrlEncodedFormEntity(p, HTTP.UTF_8);
					httpPost.setEntity(h);
					HttpResponse hr = client.execute(httpPost);
					statusCode = hr.getStatusLine().getStatusCode();
					if( statusCode == HttpStatus.SC_OK){
						HttpEntity he = hr.getEntity();
						is = he.getContent();
						br = new BufferedReader(new InputStreamReader(is));
						StringBuilder response = new StringBuilder("");
						String readLine = null;
						while((readLine = br.readLine()) != null){
							response.append(readLine);
						}
						is.close();
						br.close();
						return response.toString();
					}
				} catch (Exception e) {
					error = e;
					e.printStackTrace();
				}finally{
					
					try {
						if(is != null){
							is.close();
						}
						if(br != null){
							br.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				return null;
			}
			@Override  
		    protected void onPostExecute(String result) {
				if(result == null || result.equals("")){
					handler.onFailure(error, result);
				}else{
					handler.onSuccess(statusCode,result);

				}
		    }

			
			
		};
		
		task.execute(url);
	}
	
	public  void SendGetRequest(String url,final Map<String,String> param,final AsyncHttpResponseHandler handler)
	{
		 
		AsyncTask<String, Map<String, String>, String> task = new AsyncTask<String,Map<String,String>, String>(){
            private Throwable error;
            private int statusCode;
			@Override
			protected String doInBackground(String... params) {
				StringBuilder sb = new StringBuilder("");
				if(param != null){
					sb.append("?");
					Set<String> keys = param.keySet();
					Iterator<String> it = keys.iterator();
					while(it.hasNext()){
						String key = it.next();
						String value = param.get(key);
						if(value != null){
							try {
								
								value = URLEncoder.encode(value,"UTF-8");
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
						

						sb.append(key).append("=").append(value);
						if(it.hasNext())
							sb.append("&");
					}
				}
				params[0] += sb.toString();
				InputStream is = null;
				BufferedReader br = null;
				try {
					HttpGet httpGet = new HttpGet(params[0]); 
					Log.i(TAG, "sending get http request to "+params[0] );

					HttpResponse hr = client.execute(httpGet);
					statusCode = hr.getStatusLine().getStatusCode();
					Log.i(TAG, "response status code:"+statusCode);
					if(statusCode == HttpStatus.SC_OK){
						HttpEntity he = hr.getEntity();
						is = he.getContent();
						br = new BufferedReader(new InputStreamReader(is));
						StringBuilder response = new StringBuilder("");
						String readLine = null;
						while((readLine = br.readLine()) != null){
							response.append(readLine);
						}
						is.close();
						br.close();
						return response.toString();
					}
				} catch (UnknownHostException e) {
					error = e;
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					error = e;
					e.printStackTrace();
				} catch (IOException e) {
					error = e;
					e.printStackTrace();
				}finally{
					
					try {
						if(is != null){
							is.close();
						}
						if(br != null){
							br.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;
			}
			@Override  
		    protected void onPostExecute(String result) {
				
				if(result == null || result.equals("")){
					handler.onFailure(error, result);
				}else{
					try {
						result =new String(result.getBytes("UTF-8"),"UTF-8");
						Log.i(TAG, "http response:"+result);

					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, e.toString());
					}
					handler.onSuccess(statusCode,result);

				}
		    }

			
			
		};
		
		task.execute(url);
		
	}
	
	

}
