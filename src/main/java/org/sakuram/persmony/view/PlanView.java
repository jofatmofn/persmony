package org.sakuram.persmony.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.PlanService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.CashFlowVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.PlanSearchCriteriaVO;
import org.sakuram.persmony.valueobject.PlanSearchResultVO;
import org.sakuram.persmony.valueobject.PlanVO;
import org.sakuram.persmony.view.DTInvestmentTransactionSearchComponent.ITSearchDefaults;
import org.springframework.beans.factory.ObjectProvider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="plan", layout=PersMonyLayout.class)
@PageTitle("Income-Expenditure Mapping")
public class PlanView extends Div {

	private static final long serialVersionUID = -451808516442237695L;

	PlanService planService;
	MiscService miscService;
	
	DatePickerI18n isoDatePickerI18n;
	
	ObjectProvider<DTInvestmentTransactionSearchComponent> investmentTransactionSearchComponentObjectProvider;
	ObjectProvider<CashFlowSearchComponent> cashFlowSearchComponentObjectProvider;
	
	public PlanView(PlanService planService, MiscService miscService, DatePickerI18n isoDatePickerI18n, ObjectProvider<DTInvestmentTransactionSearchComponent> investmentTransactionSearchComponentObjectProvider, ObjectProvider<CashFlowSearchComponent> cashFlowSearchComponentObjectProvider) {
		Div content;
		Tabs tabs;
		Map<Tab, Component> tabContent = new HashMap<Tab, Component>(3);
		Component planStatusView, createPlanView, createCashFlowView;
		Tab planStatusTab, createPlanTab, createCashFlowTab;
		
		this.planService = planService;
		this.miscService = miscService;
		this.isoDatePickerI18n = isoDatePickerI18n;
		this.investmentTransactionSearchComponentObjectProvider = investmentTransactionSearchComponentObjectProvider;
		this.cashFlowSearchComponentObjectProvider = cashFlowSearchComponentObjectProvider;
		
		setSizeFull();
		
		content = new Div();
		
		planStatusTab = new Tab("Plan Status");
		planStatusView = createPlanStatustView();
		tabContent.put(planStatusTab, planStatusView);
		createPlanTab = new Tab("Create Plan");
		createPlanView = createCreatePlanView();
		tabContent.put(createPlanTab, createPlanView);
		createCashFlowTab = new Tab("Create Cash Flow");
		createCashFlowView = createCreateCashFlowView();
		tabContent.put(createCashFlowTab, createCashFlowView);
		
		tabs = new Tabs(planStatusTab, createPlanTab, createCashFlowTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> {
        	content.removeAll();
            content.add(tabContent.get(e.getSelectedTab()));
        });

        add(tabs, content);

        content.add(planStatusView);

	}
	
