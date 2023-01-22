package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestVO;
import org.sakuram.persmony.valueobject.ReceiptDuesVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.router.Route;

@Route("operation")
public class OperationView extends Div {
	private static final long serialVersionUID = 6529685098267757690L;
	
	MoneyTransactionService moneyTransactionService;
	MiscService miscService;

	public OperationView(MoneyTransactionService moneyTransactionService, MiscService miscService) {
		Span selectSpan;
		FormLayout formLayout;
		Select<Map.Entry<Integer,String>> operationSelect;
		List<Map.Entry<Integer, String>> operationItemsList;
		
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;

		operationItemsList = new ArrayList<Map.Entry<Integer,String>>() {
			private static final long serialVersionUID = 1L;

			{
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Realisation"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(3, "Accrual OR *New Transaction + Single Realisation With Bank*"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(4, "Invest"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(5, "Renewal"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(6, "Existing Investment, Receipt Dues"));
			}
		};
		selectSpan = new Span();
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		
		operationSelect = new Select<Map.Entry<Integer,String>>();
		operationSelect.setItems(operationItemsList);
		operationSelect.setItemLabelGenerator(operationItem -> {
			return operationItem.getValue();
		});
		operationSelect.setLabel("Operation");
		operationSelect.setPlaceholder("Select Operation");
		operationSelect.setId("PersmonyOperation");
		operationSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue().getKey()) {
	            case 1:
	            	handleRealisation(formLayout);
	            	break;
	            case 3:
	            	handleTxnSingleRealisationWithBank(formLayout);
	            	break;
	            case 4:
	            	handleInvest(formLayout);
	            	break;
	            case 5:
	            	handleRenewal(formLayout);
	            	break;
	            case 6:
	            	handleReceiptDues(formLayout);
	            	break;
	            }
			} catch (Exception e) {
				showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });

		selectSpan.add(operationSelect);
		add(selectSpan);
		add(formLayout);
	}

	private void handleRealisation(FormLayout parentFormLayout) {
		Select<IdValueVO> realisationTypeDvSelect;
		List<IdValueVO> idValueVOList;
		FormLayout formLayout;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		// UI Elements
		realisationTypeDvSelect = new Select<IdValueVO>();
		parentFormLayout.addFormItem(realisationTypeDvSelect, "Realisation Type");
		parentFormLayout.add(formLayout);
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_REALISATION_TYPE);
		realisationTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		realisationTypeDvSelect.setItems(idValueVOList);
		realisationTypeDvSelect.setPlaceholder("Select Realisation Type");
		realisationTypeDvSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			handleRealisation2(formLayout, realisationTypeDvSelect.getValue());
		});
	}
	
	private void handleRealisation2(FormLayout formLayout, IdValueVO selectedRealisationIdValueVO) {
		IntegerField investmentTransactionIdIntegerField, realisationIdIntegerField, savingsAccountTransactionIntegerField;	// Should be converted to LongField
		NumberField amountNumberField;
		DatePicker transactionDatePicker;
		Select<IdValueVO> closureTypeDvSelect;
		Button saveButton;
		List<IdValueVO> idValueVOList;
		Checkbox lastRealisationCheckbox;
		Select<IdValueVO> bankAccountDvSelect;
		List<IdValueVO> idValueVOList2;
		HorizontalLayout hLayout;
		
		// UI Elements
		investmentTransactionIdIntegerField = new IntegerField();
		formLayout.addFormItem(investmentTransactionIdIntegerField, "Investment Transaction Id");
		
		amountNumberField = new NumberField();
		formLayout.addFormItem(amountNumberField, "Realised Amount");
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Realised Date");
		
		lastRealisationCheckbox = new Checkbox();
		formLayout.addFormItem(lastRealisationCheckbox, "Last Realisation");
		
		closureTypeDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_CLOSURE_TYPE);
		closureTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		closureTypeDvSelect.setItems(idValueVOList);
		closureTypeDvSelect.setPlaceholder("Select Account Closure Type");
		
		bankAccountDvSelect = new Select<IdValueVO>();
		realisationIdIntegerField = new IntegerField();
		savingsAccountTransactionIntegerField = new IntegerField();
		if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
			hLayout = new HorizontalLayout();
			formLayout.addFormItem(hLayout, "Account Transaction");
			savingsAccountTransactionIntegerField.setLabel("Old: Existing Id");
			hLayout.add(savingsAccountTransactionIntegerField);
			
			hLayout.add(bankAccountDvSelect);
			bankAccountDvSelect.setLabel("New: Account");
			idValueVOList2 = miscService.fetchDvsOfCategory(Constants.CATEGORY_ACCOUNT);
			bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
				if (idValueVO == null) {	// Required if EmptySelectionAllowed
					return "None";			// EmptySelectionCaption
				} else {
					return idValueVO.getValue();
				}
			});
			bankAccountDvSelect.setItems(idValueVOList2);
			bankAccountDvSelect.setPlaceholder("Select Account");
			bankAccountDvSelect.setEmptySelectionAllowed(true);
		} else if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
			formLayout.addFormItem(realisationIdIntegerField, "Realisation Id");
		}
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			SingleRealisationVO singleRealisationVO;
			Notification notification;

			try {
				// Validation
				if (investmentTransactionIdIntegerField.getValue() == null || investmentTransactionIdIntegerField.getValue() <= 0) {
					showError("Invalid Investment Transaction Id");
					return;
				}
				if (amountNumberField.getValue() == null || amountNumberField.getValue() <= 0) {
					showError("Invalid Amount");
					return;
				}
				if (transactionDatePicker.getValue() == null) {
					showError("Date cannot be Empty");
					return;
				}
				if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
					if (savingsAccountTransactionIntegerField.getValue() == null && bankAccountDvSelect.getValue() == null ||
							savingsAccountTransactionIntegerField.getValue() != null && bankAccountDvSelect.getValue() != null) {
						showError("One of Account Transaction Id Or Account is to be provided");
						return;
					}
					if (savingsAccountTransactionIntegerField.getValue() != null && savingsAccountTransactionIntegerField.getValue() <= 0) {
						showError("Invalid Account Transaction Id");
						return;
					}
				}
				if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
					if (realisationIdIntegerField.getValue() != null && realisationIdIntegerField.getValue() <= 0) {
						showError("Invalid Realisation Id");
						return;
					}
				}
				
				// Back-end Call
				singleRealisationVO = new SingleRealisationVO(
						Long.valueOf(selectedRealisationIdValueVO.getId()),
						investmentTransactionIdIntegerField.getValue(),
						savingsAccountTransactionIntegerField.getValue() == null ? null : Long.valueOf(savingsAccountTransactionIntegerField.getValue()),
						bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId(),
						realisationIdIntegerField.getValue() == null ? null : Long.valueOf(realisationIdIntegerField.getValue()),
						(double)amountNumberField.getValue().doubleValue(),
						Date.valueOf(transactionDatePicker.getValue()),
						lastRealisationCheckbox.getValue() == null || !lastRealisationCheckbox.getValue() ? false : true,
						closureTypeDvSelect.getValue() == null? null : closureTypeDvSelect.getValue().getId());
				try {
					moneyTransactionService.realisation(singleRealisationVO);
					notification = Notification.show("Realistion Saved Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleTxnSingleRealisationWithBank(FormLayout formLayout) {
		TextField investmentIdTextField;
		NumberField amountNumberField;
		DatePicker transactionDatePicker;
		Select<IdValueVO> bankAccountDvSelect, transactionTypeDvSelect;
		Button saveButton;
		List<IdValueVO> idValueVOList;
		
		// UI Elements
		investmentIdTextField = new TextField();
		formLayout.addFormItem(investmentIdTextField, "Investment Id");
		
		transactionTypeDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(transactionTypeDvSelect, "Transaction Type");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_TRANSACTION_TYPE);
		transactionTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		transactionTypeDvSelect.setItems(idValueVOList);
		transactionTypeDvSelect.setPlaceholder("Select Transaction Type");
		
		amountNumberField = new NumberField();
		formLayout.addFormItem(amountNumberField, "Amount Paid/Received");
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Date Paid/Received");
		
		bankAccountDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(bankAccountDvSelect, "Bank Account");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		bankAccountDvSelect.setItems(idValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			TxnSingleRealisationWithBankVO txnSingleRealisationWithBankVO;
			Notification notification;

			try {
				// Validation
				if (investmentIdTextField.getValue() == null || investmentIdTextField.getValue().equals("")) {
					showError("Investment Transaction Id cannot be Empty");
					return;
				}
				if (transactionTypeDvSelect.getValue() == null) {
					showError("Transaction Type cannot be Empty");
					return;
				}
				if (amountNumberField.getValue() == null) {
					showError("Amount cannot be Empty");
					return;
				}
				if (transactionDatePicker.getValue() == null) {
					showError("Date cannot be Empty");
					return;
				}
				if (transactionTypeDvSelect.getValue().getId() != Constants.DVID_TRANSACTION_TYPE_ACCRUAL && bankAccountDvSelect.getValue() == null) {
					showError("Account cannot be Empty");
					return;
				}
				
				// Back-end Call
				txnSingleRealisationWithBankVO = new TxnSingleRealisationWithBankVO(
						Long.parseLong(investmentIdTextField.getValue()),
						transactionTypeDvSelect.getValue().getId(),
						(double)amountNumberField.getValue().doubleValue(),
						Date.valueOf(transactionDatePicker.getValue()),
						bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId());
				try {
					moneyTransactionService.txnSingleRealisationWithBank(txnSingleRealisationWithBankVO);
					notification = Notification.show("Transaction and Realisation Saved Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleInvest(FormLayout formLayout) {
		Select<IdValueVO> investorDvSelect, productProviderDvSelect, providerBranchDvSelect, productTypeDvSelect, dematAccountDvSelect, taxabilityDvSelect, bankAccountDvSelect;
		TextField productIdOfProviderTextField, investorIdWithProviderTextField, productNameTextField, investmentIdWithProviderTextField;
		RadioButtonGroup<String> accrualApplicabilityRadioButtonGroup, dynamicReceiptPeriodicityRadioButtonGroup;
		NumberField rateOfInterestNumberField, faceValueNumberField, cleanPriceNumberField, accruedInterestNumberField, chargesNumberField;
		DatePicker productEndDatePicker;
		List<IdValueVO> idValueVOList;
		HorizontalLayout hLayout;		
		Button saveButton, paymentScheduleButton, receiptScheduleButton, accrualScheduleButton;
		List<ScheduleVO> paymentScheduleVOList,  receiptScheduleVOList, accrualScheduleVOList;

		// UI Elements
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Investor");
		investorDvSelect = new Select<IdValueVO>();
		investorDvSelect.setLabel("Investor");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_INVESTOR);
		investorDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		investorDvSelect.setItems(idValueVOList);
		investorDvSelect.setPlaceholder("Select Investor");
		investorIdWithProviderTextField = new TextField("Id with Provider");
		hLayout.add(investorDvSelect, investorIdWithProviderTextField);

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Provider");
		productProviderDvSelect = new Select<IdValueVO>();
		productProviderDvSelect.setLabel("Provider");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_PARTY);
		productProviderDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		productProviderDvSelect.setItems(idValueVOList);
		productProviderDvSelect.setPlaceholder("Select Product Provider");
		providerBranchDvSelect = new Select<IdValueVO>();
		productProviderDvSelect.addValueChangeListener(event -> {
			providerBranchDvSelect.setItemLabelGenerator(idValueVO -> {
				return idValueVO.getValue();
			});
			providerBranchDvSelect.setItems(miscService.fetchBranchesOfParty(productProviderDvSelect.getValue().getId()));
		});
		providerBranchDvSelect.setLabel("Branch");
		providerBranchDvSelect.setPlaceholder("Select Provider Branch");
		hLayout.add(productProviderDvSelect, providerBranchDvSelect);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Product");
		productTypeDvSelect = new Select<IdValueVO>();
		productTypeDvSelect.setLabel("Product Type");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_PRODUCT_TYPE);
		productTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		productTypeDvSelect.setItems(idValueVOList);
		productTypeDvSelect.setPlaceholder("Select Product Type");
		productNameTextField = new TextField("Product Name");
		productIdOfProviderTextField = new TextField("Id of Provider");
		hLayout.add(productTypeDvSelect, productNameTextField, productIdOfProviderTextField);

		investmentIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id with Provider");

		dematAccountDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(dematAccountDvSelect, "Demat Account");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT);
		dematAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		dematAccountDvSelect.setItems(idValueVOList);
		dematAccountDvSelect.setPlaceholder("Select Demat Account");
		
		taxabilityDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(taxabilityDvSelect, "Taxability");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_TAXABILITY);
		taxabilityDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		taxabilityDvSelect.setItems(idValueVOList);
		taxabilityDvSelect.setPlaceholder("Select Taxability");
		
		accrualApplicabilityRadioButtonGroup = new RadioButtonGroup<>();
		formLayout.addFormItem(accrualApplicabilityRadioButtonGroup, "Accrual Applicability");
		// accrualApplicabilityRadioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		accrualApplicabilityRadioButtonGroup.setItems("Not Known", "Not Applicable", "Applicable");
		accrualApplicabilityRadioButtonGroup.setValue("Not Known");
		
		bankAccountDvSelect = new Select<IdValueVO>();
		bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		bankAccountDvSelect.setItems(miscService.fetchDvsOfCategory(Constants.CATEGORY_ACCOUNT));
		formLayout.addFormItem(bankAccountDvSelect, "Realisation from Account");
		bankAccountDvSelect.setPlaceholder("Select Account");

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Price");
		faceValueNumberField = new NumberField("Face Value");
		cleanPriceNumberField = new NumberField("Clean Price");
		accruedInterestNumberField = new NumberField("Accrued Interest");
		chargesNumberField = new NumberField("Charges");
		hLayout.add(faceValueNumberField, cleanPriceNumberField, accruedInterestNumberField, chargesNumberField);
		
		rateOfInterestNumberField = new NumberField();
		rateOfInterestNumberField.setMax(100.00);
		formLayout.addFormItem(rateOfInterestNumberField, "Rate Of Interest%");
		
		productEndDatePicker = new DatePicker();
		formLayout.addFormItem(productEndDatePicker, "Product End Date");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Schedule");
		paymentScheduleVOList = new ArrayList<ScheduleVO>();
		paymentScheduleButton = new Button("Payment (0)");
		paymentScheduleButton.addClickListener(event -> {
			acceptSchedule("Payment", paymentScheduleButton, paymentScheduleVOList);
		});
		receiptScheduleVOList = new ArrayList<ScheduleVO>();
		receiptScheduleButton = new Button("Receipt (0)");
		receiptScheduleButton.addClickListener(event -> {
			acceptSchedule("Receipt", receiptScheduleButton, receiptScheduleVOList);
		});
		accrualScheduleVOList = new ArrayList<ScheduleVO>();
		accrualScheduleButton = new Button("Accrual (0)");
		accrualScheduleButton.addClickListener(event -> {
			acceptSchedule("Accrual", accrualScheduleButton, accrualScheduleVOList);
		});
		hLayout.add(paymentScheduleButton, receiptScheduleButton, accrualScheduleButton);
		
		dynamicReceiptPeriodicityRadioButtonGroup = new RadioButtonGroup<>();
		formLayout.addFormItem(dynamicReceiptPeriodicityRadioButtonGroup, "Dynamic Receipt Periodicity");
		dynamicReceiptPeriodicityRadioButtonGroup.setItems("Not Applicable", "Yearly");
		dynamicReceiptPeriodicityRadioButtonGroup.setValue("Not Applicable");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			InvestVO investVO;
			Notification notification;

			try {
				// Validation
				if (investorDvSelect.getValue() == null) {
					showError("Investor cannot be Empty");
					return;
				}
				if (productProviderDvSelect.getValue() == null) {
					showError("Product Provider cannot be Empty");
					return;
				}
				if (productTypeDvSelect.getValue() == null) {
					showError("Product Type cannot be Empty");
					return;
				}
				if (faceValueNumberField.getValue() == null) {
					showError("Face Value cannot be Empty");
					return;
				}
				if (paymentScheduleVOList == null || paymentScheduleVOList.isEmpty()) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				
				// Back-end Call
				investVO = new InvestVO(
						investorDvSelect.getValue().getId(),
						productProviderDvSelect.getValue().getId(),
						providerBranchDvSelect.getValue() == null ? null : providerBranchDvSelect.getValue().getId(),
						productIdOfProviderTextField.getValue().equals("") ? null : productIdOfProviderTextField.getValue(),
						investorIdWithProviderTextField.getValue().equals("") ? null : investorIdWithProviderTextField.getValue(),
						productNameTextField.getValue().equals("") ? null : productNameTextField.getValue(),
						productTypeDvSelect.getValue().getId(),
						dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(),
						taxabilityDvSelect.getValue() == null ? null : taxabilityDvSelect.getValue().getId(),
						(accrualApplicabilityRadioButtonGroup.getValue() == null || accrualApplicabilityRadioButtonGroup.getValue().equals("Not Known")) ? null : (accrualApplicabilityRadioButtonGroup.getValue().equals("Not Applicable") ? false : true),
						(bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId()),
						investmentIdWithProviderTextField.getValue().equals("") ? null : investmentIdWithProviderTextField.getValue(),
						(double)faceValueNumberField.getValue().doubleValue(),
						cleanPriceNumberField.getValue() == null ? null : (double)cleanPriceNumberField.getValue().doubleValue(),
						accruedInterestNumberField.getValue() == null ? null : (double)accruedInterestNumberField.getValue().doubleValue(),
						chargesNumberField.getValue() == null ? null : (double)chargesNumberField.getValue().doubleValue(),
						rateOfInterestNumberField.getValue() == null ? null : (double)rateOfInterestNumberField.getValue().doubleValue(),
						productEndDatePicker.getValue() == null ? null : Date.valueOf(productEndDatePicker.getValue()),
						paymentScheduleVOList,
						receiptScheduleVOList,
						(dynamicReceiptPeriodicityRadioButtonGroup.getValue().equals("Not Applicable") ? null : 'Y'),
						accrualScheduleVOList);
				try {
					moneyTransactionService.invest(investVO);
					paymentScheduleVOList.clear();
					paymentScheduleButton.setText("Payment (0)");
					receiptScheduleVOList.clear();
					receiptScheduleButton.setText("Receipt (0)");
					accrualScheduleVOList.clear();
					accrualScheduleButton.setText("Accrual (0)");
					notification = Notification.show("Investment Created Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleRenewal(FormLayout formLayout) {
		TextField oldInvestmentIdTextField, investmentIdWithProviderTextField;
		NumberField rateOfInterestNumberField, faceValueNumberField;
		DatePicker productEndDatePicker;
		Label label1;
		Button saveButton, paymentScheduleButton, receiptScheduleButton, accrualScheduleButton;
		List<ScheduleVO> paymentScheduleVOList,  receiptScheduleVOList, accrualScheduleVOList;
		HorizontalLayout hLayout;
		
		// UI Elements
		label1 = new Label();
		formLayout.addFormItem(label1, "");
		label1.getElement().setProperty("innerHTML", "<b>Details of the old investment being renewed</b>");
		
		oldInvestmentIdTextField = new TextField();
		formLayout.addFormItem(oldInvestmentIdTextField, "Persmony Investment Id");
		
		label1 = new Label();
		formLayout.addFormItem(label1, "");
		label1.getElement().setProperty("innerHTML", "<b>Details of the new/renewed investment</b>");
		
		investmentIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id with Provider");
		
		faceValueNumberField = new NumberField();
		formLayout.addFormItem(faceValueNumberField, "Face Value");
		
		rateOfInterestNumberField = new NumberField();
		rateOfInterestNumberField.setMax(100.00);
		formLayout.addFormItem(rateOfInterestNumberField, "Rate Of Interest%");
		
		productEndDatePicker = new DatePicker();
		formLayout.addFormItem(productEndDatePicker, "Product End Date");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Schedule");
		paymentScheduleVOList = new ArrayList<ScheduleVO>();
		paymentScheduleButton = new Button("Payment (0)");
		paymentScheduleButton.addClickListener(event -> {
			acceptSchedule("Payment", paymentScheduleButton, paymentScheduleVOList);
		});
		receiptScheduleVOList = new ArrayList<ScheduleVO>();
		receiptScheduleButton = new Button("Receipt (0)");
		receiptScheduleButton.addClickListener(event -> {
			acceptSchedule("Receipt", receiptScheduleButton, receiptScheduleVOList);
		});
		accrualScheduleVOList = new ArrayList<ScheduleVO>();
		accrualScheduleButton = new Button("Accrual (0)");
		accrualScheduleButton.addClickListener(event -> {
			acceptSchedule("Accrual", accrualScheduleButton, accrualScheduleVOList);
		});
		hLayout.add(paymentScheduleButton, receiptScheduleButton, accrualScheduleButton);
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			RenewalVO renewalVO;
			Notification notification;

			try {
				// Validation
				if (oldInvestmentIdTextField.getValue() == null || oldInvestmentIdTextField.getValue().equals("")) {
					showError("Id of old Investment being renewed cannot be Empty");
					return;
				}
				if (investmentIdWithProviderTextField.getValue() == null || investmentIdWithProviderTextField.getValue().equals("")) {
					showError("Investment Id with Provider cannot be Empty");
					return;
				}
				if (productEndDatePicker.getValue() == null) {
					showError("Product End Date cannot be Empty");
					return;
				}
				if (faceValueNumberField.getValue() == null) {
					showError("Face Value cannot be Empty");
					return;
				}
				if (paymentScheduleVOList.isEmpty()) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				
				// Back-end Call
				renewalVO = new RenewalVO(
						Long.parseLong(oldInvestmentIdTextField.getValue()),
						investmentIdWithProviderTextField.getValue(),
						(double)faceValueNumberField.getValue().doubleValue(),
						rateOfInterestNumberField.getValue() == null ? null : (double)rateOfInterestNumberField.getValue().doubleValue(),
						Date.valueOf(productEndDatePicker.getValue()),
						paymentScheduleVOList,
						receiptScheduleVOList,
						accrualScheduleVOList);
				try {
					moneyTransactionService.renewal(renewalVO);
					paymentScheduleVOList.clear();
					paymentScheduleButton.setText("Payment (0)");
					receiptScheduleVOList.clear();
					receiptScheduleButton.setText("Receipt (0)");
					accrualScheduleVOList.clear();
					accrualScheduleButton.setText("Accrual (0)");
					notification = Notification.show("Renewal Done Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleReceiptDues(FormLayout formLayout) {
		TextField investmentIdTextField;
		Button saveButton, receiptScheduleButton;
		List<ScheduleVO> receiptScheduleVOList;
		
		investmentIdTextField = new TextField();
		formLayout.addFormItem(investmentIdTextField, "Persmony Investment Id");
		
		receiptScheduleVOList = new ArrayList<ScheduleVO>();
		receiptScheduleButton = new Button("Receipt (0)");
		receiptScheduleButton.addClickListener(event -> {
			acceptSchedule("Receipt", receiptScheduleButton, receiptScheduleVOList);
		});
		formLayout.addFormItem(receiptScheduleButton,"Schedule");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			ReceiptDuesVO receiptDuesVO;
			Notification notification;
			
			try {
				// Validation
				if (investmentIdTextField.getValue() == null || investmentIdTextField.getValue().equals("")) {
					showError("Id of Investment cannot be Empty");
					return;
				}
				if (receiptScheduleVOList.isEmpty()) {
					showError("Receipt Schedule cannot be Empty");
					return;
				}
				
				// Back-end Call
				receiptDuesVO = new ReceiptDuesVO(Long.parseLong(investmentIdTextField.getValue()), receiptScheduleVOList);
				try {
					moneyTransactionService.addReceiptDues(receiptDuesVO);
					receiptScheduleVOList.clear();
					receiptScheduleButton.setText("Receipt (0)");
					notification = Notification.show("Receipt Dues Added Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		errorDialog.open();
		
	}
	
	private void acceptSchedule(String label, Button scheduleButton, List<ScheduleVO> scheduleVOList) {
		Dialog dialog;
		VerticalLayout verticalLayout;
		HorizontalLayout hLayout;
		Button addButton, closeButton, returnButton, patternToListButton;
		Grid<ScheduleVO> scheduleGrid;
		Binder<ScheduleVO> scheduleBinder;
		Editor<ScheduleVO> scheduleEditor;
		GridListDataView<ScheduleVO> scheduleGridLDV;
		NumberField dueAmountNumberField, returnedPrincipalAmountNumberField, interestAmountNumberField, tdsAmountNumberField;
		DatePicker dueDatePicker;
		Grid.Column<ScheduleVO> dueDateColumn, dueAmountColumn, returnedPrincipalAmountColumn, interestAmountColumn, tdsAmountColumn;
		List<ScheduleVO> preEditScheduleVOList;
		TextField scheduleTextField;
		
		preEditScheduleVOList = new ArrayList<ScheduleVO>(scheduleVOList.size());
		preEditScheduleVOList.addAll(scheduleVOList);
		
		dialog = new Dialog();
		dialog.setHeaderTitle(label + " Schedule");
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
		        	scheduleVOList.clear();
		    		scheduleVOList.addAll(preEditScheduleVOList);
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		verticalLayout = new VerticalLayout();
		verticalLayout.getStyle().set("width", "75rem");
		dialog.add(verticalLayout);
		
		hLayout = new HorizontalLayout();
		verticalLayout.add(hLayout);
		scheduleTextField = new TextField("Schedule Pattern");
		hLayout.add(scheduleTextField);
		
		scheduleGrid = new Grid<>(ScheduleVO.class, false);
		scheduleGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		scheduleGridLDV = scheduleGrid.setItems(scheduleVOList);
		
		patternToListButton = new Button("Pattern To List");
		patternToListButton.addClickListener(event -> {
			Notification notification;
			try {
				List<ScheduleVO> fromPatternScheduleVOList;
				fromPatternScheduleVOList = UtilFuncs.parseScheduleData(scheduleTextField.getValue());
				if (scheduleVOList.size() == 0) {
					scheduleGridLDV.addItems(fromPatternScheduleVOList);
				} else {
					scheduleGridLDV.addItemsAfter(fromPatternScheduleVOList, scheduleVOList.get(scheduleVOList.size() - 1));
				}
			} catch (AppException e) {
				showError("Schedule: " + e.getMessage());
				return;
			}
			scheduleTextField.setValue("");
			notification = Notification.show("Pattern added to list.");
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});
		patternToListButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(patternToListButton);
		
		verticalLayout.add(scheduleGrid);
		
		hLayout = new HorizontalLayout();
		verticalLayout.add(hLayout);
		addButton = new Button("Add Row");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add Row
		addButton.addClickListener(event -> {
			try {
				scheduleGridLDV.addItem(new ScheduleVO());
			} finally {
				addButton.setEnabled(true);
			}
		});
		hLayout.add(addButton);

		returnButton = new Button("Return");
		returnButton.addClickListener(event -> {
			scheduleButton.setText(scheduleButton.getText().replaceFirst("\\(\\d+\\)", "(" + Integer.toString(scheduleVOList.size()) + ")"));
			dialog.close();
		});
		returnButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(returnButton);

		dueDateColumn = scheduleGrid.addColumn(ScheduleVO::getDueDate).setHeader("Date");
		dueAmountColumn = scheduleGrid.addColumn(ScheduleVO::getDueAmount).setHeader("Due Amount");
		returnedPrincipalAmountColumn = scheduleGrid.addColumn(ScheduleVO::getReturnedPrincipalAmount).setHeader("Principal");
		interestAmountColumn = scheduleGrid.addColumn(ScheduleVO::getInterestAmount).setHeader("Interest");
		tdsAmountColumn = scheduleGrid.addColumn(ScheduleVO::getTdsAmount).setHeader("TDS");
		scheduleGrid.addComponentColumn(scheduleVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e->{
				scheduleGridLDV.removeItem(scheduleVO);
			});
			return delButton;
		}).setWidth("120px").setFlexGrow(0);
		
		scheduleBinder = new Binder<>(ScheduleVO.class);
		scheduleEditor = scheduleGrid.getEditor();
		scheduleEditor.setBinder(scheduleBinder);
		
		dueDatePicker = new DatePicker();
		addCloseHandler(dueDatePicker, scheduleEditor);
		scheduleBinder.forField(dueDatePicker)
			.withConverter(new LocalDateToDateConverter())
			.bind(ScheduleVO::getDueDateUtil, ScheduleVO::setDueDateUtil);
		dueDateColumn.setEditorComponent(dueDatePicker);
		dueAmountNumberField = new NumberField();
		addCloseHandler(dueAmountNumberField, scheduleEditor);
		scheduleBinder.forField(dueAmountNumberField)
			.bind(ScheduleVO::getDueAmount, ScheduleVO::setDueAmount);
		dueAmountColumn.setEditorComponent(dueAmountNumberField);
		returnedPrincipalAmountNumberField = new NumberField();
		addCloseHandler(returnedPrincipalAmountNumberField, scheduleEditor);
		scheduleBinder.forField(returnedPrincipalAmountNumberField)
			.bind(ScheduleVO::getReturnedPrincipalAmount, ScheduleVO::setReturnedPrincipalAmount);
		returnedPrincipalAmountColumn.setEditorComponent(returnedPrincipalAmountNumberField);
		interestAmountNumberField = new NumberField();
		addCloseHandler(interestAmountNumberField, scheduleEditor);
		scheduleBinder.forField(interestAmountNumberField)
			.bind(ScheduleVO::getInterestAmount, ScheduleVO::setInterestAmount);
		interestAmountColumn.setEditorComponent(interestAmountNumberField);
		tdsAmountNumberField = new NumberField();
		addCloseHandler(tdsAmountNumberField, scheduleEditor);
		scheduleBinder.forField(tdsAmountNumberField)
			.bind(ScheduleVO::getTdsAmount, ScheduleVO::setTdsAmount);
		tdsAmountColumn.setEditorComponent(tdsAmountNumberField);

		scheduleGrid.addItemDoubleClickListener(e -> {
			scheduleEditor.editItem(e.getItem());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable) {
		        ((Focusable) editorComponent).focus();
		    }
		});
		
		dialog.open();
	}
	
    private static void addCloseHandler(Component scheduleField,
            Editor<ScheduleVO> editor) {
    	scheduleField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

}
