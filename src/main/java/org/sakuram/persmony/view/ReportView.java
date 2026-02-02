package org.sakuram.persmony.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.DetailsForTaxFilingRequestVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.PeriodSummaryCriteriaVO;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinSession;

@Route(value="report", layout=PersMonyLayout.class)
public class ReportView extends VerticalLayout {
	private static final long serialVersionUID = 7744877036031319646L;
	int nameReportInd;
	List<List<Object[]>> reportList;
	
	public ReportView(ReportService reportService, MiscService miscService) {
		MultiDownloadButton generateButton;
		FormLayout formLayout;
		ReportFields reportFields;
		
		reportFields = new ReportFields();
		
		reportFields.reportSelect = new Select<String>();
		reportFields.reportSelect.setItems("All Pending Transactions",
				"Receipt Transactions",
				"Open Investments",
				"Period Summary",
				"Anticipated Vs. Actual",
				"Income Vs. Spend",
				"Tax Liability", 
				"Details for Tax Filing",
				"Readiness for Tax Filing"
				// "ISIN All Details"
				);
		reportFields.reportSelect.setLabel("Report");
		reportFields.reportSelect.setPlaceholder("Select Report");
		add(reportFields.reportSelect);
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		add(formLayout);
		
		reportFields.periodFromDatePicker = new DatePicker("From");
		reportFields.periodToDatePicker = new DatePicker("To");
		reportFields.financialYearStartIntegerField = new IntegerField();
		reportFields.investorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_PRIMARY_INVESTOR, "Investor", false, false);
		reportFields.reportSelect.addValueChangeListener(event -> {
			HorizontalLayout hLayout;		
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue()) {
	            case "Receipt Transactions":
	            case "Period Summary":
	            case "Anticipated Vs. Actual":
	            case "Income Vs. Spend":
	        		hLayout = new HorizontalLayout();
	        		formLayout.addFormItem(hLayout, "Period");
	        		hLayout.add(reportFields.periodFromDatePicker, reportFields.periodToDatePicker);
	            	break;
	            case "Tax Liability":
	            	reportFields.financialYearStartIntegerField.setLabel("FY Start Year");
	        		formLayout.add(reportFields.financialYearStartIntegerField);
	            	break;
	            case "Details for Tax Filing":
	            case "Readiness for Tax Filing":
	        		formLayout.add(reportFields.investorDvSelect);
	        		reportFields.financialYearStartIntegerField.setLabel("FY Start Year");
	        		formLayout.add(reportFields.financialYearStartIntegerField);
	            	break;
	            }
			} catch (Exception e) {
				showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });

		generateButton = new MultiDownloadButton("Generate", () -> createReportStreams(reportService, reportFields));		
		// generateButton.getContent().addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		add(generateButton);
	}

	private List<StreamResource> createReportStreams(ReportService reportService, ReportFields reportFields) {
		List<StreamResource> emptyReportStreamResourceList = new ArrayList<StreamResource>(List.of(new StreamResource("report.csv", () -> { return new ByteArrayInputStream(new byte[0]); })));

		try {
			if (reportFields.reportSelect.getValue() == null) {
				showError("Select a Report before clicking Generate");
				return emptyReportStreamResourceList;
			}
			System.out.println(reportFields.reportSelect.getValue());
            switch(reportFields.reportSelect.getValue()) {
            case "All Pending Transactions":
            	reportList = reportService.pendingTransactions();
            	break;
            case "Open Investments":
            	reportList = reportService.investmentsWithPendingTransactions();
            	break;
            case "ISIN All Details":
            	// reportList = reportService.isinReport();
            	break;
            case "Receipt Transactions":
            case "Period Summary":
            case "Anticipated Vs. Actual":
            case "Income Vs. Spend":
            	PeriodSummaryCriteriaVO periodSummaryCriteriaVO;
            	if (reportFields.periodFromDatePicker.getValue() == null || reportFields.periodToDatePicker.getValue() == null) {
					showError("Select the period before clicking Generate");
					return emptyReportStreamResourceList;
				}
            	periodSummaryCriteriaVO = new PeriodSummaryCriteriaVO(reportFields.periodFromDatePicker.getValue(), reportFields.periodToDatePicker.getValue());
	            switch(reportFields.reportSelect.getValue()) {
	            case "Receipt Transactions":
	            	reportList = reportService.receiptTransactions(periodSummaryCriteriaVO);
	            	break;
	            case "Period Summary":
            		reportList = reportService.periodSummary(periodSummaryCriteriaVO);
            		break;
	            case "Anticipated Vs. Actual":
            		reportList = reportService.anticipatedVsActual(periodSummaryCriteriaVO);
            		break;
	            case "Income Vs. Spend":
	            	reportList = reportService.incomeVsSpend(periodSummaryCriteriaVO);
	            	break;
            	}
            	break;
            case "Tax Liability":
            	if (reportFields.financialYearStartIntegerField.getValue() == null) {
					showError("Provide the FY Start Year before clicking Generate");
					return emptyReportStreamResourceList;
				}
        		reportList = reportService.advanceTaxLiability(reportFields.financialYearStartIntegerField.getValue());
            	break;
            case "Details for Tax Filing":
            case "Readiness for Tax Filing":
            	DetailsForTaxFilingRequestVO detailsForTaxFilingRequestVO;
            	if (reportFields.financialYearStartIntegerField.getValue() == null || reportFields.investorDvSelect.getValue() == null) {
            		if (reportFields.investorDvSelect.getValue() == null) {
						showError("Select an Investor before clicking Generate");					            			
            		} else {
            			showError("Provide the FY Start Year before clicking Generate");
            		}
					return emptyReportStreamResourceList;
				}
            	detailsForTaxFilingRequestVO = new DetailsForTaxFilingRequestVO(
            			reportFields.financialYearStartIntegerField.getValue(),
            			reportFields.investorDvSelect.getValue().getId()
        				);
            	reportList = reportFields.reportSelect.getValue().equals("Details for Tax Filing") ?
            			reportService.detailsForTaxFiling(detailsForTaxFilingRequestVO) :
            			reportService.readinessForTaxFiling(detailsForTaxFilingRequestVO);
            	break;
            }
		} catch (Exception e) {
			showError(UtilFuncs.messageFromException(e));
			return emptyReportStreamResourceList;
		}
		
		System.out.println("No. of reports: " + reportList.size());
		int reportInd = 1;
		List<StreamResource> reportStreamResourceList = new ArrayList<StreamResource>(reportList.size());
		for (List<Object[]> recordList : reportList) {
			System.out.println("Report Size: " + recordList.size());
			reportStreamResourceList.add(new StreamResource(reportFields.reportSelect.getValue() + "_" + reportInd++ + ".csv", 
			new StreamResourceWriter() {
				
				@Override
				public void accept(OutputStream stream, VaadinSession session) throws IOException {
					try (CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
						for (Object[] record : recordList) {
							csvPrinter.printRecord(record);
						}
						csvPrinter.flush();
					}					
				}
			}
			));
		}
		return reportStreamResourceList;
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
	
	private class ReportFields {
		Select<String> reportSelect;
		DatePicker periodFromDatePicker, periodToDatePicker;
		IntegerField financialYearStartIntegerField;
		Select<IdValueVO> investorDvSelect;
	}
	
}
