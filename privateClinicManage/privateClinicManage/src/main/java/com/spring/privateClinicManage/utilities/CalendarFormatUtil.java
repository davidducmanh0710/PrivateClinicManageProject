package com.spring.privateClinicManage.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarFormatUtil {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static CalendarFormat parseStringToCalendar(String s) {
		Date d;
		Calendar calendar = Calendar.getInstance();
		try {
			d = dateFormat.parse(s);
			calendar.setTime(d);
		} catch (ParseException e) {

			System.err.println("ParseException: " + e.getMessage());
		}

		Integer year = calendar.get(Calendar.YEAR);
		Integer month = calendar.get(Calendar.MONTH) + 1; // Tháng bắt đầu từ 0
		Integer day = calendar.get(Calendar.DAY_OF_MONTH);

		return new CalendarFormat(year, month, day);
	}

}
