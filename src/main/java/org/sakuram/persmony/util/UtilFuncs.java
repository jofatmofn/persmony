package org.sakuram.persmony.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

public class UtilFuncs {
	
	static SimpleDateFormat format = new SimpleDateFormat(Constants.CSV_DATE_FORMAT);
	static Pattern rangePattern = Pattern.compile("(\\d*)-(\\d*)");
	static Pattern schedulePattern = Pattern.compile("\\[((?:\\d|,|-)*)\\]\\[((?:\\d|,|-)*)\\]\\[((?:\\d|,|-)*)\\](\\d*\\.\\d*),?");
	static final Integer END_DAY_OF_MONTH = 32;
	
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
    	Double value;
    	int matcherEnd;
    	Date dueDate;
    	
    	scheduleVOList = new ArrayList<ScheduleVO>();
    	if (inStr == null || inStr.equalsIgnoreCase("None")) {
        	return scheduleVOList;
    	}
    	inStr = inStr.replaceAll("\\s+", "");
    	matcherEnd = 0;
    	matcher = schedulePattern.matcher(inStr);
        while (matcher.find()) {
        	if (matcher.start(0) != matcherEnd) {
            	throw new AppException("Invalid structure for Scheduled-Values.", null);
        	}
        	try {
        	yearsList = parseListStr(matcher.group(1));
        	monthsList = parseListStr(matcher.group(2));
        	daysList = parseListStr(matcher.group(3));
        	value = Double.valueOf(matcher.group(4));
        	for (Integer year : yearsList) {
        		for (Integer month : monthsList) {
        			for (Integer day : daysList) {
        				if (day.equals(END_DAY_OF_MONTH)) {
        					dueDate = Date.valueOf(YearMonth.of(year, month).atEndOfMonth());
        				} else {
        					dueDate = Date.valueOf(year + "-" + month + "-" + day);
        				}
        				scheduleVOList.add(new ScheduleVO(dueDate, value == 0? null : value, null, null, null));
        			}
        		}
        	}
        	matcherEnd = matcher.end();
        	} catch (Exception e) {
            	throw new AppException("Invalid structure for Scheduled-Values.", null);
        	}
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
    		if (singleInput.contains("-")) {	// END_DAY_OF_MONTH and Hyphen combination not handled
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

	public static String messageFromException(Exception e) {
		if (e instanceof AppException) {
			return e.getMessage();
		} else {
			e.printStackTrace();
			return "Unexpected Error: " + (e.getMessage() == null ? e.getClass().getName() : e.getMessage());
		}
		
	}

	public static StringBuffer sqlWhereClauseText(SearchCriterionVO searchCriterionVO) {
		StringBuffer stringBuffer;
		String criterionValue;
		
		stringBuffer = new StringBuffer(127);
		if (sqlNullHandling(searchCriterionVO, stringBuffer)) {
			return stringBuffer;
		}
		
		stringBuffer.append("LOWER(");
		stringBuffer.append(searchCriterionVO.getFieldName());
		stringBuffer.append(") ");
		
		criterionValue = searchCriterionVO.getValuesCSV().toLowerCase();
		if (searchCriterionVO.getOperator().equals(FieldSpecVO.TxtOperator.EQ.name())) {
			stringBuffer.append("= ");
			stringBuffer.append("'");
			stringBuffer.append(criterionValue);
			stringBuffer.append("'");
		} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.NE.name()) {
			stringBuffer.append("<> ");
			stringBuffer.append("'");
			stringBuffer.append(criterionValue);
			stringBuffer.append("'");
		} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.STARTS.name()) {
			stringBuffer.append("LIKE '");
			stringBuffer.append(criterionValue);
			stringBuffer.append("%'");
		} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.ENDS.name()) {
			stringBuffer.append("LIKE '%");
			stringBuffer.append(criterionValue);
			stringBuffer.append("'");
		} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.CONTAINS.name()) {
			stringBuffer.append("LIKE '%");
			stringBuffer.append(criterionValue);
			stringBuffer.append("%'");
		}
		stringBuffer.append(") "); // Opened by sqlNullHandling
		return stringBuffer;
	}
	
	public static StringBuffer sqlWhereClauseSequencable(SearchCriterionVO searchCriterionVO, boolean isDate) {
		StringBuffer stringBuffer;
		String valuesArr[];
		
		if (searchCriterionVO.getOperator() == null) {
			throw new AppException("Operator not specified for " + searchCriterionVO.getFieldName(), null);
		}
		stringBuffer = new StringBuffer(127);
		if (sqlNullHandling(searchCriterionVO, stringBuffer)) {
			return stringBuffer;
		}
		
		stringBuffer.append(searchCriterionVO.getFieldName());
		stringBuffer.append(" ");
		if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.BETWEEN.name()) {
			valuesArr = searchCriterionVO.getValuesCSV().split("\\s*,\\s*");
			if (valuesArr.length != 2) {
				throw new AppException("With BETWEEN as Operator, two values are to be specified for " + searchCriterionVO.getFieldName() + ". Current Input: " + searchCriterionVO.getValuesCSV() + "; Count: " + valuesArr.length, null);	
			}
			if (isDate) {
				valuesArr[0] = "'" + valuesArr[0] + "'";
				valuesArr[1] = "'" + valuesArr[1] + "'";
			} else {
				try {
					Double.valueOf(valuesArr[0]);
					Double.valueOf(valuesArr[1]);
				} catch (NumberFormatException e) {
					throw new AppException("Numeric value expected for " + searchCriterionVO.getFieldName(), null);
				}
			}
			stringBuffer.append("BETWEEN ");
			stringBuffer.append(valuesArr[0]);
			stringBuffer.append(" AND ");
			stringBuffer.append(valuesArr[1]);
		} else {
			if (!isDate) {
				try {
					Double.valueOf(searchCriterionVO.getValuesCSV());
				} catch (NumberFormatException e) {
					throw new AppException("Numeric value expected for " + searchCriterionVO.getFieldName(), null);
				}
			}
			if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.EQ.name()) {
				stringBuffer.append("= ");
			} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.NE.name()) {
				stringBuffer.append("<> ");
			} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.LT.name()) {
				stringBuffer.append("< ");
			} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.LE.name()) {
				stringBuffer.append("<= ");
			} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.GT.name()) {
				stringBuffer.append("> ");
			} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.GE.name()) {
				stringBuffer.append(">= ");
			}
			if (isDate) {
				stringBuffer.append("'");
				stringBuffer.append(searchCriterionVO.getValuesCSV());
				stringBuffer.append("' ");
			} else {
				stringBuffer.append(searchCriterionVO.getValuesCSV());
				stringBuffer.append(" ");
			}
		}
		stringBuffer.append(") "); // Opened by sqlNullHandling
		return stringBuffer;
	}

	private static boolean sqlNullHandling(SearchCriterionVO searchCriterionVO, StringBuffer stringBuffer) {
		// May be Redundant when the value can never be null
		
		if (searchCriterionVO.getValuesCSV() == null) {
			stringBuffer.append(searchCriterionVO.getFieldName());
			stringBuffer.append(" IS ");
			if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.NE.name()) {
				stringBuffer.append("NOT ");
			}
			stringBuffer.append("NULL ");
			return true;
		}
		
		stringBuffer.append("("); // This has to be closed by the Caller
		if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.NE.name()) { // Include this for all NOT conditions
			stringBuffer.append(searchCriterionVO.getFieldName());
			stringBuffer.append(" IS NULL OR ");
		}
		stringBuffer.append(searchCriterionVO.getFieldName());
		stringBuffer.append(" IS NOT NULL AND ");
		return false;
	}
	
	public static boolean toFormQueryForNullableField(String operator, String value) {
		return (value != null || operator != null && (operator.equals(FieldSpecVO.TxtOperator.EQ.name()) || operator.equals(FieldSpecVO.TxtOperator.NE.name())));
	}
	
    @SafeVarargs
    public static <T> Iterable<T> combine(List<T>... lists) {
        return () -> new Iterator<T>() {
            int index = 0;
            Iterator<T> current = lists.length > 0 ? lists[0].iterator() : Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext() && index < lists.length - 1) {
                    current = lists[++index].iterator();
                }
                return current.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return current.next();
            }
        };
    }
    
    public static void main(String[] args){
    	for (ScheduleVO scheduleVO : parseScheduleData("[2022-2022] [3] [31] 725.00,[2030, 2031] [3] [31] 600.00")) {
    		System.out.print(scheduleVO.getDueDate());
    		System.out.println(scheduleVO.getDueAmount());
    	}
    }
}
