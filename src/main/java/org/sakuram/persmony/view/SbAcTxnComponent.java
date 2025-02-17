package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.List;
import java.util.function.Supplier;

import org.sakuram.persmony.service.SbAcTxnService;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter(AccessLevel.NONE)
public class SbAcTxnComponent {
	Long savingsAccountTransactionId;
	HorizontalLayout layout;
	
	public SbAcTxnComponent(SbAcTxnService sbAcTxnService, Long bankAccountDvId, Supplier<Date> transactionDateSupplier) {
		IntegerField sbAcTxnIdIntegerField;
		Button fetchButton;
		
		layout = new HorizontalLayout();
		sbAcTxnIdIntegerField = new IntegerField();
		layout.add(sbAcTxnIdIntegerField);
		fetchButton = new Button("Fetch");
		layout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			SbAcTxnCriteriaVO sbAcTxnCriteriaVO;
			Notification notification;
			Dialog dialog;
			Button closeButton;
			VerticalLayout verticalLayout;
			Grid<SavingsAccountTransactionVO> savingsAccountTransactionsGrid;
			List<SavingsAccountTransactionVO> recordList = null;
			
			try {
				if (bankAccountDvId == null || transactionDateSupplier.get() == null) {
					notification = Notification.show("Bank Account and Transaction Date are required to fetch Savings Account Transactions");
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					return;
				}
				sbAcTxnCriteriaVO = new SbAcTxnCriteriaVO();
				sbAcTxnCriteriaVO.setFromDate(transactionDateSupplier.get());
				sbAcTxnCriteriaVO.setToDate(transactionDateSupplier.get());
				sbAcTxnCriteriaVO.setBankAccountDvId(bankAccountDvId);

				dialog = new Dialog();
				dialog.setHeaderTitle("SB A/c Transactions");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> {
				        	dialog.close();
				        });
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
				verticalLayout = new VerticalLayout();
				verticalLayout.getStyle().set("width", "75rem");
				dialog.add(verticalLayout);
				
				savingsAccountTransactionsGrid = new Grid<>(SavingsAccountTransactionVO.class);
				savingsAccountTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
				savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "bankAccount.value", "transactionDate", "narration", "booking.value", "amount", "balance", "valueDate", "reference", "transactionId", "utrNumber", "remitterBranch", "transactionCode.value", "branchCode", "transactionTime", "costCenter.value", "voucherType.value", "transactionCategory.value", "endAccountReference");
				for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionsGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add(savingsAccountTransactionsGrid);					
				savingsAccountTransactionsGrid.addItemDoubleClickListener(dcEvent -> {
					savingsAccountTransactionId = dcEvent.getItem().getSavingsAccountTransactionId();
					sbAcTxnIdIntegerField.setValue(savingsAccountTransactionId.intValue()); // TODO: LongField
		        	dialog.close();
				});
				dialog.open();
					
				try {
					recordList = sbAcTxnService.searchSavingsAccountTransactions(sbAcTxnCriteriaVO);
					notification = Notification.show("No. of Savings Account Transactions fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					savingsAccountTransactionsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});
	}
}
