package com.guogu.ismartdataentry;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class RemoteUserService {	
	
	public static void sendData(String pkgID,String pkgPWD,JSONArray jsonArr, AsyncHttpResponseHandler handler){
		String url = "http://115.28.165.93:86/" + "api/user/inputPackageDevices";
		Map<String,String> params = new HashMap<String,String>();
		params.put("pkgID", pkgID);
		params.put("pkgPWD", pkgPWD);
		params.put("devices", jsonArr.toString());
		HttpManager.getInstance().SendGetRequest(url, params, handler);
	}
}
