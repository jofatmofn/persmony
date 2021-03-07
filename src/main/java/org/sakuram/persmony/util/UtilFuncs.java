package org.sakuram.persmony.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class UtilFuncs {
	
	static SimpleDateFormat format = new SimpleDateFormat(Constants.CSV_DATE_FORMAT);
	
	public static BigDecimal computeAssessmentYear(Date date) {
		LocalDate localDate;
		int assessmentYear;
		
		if(date == null) {
			return null;
		}
		
		localDate = date.toLocalDate();
		assessmentYear = localDate.getYear();
		if(localDate.getMonthValue() >= Constants.ASSESSMENT_YEAR_START_MONTH) {
			assessmentYear++;
		}
		
		return new BigDecimal(assessmentYear);
	}
	
	public static Date createDate(String dateStr) {
		try {
			return dateStr == null ? null : new Date(format.parse(dateStr).getTime());
		} catch (ParseException e) {
			throw new AppException("Invalid Date.", e);
		}
	}
}
