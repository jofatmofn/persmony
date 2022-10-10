package org.sakuram.persmony.view;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.UtilFuncs;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route("report")
public class ReportView extends VerticalLayout {
	private static final long serialVersionUID = 7744877036031319646L;
	static final String DATA_FOLDER = "D:\\RSureshK\\RSKPers\\PersMony\\";
	
	public ReportView(ReportService reportService) {
		Select<String> reportSelect;
		Button generateButton;
		FileDownloadWrapper fileDownloadWrapper;
		StringWriter stringWriter = new StringWriter();
		
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
		            	break;
		            case "Pending Investments":
	    				recordList = reportService.investmentsWithPendingTransactions();
		            	break;
		            }
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
					return;
				}
				
				// Generate CSV
				stringWriter.getBuffer().setLength(0);
	    		try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT)) {
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
		fileDownloadWrapper = new FileDownloadWrapper(
		    new StreamResource("report.csv", () -> {
		    	try {
					return new ByteArrayInputStream(stringWriter.toString().getBytes("utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return null;
				}
		    }));
		fileDownloadWrapper.wrapComponent(generateButton);

		add(reportSelect);
		add(fileDownloadWrapper);
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
