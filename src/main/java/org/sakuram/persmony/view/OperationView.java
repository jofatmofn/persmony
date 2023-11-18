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
import org.sakuram.persmony.valueobject.InvestmentTransactionVO;
import org.sakuram.persmony.valueobject.ReceiptDuesVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationVO;
import org.sakuram.persmony.valueobject.TransferVO;
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
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "Accrual"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(3, "Invest"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(4, "Existing Investment, Receipt Dues"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(5, "Renewal"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(6, "New Receipt + Single Realisation With Bank"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(7, "New Payment + Single Realisation With Bank"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(8, "Transfer"));
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
	            case 2:
	            	handleTxnSingleRealisationWithBank(formLayout, Constants.DVID_TRANSACTION_TYPE_ACCRUAL);
	            	break;
	            case 3:
	            	handleInvest(formLayout);
	            	break;
	            case 4:
	            	handleReceiptDues(formLayout);
	            	break;
	            case 5:
	            	handleRenewal(formLayout);
	            	break;
	            case 6:
	            	handleTxnSingleRealisationWithBank(formLayout, Constants.DVID_TRANSACTION_TYPE_RECEIPT);
	            	break;
	            case 7:
	            	handleTxnSingleRealisationWithBank(formLayout, Constants.DVID_TRANSACTION_TYPE_PAYMENT);
	            	break;
	            case 8:
	            	handleTransfer(formLayout);
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

	@SuppressWarnings("unused")
	private void handleRealisation(FormLayout parentFormLayout) {
		IntegerField investmentTransactionIdIntegerField;
		Select<IdValueVO> realisationTypeDvSelect;
		FormLayout formLayout;
		InvestmentTransactionVO investmentTransactionVO;
		Button proceedButton;
		
		investmentTransactionVO = new InvestmentTransactionVO();
		
		// UI Elements
		investmentTransactionIdIntegerField = new IntegerField();
		parentFormLayout.addFormItem(investmentTransactionIdIntegerField, "Investment Transaction Id");
		realisationTypeDvSelect = newDvSelect("Realisation Type", Constants.CATEGORY_REALISATION_TYPE, false);
		parentFormLayout.addFormItem(realisationTypeDvSelect, "Realisation Type");
		proceedButton = new Button("Proceed");
		parentFormLayout.add(proceedButton);
		proceedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		proceedButton.setDisableOnClick(true);
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		parentFormLayout.add(formLayout);
		
		investmentTransactionIdIntegerField.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
		});
		
		realisationTypeDvSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
		});
		
		proceedButton.addClickListener(event -> {
			try {
				if (investmentTransactionIdIntegerField.getValue() == null) {
					investmentTransactionVO.setInvestmentTransactionId(0);
					showError("Provide Investment Transaction Id");
					return;
				} else if (realisationTypeDvSelect == null || realisationTypeDvSelect.getValue() == null) {
					showError("Select Realisation Type");
					return;
				} else {
					try {
						InvestmentTransactionVO investmentTransactionVOL;
						investmentTransactionVOL = miscService.fetchInvestmentTransaction(investmentTransactionIdIntegerField.getValue());
						investmentTransactionVOL.copyTo(investmentTransactionVO); // To overcome "Local variable defined in an enclosing scope must be final or effectively final"
						// TODO: Display the values from investmentTransactionVO
						if (investmentTransactionVO.getStatusDvId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
							showError("This transaction is currently not pending for realisation");
							return;
						}
						if (investmentTransactionVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
							showError("Realisation of an Accrual transaction cannot be done with this feature");
							return;
						}
						handleRealisation2(formLayout, realisationTypeDvSelect.getValue(), investmentTransactionVO);
					} catch (Exception e) {
						showError(UtilFuncs.messageFromException(e));
						return;
					}
				}
			} finally {
				proceedButton.setEnabled(true);
			}
		});
	}
	
	private void handleRealisation2(FormLayout formLayout, IdValueVO selectedRealisationIdValueVO, InvestmentTransactionVO investmentTransactionVO) {
		IntegerField realisationIdIntegerField, savingsAccountTransactionIntegerField;	// Should be converted to LongField
		DatePicker transactionDatePicker;
		Select<IdValueVO> closureTypeDvSelect, bankAccountDvSelect;
		Button saveButton;
		Checkbox lastRealisationCheckbox;
		HorizontalLayout hLayout;
		AmountComponent amountComponent;
		
		// UI Elements
		amountComponent = new AmountComponent(investmentTransactionVO.getTransactionTypeDvId());
		formLayout.addFormItem(amountComponent.getLayout(), "Realised Amount");
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Realised Date");
		
		lastRealisationCheckbox = new Checkbox();
		formLayout.addFormItem(lastRealisationCheckbox, "Last Realisation");
		lastRealisationCheckbox.setValue(true);
		
		closureTypeDvSelect = newDvSelect("Account Closure Type", Constants.CATEGORY_CLOSURE_TYPE, false);
		formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");
		
		bankAccountDvSelect = newDvSelect("Account", Constants.CATEGORY_ACCOUNT, true);
		realisationIdIntegerField = new IntegerField();
		savingsAccountTransactionIntegerField = new IntegerField();
		if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
			hLayout = new HorizontalLayout();
			formLayout.addFormItem(hLayout, "Account Transaction");
			savingsAccountTransactionIntegerField.setLabel("Old: Existing Id");
			hLayout.add(savingsAccountTransactionIntegerField);
			
			hLayout.add(bankAccountDvSelect);
			bankAccountDvSelect.setLabel("New: Account");
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
				if (!amountComponent.isInputValid()) {
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
						investmentTransactionVO.getInvestmentTransactionId(),
						savingsAccountTransactionIntegerField.getValue() == null ? null : Long.valueOf(savingsAccountTransactionIntegerField.getValue()),
						bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId(),
						realisationIdIntegerField.getValue() == null ? null : Long.valueOf(realisationIdIntegerField.getValue()),
						amountComponent.getNetAmount(),
						amountComponent.getReturnedPrincipalAmount(),
						amountComponent.getInterestAmount(),
						amountComponent.getTdsAmount(),
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
	
	private void handleTxnSingleRealisationWithBank(FormLayout formLayout, long transactionTypeDvId) {
		TextField investmentIdTextField;
		AmountComponent amountComponent;
		DatePicker transactionDatePicker;
		Select<IdValueVO> bankAccountDvSelect;
		Button saveButton;
		String label;
		
		label = (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_PAYMENT) ? "Paid" : ((transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) ? "Received" : "Accrued");
		// UI Elements
		investmentIdTextField = new TextField();
		formLayout.addFormItem(investmentIdTextField, "Investment Id");
		
		amountComponent = new AmountComponent(transactionTypeDvId);
		formLayout.addFormItem(amountComponent.getLayout(), "Amount " + label);
		
		transactionDatePicker = new DatePicker();
		formLayout.addFormItem(transactionDatePicker, "Date " + label);
		
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			bankAccountDvSelect = null;
		} else {
			bankAccountDvSelect = newDvSelect("Bank Account", Constants.CATEGORY_ACCOUNT, false);
			formLayout.addFormItem(bankAccountDvSelect, "Bank Account");
		}
		
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
				if (!amountComponent.isInputValid()) {
					showError("Invalid Amount");
					return;
				}
				if (transactionDatePicker.getValue() == null) {
					showError("Date cannot be Empty");
					return;
				}
				if (transactionTypeDvId != Constants.DVID_TRANSACTION_TYPE_ACCRUAL && bankAccountDvSelect.getValue() == null) {
					showError("Account cannot be Empty");
					return;
				}
				
				// Back-end Call
				txnSingleRealisationWithBankVO = new TxnSingleRealisationWithBankVO(
						Long.parseLong(investmentIdTextField.getValue()),
						transactionTypeDvId,
						amountComponent.getNetAmount(),
						amountComponent.getReturnedPrincipalAmount(),
						amountComponent.getInterestAmount(),
						amountComponent.getTdsAmount(),
						Date.valueOf(transactionDatePicker.getValue()),
						(transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL || bankAccountDvSelect.getValue() == null) ? null : bankAccountDvSelect.getValue().getId());
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
		Select<IdValueVO> investorDvSelect, productProviderDvSelect, providerBranchSelect, productTypeDvSelect, dematAccountDvSelect, taxabilityDvSelect, bankAccountDvSelect;
		TextField productIdOfProviderTextField, investorIdWithProviderTextField, productNameTextField, investmentIdWithProviderTextField;
		RadioButtonGroup<String> accrualApplicabilityRadioButtonGroup, dynamicReceiptPeriodicityRadioButtonGroup;
		NumberField rateOfInterestNumberField, faceValueNumberField, cleanPriceNumberField, accruedInterestNumberField, chargesNumberField, unitsNumberField;
		DatePicker investmentStartDatePicker, investmentEndDatePicker;
		HorizontalLayout hLayout;		
		Button saveButton, paymentScheduleButton, receiptScheduleButton, accrualScheduleButton;
		List<ScheduleVO> paymentScheduleVOList,  receiptScheduleVOList, accrualScheduleVOList;

		// UI Elements
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Investor");
		investorDvSelect = newDvSelect("Investor", Constants.CATEGORY_INVESTOR, false);
		investorIdWithProviderTextField = new TextField("Id with Provider");
		hLayout.add(investorDvSelect, investorIdWithProviderTextField);

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Provider");
		productProviderDvSelect = newDvSelect("Provider", Constants.CATEGORY_PARTY, false);
		providerBranchSelect = new Select<IdValueVO>();
		productProviderDvSelect.addValueChangeListener(event -> {
			providerBranchSelect.setItemLabelGenerator(idValueVO -> {
				return idValueVO.getValue();
			});
			providerBranchSelect.setItems(miscService.fetchBranchesOfParty(productProviderDvSelect.getValue().getId()));
		});
		providerBranchSelect.setLabel("Branch");
		providerBranchSelect.setPlaceholder("Select Provider Branch");
		hLayout.add(productProviderDvSelect, providerBranchSelect);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Product");
		productTypeDvSelect = newDvSelect("Product Type", Constants.CATEGORY_PRODUCT_TYPE, false);
		productNameTextField = new TextField("Product Name");
		productIdOfProviderTextField = new TextField("Id of Provider");
		hLayout.add(productTypeDvSelect, productNameTextField, productIdOfProviderTextField);

		investmentIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id with Provider");

		dematAccountDvSelect = newDvSelect("Demat Account", Constants.CATEGORY_DEMAT_ACCOUNT, true);
		formLayout.addFormItem(dematAccountDvSelect, "Demat Account");
		
		taxabilityDvSelect = newDvSelect("Taxability", Constants.CATEGORY_TAXABILITY, true);
		formLayout.addFormItem(taxabilityDvSelect, "Taxability");
		
		accrualApplicabilityRadioButtonGroup = new RadioButtonGroup<>();
		formLayout.addFormItem(accrualApplicabilityRadioButtonGroup, "Accrual Applicability");
		// accrualApplicabilityRadioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
		accrualApplicabilityRadioButtonGroup.setItems("Not Known", "Not Applicable", "Applicable");
		accrualApplicabilityRadioButtonGroup.setValue("Not Known");
		
		bankAccountDvSelect = newDvSelect("Account", Constants.CATEGORY_ACCOUNT, true);
		formLayout.addFormItem(bankAccountDvSelect, "Realisation from Account");

		unitsNumberField = new NumberField();
		formLayout.addFormItem(unitsNumberField, "No. of Units");
		
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
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Investment Period");
		investmentStartDatePicker = new DatePicker();
		investmentEndDatePicker = new DatePicker();
		hLayout.add(investmentStartDatePicker, investmentEndDatePicker);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Schedule");
		paymentScheduleVOList = new ArrayList<ScheduleVO>();
		paymentScheduleButton = new Button("Payment (0)");
		paymentScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_PAYMENT, "Payment", paymentScheduleButton, paymentScheduleVOList);
		});
		receiptScheduleVOList = new ArrayList<ScheduleVO>();
		receiptScheduleButton = new Button("Receipt (0)");
		receiptScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_RECEIPT, "Receipt", receiptScheduleButton, receiptScheduleVOList);
		});
		accrualScheduleVOList = new ArrayList<ScheduleVO>();
		accrualScheduleButton = new Button("Accrual (0)");
		accrualScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_ACCRUAL, "Accrual", accrualScheduleButton, accrualScheduleVOList);
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
				if (unitsNumberField.getValue() != null && unitsNumberField.getValue() <= 0) {
					showError("No. of units should be Positive");
					return;
				}
				if (faceValueNumberField.getValue() == null) {
					showError("Face Value cannot be Empty");
					return;
				}
				if (investmentStartDatePicker.getValue() != null && investmentEndDatePicker.getValue() != null && investmentStartDatePicker.getValue().compareTo(investmentEndDatePicker.getValue()) > 0) {
					showError("Investment Period: Invalid range of dates");
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
						providerBranchSelect.getValue() == null ? null : providerBranchSelect.getValue().getId(),
						productIdOfProviderTextField.getValue().equals("") ? null : productIdOfProviderTextField.getValue(),
						investorIdWithProviderTextField.getValue().equals("") ? null : investorIdWithProviderTextField.getValue(),
						productNameTextField.getValue().equals("") ? null : productNameTextField.getValue(),
						productTypeDvSelect.getValue().getId(),
						dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(),
						taxabilityDvSelect.getValue() == null ? null : taxabilityDvSelect.getValue().getId(),
						(accrualApplicabilityRadioButtonGroup.getValue() == null || accrualApplicabilityRadioButtonGroup.getValue().equals("Not Known")) ? null : (accrualApplicabilityRadioButtonGroup.getValue().equals("Not Applicable") ? false : true),
						(bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId()),
						investmentIdWithProviderTextField.getValue().equals("") ? null : investmentIdWithProviderTextField.getValue(),
						unitsNumberField.getValue() == null ? null : (double)unitsNumberField.getValue().doubleValue(),
						(double)faceValueNumberField.getValue().doubleValue(),
						cleanPriceNumberField.getValue() == null ? null : (double)cleanPriceNumberField.getValue().doubleValue(),
						accruedInterestNumberField.getValue() == null ? null : (double)accruedInterestNumberField.getValue().doubleValue(),
						chargesNumberField.getValue() == null ? null : (double)chargesNumberField.getValue().doubleValue(),
						rateOfInterestNumberField.getValue() == null ? null : (double)rateOfInterestNumberField.getValue().doubleValue(),
						investmentStartDatePicker.getValue() == null ? null : Date.valueOf(investmentStartDatePicker.getValue()),
						investmentEndDatePicker.getValue() == null ? null : Date.valueOf(investmentEndDatePicker.getValue()),
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
		DatePicker investmentEndDatePicker;
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
		
		investmentEndDatePicker = new DatePicker();
		formLayout.addFormItem(investmentEndDatePicker, "Investment End Date");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Schedule");
		paymentScheduleVOList = new ArrayList<ScheduleVO>();
		paymentScheduleButton = new Button("Payment (0)");
		paymentScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_PAYMENT, "Payment", paymentScheduleButton, paymentScheduleVOList);
		});
		receiptScheduleVOList = new ArrayList<ScheduleVO>();
		receiptScheduleButton = new Button("Receipt (0)");
		receiptScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_RECEIPT, "Receipt", receiptScheduleButton, receiptScheduleVOList);
		});
		accrualScheduleVOList = new ArrayList<ScheduleVO>();
		accrualScheduleButton = new Button("Accrual (0)");
		accrualScheduleButton.addClickListener(event -> {
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_ACCRUAL, "Accrual", accrualScheduleButton, accrualScheduleVOList);
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
				if (investmentEndDatePicker.getValue() == null) {
					showError("Investment End Date cannot be Empty");
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
						Date.valueOf(investmentEndDatePicker.getValue()),
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
	
	private void handleTransfer(FormLayout formLayout) {
		Select<IdValueVO> investorDvSelect, dematAccountDvSelect;
		TextField oldInvestmentIdTextField, investmentIdWithProviderTextField, investorIdWithProviderTextField;
		NumberField faceValueNumberField, unitsNumberField;
		DatePicker investmentStartDatePicker;
		Label label1;
		Button saveButton;
		HorizontalLayout hLayout;
		
		// UI Elements
		label1 = new Label();
		formLayout.addFormItem(label1, "");
		label1.getElement().setProperty("innerHTML", "<b>Details of the old investment being transferred</b>");
		
		oldInvestmentIdTextField = new TextField();
		formLayout.addFormItem(oldInvestmentIdTextField, "Persmony Investment Id");
		
		label1 = new Label();
		formLayout.addFormItem(label1, "");
		label1.getElement().setProperty("innerHTML", "<b>Details of the transfer</b>");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Recipient");
		investorDvSelect = newDvSelect("Investor", Constants.CATEGORY_INVESTOR, false);
		investorIdWithProviderTextField = new TextField("Id with Provider");
		hLayout.add(investorDvSelect, investorIdWithProviderTextField);
		
		investmentIdWithProviderTextField = new TextField();
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id with Provider");
		
		dematAccountDvSelect = newDvSelect("Demat Account", Constants.CATEGORY_DEMAT_ACCOUNT, true);
		formLayout.addFormItem(dematAccountDvSelect, "Demat Account");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Quantity");
		unitsNumberField = new NumberField("No. of Units");
		faceValueNumberField = new NumberField("Value");
		hLayout.add(unitsNumberField, faceValueNumberField);
		
		investmentStartDatePicker = new DatePicker();
		formLayout.addFormItem(investmentStartDatePicker, "Start Date");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			TransferVO transferVO;
			Notification notification;

			try {
				// Validation
				if (oldInvestmentIdTextField.getValue() == null || oldInvestmentIdTextField.getValue().equals("")) {
					showError("Id of old Investment being renewed cannot be Empty");
					return;
				}
				if (investorDvSelect.getValue() == null) {
					showError("Investor cannot be Empty");
					return;
				}
				if (unitsNumberField.getValue() == null && faceValueNumberField.getValue() == null ||
						unitsNumberField.getValue() != null && faceValueNumberField.getValue() != null) {
					showError("Either No. of units Or Value (not both) should be provided");
					return;
				}
				if (unitsNumberField.getValue() != null && unitsNumberField.getValue() <= 0) {
					showError("No. of units should be Positive");
					return;
				}
				if (faceValueNumberField.getValue() != null && faceValueNumberField.getValue() <= 0) {
					showError("Value should be Positive");
					return;
				}
				if (investmentStartDatePicker.getValue() == null) {
					showError("Start Date cannot be Empty");
					return;
				}
				
				transferVO = new TransferVO(
						Long.parseLong(oldInvestmentIdTextField.getValue()),
						investorDvSelect.getValue().getId(),
						investorIdWithProviderTextField.getValue().equals("") ? null : investorIdWithProviderTextField.getValue(),
						dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(),
						investmentIdWithProviderTextField.getValue().equals("") ? null : investmentIdWithProviderTextField.getValue(),
						unitsNumberField.getValue() == null ? null : unitsNumberField.getValue().doubleValue(),
						faceValueNumberField.getValue() == null ? null : faceValueNumberField.getValue().doubleValue(),
						investmentStartDatePicker.getValue() == null ? null : Date.valueOf(investmentStartDatePicker.getValue()));
				try {
					moneyTransactionService.transfer(transferVO);
					notification = Notification.show("Investment Transferred Successfully.");
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
			acceptSchedule(Constants.DVID_TRANSACTION_TYPE_RECEIPT, "Receipt", receiptScheduleButton, receiptScheduleVOList);
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
	
	private void acceptSchedule(long transactionTypeDvId, String label, Button scheduleButton, List<ScheduleVO> scheduleVOList) {
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
				if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
					for (ScheduleVO scheduleVO : fromPatternScheduleVOList) {
						scheduleVO.setInterestAmount(scheduleVO.getDueAmount());
						scheduleVO.setDueAmount(null);
					}
				}
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
		returnedPrincipalAmountColumn = scheduleGrid.addColumn(ScheduleVO::getReturnedPrincipalAmount).setHeader("Returned Principal");
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
		
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			returnedPrincipalAmountNumberField = new NumberField();
			addCloseHandler(returnedPrincipalAmountNumberField, scheduleEditor);
			scheduleBinder.forField(returnedPrincipalAmountNumberField)
				.bind(ScheduleVO::getReturnedPrincipalAmount, ScheduleVO::setReturnedPrincipalAmount);
			returnedPrincipalAmountColumn.setEditorComponent(returnedPrincipalAmountNumberField);
		}
		
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT || transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
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
		}

		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_PAYMENT || transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			dueAmountNumberField = new NumberField();
			addCloseHandler(dueAmountNumberField, scheduleEditor);
			scheduleBinder.forField(dueAmountNumberField)
				.bind(ScheduleVO::getDueAmount, ScheduleVO::setDueAmount);
			dueAmountColumn.setEditorComponent(dueAmountNumberField);
		}
		
		scheduleGrid.addItemDoubleClickListener(e -> {
			scheduleEditor.editItem(e.getItem());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable<?>) {
		        ((Focusable<?>) editorComponent).focus();
		    }
		});
		
		dialog.open();
	}
	
    private static void addCloseHandler(Component scheduleField,
            Editor<ScheduleVO> editor) {
    	scheduleField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

    private Select<IdValueVO> newDvSelect(String label, String dvCategory, boolean isEmptySelectionAllowed) {
		List<IdValueVO> idValueVOList;
		Select<IdValueVO> dvSelect;

		dvSelect = new Select<IdValueVO>();
		idValueVOList = miscService.fetchDvsOfCategory(dvCategory);
		dvSelect.setItemLabelGenerator(idValueVO -> {
			if (isEmptySelectionAllowed && idValueVO == null) {	// Required if EmptySelectionAllowed
				return "Not Known/Applicable";
			}
			return idValueVO.getValue();
		});
		dvSelect.setItems(idValueVOList);
		dvSelect.setLabel(label);
		dvSelect.setPlaceholder("Select " + label);
		dvSelect.setEmptySelectionAllowed(isEmptySelectionAllowed);
		if (isEmptySelectionAllowed) {
			dvSelect.setEmptySelectionCaption("Not Known/Applicable");
		}
		return dvSelect;
    }
}
