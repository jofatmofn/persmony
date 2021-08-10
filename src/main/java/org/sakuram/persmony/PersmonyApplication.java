package org.sakuram.persmony;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.InvestVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PersmonyApplication implements CommandLineRunner {

	@Autowired
	DomainValueRepository domainValueRepository;
	@Autowired
	private MoneyTransactionService moneyTransactionService;
	@Autowired
	ReportService reportService;
	
	static final String DATA_FOLDER = "D:\\RSureshK\\RSKPers\\PersMony\\";
	
	public static void main(String[] args) {
		SpringApplication.run(PersmonyApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
    	loadCache();
    	if (args.length == 2) {
    		switch(args[0].toLowerCase()) {
    			case "transact":
    		    	// Reader in = new FileReader(args[1]);
    		    	// Iterable<CSVRecord> parser = CSVFormat.EXCEL.withSkipHeaderRecord(true).parse(in);
    				Reader in = new InputStreamReader(new BOMInputStream(new FileInputStream(args[1])), "UTF-8");
    				CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader().withSkipHeaderRecord(true).withNullString(""));
    		    	for (CSVRecord record : parser) {
    		    	    switch(record.get(0).toLowerCase()) {
    		    	    	case "checkifitruns":
    		    	    		System.out.println("PersMony Application Runs!!!");
    		    	    		break;
    		    	    	case "singlerealisationwithbank":
    		    				SingleRealisationWithBankVO singleRealisationWithBankVO;
    		    				
    		    				try {
        		    				singleRealisationWithBankVO = new SingleRealisationWithBankVO(
        		    						NumberUtils.createLong(record.get(1)),
        		    						NumberUtils.createFloat(record.get(2)),
        		    						UtilFuncs.createDate(record.get(3)),
        		    						NumberUtils.createLong(record.get(4)),
        		    						null);
    		    					moneyTransactionService.singleRealisationWithBank(singleRealisationWithBankVO);
	    		    				System.out.println(String.format("Processed %s", record.toString()));
    		    				}
    		    				catch (AppException aE) {
	    		    				System.out.println(String.format("Skipped %s", record.toString()));
    		    				}
    		    	    		break;
    		    	    	case "txnsinglerealisationwithbank":
    		    				TxnSingleRealisationWithBankVO txnSingleRealisationWithBankVO;
    		    				
    		    				try {
        		    				txnSingleRealisationWithBankVO = new TxnSingleRealisationWithBankVO(
        		    						NumberUtils.createLong(record.get(1)),
        		    						NumberUtils.createLong(record.get(2)),
        		    						NumberUtils.createFloat(record.get(3)),
        		    						UtilFuncs.createDate(record.get(4)),
        		    						NumberUtils.createLong(record.get(5)));
    		    					moneyTransactionService.txnSingleRealisationWithBank(txnSingleRealisationWithBankVO);
	    		    				System.out.println(String.format("Processed %s", record.toString()));
    		    				}
    		    				catch (AppException aE) {
	    		    				System.out.println(String.format("Skipped %s", record.toString()));
    		    				}
    		    	    		break;
    		    	    	case "singlelastrealisationwithbank":
    		    				SingleRealisationWithBankVO singleRealisationWithBankVO2;
    		    				
    		    				try {
        		    				singleRealisationWithBankVO2 = new SingleRealisationWithBankVO(
        		    						NumberUtils.createLong(record.get(1)),
        		    						NumberUtils.createFloat(record.get(2)),
        		    						UtilFuncs.createDate(record.get(3)),
        		    						NumberUtils.createLong(record.get(4)),
        		    						NumberUtils.createLong(record.get(5)));
    		    					moneyTransactionService.singleLastRealisationWithBank(singleRealisationWithBankVO2);
	    		    				System.out.println(String.format("Processed %s", record.toString()));
    		    				}
    		    				catch (AppException aE) {
	    		    				System.out.println(String.format("Skipped %s", record.toString()));
    		    				}
    		    	    		break;
    		    	    	case "renewal":
		    	    			RenewalVO renewalVO;
    		    				try {
    		    	    			renewalVO = new RenewalVO(
    		    	    					NumberUtils.createLong(record.get(1)),
    		    	    					record.get(2),
    		    	    					NumberUtils.createFloat(record.get(3)),
    		    	    					UtilFuncs.createDate(record.get(4)),
    		    	    					UtilFuncs.parseScheduleData(record.get(5)),
    		    	    					UtilFuncs.parseScheduleData(record.get(6)),
    		    	    					UtilFuncs.parseScheduleData(record.get(7)));
    		    					moneyTransactionService.renewal(renewalVO);
	    		    				System.out.println(String.format("Processed %s", record.toString()));
    		    				}
    		    				catch (AppException aE) {
	    		    				System.out.println(String.format("Skipped %s", record.toString()));
    		    				}
    		    	    		break;
    		    	    	case "invest":
		    	    			InvestVO investVO;
    		    				try {
    		    	    			investVO = new InvestVO(
    		    	    					NumberUtils.createLong(record.get(1)),
    		    	    					NumberUtils.createLong(record.get(2)),
    		    	    					record.get(3),
    		    	    					record.get(4),
    		    	    					record.get(5),
    		    	    					NumberUtils.createLong(record.get(6)),
    		    	    					NumberUtils.createLong(record.get(7)),
    		    	    					NumberUtils.createLong(record.get(8)),
    		    	    					UtilFuncs.createBoolean(record.get(9)),
    		    	    					NumberUtils.createLong(record.get(10)),
    		    	    					record.get(11),
    		    	    					NumberUtils.createFloat(record.get(12)),
    		    	    					UtilFuncs.createDate(record.get(13)),
    		    	    					UtilFuncs.parseScheduleData(record.get(14)),
    		    	    					UtilFuncs.parseScheduleData(record.get(15)),
    		    	    					UtilFuncs.parseScheduleData(record.get(16)));
    		    					moneyTransactionService.invest(investVO);
	    		    				System.out.println(String.format("Processed %s", record.toString()));
    		    				}
    		    				catch (AppException aE) {
	    		    				System.out.println(String.format("Skipped %s", record.toString()));
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
    	    			case "investmentsWithPendingTransactions":
    	    				recordList = reportService.investmentsWithPendingTransactions();
    	    				outFile = "investmentsWithPendingTransactions.csv";
    	    				break;
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
    
    private void quitCodeWithError(String errorMessage) {
    	System.err.println(errorMessage);
    	System.exit(16);
    }
    
    private void loadCache() {
    	Constants.domainValueCache = new HashMap<Long, DomainValue>();
    	for(DomainValue domainValue : domainValueRepository.findAll()) {
    		Constants.domainValueCache.put(domainValue.getId(), domainValue);
    	}
    }
}
