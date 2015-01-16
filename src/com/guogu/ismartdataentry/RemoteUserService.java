package com.guogu.ismartdataentry;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class RemoteUserService {	

	public static final String DATA_SERVER_URL = "http://regsrv1.guogee.com:86/";
	public static void sendData(String pkgID,String pkgPWD,JSONArray jsonArr, AsyncHttpResponseHandler handler){
		
		String url = DATA_SERVER_URL + "api/user/inputPackageDevices";
		Map<String,String> params = new HashMap<String,String>();
		params.put("pkgID", pkgID);
		params.put("pkgPWD", pkgPWD);
		params.put("devices", jsonArr.toString());
		HttpManager.getInstance().SendGetRequest(url, params, handler);
	}
}
