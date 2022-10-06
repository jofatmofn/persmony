package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
					System.out.println("Exception Caught!!!");
					showError(messageFromException(e));
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
					System.out.println("Exception Caught!!!");
					showError(messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleInvest(FormLayout formLayout) {
	}
	
	private void handleRenewal(FormLayout formLayout) {
	}
	
	private String messageFromException(Exception e) {
		if (e instanceof AppException) {
			return e.getMessage();
		} else {
			return "Unexpected Error: " + e.getMessage() == null ? "No further details!" : e.getMessage();
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
