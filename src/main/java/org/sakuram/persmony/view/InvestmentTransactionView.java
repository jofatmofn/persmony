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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
		Select<Map.Entry<Integer,String>> operationSelect;
		List<Map.Entry<Integer, String>> operationItemsList;
		
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;

		operationItemsList = new ArrayList<Map.Entry<Integer,String>>() {
			private static final long serialVersionUID = 1L;

			{
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Existing Transaction, Single Realisation With Bank"));
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
	            	handleSingleRealisationWithBank(formLayout);
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
	
	private void handleSingleRealisationWithBank(FormLayout formLayout) {
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
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		bankAccountDvSelect.setItems(idValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");

		closureTypeDvSelect = new Select<IdValueVO>();
		formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");
		idValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_CLOSURE_TYPE);
		closureTypeDvSelect.setItemLabelGenerator(idValueVO -> {
			return idValueVO.getValue();
		});
		closureTypeDvSelect.setItems(idValueVOList);
		closureTypeDvSelect.setPlaceholder("Select Account Closure Type");
		
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
				
				// Back-end Call
				singleRealisationWithBankVO = new SingleRealisationWithBankVO(
						Long.parseLong(investmentTransactionIdTextField.getValue()),
						(double)amountNumberField.getValue().doubleValue(),
						Date.valueOf(transactionDatePicker.getValue()),
						bankAccountDvSelect.getValue().getId(),
						closureTypeDvSelect.getValue() == null? null : closureTypeDvSelect.getValue().getId());
				try {
					moneyTransactionService.singleRealisationWithBank(singleRealisationWithBankVO);
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
		TextField productIdOfProviderTextField, investorIdWithProviderTextField, productNameTextField, investmentIdWithProviderTextField, paymentScheduleTextField, receiptScheduleTextField, accrualScheduleTextField;
		RadioButtonGroup<String> accrualApplicabilityRadioButtonGroup, dynamicReceiptPeriodicityRadioButtonGroup;
		NumberField rateOfInterestNumberField, faceValueNumberField, cleanPriceNumberField, accruedInterestNumberField, chargesNumberField;
		DatePicker productEndDatePicker;
		List<IdValueVO> idValueVOList;
		HorizontalLayout hLayout;
		
		Button saveButton;
		
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
		investorDvSelect.addValueChangeListener(event -> {
			bankAccountDvSelect.setItemLabelGenerator(idValueVO -> {
				return idValueVO.getValue();
			});
			bankAccountDvSelect.setItems(miscService.fetchAccountsOfInvestor(investorDvSelect.getValue().getId()));
		});
		formLayout.addFormItem(bankAccountDvSelect, "Investment from Bank Account");
		bankAccountDvSelect.setPlaceholder("Select Bank Account");

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
		
		paymentScheduleTextField = new TextField();
		formLayout.addFormItem(paymentScheduleTextField, "Payment Schedule");
		
		receiptScheduleTextField = new TextField();
		receiptScheduleTextField.setValue("None");
		formLayout.addFormItem(receiptScheduleTextField, "Receipt Schedule");
		
		dynamicReceiptPeriodicityRadioButtonGroup = new RadioButtonGroup<>();
		formLayout.addFormItem(dynamicReceiptPeriodicityRadioButtonGroup, "Dynamic Receipt Periodicity");
		dynamicReceiptPeriodicityRadioButtonGroup.setItems("Not Applicable", "Yearly");
		dynamicReceiptPeriodicityRadioButtonGroup.setValue("Not Applicable");
		
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
				if (investmentIdWithProviderTextField.getValue() == null || investmentIdWithProviderTextField.getValue().equals("")) {
					showError("Investment Id With Provider cannot be Empty");
					return;
				}				
				if (bankAccountDvSelect.getValue() == null) {
					showError("Bank Account cannot be Empty");
					return;
				}
				if (faceValueNumberField.getValue() == null) {
					showError("Face Value cannot be Empty");
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
						providerBranchDvSelect.getValue() == null ? null : providerBranchDvSelect.getValue().getId(),
						productIdOfProviderTextField.getValue().equals("") ? null : productIdOfProviderTextField.getValue(),
						investorIdWithProviderTextField.getValue().equals("") ? null : investorIdWithProviderTextField.getValue(),
						productNameTextField.getValue().equals("") ? null : productNameTextField.getValue(),
						productTypeDvSelect.getValue().getId(),
						dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(),
						taxabilityDvSelect.getValue() == null ? null : taxabilityDvSelect.getValue().getId(),
						(accrualApplicabilityRadioButtonGroup.getValue() == null || accrualApplicabilityRadioButtonGroup.getValue().equals("Not Known")) ? null : (accrualApplicabilityRadioButtonGroup.getValue().equals("Not Applicable") ? false : true),
						bankAccountDvSelect.getValue().getId(),
						investmentIdWithProviderTextField.getValue(),
						(double)faceValueNumberField.getValue().doubleValue(),
						cleanPriceNumberField.getValue() == null ? null : (double)cleanPriceNumberField.getValue().doubleValue(),
						accruedInterestNumberField.getValue() == null ? null : (double)accruedInterestNumberField.getValue().doubleValue(),
						chargesNumberField.getValue() == null ? null : (double)chargesNumberField.getValue().doubleValue(),
						rateOfInterestNumberField.getValue() == null ? null : (double)rateOfInterestNumberField.getValue().doubleValue(),
						Date.valueOf(productEndDatePicker.getValue()),
						paymentScheduleVOList,
						receiptScheduleVOList,
						(dynamicReceiptPeriodicityRadioButtonGroup.getValue().equals("Not Applicable") ? null : 'Y'),
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
		NumberField rateOfInterestNumberField, faceValueNumberField;
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
		
		faceValueNumberField = new NumberField();
		formLayout.addFormItem(faceValueNumberField, "Face Value");
		
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
						(double)faceValueNumberField.getValue().doubleValue(),
						rateOfInterestNumberField.getValue() == null ? null : (double)rateOfInterestNumberField.getValue().doubleValue(),
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
	
	private void handleReceiptDues(FormLayout formLayout) {
		TextField investmentIdTextField, receiptScheduleTextField;
		Button saveButton;
		
		investmentIdTextField = new TextField();
		formLayout.addFormItem(investmentIdTextField, "Persmony Investment Id");
		
		receiptScheduleTextField = new TextField();
		formLayout.addFormItem(receiptScheduleTextField, "Receipt Schedule");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			ReceiptDuesVO receiptDuesVO;
			Notification notification;
			List<ScheduleVO> receiptScheduleVOList;
			
			try {
				// Validation
				if (investmentIdTextField.getValue() == null || investmentIdTextField.getValue().equals("")) {
					showError("Id of Investment cannot be Empty");
					return;
				}
				if (receiptScheduleTextField.getValue() == null || receiptScheduleTextField.getValue().equals("") || receiptScheduleTextField.getValue().equals("None")) {
					showError("Receipt Schedule cannot be Empty");
					return;
				}
				try {
					receiptScheduleVOList = UtilFuncs.parseScheduleData(receiptScheduleTextField.getValue());
				} catch (AppException e) {
					showError("Receipt Schedule: " + e.getMessage());
					return;
				}
				
				// Back-end Call
				receiptDuesVO = new ReceiptDuesVO(Long.parseLong(investmentIdTextField.getValue()), receiptScheduleVOList);
				try {
					moneyTransactionService.addReceiptDues(receiptDuesVO);
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
}
