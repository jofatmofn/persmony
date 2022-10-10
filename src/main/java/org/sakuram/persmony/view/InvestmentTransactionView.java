package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("itran")
public class InvestmentTransactionView extends Div {
	private static final long serialVersionUID = 6529685098267757690L;
	
	MoneyTransactionService moneyTransactionService;
	MiscService miscService;

	public InvestmentTransactionView(MoneyTransactionService moneyTransactionService, MiscService miscService) {
		Span selectSpan;
		FormLayout formLayout;
		Select<String> operationSelect;
		
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;

		selectSpan = new Span();
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		
		operationSelect = new Select<String>();
		operationSelect.setItems("Existing Transaction, Single Realisation With Bank",
				"Existing Transaction, Single Realisation With Bank, Close Investment",
				"Accrual OR *New Transaction, Single Realisation With Bank*",
				"Invest",
				"Renewal");
		operationSelect.setLabel("Operation");
		operationSelect.setPlaceholder("Select Operation");
		operationSelect.setId("PersmonyOperation");
		operationSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			// TODO: Use id instead of label in the switch-case
			try {
	            switch(event.getValue()) {
	            case "Existing Transaction, Single Realisation With Bank":
	            	handleSingleRealisationWithBank(formLayout, false);
	            	break;
	            case "Accrual OR *New Transaction, Single Realisation With Bank*":
	            	handleTxnSingleRealisationWithBank(formLayout);
	            	break;
	            case "Existing Transaction, Single Realisation With Bank, Close Investment":
	            	handleSingleRealisationWithBank(formLayout, true);
	            	break;
	            case "Invest":
	            	handleInvest(formLayout);
	            	break;
	            case "Renewal":
	            	handleRenewal(formLayout);
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
	
	private void handleSingleRealisationWithBank(FormLayout formLayout, boolean isLast) {
		TextField investmentTransactionIdTextField;
		NumberField amountNumberField;
		DatePicker transactionDatePicker;
		Select<IdValueVO> bankAccountDvSelect, closureTypeDvSelect;
		Button saveButton;
		List<IdValueVO> idValueVOList;
		
		// UI Elements
		investmentTransactionIdTextField = new TextField();
		formLayout.addFormItem(investmentTransactionIdTextField, "Investment Transaction Id");
		
		amountNumberField = new NumberField();
		formLayout.addFormItem(amountNumberField, "Actual Amount Paid/Received");
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Actual Date Paid/Received");
		
		bankAccountDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(bankAccountDvSelect, "Bank Account");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_BANK_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		bankAccountDvSelect.setItems(idValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");

		closureTypeDvSelect = new Select<IdValueVO>();
		if (isLast) {
			formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");
			idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_CLOSURE_TYPE);
			closureTypeDvSelect.setItemLabelGenerator(idValueVO -> {
				return idValueVO.getValue();
			});
			closureTypeDvSelect.setItems(idValueVOList);
			closureTypeDvSelect.setPlaceholder("Select Account Closure Type");
		}
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			SingleRealisationWithBankVO singleRealisationWithBankVO;
			Notification notification;

			try {
				// Validation
				if (investmentTransactionIdTextField.getValue() == null || investmentTransactionIdTextField.getValue().equals("")) {
					showError("Investment Transaction Id cannot be Empty");
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
				if (bankAccountDvSelect.getValue() == null) {
					showError("Account cannot be Empty");
					return;
				}
				if (isLast && closureTypeDvSelect.getValue() == null) {
					showError("Account Closure Type cannot be Empty");
					return;
				}
				
				// Back-end Call
				singleRealisationWithBankVO = new SingleRealisationWithBankVO(
						Long.parseLong(investmentTransactionIdTextField.getValue()),
						(float)amountNumberField.getValue().doubleValue(),
						Date.valueOf(transactionDatePicker.getValue()),
						bankAccountDvSelect.getValue().getId(),
						isLast ? closureTypeDvSelect.getValue().getId() : null);
				try {
					if (isLast) {
						moneyTransactionService.singleLastRealisationWithBank(singleRealisationWithBankVO);
					} else {
						moneyTransactionService.singleRealisationWithBank(singleRealisationWithBankVO, null);
					}
					notification = Notification.show("Investment Transaction Saved Successfully.");
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
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_BANK_ACCOUNT);
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
						(float)amountNumberField.getValue().doubleValue(),
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
		Select<IdValueVO> investorDvSelect, productProviderDvSelect, productTypeDvSelect, dematAccountDvSelect, taxabilityDvSelect, bankAccountDvSelect;
		TextField productIdOfProviderTextField, investorIdWithProviderTextField, productNameTextField, investmentIdWithProviderTextField, paymentScheduleTextField, receiptScheduleTextField, accrualScheduleTextField;
		RadioButtonGroup<String> accrualApplicabilityRadioButtonGroup;
		NumberField rateOfInterestNumberField;
		DatePicker productEndDatePicker;
		List<IdValueVO> idValueVOList;
		
		Button saveButton;
		
		// UI Elements
		investorDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(investorDvSelect, "Investor");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_INVESTOR);
		investorDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		investorDvSelect.setItems(idValueVOList);
		investorDvSelect.setPlaceholder("Select Investor");

		productProviderDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(productProviderDvSelect, "Product Provider");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_PARTY);
		productProviderDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		productProviderDvSelect.setItems(idValueVOList);
		productProviderDvSelect.setPlaceholder("Select Product Provider");
		
		productIdOfProviderTextField = new TextField();
		formLayout.addFormItem(productIdOfProviderTextField, "Product Id of Provider");

		investorIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investorIdWithProviderTextField, "Investor Id with Provider");

		productNameTextField = new TextField();
		formLayout.addFormItem(productNameTextField, "Product Name");

		productTypeDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(productTypeDvSelect, "Product Type");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_PRODUCT_TYPE);
		productTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		productTypeDvSelect.setItems(idValueVOList);
		productTypeDvSelect.setPlaceholder("Select Product Type");
		
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
		formLayout.addFormItem(bankAccountDvSelect, "Investment from Bank Account");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_BANK_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		bankAccountDvSelect.setItems(idValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");

		investmentIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id with Provider");

		rateOfInterestNumberField = new NumberField();
		rateOfInterestNumberField.setMax(100.00);
		formLayout.addFormItem(rateOfInterestNumberField, "Rate Of Interest%");
		
		productEndDatePicker = new DatePicker();
		formLayout.addFormItem(productEndDatePicker, "Product End Date");
		
		paymentScheduleTextField = new TextField();
		formLayout.addFormItem(paymentScheduleTextField, "Payment Schedule");
		
		receiptScheduleTextField = new TextField();
		receiptScheduleTextField.setValue("None");
		formLayout.addFormItem(receiptScheduleTextField, "Receipt Schedule");
		
		accrualScheduleTextField = new TextField();
		accrualScheduleTextField.setValue("None");
		formLayout.addFormItem(accrualScheduleTextField, "Accrual Schedule");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			InvestVO investVO;
			Notification notification;
			List<ScheduleVO> paymentScheduleVOList;
			List<ScheduleVO> receiptScheduleVOList;
			List<ScheduleVO> accrualScheduleVOList;

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
				if (bankAccountDvSelect.getValue() == null) {
					showError("Bank Account cannot be Empty");
					return;
				}
				if (investmentIdWithProviderTextField.getValue() == null || investmentIdWithProviderTextField.getValue().equals("")) {
					showError("Investment Id With Provider cannot be Empty");
					return;
				}				
				if (productEndDatePicker.getValue() == null) {
					showError("Product End Date cannot be Empty");
					return;
				}
				if (paymentScheduleTextField.getValue() == null || paymentScheduleTextField.getValue().equals("")) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				try {
					paymentScheduleVOList = UtilFuncs.parseScheduleData(paymentScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Payment Schedule: " + e.getMessage());
					return;
				}
				if (paymentScheduleVOList.isEmpty()) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				if (receiptScheduleTextField.getValue() == null || receiptScheduleTextField.getValue().equals("")) {
					showError("Specify None if there is no Receipt Schedule");
					return;
				}
				try {
					receiptScheduleVOList = UtilFuncs.parseScheduleData(receiptScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Receipt Schedule: " + e.getMessage());
					return;
				}
				if (accrualScheduleTextField.getValue() == null || accrualScheduleTextField.getValue().equals("")) {
					showError("Specify None if there is no Accrual Schedule");
					return;
				}
				try {
					accrualScheduleVOList = UtilFuncs.parseScheduleData(accrualScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Accrual Schedule: " + e.getMessage());
					return;
				}
				
				// Back-end Call
				investVO = new InvestVO(
						investorDvSelect.getValue().getId(),
						productProviderDvSelect.getValue().getId(),
						productIdOfProviderTextField.getValue().equals("") ? null : productIdOfProviderTextField.getValue(),
						investorIdWithProviderTextField.getValue().equals("") ? null : investorIdWithProviderTextField.getValue(),
						productNameTextField.getValue().equals("") ? null : productNameTextField.getValue(),
						productTypeDvSelect.getValue().getId(),
						dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(),
						taxabilityDvSelect.getValue() == null ? null : taxabilityDvSelect.getValue().getId(),
						(accrualApplicabilityRadioButtonGroup.getValue() == null || accrualApplicabilityRadioButtonGroup.getValue().equals("Not Known")) ? null : (accrualApplicabilityRadioButtonGroup.getValue().equals("Not Applicable") ? false : true),
						bankAccountDvSelect.getValue().getId(),
						investmentIdWithProviderTextField.getValue(),
						rateOfInterestNumberField.getValue() == null ? null : (float)rateOfInterestNumberField.getValue().doubleValue(),
						Date.valueOf(productEndDatePicker.getValue()),
						paymentScheduleVOList,
						receiptScheduleVOList,
						accrualScheduleVOList);
				try {
					moneyTransactionService.invest(investVO);
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
		TextField oldInvestmentIdTextField, investmentIdWithProviderTextField, paymentScheduleTextField, receiptScheduleTextField, accrualScheduleTextField;
		NumberField rateOfInterestNumberField;
		DatePicker productEndDatePicker;
		Label label1;
		Button saveButton;
		
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
		
		rateOfInterestNumberField = new NumberField();
		rateOfInterestNumberField.setMax(100.00);
		formLayout.addFormItem(rateOfInterestNumberField, "Rate Of Interest%");
		
		productEndDatePicker = new DatePicker();
		formLayout.addFormItem(productEndDatePicker, "Product End Date");
		
		paymentScheduleTextField = new TextField();
		formLayout.addFormItem(paymentScheduleTextField, "Payment Schedule");
		
		receiptScheduleTextField = new TextField();
		receiptScheduleTextField.setValue("None");
		formLayout.addFormItem(receiptScheduleTextField, "Receipt Schedule");
		
		accrualScheduleTextField = new TextField();
		accrualScheduleTextField.setValue("None");
		formLayout.addFormItem(accrualScheduleTextField, "Accrual Schedule");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			RenewalVO renewalVO;
			Notification notification;
			List<ScheduleVO> paymentScheduleVOList;
			List<ScheduleVO> receiptScheduleVOList;
			List<ScheduleVO> accrualScheduleVOList;

			try {
				// Validation
				if (oldInvestmentIdTextField.getValue() == null || oldInvestmentIdTextField.getValue().equals("")) {
					showError("Transaction Id of Investment being renewed cannot be Empty");
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
				if (paymentScheduleTextField.getValue() == null || paymentScheduleTextField.getValue().equals("")) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				try {
					paymentScheduleVOList = UtilFuncs.parseScheduleData(paymentScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Payment Schedule: " + e.getMessage());
					return;
				}
				if (paymentScheduleVOList.isEmpty()) {
					showError("Payment Schedule cannot be Empty");
					return;
				}
				if (receiptScheduleTextField.getValue() == null || receiptScheduleTextField.getValue().equals("")) {
					showError("Specify None if there is no Receipt Schedule");
					return;
				}
				try {
					receiptScheduleVOList = UtilFuncs.parseScheduleData(receiptScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Receipt Schedule: " + e.getMessage());
					return;
				}
				if (accrualScheduleTextField.getValue() == null || accrualScheduleTextField.getValue().equals("")) {
					showError("Specify None if there is no Accrual Schedule");
					return;
				}
				try {
					accrualScheduleVOList = UtilFuncs.parseScheduleData(accrualScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Accrual Schedule: " + e.getMessage());
					return;
				}
				
				// Back-end Call
				renewalVO = new RenewalVO(
						Long.parseLong(oldInvestmentIdTextField.getValue()),
						investmentIdWithProviderTextField.getValue(),
						rateOfInterestNumberField.getValue() == null ? null : (float)rateOfInterestNumberField.getValue().doubleValue(),
						Date.valueOf(productEndDatePicker.getValue()),
						paymentScheduleVOList,
						receiptScheduleVOList,
						accrualScheduleVOList);
				try {
					moneyTransactionService.renewal(renewalVO);
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
	
	private void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		errorDialog.open();
		
	}
}
