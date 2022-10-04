package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.DomainValueVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
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
		FormLayout formLayout;
		Select<String> transactionTypeSelect;
		
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;
		
		formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
        
		transactionTypeSelect = new Select<String>();
		transactionTypeSelect.setItems("Single Realisation With Bank", "Txn + Single Realisation With Bank", "Single Last Realisation With Bank", "Invest", "Renewal");
		transactionTypeSelect.setPlaceholder("Select Transaction Type");
		transactionTypeSelect.addValueChangeListener(event -> {
			// Notification.show("Selected index" + transactionTypeSelect.getItemPosition(event.getValue()));
            switch(event.getValue()) {
            case "Single Realisation With Bank":
            	handleSingleRealisationWithBank(formLayout, false);
            	break;
            case "Txn + Single Realisation With Bank":
            	handleTxnSingleRealisationWithBank(formLayout);
            	break;
            case "Single Last Realisation With Bank":
            	handleSingleRealisationWithBank(formLayout, true);
            	break;
            case "Invest":
            	handleInvest(formLayout);
            	break;
            case "Renewal":
            	handleRenewal(formLayout);
            	break;
            }
        });

		formLayout.addFormItem(transactionTypeSelect, "Transaction Type");
		add(formLayout);
	}
	
	private void handleSingleRealisationWithBank(FormLayout formLayout, boolean isLast) {
		TextField investmentTransactionIdTextField;
		NumberField amountNumberField;
		DatePicker transactionDatePicker;
		Select<DomainValueVO> bankAccountDvSelect, closureTypeDvSelect;
		Button saveButton;
		List<DomainValueVO> domainValueVOList;
		
		// UI Elements
		investmentTransactionIdTextField = new TextField();
		formLayout.addFormItem(investmentTransactionIdTextField, "Investment Transaction Id");
		
		amountNumberField = new NumberField();
		formLayout.addFormItem(amountNumberField, "Actual Amount Paid/Received");
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Actual Date Paid/Received");
		
		bankAccountDvSelect = new Select<DomainValueVO>();
		formLayout.addFormItem(bankAccountDvSelect, "Bank Account");
		domainValueVOList = miscService.fetchDvOfCategory(Constants.CATEGORY_BANK_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(domainValueVO -> {
			return domainValueVO.getValue();
		});
		bankAccountDvSelect.setItems(domainValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");

		closureTypeDvSelect = new Select<DomainValueVO>();
		if (isLast) {
			formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");
			domainValueVOList = miscService.fetchDvOfCategory(Constants.CATEGORY_CLOSURE_TYPE);
			closureTypeDvSelect.setItemLabelGenerator(domainValueVO -> {
				return domainValueVO.getValue();
			});
			closureTypeDvSelect.setItems(domainValueVOList);
			closureTypeDvSelect.setPlaceholder("Select Account Closure Type");
		}
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		// On click of Save
		saveButton.addClickListener(event -> {
			SingleRealisationWithBankVO singleRealisationWithBankVO;
			Notification notification;
			
			// Validation
			if (investmentTransactionIdTextField.getValue() == null || investmentTransactionIdTextField.getValue().equals("")) {
				showError("Investment Transaction Id cannot be Empty");
				return;
			}
			if (amountNumberField.getValue() == null || amountNumberField.getValue().equals("")) {
				showError("Amount cannot be Empty");
				return;
			}
			if (transactionDatePicker.getValue() == null || transactionDatePicker.getValue().equals("")) {
				showError("Date cannot be Empty");
				return;
			}
			if (bankAccountDvSelect.getValue() == null || bankAccountDvSelect.getValue().equals("")) {
				showError("Account cannot be Empty");
				return;
			}
			if (!isLast && (closureTypeDvSelect.getValue() == null || closureTypeDvSelect.getValue().equals(""))) {
				showError("Account Closure Type cannot be Empty");
				return;
			}
			
			// Back-end Call
			singleRealisationWithBankVO = new SingleRealisationWithBankVO(
					Long.parseLong(investmentTransactionIdTextField.getValue()),
					(float)amountNumberField.getValue().doubleValue(),
					Date.valueOf(transactionDatePicker.getValue()),
					bankAccountDvSelect.getValue().getDomainValueId(),
					isLast ? closureTypeDvSelect.getValue().getDomainValueId() : null);
			saveButton.setDisableOnClick(true);
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
			saveButton.setEnabled(true);
		});
	}
	
	private void handleTxnSingleRealisationWithBank(FormLayout formLayout) {
		
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
