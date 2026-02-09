package org.sakuram.persmony.view;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Stream;

import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.SearchService;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.InvestmentDetailsVO;
import org.sakuram.persmony.valueobject.InvestmentTransactionVO;
import org.sakuram.persmony.valueobject.InvestmentVO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;
import org.springframework.context.annotation.Scope;
import org.vaadin.firitin.components.DynamicFileDownloader;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class InvestmentDetailComponent extends Div {
	
	private static final long serialVersionUID = 5901252151959194081L;
	
	MoneyTransactionService moneyTransactionService;
	SearchService searchService;
	
	public InvestmentDetailComponent(SearchService searchService, MoneyTransactionService moneyTransactionService) {
		this.searchService = searchService;
		this.moneyTransactionService = moneyTransactionService;
	}
	
	public void showDetail(long investmentId) {
		InvestmentDetailsVO investmentDetailsVO;
		Notification notification;
		Dialog dialog;
		VerticalLayout verticalLayout;
		Grid<InvestmentTransactionVO> investmentTransactionsGrid;
		Grid<RealisationVO> realisationGrid;
		Grid<SavingsAccountTransactionVO> savingsAccountTransactionGrid;
		Button closeButton;
		
		try {
			
			dialog = new Dialog();
			dialog.setHeaderTitle("Investment Details");
			closeButton = new Button(new Icon("lumo", "cross"),
			        (e) -> dialog.close());
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			dialog.getHeader().add(closeButton);
			verticalLayout = new VerticalLayout();
			verticalLayout.getStyle().set("width", "90rem");
			dialog.add(verticalLayout);
			
			verticalLayout.add(createInvestmentForm(searchService.searchInvestments(List.of(
					new SearchCriterionVO("I.id", FieldSpecVO.SeqOperator.EQ.name(), String.valueOf(investmentId))
					)).get(0)));
			investmentDetailsVO = moneyTransactionService.fetchInvestmentDetails(investmentId);
			
			investmentTransactionsGrid = new Grid<>(InvestmentTransactionVO.class);
			investmentTransactionsGrid.setColumns("investmentTransactionId", "transactionType", "dueDate", "assessmentYear", "dueAmount", "status", "settledAmount", "returnedPrincipalAmount", "interestAmount", "tdsAmount", "accrualTdsReference", "taxGroup");
			for (Column<InvestmentTransactionVO> column : investmentTransactionsGrid.getColumns()) {
				column.setResizable(true);
			}
			verticalLayout.add("Investment Transactions");
			verticalLayout.add(investmentTransactionsGrid);
			
			realisationGrid = new Grid<>(RealisationVO.class);
			realisationGrid.setColumns("realisationId", "investmentTransactionId", "realisationDate", "realisationType", "amount", "returnedPrincipalAmount", "interestAmount", "tdsAmount", "tdsReference");
			realisationGrid.addColumn(realisationVO -> realisationVO.getSavingsAccountTransactionId() == null ? realisationVO.getReferredRealisationId() : realisationVO.getSavingsAccountTransactionId())
				.setHeader("Referred SAT/Realisation");
			for (Column<RealisationVO> column : realisationGrid.getColumns()) {
				column.setResizable(true);
			}
			verticalLayout.add("Realisations");
			verticalLayout.add(realisationGrid);
			verticalLayout.add(new DynamicFileDownloader("Download as CSV...", "realisations.csv", out -> {
				Stream<RealisationVO> realisationVOStream = null;
				realisationVOStream = realisationGrid.getGenericDataView().getItems();

				PrintWriter writer = new PrintWriter(out);
				writer.println("realisationId,investmentTransactionId,realisationDate,realisationType,amount,returnedPrincipalAmount,interestAmount,tdsAmount,tdsReference,Referred SAT/Realisation");
				realisationVOStream.forEach(realisationVO -> {
					writer.println(realisationVO.toString());
				});
				writer.close();
			}));
			
			savingsAccountTransactionGrid = new Grid<>(SavingsAccountTransactionVO.class);
			savingsAccountTransactionGrid.setColumns("savingsAccountTransactionId", "bankAccountOrInvestor.value", "transactionDate", "amount", "booking.value");
			for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionGrid.getColumns()) {
				column.setResizable(true);
			}
			verticalLayout.add("Savings Account Transactions");
			verticalLayout.add(savingsAccountTransactionGrid);
			
			investmentTransactionsGrid.setItems(investmentDetailsVO.getInvestmentTransactionVOList());
			realisationGrid.setItems(investmentDetailsVO.getRealisationVOList());
			savingsAccountTransactionGrid.setItems(investmentDetailsVO.getSavingsAccountTransactionVOList());
			
			dialog.open();
		} catch (Exception e) {
			ViewFuncs.showError(UtilFuncs.messageFromException(e));
			return;
		}
		notification = Notification.show("No. of investment transactions: " + investmentDetailsVO.getInvestmentTransactionVOList().size() +
				"\nNo. of realisations: " + investmentDetailsVO.getRealisationVOList().size() +
				"\nNo. of savings account transactions: " + investmentDetailsVO.getSavingsAccountTransactionVOList().size());
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		
	}

	public FormLayout createInvestmentForm(InvestmentVO investmentVO) {
		FormLayout formLayout;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 4));
		
		IntegerField investmentIdIntegerField = new IntegerField();
		investmentIdIntegerField.setEnabled(false);
		investmentIdIntegerField.setValue((int)investmentVO.getInvestmentId());
		TextField investorTextField = new TextField();
		investorTextField.setEnabled(false);
		investorTextField.setValue(investmentVO.getInvestor() == null ? "" : investmentVO.getInvestor());
		TextField productProviderTextField = new TextField();
		productProviderTextField.setEnabled(false);
		productProviderTextField.setValue(investmentVO.getProductProvider() == null ? "" : investmentVO.getProductProvider());
		TextField dematAccountTextField = new TextField();
		dematAccountTextField.setEnabled(false);
		dematAccountTextField.setValue(investmentVO.getDematAccount() == null ? "" : investmentVO.getDematAccount());
		TextField facilitatorTextField = new TextField();
		facilitatorTextField.setEnabled(false);
		facilitatorTextField.setValue(investmentVO.getFacilitator() == null ? "" : investmentVO.getFacilitator());
		TextField investorIdWithProviderTextField = new TextField();
		investorIdWithProviderTextField.setEnabled(false);
		investorIdWithProviderTextField.setValue(investmentVO.getInvestorIdWithProvider() == null ? "" : investmentVO.getInvestorIdWithProvider());
		TextField productIdOfProviderTextField = new TextField();
		productIdOfProviderTextField.setEnabled(false);
		productIdOfProviderTextField.setValue(investmentVO.getProductIdOfProvider() == null ? "" : investmentVO.getProductIdOfProvider());
		TextField investmentIdWithProviderTextField = new TextField();
		investmentIdWithProviderTextField.setEnabled(false);
		investmentIdWithProviderTextField.setValue(investmentVO.getInvestmentIdWithProvider() == null ? "" : investmentVO.getInvestmentIdWithProvider());
		TextField productNameTextField = new TextField();
		productNameTextField.setEnabled(false);
		productNameTextField.setValue(investmentVO.getProductName() == null ? "" : investmentVO.getProductName());
		TextField productTypeTextField = new TextField();
		productTypeTextField.setEnabled(false);
		productTypeTextField.setValue(investmentVO.getProductType() == null ? "" : investmentVO.getProductType());
		NumberField unitsNumberField = new NumberField();
		unitsNumberField.setEnabled(false);
		unitsNumberField.setValue(investmentVO.getUnits());
		NumberField worthNumberField = new NumberField();
		worthNumberField.setEnabled(false);
		worthNumberField.setValue(investmentVO.getWorth());
		NumberField cleanPriceNumberField = new NumberField();
		cleanPriceNumberField.setEnabled(false);
		cleanPriceNumberField.setValue(investmentVO.getCleanPrice());
		NumberField accruedInterestNumberField = new NumberField();
		accruedInterestNumberField.setEnabled(false);
		accruedInterestNumberField.setValue(investmentVO.getAccruedInterest());
		NumberField chargesNumberField = new NumberField();
		chargesNumberField.setEnabled(false);
		chargesNumberField.setValue(investmentVO.getCharges());
		NumberField rateOfInterestNumberField = new NumberField();
		rateOfInterestNumberField.setEnabled(false);
		rateOfInterestNumberField.setValue(investmentVO.getRateOfInterest());
		TextField taxabilityTextField = new TextField();
		taxabilityTextField.setEnabled(false);
		taxabilityTextField.setValue(investmentVO.getTaxability() == null ? "" : investmentVO.getTaxability());
		IntegerField previousInvestmentIntegerField = new IntegerField();
		previousInvestmentIntegerField.setEnabled(false);
		previousInvestmentIntegerField.setValue(investmentVO.getPreviousInvestment() == null ? null : investmentVO.getPreviousInvestment().intValue());
		TextField newInvestmentReasonTextField = new TextField();
		newInvestmentReasonTextField.setEnabled(false);
		newInvestmentReasonTextField.setValue(investmentVO.getNewInvestmentReason() == null ? "" : investmentVO.getNewInvestmentReason());
		DatePicker investmentStartDateDatePicker = new DatePicker();
		investmentStartDateDatePicker.setEnabled(false);
		investmentStartDateDatePicker.setValue(investmentVO.getInvestmentStartDate());
		DatePicker investmentEndDateDatePicker = new DatePicker();
		investmentEndDateDatePicker.setEnabled(false);
		investmentEndDateDatePicker.setValue(investmentVO.getInvestmentEndDate());
		Checkbox isClosedCheckbox = new Checkbox();
		isClosedCheckbox.setEnabled(false);
		isClosedCheckbox.setValue(investmentVO.isClosed());
		TextField closureTypeTextField = new TextField();
		closureTypeTextField.setEnabled(false);
		closureTypeTextField.setValue(investmentVO.getClosureType() == null ? "" : investmentVO.getClosureType());
		DatePicker closureDateDatePicker = new DatePicker();
		closureDateDatePicker.setEnabled(false);
		closureDateDatePicker.setValue(investmentVO.getClosureDate());
		Checkbox isAccrualApplicableCheckbox = new Checkbox();
		isAccrualApplicableCheckbox.setEnabled(false);
		isAccrualApplicableCheckbox.setValue(investmentVO.getIsAccrualApplicable());
		TextField dynamicReceiptPeriodicityTextField = new TextField();
		dynamicReceiptPeriodicityTextField.setEnabled(false);
		dynamicReceiptPeriodicityTextField.setValue(investmentVO.getDynamicReceiptPeriodicity() == null ? "" : investmentVO.getDynamicReceiptPeriodicity().toString());
		TextField providerBranchTextField = new TextField();
		providerBranchTextField.setEnabled(false);
		providerBranchTextField.setValue(investmentVO.getProviderBranch() == null ? "" : investmentVO.getProviderBranch());

		formLayout.addFormItem(investmentIdIntegerField, "Investment Id");
		formLayout.addFormItem(investorTextField, "Investor");
		formLayout.addFormItem(productProviderTextField, "Product Provider");
		formLayout.addFormItem(providerBranchTextField, "Provider Branch");
		formLayout.addFormItem(dematAccountTextField, "Demat Account");
		formLayout.addFormItem(investorIdWithProviderTextField, "Investor Id With Provider");
		formLayout.addFormItem(productIdOfProviderTextField, "Product Id of Provider");
		formLayout.addFormItem(investmentIdWithProviderTextField, "Investment Id With Provider");
		formLayout.addFormItem(productNameTextField, "Product Name");
		formLayout.addFormItem(productTypeTextField, "Product Type");
		formLayout.addFormItem(unitsNumberField, "Units");
		formLayout.addFormItem(worthNumberField, "Worth");
		formLayout.addFormItem(cleanPriceNumberField, "Clean Price");
		formLayout.addFormItem(accruedInterestNumberField, "Accrued Interest");
		formLayout.addFormItem(chargesNumberField, "Charges");
		formLayout.addFormItem(rateOfInterestNumberField, "Rate of Interest (%)");
		formLayout.addFormItem(taxabilityTextField, "Taxability");
		formLayout.addFormItem(previousInvestmentIntegerField, "Previous Investment");
		formLayout.addFormItem(newInvestmentReasonTextField, "New Investment Reason");
		formLayout.addFormItem(investmentStartDateDatePicker, "Investment Start Date");
		formLayout.addFormItem(investmentEndDateDatePicker, "Investment End Date");
		formLayout.addFormItem(isClosedCheckbox, "Closed?");
		formLayout.addFormItem(closureTypeTextField, "Closure Type");
		formLayout.addFormItem(closureDateDatePicker, "Closure Date");
		formLayout.addFormItem(isAccrualApplicableCheckbox, "Accrual Applicable?");
		formLayout.addFormItem(dynamicReceiptPeriodicityTextField, "Dynamic Receipt Periodicity");
		formLayout.addFormItem(facilitatorTextField, "Facilitator");
		
		return formLayout;
	}
}
