package org.sakuram.persmony.view;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.InvestmentTransaction3VO;
import org.sakuram.persmony.valueobject.InvestmentTransactionCriteriaVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class InvestmentTransactionSearchComponent extends Div {

	private static final long serialVersionUID = -7053684638596163476L;

	MoneyTransactionService moneyTransactionService;
	
	Grid<InvestmentTransaction3VO> investmentTransactionsGrid;
	
	public InvestmentTransactionSearchComponent(MoneyTransactionService moneyTransactionService) {
		this.moneyTransactionService = moneyTransactionService;
		
	}
	
	public FormLayout showForm() {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		DatePicker dueOnOrBeforeDatePicker;
		Checkbox statusPendingCheckbox, statusCompletedCheckbox, statusCancelledCheckbox, typePaymentCheckbox, typeReceiptCheckbox, typeAccrualCheckbox;
		Button fetchButton;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		dueOnOrBeforeDatePicker = new DatePicker("From");
		formLayout.addFormItem(dueOnOrBeforeDatePicker, "Due On or Before");
		dueOnOrBeforeDatePicker.setValue(LocalDate.now());
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Transaction Status");
		statusPendingCheckbox = new Checkbox("Pending");
		statusPendingCheckbox.setValue(true);
		statusCompletedCheckbox = new Checkbox("Completed");
		statusCompletedCheckbox.setValue(false);
		statusCancelledCheckbox = new Checkbox("Cancelled");
		statusCancelledCheckbox.setValue(false);
		hLayout.add(statusPendingCheckbox, statusCompletedCheckbox, statusCancelledCheckbox);
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Transaction Type");
		typePaymentCheckbox = new Checkbox("Payment");
		typePaymentCheckbox.setValue(true);
		typeReceiptCheckbox = new Checkbox("Receipt");
		typeReceiptCheckbox.setValue(true);
		typeAccrualCheckbox = new Checkbox("Accrual");
		typeAccrualCheckbox.setValue(false);
		hLayout.add(typePaymentCheckbox, typeReceiptCheckbox, typeAccrualCheckbox);
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		investmentTransactionsGrid = new Grid<>(InvestmentTransaction3VO.class);
		investmentTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		investmentTransactionsGrid.setColumns(InvestmentTransaction3VO.gridColumns());
		for (Column<InvestmentTransaction3VO> column : investmentTransactionsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(investmentTransactionsGrid);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			List<InvestmentTransaction3VO> recordList = null;
			Notification notification;
			
			try {
				// Validation
				if (dueOnOrBeforeDatePicker.getValue() == null) {
					ViewFuncs.showError("Specify value for Due Date On or Before");
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
							dueOnOrBeforeDatePicker.getValue(),
							statusPendingCheckbox.getValue(), statusCompletedCheckbox.getValue(), statusCancelledCheckbox.getValue(),
							typePaymentCheckbox.getValue(), typeReceiptCheckbox.getValue(), typeAccrualCheckbox.getValue()
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
	
}
