package org.sakuram.persmony.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakuram.persmony.valueobject.ScheduleVO;

public class UtilFuncs {
	
	static SimpleDateFormat format = new SimpleDateFormat(Constants.CSV_DATE_FORMAT);
	static Pattern rangePattern = Pattern.compile("(\\d*)-(\\d*)");
	static Pattern schedulePattern = Pattern.compile("\\[((?:\\d|,|-)*)\\]\\[((?:\\d|,|-)*)\\]\\[((?:\\d|,|-)*)\\](\\d*\\.\\d*),?");
	
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
	
	public static Boolean createBoolean(String booleanStr) {
		return booleanStr == null ? null : Boolean.valueOf(booleanStr);
	}
	
    public static List<ScheduleVO> parseScheduleData(String inStr) {
    	List<ScheduleVO> scheduleVOList;
    	Matcher matcher;
    	List<Integer> yearsList, monthsList, daysList;
    	Float value;
    	int matcherEnd;
    	
    	scheduleVOList = new ArrayList<ScheduleVO>();
    	if (inStr == null) {
        	return scheduleVOList;
    	}
    	inStr = inStr.replaceAll("\\s+", "");
    	matcherEnd = 0;
    	matcher = schedulePattern.matcher(inStr);
        while (matcher.find()) {
        	if (matcher.start(0) != matcherEnd) {
            	throw new AppException("Invalid structure for Scheduled-Values.", null);
        	}
        	yearsList = parseListStr(matcher.group(1));
        	monthsList = parseListStr(matcher.group(2));
        	daysList = parseListStr(matcher.group(3));
        	value = Float.valueOf(matcher.group(4));
        	for (Integer year : yearsList) {
        		for (Integer month : monthsList) {
        			for (Integer day : daysList) {
        				scheduleVOList.add(new ScheduleVO(Date.valueOf(year + "-" + month + "-" + day), value == 0? null : value));
        			}
        		}
        	}
        	matcherEnd = matcher.end();
        }
        
        if (inStr != null && inStr != "" && (scheduleVOList.size() == 0 || matcherEnd != inStr.length())) {
        	throw new AppException("Invalid structure for Scheduled-Values.", null);
        }
        
    	scheduleVOList.sort(Comparator.comparing(ScheduleVO::getDueDate));
    	return scheduleVOList;
    }
    
    private static List<Integer> parseListStr(String inStr) {
    	String[] inStrSplittedCSV;
    	List<Integer> outList;
    	Matcher matcher;
    	Integer rangeStart, rangeEnd;
    	
    	outList = new ArrayList<Integer>();
    	inStrSplittedCSV = inStr.split(",");
    	for(String singleInput : inStrSplittedCSV) {
    		if (singleInput.contains("-")) {
    	    	matcher = rangePattern.matcher(inStr);
    	        if (matcher.find()) {
    	        	rangeStart = Integer.valueOf(matcher.group(1));
    	        	rangeEnd = Integer.valueOf(matcher.group(2));
    	            if (rangeStart > rangeEnd) {
    	            	throw new AppException("Invalid structure for Scheduled-Values.", null);
    	            }
    	            
    	        	for(int inp = rangeStart; inp <= rangeEnd; inp++) {
    	    			outList.add(inp);
    	        	}
    	        }
    		} else {
    			outList.add(Integer.valueOf(singleInput));
    		}
    	}
    	return outList;
    }

    public static void main(String[] args){
    	parseScheduleData("[2022-2022] [3] [31] 725.00,[2030, 2031] [3] [31] 600.00");
    	
    }
}
