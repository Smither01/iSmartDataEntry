package com.guogu.ismartdataentry;

import android.app.Application;


public class iSmartApplication extends Application {
	
	private static final String TAG = "iSmartApplication";
	private static iSmartApplication app;
	
	//////////////////////////////////////////////////////////////////////////////
	/*
	 * ����ʾ��ʶ
	 */
	public boolean enableVibrator = true;
	public iSmartApplication() {  
        
	}
	
	public static iSmartApplication getApp(){
		return app;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;	
	}
	
}
