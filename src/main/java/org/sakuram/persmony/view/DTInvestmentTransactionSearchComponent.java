package org.sakuram.persmony.view;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestmentTransaction3VO;
import org.sakuram.persmony.valueobject.InvestmentTransactionCriteriaVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class DTInvestmentTransactionSearchComponent extends Div {

	private static final long serialVersionUID = -7053684638596163476L;

	MoneyTransactionService moneyTransactionService;
	MiscService miscService;
	DatePickerI18n isoDatePickerI18n;
	
	Grid<InvestmentTransaction3VO> investmentTransactionsGrid;
	
	public DTInvestmentTransactionSearchComponent(MoneyTransactionService moneyTransactionService, MiscService miscService, DatePickerI18n isoDatePickerI18n) {
		this.moneyTransactionService = moneyTransactionService;
		this.miscService = miscService;
		this.isoDatePickerI18n = isoDatePickerI18n;
	}
	
	public FormLayout showForm() {
		return showForm(new ITSearchDefaults(null, LocalDate.now(), true, false, false, true, true, false));
	}
	
	public FormLayout showForm(ITSearchDefaults itSearchDefaults) {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		DatePicker dueDateFromDatePicker, dueDateToDatePicker;
		Checkbox statusPendingCheckbox, statusCompletedCheckbox, statusCancelledCheckbox, typePaymentCheckbox, typeReceiptCheckbox, typeAccrualCheckbox;
		Button fetchButton;
		Select<IdValueVO> investorDvSelect, productProviderDvSelect;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 2));
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Due Date");
		dueDateFromDatePicker = new DatePicker("From");
		dueDateFromDatePicker.setI18n(isoDatePickerI18n);
		dueDateToDatePicker = new DatePicker("To");
		dueDateToDatePicker.setI18n(isoDatePickerI18n);
		hLayout.add(dueDateFromDatePicker, dueDateToDatePicker);
		dueDateFromDatePicker.setValue(itSearchDefaults.dueDateFrom);
		dueDateToDatePicker.setValue(itSearchDefaults.dueDateTo);
		
		investorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_INVESTOR, null, true, false);
		formLayout.addFormItem(investorDvSelect, "Investor");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Transaction Status");
		statusPendingCheckbox = new Checkbox("Pending");
		statusPendingCheckbox.setValue(itSearchDefaults.statusPending);
		statusCompletedCheckbox = new Checkbox("Completed");
		statusCompletedCheckbox.setValue(itSearchDefaults.statusCompleted);
		statusCancelledCheckbox = new Checkbox("Cancelled");
		statusCancelledCheckbox.setValue(itSearchDefaults.statusCancelled);
		hLayout.add(statusPendingCheckbox, statusCompletedCheckbox, statusCancelledCheckbox);
		
		productProviderDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_PARTY, null, true, false);
		formLayout.addFormItem(productProviderDvSelect, "Product Provider");
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Transaction Type");
		typePaymentCheckbox = new Checkbox("Payment");
		typePaymentCheckbox.setValue(itSearchDefaults.typePayment);
		typeReceiptCheckbox = new Checkbox("Receipt");
		typeReceiptCheckbox.setValue(itSearchDefaults.typeReceipt);
		typeAccrualCheckbox = new Checkbox("Accrual");
		typeAccrualCheckbox.setValue(itSearchDefaults.typeAccrual);
		hLayout.add(typePaymentCheckbox, typeReceiptCheckbox, typeAccrualCheckbox);
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		formLayout.setColspan(fetchButton, 2);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		investmentTransactionsGrid = new Grid<>(InvestmentTransaction3VO.class);
		investmentTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		investmentTransactionsGrid.setColumns(InvestmentTransaction3VO.gridColumns());
		for (Column<InvestmentTransaction3VO> column : investmentTransactionsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(investmentTransactionsGrid);
		formLayout.setColspan(investmentTransactionsGrid, 2);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			List<InvestmentTransaction3VO> recordList = null;
			Notification notification;
			
			try {
				// Validation
				if (dueDateFromDatePicker.getValue() != null && dueDateToDatePicker.getValue() != null &&
						dueDateFromDatePicker.getValue().isAfter(dueDateToDatePicker.getValue())) {
					ViewFuncs.showError("From Due Date cannot be after To Due Date");
					return;
				}
				if (!statusPendingCheckbox.getValue() && !statusCompletedCheckbox.getValue() && !statusCancelledCheckbox.getValue()) {
					ViewFuncs.showError("Select one or more Transaction Statuses");
					return;					
				}
				if (!typePaymentCheckbox.getValue() && !typeReceiptCheckbox.getValue() && !typeAccrualCheckbox.getValue()) {
					ViewFuncs.showError("Select one or more Transaction Types");
					return;					
				}

				// Back-end Call
				try {
					recordList = moneyTransactionService.retrieveInvestmentTransactionsDueBefore(new InvestmentTransactionCriteriaVO(
							dueDateFromDatePicker.getValue(),
							dueDateToDatePicker.getValue(),
							statusPendingCheckbox.getValue(), statusCompletedCheckbox.getValue(), statusCancelledCheckbox.getValue(),
							typePaymentCheckbox.getValue(), typeReceiptCheckbox.getValue(), typeAccrualCheckbox.getValue(),
							investorDvSelect.getValue() == null ? null : investorDvSelect.getValue().getId(),
							productProviderDvSelect.getValue() == null ? null : productProviderDvSelect.getValue().getId()
							));
					notification = Notification.show("No. of Investment Transactions fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					investmentTransactionsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});
		
		return formLayout;
	}
	
	public Grid<InvestmentTransaction3VO> getInvestmentTransactionsGrid() {
		return investmentTransactionsGrid;
	}
	
	static class ITSearchDefaults {
		LocalDate dueDateFrom, dueDateTo;
		boolean statusPending, statusCompleted, statusCancelled, typePayment, typeReceipt, typeAccrual;
		
		ITSearchDefaults(LocalDate dueDateFrom, LocalDate dueDateTo, boolean statusPending, boolean statusCompleted, boolean statusCancelled, boolean typePayment, boolean typeReceipt, boolean typeAccrual) {
			this.dueDateFrom = dueDateFrom;
			this.dueDateTo = dueDateTo;
			this.statusPending = statusPending;
			this.statusCompleted = statusCompleted;
			this.statusCancelled = statusCancelled;
			this.typePayment = typePayment;
			this.typeReceipt = typeReceipt;
			this.typeAccrual = typeAccrual;
		}
	}
}
