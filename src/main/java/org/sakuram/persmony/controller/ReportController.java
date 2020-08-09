package org.sakuram.persmony.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {
	@Autowired
	ReportService reportService;

    @RequestMapping(value = "/pendingTransactions", method = RequestMethod.GET)
    public void pendingTransactions(HttpServletResponse response) throws IOException
    {
    	List<Object[]> recordList;
    	
    	recordList = reportService.pendingTransactions();
    	try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT)) {
	    	for (Object[] record : recordList) {
	    		csvPrinter.printRecord(record);
	    	}
    	}
    	response.setHeader("Content-Type", "text/csv");
    	response.setHeader("Content-Disposition", "attachment; filename=\"transactions.csv\"");
    }

}
