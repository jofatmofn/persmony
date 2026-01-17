package org.sakuram.persmony.view;

import java.time.LocalDate;
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

import lombok.Getter;
import lombok.Setter;

public class SbAcTxnComponent {
	@Getter
	HorizontalLayout layout;
	@Getter @Setter
	IntegerField sbAcTxnIdIntegerField;	// TODO: Should be LongField
	Button fetchButton;
	
	public SbAcTxnComponent(SbAcTxnService sbAcTxnService, Supplier<Long> bankAccountDvIdSupplier, Supplier<LocalDate> transactionDateSupplier) {
		
		layout = new HorizontalLayout();
		sbAcTxnIdIntegerField = new IntegerField("SAT Id");
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
				if (bankAccountDvIdSupplier == null || bankAccountDvIdSupplier.get() == null ||
						transactionDateSupplier == null || transactionDateSupplier.get() == null) {
					return;
				}
				sbAcTxnCriteriaVO = new SbAcTxnCriteriaVO();
				sbAcTxnCriteriaVO.setFromDate(transactionDateSupplier.get());
				sbAcTxnCriteriaVO.setToDate(transactionDateSupplier.get());
				sbAcTxnCriteriaVO.setBankAccountOrInvestorDvId(bankAccountDvIdSupplier.get());

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
				savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "bankAccountOrInvestor.value", "transactionDate", "narration", "booking.value", "amount", "balance", "valueDate", "reference", "transactionId", "utrNumber", "remitterBranch", "transactionCode.value", "branchCode", "transactionTime", "costCenter.value", "voucherType.value");
				for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionsGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add(savingsAccountTransactionsGrid);					
				savingsAccountTransactionsGrid.addItemDoubleClickListener(dcEvent -> {
					sbAcTxnIdIntegerField.setValue((int) dcEvent.getItem().getSavingsAccountTransactionId()); // TODO: LongField
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
	
	void setEnabled(boolean enabled) {
		sbAcTxnIdIntegerField.setEnabled(enabled);
		fetchButton.setEnabled(enabled);
	}
}
