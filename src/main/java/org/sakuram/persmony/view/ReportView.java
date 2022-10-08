package org.sakuram.persmony.view;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.AppException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

@Route("report")
public class ReportView extends VerticalLayout {
	private static final long serialVersionUID = 7744877036031319646L;
	static final String DATA_FOLDER = "D:\\RSureshK\\RSKPers\\PersMony\\";
	
	public ReportView(ReportService reportService) {
		Select<String> reportSelect;
		Button generateButton;
		
		reportSelect = new Select<String>();
		reportSelect.setItems("Pending Transactions",
				"Pending Investments");
		reportSelect.setLabel("Report");
		reportSelect.setPlaceholder("Select Report");

		generateButton = new Button("Generate");
		generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		generateButton.setDisableOnClick(true);
		// On click of Save
		generateButton.addClickListener(event -> {
			List<Object[]> recordList = null;
			String outFile = null;
			try {
				// Validation
				if (reportSelect.getValue() == null) {
					showError("Select a Report before clicking Generate");
					return;
				}
				
				// Back-end Call
				try {
		            switch(reportSelect.getValue()) {
		            case "Pending Transactions":
	    				recordList = reportService.pendingTransactions();
	    				outFile = "pendingTransactions.csv";
		            	break;
		            case "Pending Investments":
	    				recordList = reportService.investmentsWithPendingTransactions();
	    				outFile = "investmentsWithPendingTransactions.csv";
		            	break;
		            }
				} catch (Exception e) {
					showError(messageFromException(e));
					return;
				}
				
				// Generate CSV
	    		try (CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(DATA_FOLDER + outFile)), CSVFormat.DEFAULT)) {
	    			for (Object[] record : recordList) {
	    				csvPrinter.printRecord(record);
	    			}
	    			csvPrinter.flush();
	    		}
			} catch (Exception e) {
				showError("System Error!!! Contact Support.");
				return;
			} finally {
				generateButton.setEnabled(true);
			}
		});

		add(reportSelect);
		add(generateButton);
	}
	
	private String messageFromException(Exception e) {
		if (e instanceof AppException) {
			System.out.println("AppException Caught!!!");
			return e.getMessage();
		} else {
			System.out.println(e.getClass().getName() + " Caught!!!");
			return "Unexpected Error: " + (e.getMessage() == null ? "No further details!" : e.getMessage());
		}
		
	}
	
	private void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		errorDialog.open();
		
	}
}
