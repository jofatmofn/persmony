package org.sakuram.persmony.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.CustomBeanToCSVMappingStrategy;
import org.sakuram.persmony.valueobject.Report01VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

@RestController
@RequestMapping("/report")
public class ReportController {
	@Autowired
	ReportService reportService;

    @RequestMapping(value = "/pendingTransactions", method = RequestMethod.GET, produces = "text/csv")
    public void pendingTransactions(HttpServletResponse response)
    {
    	List<Report01VO> report01VOList;
    	ColumnPositionMappingStrategy<Report01VO> columnPositionMappingStrategy;
    	StatefulBeanToCsv<Report01VO> btcsv;
    	
    	columnPositionMappingStrategy = new CustomBeanToCSVMappingStrategy<>();
    	columnPositionMappingStrategy.setType(Report01VO.class);
    	
		try {
			btcsv = new StatefulBeanToCsvBuilder<Report01VO>(response.getWriter())
					.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
					.withMappingStrategy(columnPositionMappingStrategy)
					.withSeparator(',')
					.build();
		} catch (IOException e) {
			throw new AppException("Error creating CSV Builder", e);
		}
    	report01VOList = reportService.pendingTransactions();
    	for (Report01VO report01VO : report01VOList)
    		System.out.println(report01VO.getInvestmentIdWithProvider());
    	try {
			btcsv.write(report01VOList);
		} catch (CsvException e) {
			throw new AppException("Error mapping data to CSV", e);
		}
    	try {
			response.getWriter().flush();
		} catch (IOException e) {
			throw new AppException("Error writing CSV to response", e);
		}
    }

}
