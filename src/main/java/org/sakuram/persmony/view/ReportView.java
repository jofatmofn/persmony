package org.sakuram.persmony.view;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.PeriodSummaryCriteriaVO;
import org.vaadin.stefan.LazyDownloadButton;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;

@Route("report")
public class ReportView extends VerticalLayout {
	private static final long serialVersionUID = 7744877036031319646L;
	
	public ReportView(ReportService reportService) {
		Select<String> reportSelect;
		LazyDownloadButton generateButton;
		FormLayout formLayout;
		DatePicker periodFromDatePicker, periodToDatePicker;
		
		reportSelect = new Select<String>();
		reportSelect.setItems("Pending Transactions",
				"Open Investments",
				"Period Summary");
		reportSelect.setLabel("Report");
		reportSelect.setPlaceholder("Select Report");
		add(reportSelect);
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		add(formLayout);
		
		periodFromDatePicker = new DatePicker("From");
		periodToDatePicker = new DatePicker("To");
		reportSelect.addValueChangeListener(event -> {
			HorizontalLayout hLayout;		
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue()) {
	            case "Pending Transactions":
	            	break;
	            case "Open Investments":
	            	break;
	            case "Period Summary":
	        		hLayout = new HorizontalLayout();
	        		formLayout.addFormItem(hLayout, "Period");
	        		hLayout.add(periodFromDatePicker, periodToDatePicker);
	            	break;
	            }
			} catch (Exception e) {
				showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });

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
				            case "Open Investments":
			    				recordList = reportService.investmentsWithPendingTransactions();
				            	break;
				            case "Period Summary":
				            	PeriodSummaryCriteriaVO periodSummaryCriteriaVO;
				            	if (periodFromDatePicker.getValue() == null || periodToDatePicker.getValue() == null) {
									showError("Select the period before clicking Generate");
									return new ByteArrayInputStream(new byte[0]);
								}
				            	periodSummaryCriteriaVO = new PeriodSummaryCriteriaVO(Date.valueOf(periodFromDatePicker.getValue()), Date.valueOf(periodToDatePicker.getValue()));
			    				recordList = reportService.periodSummary(periodSummaryCriteriaVO);
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
