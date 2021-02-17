package org.sakuram.persmony;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.valueobject.ReceiptSingleRealisationIntoBankVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PersmonyApplication implements CommandLineRunner {

	@Autowired
	private MoneyTransactionService moneyTransactionService;
	@Autowired
	ReportService reportService;
	
	static final String DATA_FOLDER = "D:\\RSureshK#Personal#\\RSKPers\\PersMony\\";
	
	public static void main(String[] args) {
		SpringApplication.run(PersmonyApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
    	if (args.length == 2) {
    		switch(args[0].toLowerCase()) {
    			case "transact":
    				SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy");
    		    	// Reader in = new FileReader(args[1]);
    		    	// Iterable<CSVRecord> parser = CSVFormat.EXCEL.withSkipHeaderRecord(true).parse(in);
    				Reader in = new InputStreamReader(new BOMInputStream(new FileInputStream(args[1])), "UTF-8");
    				CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader().withSkipHeaderRecord(true));
    		    	for (CSVRecord record : parser) {
    		    	    switch(record.get(0).toLowerCase()) {
    		    	    	case "checkifitruns":
    		    	    		System.out.println("PersMony Application Runs!!!");
    		    	    		break;
    		    	    	case "receiptsinglerealisationintobank":
    		    	    		if (record.size() == 5) {
	    		    				ReceiptSingleRealisationIntoBankVO receiptSingleRealisationIntoBankVO;
	    		    				receiptSingleRealisationIntoBankVO = new ReceiptSingleRealisationIntoBankVO(Integer.valueOf(record.get(1)), Float.valueOf(record.get(2)), new java.sql.Date(format.parse(record.get(3)).getTime()), Integer.valueOf(record.get(4)));
	    		    				try {
	    		    					moneyTransactionService.receiptSingleRealisationIntoBank(receiptSingleRealisationIntoBankVO);
		    		    				System.out.println(String.format("Processed %s", record.toString()));
	    		    				}
	    		    				catch (AppException aE) {
		    		    				System.out.println(String.format("Skipped %s", record.toString()));
	    		    				}
    		    	    		} else {
        		    	    		quitCodeWithError("For transaction type ReceiptSingleRealisationIntoBank, 4 inputs are expected.");
    		    	    		}
    		    	    		break;
    		    	    	default:
    		    	    		quitCodeWithError(String.format("%s is not a valid transaction type.", record.get(0)));
    		    	    }
    		    	}
    				break;
    			case "report":
    				List<Object[]> recordList = null;
    				String outFile = null;
    	    		switch(args[1].toLowerCase()) {
    	    			case "pendingtransactions":
    	    				recordList = reportService.pendingTransactions();
    	    				outFile = "pendingTransactions.csv";
    	    				break;
		    	    	default:
		    	    		quitCodeWithError(String.format("%s is not a valid report.", args[1]));
    	    		}
    	    		try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(DATA_FOLDER + outFile)), CSVFormat.DEFAULT)) {
    	    			for (Object[] record : recordList) {
    	    				csvPrinter.printRecord(record);
    	    			}
    	    			csvPrinter.flush();
    	    		}
    				break;
    			default:
    				quitCodeWithError(String.format("%s is not a valid action.", args[0]));
    		}
    	} else {
    		quitCodeWithError("Nothing to do!");
    	}
    }
    
    public void quitCodeWithError(String errorMessage) {
    	System.err.println(errorMessage);
    	System.exit(16);
    }
}
