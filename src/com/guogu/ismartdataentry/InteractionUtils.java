package com.guogu.ismartdataentry;


import android.content.Context;
import android.os.Vibrator;

public class InteractionUtils {
	private static long lastClickTime;
	public static int millsecoundSpam = 200;
	public static boolean isFastDoubleFired() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < millsecoundSpam) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	public static void vibrator(Context context){
		iSmartApplication app = (iSmartApplication)context.getApplicationContext();
		if (app.enableVibrator)
		{
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); 
			vibrator.vibrate(70); 
		}
	}
}