	private Component createPlanStatustView() {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		DatePicker incomeFromDateDatePicker, incomeToDateDatePicker, expenditureFromDateDatePicker, expenditureToDateDatePicker;
		Select<IdValueVO> incomeBankAccountOrInvestorDvSelect, expenditureBankAccountOrInvestorDvSelect;
		NumberField mappedFromAmoutNumberField, mappedToAmoutNumberField;
		Button searchButton;
		Grid<PlanSearchResultVO> planSearchResultGrid;

		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Income");
		incomeFromDateDatePicker = new DatePicker("From");
		incomeFromDateDatePicker.setI18n(isoDatePickerI18n);
		incomeToDateDatePicker = new DatePicker("To");
		incomeToDateDatePicker.setI18n(isoDatePickerI18n);
		incomeBankAccountOrInvestorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT + "+" + Constants.CATEGORY_PRIMARY_INVESTOR, "Account", true, false);
		hLayout.add(incomeFromDateDatePicker, incomeToDateDatePicker, incomeBankAccountOrInvestorDvSelect);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Expenditure");
		expenditureFromDateDatePicker = new DatePicker("From");
		expenditureFromDateDatePicker.setI18n(isoDatePickerI18n);
		expenditureToDateDatePicker = new DatePicker("To");
		expenditureToDateDatePicker.setI18n(isoDatePickerI18n);
		expenditureBankAccountOrInvestorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT + "+" + Constants.CATEGORY_PRIMARY_INVESTOR, "Account", true, false);
		hLayout.add(expenditureFromDateDatePicker, expenditureToDateDatePicker, expenditureBankAccountOrInvestorDvSelect);
		
		mappedFromAmoutNumberField = new NumberField("From");
		mappedToAmoutNumberField = new NumberField("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Mapped Amount");
		hLayout.add(mappedFromAmoutNumberField, mappedToAmoutNumberField);

		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		searchButton = new Button("Search");
		hLayout.add(searchButton);
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.setDisableOnClick(true);
		
		planSearchResultGrid = new Grid<>(PlanSearchResultVO.class);
		planSearchResultGrid.setColumns("planId", "incomeDetail", "expenditureDetail", "mappedAmount", "statusIdValueVO.value");
		for (Column<PlanSearchResultVO> column : planSearchResultGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(planSearchResultGrid);
				
		searchButton.addClickListener(event -> {
			Notification notification;
			List<PlanSearchResultVO> recordList = null;
			try {
				if (incomeFromDateDatePicker.getValue() != null && incomeToDateDatePicker.getValue() != null &&
						incomeFromDateDatePicker.getValue().isAfter(incomeToDateDatePicker.getValue())) {
					ViewFuncs.showError("Income From Date cannot be after the To Date");
					return;
				}
				if (expenditureFromDateDatePicker.getValue() != null && expenditureToDateDatePicker.getValue() != null &&
						expenditureFromDateDatePicker.getValue().isAfter(expenditureToDateDatePicker.getValue())) {
					ViewFuncs.showError("Expenditure From Date cannot be after the To Date");
					return;
				}
				if (mappedFromAmoutNumberField.getValue() != null && mappedToAmoutNumberField.getValue() != null &&
						mappedFromAmoutNumberField.getValue() > mappedToAmoutNumberField.getValue()) {
					ViewFuncs.showError("From Amount cannot be greater than the To Amount");
					return;
				}
				
				// Back-end Call
				try {
					recordList = planService.retrievePlanStatus(new PlanSearchCriteriaVO(
							incomeFromDateDatePicker.getValue(),
							incomeToDateDatePicker.getValue(),
							expenditureFromDateDatePicker.getValue(),
							expenditureToDateDatePicker.getValue(),
							(incomeBankAccountOrInvestorDvSelect.getValue() == null ? null : incomeBankAccountOrInvestorDvSelect.getValue().getId()),
							(expenditureBankAccountOrInvestorDvSelect.getValue() == null ? null : expenditureBankAccountOrInvestorDvSelect.getValue().getId()),
							(mappedFromAmoutNumberField.getValue() == null ? null : BigDecimal.valueOf(mappedFromAmoutNumberField.getValue())),
							(mappedToAmoutNumberField.getValue() == null ? null : BigDecimal.valueOf(mappedToAmoutNumberField.getValue()))
							));
					notification = Notification.show("No. of Plans fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					planSearchResultGrid.setItems(recordList);
				} catch (Exception e ) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				searchButton.setEnabled(true);
			}
		});
		
		planSearchResultGrid.addComponentColumn(planSearchResultVO -> {
			Select<IdValueVO> transactionStatusDvSelect;
			
			transactionStatusDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_STATUS, null, false, false);
			if (planSearchResultVO.getStatusIdValueVO().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
				transactionStatusDvSelect.setEnabled(false);
			}
			transactionStatusDvSelect.setValue(planSearchResultVO.getStatusIdValueVO());
			transactionStatusDvSelect.addValueChangeListener(e->{
				if (e.isFromClient()) {
					ConfirmDialog confirmDialog = new ConfirmDialog();
					confirmDialog.setHeader("Confirm");
					confirmDialog.setText("Are you sure you want to update the status?");
					confirmDialog.setConfirmButton("Confirm", confirmEvent -> {
						Notification notification;
						
						planService.updatePlanStatus(planSearchResultVO.getPlanId(), transactionStatusDvSelect.getValue().getId());
						notification = Notification.show("Status updated successfully");
						notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						transactionStatusDvSelect.setEnabled(false);
					});
					confirmDialog.setCancelButton("Cancel", cancelEvent -> {
						transactionStatusDvSelect.setValue(planSearchResultVO.getStatusIdValueVO());
					});
					confirmDialog.open();
				}
			});
			return transactionStatusDvSelect;
		});
		
		return formLayout;
	}
	
	private Component createCreatePlanView() {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		IntegerField incomeItIdIntegerField, incomeCfIdIntegerField, expenditureItIdIntegerField, expenditureCfIdIntegerField;
		NumberField mappedAmountNumberField;
		Button createButton, incomeItSearchButton, expenditureItSearchButton, incomeCfSearchButton, expenditureCfSearchButton;
		DTInvestmentTransactionSearchComponent incomeInvestmentTransactionSearchComponent, expenditureInvestmentTransactionSearchComponent;
		CashFlowSearchComponent incomeCashFlowSearchComponent, expenditureCashFlowSearchComponent;

		incomeInvestmentTransactionSearchComponent = investmentTransactionSearchComponentObjectProvider.getObject();
		expenditureInvestmentTransactionSearchComponent = investmentTransactionSearchComponentObjectProvider.getObject();
		incomeCashFlowSearchComponent = cashFlowSearchComponentObjectProvider.getObject();
		expenditureCashFlowSearchComponent = cashFlowSearchComponentObjectProvider.getObject();
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Income");
		incomeItIdIntegerField = new IntegerField("Investment Transaction");
		incomeItSearchButton = new Button("IT Search");
		incomeItSearchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		incomeItSearchButton.setDisableOnClick(true);
		incomeCfIdIntegerField = new IntegerField("Cash Flow");
		incomeCfSearchButton = new Button("CF Search");
		incomeCfSearchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		incomeCfSearchButton.setDisableOnClick(true);
		hLayout.add(incomeItIdIntegerField, incomeItSearchButton, incomeCfIdIntegerField, incomeCfSearchButton);
		hLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Expenditure");
		expenditureItIdIntegerField = new IntegerField("Investment Transaction");
		expenditureItSearchButton = new Button("IT Search");
		expenditureItSearchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		expenditureItSearchButton.setDisableOnClick(true);
		expenditureCfIdIntegerField = new IntegerField("Cash Flow");
		expenditureCfSearchButton = new Button("CF Search");
		expenditureCfSearchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		expenditureCfSearchButton.setDisableOnClick(true);
		hLayout.add(expenditureItIdIntegerField, expenditureItSearchButton, expenditureCfIdIntegerField, expenditureCfSearchButton);
		hLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		
		mappedAmountNumberField = new NumberField();
		formLayout.addFormItem(mappedAmountNumberField, "Mapped Amount");
		
		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		createButton = new Button("Create");
		hLayout.add(createButton);
		createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createButton.setDisableOnClick(true);
		
		incomeItIdIntegerField.addValueChangeListener(e -> {
			if (e.isFromClient() && incomeItIdIntegerField.getValue() != null) {
				incomeCfIdIntegerField.setValue(null);
			}
		});
		incomeCfIdIntegerField.addValueChangeListener(e -> {
			if (e.isFromClient() && incomeCfIdIntegerField.getValue() != null) {
				incomeItIdIntegerField.setValue(null);
			}
		});
		expenditureItIdIntegerField.addValueChangeListener(e -> {
			if (e.isFromClient() && expenditureItIdIntegerField.getValue() != null) {
				expenditureCfIdIntegerField.setValue(null);
			}
		});
		expenditureCfIdIntegerField.addValueChangeListener(e -> {
			if (e.isFromClient() && expenditureCfIdIntegerField.getValue() != null) {
				expenditureItIdIntegerField.setValue(null);
			}
		});
		incomeItSearchButton.addClickListener(event -> {
			Dialog dialog;
			Button closeButton;

			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Income Investment Transaction");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
	    		dialog.add(incomeInvestmentTransactionSearchComponent.showForm(
	    				new ITSearchDefaults(LocalDate.now(), null, true, false, false, false, true, false)
	    				));
	    		dialog.open();
	    		incomeInvestmentTransactionSearchComponent.getInvestmentTransactionsGrid().addItemDoubleClickListener(dcEvent -> {
	    			incomeItIdIntegerField.setValue((int) dcEvent.getItem().getId());
	    			dialog.close();
	    		});
			} finally {
				incomeItSearchButton.setEnabled(true);
			}
		});
		incomeCfSearchButton.addClickListener(event -> {
			Dialog dialog;
			Button closeButton;

			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Income Cash Flow");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
	    		dialog.add(incomeCashFlowSearchComponent.showForm(Constants.DVID_BOOKING_CREDIT));
	    		dialog.open();
	    		incomeCashFlowSearchComponent.getCashFlowsGrid().addItemDoubleClickListener(dcEvent -> {
	    			incomeCfIdIntegerField.setValue(dcEvent.getItem().getId().intValue());
	    			dialog.close();
	    		});
			} finally {
				incomeCfSearchButton.setEnabled(true);
			}
		});
		expenditureItSearchButton.addClickListener(event -> {
			Dialog dialog;
			Button closeButton;

			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Expenditure Investment Transaction");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
	    		dialog.add(expenditureInvestmentTransactionSearchComponent.showForm(
	    				new ITSearchDefaults(LocalDate.now(), null, true, false, false, true, false, false)
	    				));
	    		dialog.open();
	    		expenditureInvestmentTransactionSearchComponent.getInvestmentTransactionsGrid().addItemDoubleClickListener(dcEvent -> {
	    			incomeItIdIntegerField.setValue((int) dcEvent.getItem().getId());
	    			dialog.close();
	    		});
			} finally {
				expenditureItSearchButton.setEnabled(true);
			}
		});
		expenditureCfSearchButton.addClickListener(event -> {
			Dialog dialog;
			Button closeButton;

			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Expenditure Cash Flow");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
	    		dialog.add(expenditureCashFlowSearchComponent.showForm(Constants.DVID_BOOKING_DEBIT));
	    		dialog.open();
	    		expenditureCashFlowSearchComponent.getCashFlowsGrid().addItemDoubleClickListener(dcEvent -> {
	    			expenditureCfIdIntegerField.setValue(dcEvent.getItem().getId().intValue());
	    			dialog.close();
	    		});
			} finally {
				expenditureCfSearchButton.setEnabled(true);
			}
		});
		createButton.addClickListener(event -> {
			Notification notification;
			try {
				if (incomeItIdIntegerField.getValue() == null && incomeCfIdIntegerField.getValue() == null) {
					ViewFuncs.showError("Investment Transaction Or Cash Flow (not both) is required for the Income");
					return;
				}
				if (expenditureItIdIntegerField.getValue() == null && expenditureCfIdIntegerField.getValue() == null) {
					ViewFuncs.showError("Investment Transaction Or Cash Flow (not both) is required for the Expenditure");
					return;
				}
				if (mappedAmountNumberField.getValue() == null) {
					ViewFuncs.showError("Amount to be mapped is to be specified");
					return;
				}
				try {
					planService.createPlan(
							new PlanVO(null,
									incomeItIdIntegerField.getValue() == null ? null : incomeItIdIntegerField.getValue().longValue(),
									incomeCfIdIntegerField.getValue() == null ? null : incomeCfIdIntegerField.getValue().longValue(),
									expenditureItIdIntegerField.getValue() == null ? null : expenditureItIdIntegerField.getValue().longValue(),
									expenditureCfIdIntegerField.getValue() == null ? null : expenditureCfIdIntegerField.getValue().longValue(),
									BigDecimal.valueOf(mappedAmountNumberField.getValue()))
							);
					notification = Notification.show("Plan Created Successfully");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e ) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				createButton.setEnabled(true);
			}
		});
		
		return formLayout;
	}
	
	private Component createCreateCashFlowView() {
		FormLayout formLayout;
		Select<IdValueVO> bankAccountOrInvestorDvSelect, transactionCategoryDvSelect, endAccountReferenceDvSelect;
		DatePicker flowDateDatePicker;
		RadioButtonGroup<String> transactionTypeRadioButtonGroup;
		NumberField flowAmountNumberField;
		TextField narrationTextField;
		Div editorWrapper;
		TextField endAccountReferenceTextField;
		HorizontalLayout hLayout;
		Button createButton;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		bankAccountOrInvestorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT + "+" + Constants.CATEGORY_PRIMARY_INVESTOR, null, false, false);
		formLayout.addFormItem(bankAccountOrInvestorDvSelect, "Account");
		
		flowDateDatePicker = new DatePicker();
		flowDateDatePicker.setI18n(isoDatePickerI18n);
		formLayout.addFormItem(flowDateDatePicker, "Date");
		
		transactionTypeRadioButtonGroup = new RadioButtonGroup<String>();
		transactionTypeRadioButtonGroup.setItems("Income", "Expenditure");
		transactionTypeRadioButtonGroup.setValue("Expenditure");
		formLayout.addFormItem(transactionTypeRadioButtonGroup, "Transaction Type");
		
		flowAmountNumberField = new NumberField();
		formLayout.addFormItem(flowAmountNumberField, "Amount");

		narrationTextField = new TextField();
		formLayout.addFormItem(narrationTextField, "Narration");
		
		transactionCategoryDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CATEGORY, null, false, false);
		formLayout.addFormItem(transactionCategoryDvSelect, "Transaction Category");

		editorWrapper = new Div();
		formLayout.addFormItem(editorWrapper, "End Account Reference");
		endAccountReferenceDvSelect = new Select<>();
		endAccountReferenceDvSelect.setVisible(false);
		endAccountReferenceTextField = new TextField();
		endAccountReferenceTextField.setVisible(false);
		editorWrapper.add(endAccountReferenceDvSelect, endAccountReferenceTextField);

		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		createButton = new Button("Create");
		hLayout.add(createButton);
		createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createButton.setDisableOnClick(true);
		
		transactionCategoryDvSelect.addValueChangeListener(e -> {
			String dvCategory;
			
			endAccountReferenceDvSelect.clear();
			endAccountReferenceDvSelect.setVisible(false);
			endAccountReferenceTextField.setValue("");
			endAccountReferenceTextField.setVisible(false);
			
			dvCategory = Constants.TXN_CAT_TO_DV_CAT_MAP.get(transactionCategoryDvSelect.getValue().getId());
			if (dvCategory == null || dvCategory.equals("")) {
				endAccountReferenceTextField.setVisible(true);
			} else if (dvCategory.equals(Constants.CATEGORY_NONE)) {
			} else {
				ViewFuncs.newDvSelect(endAccountReferenceDvSelect, miscService, dvCategory, null, false, false);
				endAccountReferenceDvSelect.setVisible(true);
			}
		});

		createButton.addClickListener(event -> {
			String dvCategory;
			Notification notification;
			try {
				if (bankAccountOrInvestorDvSelect.getValue() == null) {
					ViewFuncs.showError("Account cannot be Empty");
					return;
				}
				if (flowDateDatePicker.getValue() == null) {
					ViewFuncs.showError("Flow Date cannot be Empty");
					return;
				}
				if (flowAmountNumberField.getValue() == null || flowAmountNumberField.getValue() <= 0) {
					ViewFuncs.showError("Transaction Amount cannot be Empty / Zero / Negative");
					return;
				}
				if (narrationTextField.getValue() == null || narrationTextField.getValue().trim().equals("")) {
					ViewFuncs.showError("Narration cannot be Empty");
					return;
				}
				if (transactionCategoryDvSelect.getValue() == null) {
					ViewFuncs.showError("Transaction Category cannot be Empty");
					return;
				}
				dvCategory = Constants.TXN_CAT_TO_DV_CAT_MAP.get(transactionCategoryDvSelect.getValue().getId());
				if ((dvCategory == null || dvCategory.equals("")) && endAccountReferenceTextField.isEmpty() ||
						dvCategory != null && !dvCategory.equals(Constants.CATEGORY_NONE) && endAccountReferenceDvSelect.getValue() == null) {
					ViewFuncs.showError("End Account Reference cannot be empty");
					return;
				}
				
				// Back-end Call
				try {
					String endAccountReference;
					if (dvCategory == null || dvCategory.equals("")) {
						endAccountReference = endAccountReferenceTextField.getValue();
					} else if (dvCategory.equals(Constants.CATEGORY_NONE)) {
						endAccountReference = null;
					} else {
						endAccountReference = String.valueOf(endAccountReferenceDvSelect.getValue().getId());
					}
					planService.createCashFlow(
							new CashFlowVO(
									null,
									bankAccountOrInvestorDvSelect.getValue(),
									flowDateDatePicker.getValue(),
									new IdValueVO(transactionTypeRadioButtonGroup.getValue().equals("Income") ? Constants.DVID_TRANSACTION_TYPE_RECEIPT : Constants.DVID_TRANSACTION_TYPE_PAYMENT),
									BigDecimal.valueOf(flowAmountNumberField.getValue()),
									narrationTextField.getValue(),
									transactionCategoryDvSelect.getValue(),
									endAccountReference
									)
							);
					notification = Notification.show("Cash Flow Created Successfully");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e ) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				createButton.setEnabled(true);
			}
		});
		
		return formLayout;
	}
	
}
