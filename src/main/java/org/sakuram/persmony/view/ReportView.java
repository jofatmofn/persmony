package org.sakuram.persmony.view;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.UtilFuncs;
import org.vaadin.stefan.LazyDownloadButton;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

@Route("report")
public class ReportView extends VerticalLayout {
	private static final long serialVersionUID = 7744877036031319646L;
	
	public ReportView(ReportService reportService) {
		Select<String> reportSelect;
		LazyDownloadButton generateButton;
		
		reportSelect = new Select<String>();
		reportSelect.setItems("Pending Transactions",
				"Pending Investments");
		reportSelect.setLabel("Report");
		reportSelect.setPlaceholder("Select Report");

		generateButton = new LazyDownloadButton("Generate",
				() -> {
					return reportSelect.getValue() + ".csv";
				},
				() -> {
					StringWriter stringWriter;
					List<Object[]> recordList = null;
					try {
						// Back-end Call
						try {
							if (reportSelect.getValue() == null) {
								showError("Select a Report before clicking Generate");
								return new ByteArrayInputStream(new byte[0]);
							}
							System.out.println(reportSelect.getValue());
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
							return new ByteArrayInputStream(new byte[0]);
						}
						
						// Generate CSV
						stringWriter = new StringWriter();
						stringWriter.getBuffer().setLength(0);
			    		try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT)) {
			    			for (Object[] record : recordList) {
			    				csvPrinter.printRecord(record);
			    			}
			    			csvPrinter.flush();
			    			try {
			    				return new ByteArrayInputStream(stringWriter.toString().getBytes("utf-8"));
			    			} catch (UnsupportedEncodingException e) {
			    				e.printStackTrace();
			    				return new ByteArrayInputStream(new byte[0]);
			    			}
			    		}
					} catch (Exception e) {
						showError("System Error!!! Contact Support.");
						return new ByteArrayInputStream(new byte[0]);
					}
		});
		generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		add(reportSelect);
		add(generateButton);
	}
	
	private void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		getUI().ifPresent(ui -> ui.access(() -> {
			errorDialog.open();
			}));
		
	}
}
