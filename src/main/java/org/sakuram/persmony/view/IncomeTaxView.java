package org.sakuram.persmony.view;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.DueRealisationVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestmentTransaction2VO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsRequestVO;
import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsResponseVO;
import org.sakuram.persmony.valueobject.UpdateTaxDetailRequestVO;
import org.vaadin.firitin.components.DynamicFileDownloader;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="it", layout=PersMonyLayout.class)
@PageTitle("Income Tax")
public class IncomeTaxView extends Div {
	
	private static final long serialVersionUID = 7028492529441149519L;

	MiscService miscService;
	MoneyTransactionService moneyTransactionService;
	
	DTInvestmentTransactionDetailComponent dtInvestmentTransactionDetailComponent;
	
	public IncomeTaxView(MoneyTransactionService moneyTransactionService, MiscService miscService, DTInvestmentTransactionDetailComponent dtInvestmentTransactionDetailComponent) {
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;
		this.dtInvestmentTransactionDetailComponent = dtInvestmentTransactionDetailComponent;
		showForm();
	}
		
	public void showForm() {
		FormLayout parentFormLayout, formLayout;
		Select<Map.Entry<Integer,String>> operationSelect;
		List<Map.Entry<Integer, String>> operationItemsList;
		
		operationItemsList = new ArrayList<Map.Entry<Integer,String>>() {
			private static final long serialVersionUID = 1L;

			{
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Break-up for Realisation"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "Complete Investment Transactions"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(3, "Update Tax Detail"));
			}
		};
		
		parentFormLayout = new FormLayout();
		parentFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		add(parentFormLayout);
		
		operationSelect = new Select<Map.Entry<Integer,String>>();
		operationSelect.setItems(operationItemsList);
		operationSelect.setItemLabelGenerator(operationItem -> {
			return operationItem.getValue();
		});
		operationSelect.setPlaceholder("Select Operation");
		operationSelect.setId("PersmonyOperation");
		parentFormLayout.addFormItem(operationSelect, "Operation");
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                new ResponsiveStep("0", 1));
		parentFormLayout.add(formLayout);
		
		operationSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue().getKey()) {
	            case 1:
	            	handleBreakupRealisation(formLayout);
	            	break;
	            case 3:
	            	handleUpdateTaxDetail(formLayout);
	            	break;
	            }
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });
	}
	
	private void handleUpdateTaxDetail(FormLayout parentFormLayout) {
		HorizontalLayout hLayout;
		IntegerField financialYearStartIntegerField;
		Select<IdValueVO> investorDvSelect, productProviderDvSelect;
		FormLayout formLayout;
		RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisationsResponseVO;
		Button proceedButton;
		TriStateBooleanRadioButtonGroup inForm26asRBG, inAisRBG, withBreakupRBG;
		
		retrieveAccrualsRealisationsResponseVO = new RetrieveAccrualsRealisationsResponseVO();
		
		// UI Elements
		hLayout = new HorizontalLayout();
		parentFormLayout.addFormItem(hLayout, "Search Criteria");
		
		financialYearStartIntegerField = new IntegerField();
		financialYearStartIntegerField.setLabel("FY Start Year");
		hLayout.add(financialYearStartIntegerField);
		investorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_PRIMARY_INVESTOR, null, true, false);
		investorDvSelect.setLabel("Investor");
		hLayout.add(investorDvSelect);
		productProviderDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_PARTY, null, true, false);
		productProviderDvSelect.setLabel("Provider");
		hLayout.add(productProviderDvSelect);
		
		inForm26asRBG = new TriStateBooleanRadioButtonGroup();
		inForm26asRBG.setLabel("In Form 26AS");
		hLayout.add(inForm26asRBG);

		inAisRBG = new TriStateBooleanRadioButtonGroup();
		inAisRBG.setLabel("In AIS");
		hLayout.add(inAisRBG);

		withBreakupRBG = new TriStateBooleanRadioButtonGroup();
		withBreakupRBG.setLabel("With Breakup");
		hLayout.add(withBreakupRBG);

		proceedButton = new Button("Proceed");
		hLayout.add(proceedButton);
		proceedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		proceedButton.setDisableOnClick(true);
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		parentFormLayout.add(formLayout);
		
		financialYearStartIntegerField.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
		});
		
		investorDvSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
		});
		
		productProviderDvSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
		});
		
		proceedButton.addClickListener(event -> {
			try {
				Notification notification;
				formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
				if (financialYearStartIntegerField.getValue() == null) {
					ViewFuncs.showError("Provide the Start year of the FY");
					return;
				} else {
					try {
						RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisationsResponseVOL;
						retrieveAccrualsRealisationsResponseVOL = moneyTransactionService.retrieveAccrualsRealisations(
								new RetrieveAccrualsRealisationsRequestVO(
									financialYearStartIntegerField.getValue(),
									investorDvSelect.getValue() == null ? null : investorDvSelect.getValue().getId(),
									productProviderDvSelect.getValue() == null ? null : productProviderDvSelect.getValue().getId(),
									inForm26asRBG.getValue(),
									inAisRBG.getValue(),
									withBreakupRBG.getValue()
								));
						retrieveAccrualsRealisationsResponseVOL.copyTo(retrieveAccrualsRealisationsResponseVO); // To overcome "Local variable defined in an enclosing scope must be final or effectively final"
						handleUpdateTaxDetail2(formLayout, retrieveAccrualsRealisationsResponseVO);
					} catch (Exception e) {
						ViewFuncs.showError(UtilFuncs.messageFromException(e));
						return;
					}
					notification = Notification.show("No. of accruals / receipts fetched: " + retrieveAccrualsRealisationsResponseVO.getDueRealisationVOList().size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				}
			} finally {
				proceedButton.setEnabled(true);
			}
		});
	}

	private void handleBreakupRealisation(FormLayout formLayout) {
		HorizontalLayout hLayout;
		IntegerField realisationIdIntegerField;
		Button saveButton;
		AmountComponent amountComponent;
		InvestmentTransaction2VO investmentTransaction2VO;
		RealisationVO realisationVO;
		
		investmentTransaction2VO = new InvestmentTransaction2VO();
		realisationVO = new RealisationVO();
		
		realisationIdIntegerField = new IntegerField();
		formLayout.addFormItem(realisationIdIntegerField, "Realisation Id");
		
		formLayout.add(dtInvestmentTransactionDetailComponent.template());
		
		amountComponent = new AmountComponent(Constants.DVID_TRANSACTION_TYPE_RECEIPT);
		formLayout.addFormItem(amountComponent.getLayout(), "Realised Amount");
		
		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		saveButton = new Button("Save");
		hLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		
		realisationIdIntegerField.addValueChangeListener(event -> {
			try {
				if (realisationIdIntegerField.getValue() != null) {
					
					miscService.fetchRealisation(realisationIdIntegerField.getValue().longValue()).copyTo(realisationVO);
					amountComponent.setFieldValues(realisationVO.getAmount(), realisationVO.getReturnedPrincipalAmount(), realisationVO.getInterestAmount(), realisationVO.getTdsAmount());
					
					miscService.fetchInvestmentTransaction(realisationVO.getInvestmentTransactionId()).copyTo(investmentTransaction2VO);
					if (investmentTransaction2VO.getTransactionTypeDvId() != Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
						ViewFuncs.showError("Given Realisation does not correspond to a RECEIPT transaction");
					}
					dtInvestmentTransactionDetailComponent.showDetail(investmentTransaction2VO);
				}
			} catch (Exception e ) {
				ViewFuncs.showError(UtilFuncs.messageFromException(e));
				return;
			}
		});
		
		saveButton.addClickListener(event -> {
			Notification notification;

			try {
				if (!amountComponent.isInputValid()) {
					ViewFuncs.showError("Invalid Break-up");
					return;
				}
				try {
					RealisationVO realisationVOL;
					realisationVOL = new RealisationVO();
					realisationVOL.setRealisationId(realisationVO.getRealisationId());
					realisationVOL.setAmount(amountComponent.getNetAmount());
					realisationVOL.setReturnedPrincipalAmount(amountComponent.getReturnedPrincipalAmount());
					realisationVOL.setInterestAmount(amountComponent.getInterestAmount());
					realisationVOL.setTdsAmount(amountComponent.getTdsAmount());
					moneyTransactionService.updateRealisationAmounts(realisationVOL);
					notification = Notification.show("Realisation Break-up Saved Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}

	private void handleUpdateTaxDetail2(FormLayout formLayout, RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisationsResponseVO) {
		Grid<DueRealisationVO> accrualsRealisationsGrid;
		GridListDataView<DueRealisationVO> accrualsRealisationsGridLDV;
		FormLayout childFormLayout;
		
		accrualsRealisationsGrid = new Grid<>(DueRealisationVO.class);
		accrualsRealisationsGrid.setColumns("investmentId", "investor", "productProvider", "investmentIdWithProvider", "productType", "worth", "investmentTransactionId", "transactionType", "taxGroup", "dueDate", "dueAmount", "realisationId", "realisationDate", "realisationAmount", "interestAmount", "tdsAmount", "tdsReference", "inAis", "form26asBookingDate");
		for (Column<DueRealisationVO> column : accrualsRealisationsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(accrualsRealisationsGrid);
		accrualsRealisationsGridLDV = accrualsRealisationsGrid.setItems(retrieveAccrualsRealisationsResponseVO.getDueRealisationVOList());
		
		// Acknowledgement: https://cookbook.vaadin.com/grid-csv-export
		// TODO: To use CSVPrinter
		formLayout.add(new DynamicFileDownloader("Download as CSV...", "tax_transactions.csv", out -> {
			Stream<DueRealisationVO> dueRealisationVOStream = null;
			dueRealisationVOStream = accrualsRealisationsGrid.getGenericDataView().getItems();

			PrintWriter writer = new PrintWriter(out);
			writer.println("investmentId,investor,productProvider,investmentIdWithProvider,productType,worth,investmentTransactionId,transactionType,taxGroup,dueDate,dueAmount,realisationId,realisationDate,realisationAmount,interestAmount,tdsAmount,tdsReference,inAis,form26asBookingDate");
			dueRealisationVOStream.forEach(dueRealisationVO -> {
				writer.println(dueRealisationVO.toString());
			});
			writer.close();
		}));

		childFormLayout = new FormLayout();
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		formLayout.add(childFormLayout);
		
		accrualsRealisationsGrid.addItemClickListener(event -> {
			DueRealisationVO selectedDueRealisationVO;
			
			selectedDueRealisationVO = event.getItem();
			
			childFormLayout.remove(childFormLayout.getChildren().collect(Collectors.toList()));
			
			handleUpdateTaxDetail3(childFormLayout, selectedDueRealisationVO, accrualsRealisationsGridLDV);
		});

	}
	
	private void handleUpdateTaxDetail3(FormLayout formLayout, DueRealisationVO selectedDueRealisationVO, GridListDataView<DueRealisationVO> accrualsRealisationsGridLDV) {
		HorizontalLayout hLayout;
		NativeLabel label1;
		NumberField interestNumberField, tdsNumberField;
		DatePicker accountedDatePicker, form26asBookingDatePicker;
		TextField form16aCertificateTextField;
		Checkbox inAisCheckbox;
		Button saveButton;
		long id;
		
		id = selectedDueRealisationVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? selectedDueRealisationVO.getInvestmentTransactionId() : selectedDueRealisationVO.getRealisationId();
		// UI Elements
		label1 = new NativeLabel();
		// formLayout.addFormItem(label1, "Tax Details of");
		label1.getElement().setProperty("innerHTML", "<b>" + selectedDueRealisationVO.getTransactionType()
				+ "</b> id <b>"
				+ id
				+ "</b>");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Amount");
		interestNumberField = new NumberField();
		tdsNumberField = new NumberField();
		accountedDatePicker = new DatePicker();
		hLayout.add(new Span("Interest"), interestNumberField, new Span("TDS"), tdsNumberField, new Span("Accounted Date"), accountedDatePicker);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Tax Detail");
		form26asBookingDatePicker = new DatePicker();
		form16aCertificateTextField = new TextField();
		inAisCheckbox = new Checkbox();
		inAisCheckbox.setValue(false);
		hLayout.add(new Span("Form 26AS Booking Date"), form26asBookingDatePicker, new Span("Form 16A Certificate"), form16aCertificateTextField, new Span("In AIS"), inAisCheckbox);
    	if (selectedDueRealisationVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
    		if (selectedDueRealisationVO.getInvestmentTransactionInterestAmount() != null) {
    			interestNumberField.setValue(selectedDueRealisationVO.getInvestmentTransactionInterestAmount());
    		}
    		if (selectedDueRealisationVO.getInvestmentTransactionTdsAmount() != null) {
    			tdsNumberField.setValue(selectedDueRealisationVO.getInvestmentTransactionTdsAmount());
    		}
    		if (selectedDueRealisationVO.getDueDate() != null) {
        		accountedDatePicker.setValue(selectedDueRealisationVO.getDueDate());    			
    		}
    		if (selectedDueRealisationVO.getInvestmentTransactionForm26asBookingDate() != null) {
        		form26asBookingDatePicker.setValue(selectedDueRealisationVO.getInvestmentTransactionForm26asBookingDate());
    		}
    		if (selectedDueRealisationVO.getAccrualTdsReference() != null) {
    			form16aCertificateTextField.setValue(selectedDueRealisationVO.getAccrualTdsReference());
    		}
    		if (selectedDueRealisationVO.getInvestmentTransactionInAis() != null) {
    			inAisCheckbox.setValue(selectedDueRealisationVO.getInvestmentTransactionInAis());
    		}
    	} else {
    		if (selectedDueRealisationVO.getRealisationInterestAmount() != null) {    			
    			interestNumberField.setValue(selectedDueRealisationVO.getRealisationInterestAmount());
    		}
    		if (selectedDueRealisationVO.getRealisationTdsAmount() != null) {
    			tdsNumberField.setValue(selectedDueRealisationVO.getRealisationTdsAmount());
    		}
    		if (selectedDueRealisationVO.getRealisationDate() != null) {
    			accountedDatePicker.setValue(selectedDueRealisationVO.getRealisationDate());
    		}
    		if (selectedDueRealisationVO.getRealisationForm26asBookingDate() != null) {
    			form26asBookingDatePicker.setValue(selectedDueRealisationVO.getRealisationForm26asBookingDate());
    		}
    		if (selectedDueRealisationVO.getRealisationTdsReference() != null) {
    			form16aCertificateTextField.setValue(selectedDueRealisationVO.getRealisationTdsReference());
    		}
    		if (selectedDueRealisationVO.getRealisationInAis() != null) {
    			inAisCheckbox.setValue(selectedDueRealisationVO.getRealisationInAis());
    		}
    	}
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			Notification notification;

			try {
				UpdateTaxDetailRequestVO updateTaxDetailRequestVO;
				// Back-end Call
				try {
					updateTaxDetailRequestVO = new UpdateTaxDetailRequestVO(
							id,
							selectedDueRealisationVO.getTransactionTypeDvId(),
							accountedDatePicker.getValue(),
							interestNumberField.getValue() == null ? null : interestNumberField.getValue().doubleValue(),
							tdsNumberField.getValue() == null ? null : tdsNumberField.getValue().doubleValue(),
							form16aCertificateTextField.getValue().equals("") ? null : form16aCertificateTextField.getValue(),
							inAisCheckbox.getValue(),
							form26asBookingDatePicker.getValue()
							);
							
					moneyTransactionService.updateTaxDetail(updateTaxDetailRequestVO);
					notification = Notification.show("Realistion Saved Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			    	if (selectedDueRealisationVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			    		selectedDueRealisationVO.setDueDate(updateTaxDetailRequestVO.getAccountedDate());
			    		selectedDueRealisationVO.setInvestmentTransactionInterestAmount(updateTaxDetailRequestVO.getInterestAmount());
			    		selectedDueRealisationVO.setInvestmentTransactionTdsAmount(updateTaxDetailRequestVO.getTdsAmount());
			    		selectedDueRealisationVO.setAccrualTdsReference(updateTaxDetailRequestVO.getTdsReference());
			    		selectedDueRealisationVO.setInvestmentTransactionInAis(updateTaxDetailRequestVO.getInAis() ? true : null); // In DB, a FALSE and Not Known will be NULL, but inAisCheckbox.getValue() will never be NULL
			    		selectedDueRealisationVO.setInvestmentTransactionForm26asBookingDate(updateTaxDetailRequestVO.getForm26asBookingDate());
			    	} else {
			    		selectedDueRealisationVO.setRealisationDate(updateTaxDetailRequestVO.getAccountedDate());
			    		selectedDueRealisationVO.setRealisationInterestAmount(updateTaxDetailRequestVO.getInterestAmount());
			    		selectedDueRealisationVO.setRealisationTdsAmount(updateTaxDetailRequestVO.getTdsAmount());
			    		selectedDueRealisationVO.setRealisationTdsReference(updateTaxDetailRequestVO.getTdsReference());
			    		selectedDueRealisationVO.setRealisationInAis(updateTaxDetailRequestVO.getInAis() ? true : null);
			    		selectedDueRealisationVO.setRealisationForm26asBookingDate(updateTaxDetailRequestVO.getForm26asBookingDate());
			    	}
					accrualsRealisationsGridLDV.refreshItem(selectedDueRealisationVO);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
}
