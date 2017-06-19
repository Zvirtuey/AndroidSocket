package com.virtue.socketdemo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class RequestUtil {
	private static int requestID = 100;

	/**
	 * 获取手机IMEI
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneMini(Context context) {
		String imei = "";
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();// 手机IMEI
		if (imei == null || imei.length() < 1) {
			imei = "";
		}
		return imei;
	}

	/**
	 * 检查该字符串是否只包含字母和数字
	 * @param str
	 * @return
	 * @throws PatternSyntaxException
	 */
	public static boolean StringFilter(String str) throws PatternSyntaxException {
		// 只允许字母和数字
		String regEx = "[a-zA-Z0-9]{6,16}";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 检查当前网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) 
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) 
		{
			return false;
		} 
		else 
		{
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
			if (networkInfo != null && networkInfo.length > 0) 
			{
				for (int i = 0; i < networkInfo.length; i++) 
				{
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
				}
			}
		}
		// 网络不可以用
		return false;
	}

}
