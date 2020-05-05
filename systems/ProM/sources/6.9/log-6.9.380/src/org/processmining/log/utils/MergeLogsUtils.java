package org.processmining.log.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.processmining.log.parameters.MergeLogsParameters;

public class MergeLogsUtils {
	
	public static Date getDate(MergeLogsParameters parameters, String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(parameters.getDateFormat());
		dateFormat.setLenient(false);
		try {
			return dateFormat.parse(date.trim());
		} catch (ParseException pe) {
			return null;
		}
		
	}

}
