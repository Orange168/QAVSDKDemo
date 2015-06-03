package com.tencent.avsdk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyDate {
	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd.HH",
				Locale.getDefault());
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;
	}

	public static String getDateEN() {
		SimpleDateFormat format1 = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault());
		String date1 = format1.format(new Date(System.currentTimeMillis()));
		return date1;
	}

}