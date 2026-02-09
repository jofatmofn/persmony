
package org.sakuram.persmony.view;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.SbAcTxnService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestmentTransaction2VO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SingleRealisationVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.Getter;

@SpringComponent
@Scope("prototype")
public class RealisationComponent extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	MiscService miscService;
	MoneyTransactionService moneyTransactionService;
	SbAcTxnService sbAcTxnService;
	InvestmentDetailComponent investmentDetailComponent;
	@Getter
	Button saveButton;
	
	public RealisationComponent(MiscService miscService, MoneyTransactionService moneyTransactionService, SbAcTxnService sbAcTxnService, InvestmentDetailComponent investmentDetailComponent) {
		this.miscService = miscService;
		this.moneyTransactionService = moneyTransactionService;
		this.sbAcTxnService = sbAcTxnService;
		this.investmentDetailComponent = investmentDetailComponent;
		saveButton = new Button("Save");
		this.removeAll();
	}
	
	public void handleRealisation() {
		handleRealisation(null);
	}
	
	public void handleRealisation(SavingsAccountTransactionVO savingsAccountTransactionVO) {
		IntegerField investmentTransactionIdIntegerField;
		Select<IdValueVO> realisationTypeDvSelect;
		HorizontalLayout topPaneHorizontalLayout;
		FormLayout inFields1FormLayout, inFields2FormLayout, outFields1FormLayout, outFields2FormLayout, outFields3FormLayout;
		InvestmentTransaction2VO investmentTransaction2VO;
		Button proceedButton;
		List<IdValueVO> realisationTypeIdValueVOList;
		IdValueVO savingsAccountRealisationTypeIdValueVO;
		
		realisationTypeIdValueVOList = miscService.fetchDvsOfCategory(Constants.CATEGORY_REALISATION_TYPE, false, false);
		savingsAccountRealisationTypeIdValueVO = realisationTypeIdValueVOList.stream()
				.filter(idValueVO -> idValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT)
				.findFirst()
				.orElse(null);
		
		investmentTransaction2VO = new InvestmentTransaction2VO();
		
		topPaneHorizontalLayout = new HorizontalLayout();
		add(topPaneHorizontalLayout);
		
		// UI Elements
		inFields1FormLayout = new FormLayout();
		inFields1FormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		topPaneHorizontalLayout.add(inFields1FormLayout);
		investmentTransactionIdIntegerField = new IntegerField();
		inFields1FormLayout.addFormItem(investmentTransactionIdIntegerField, "Investment Transaction Id");
		realisationTypeDvSelect = ViewFuncs.newDvSelect(realisationTypeIdValueVOList, "Realisation Type", false, false);
		inFields1FormLayout.addFormItem(realisationTypeDvSelect, "Realisation Type");
		if (savingsAccountTransactionVO == null) {
			realisationTypeDvSelect.setEnabled(true);
		} else {
			realisationTypeDvSelect.setEnabled(false);
			realisationTypeDvSelect.setValue(savingsAccountRealisationTypeIdValueVO);
		}
		proceedButton = new Button("Proceed");
		inFields1FormLayout.add(proceedButton);
		proceedButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		proceedButton.setDisableOnClick(true);
		
		inFields2FormLayout = new FormLayout();
		inFields2FormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		add(inFields2FormLayout);

		outFields1FormLayout = new FormLayout();
		outFields1FormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		topPaneHorizontalLayout.add(outFields1FormLayout);
		
		outFields2FormLayout = new FormLayout();
		outFields2FormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		topPaneHorizontalLayout.add(outFields2FormLayout);
		
		outFields3FormLayout = new FormLayout();
		outFields3FormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		topPaneHorizontalLayout.add(outFields3FormLayout);
		
		investmentTransactionIdIntegerField.addValueChangeListener(event -> {
			NativeLabel transactionTypeLabel, dueAmountLabel, statusLabel, investorLabel, productProviderLabel, productTypeLabel;
			Button investmentIdButton;
			InvestmentTransaction2VO investmentTransaction2VOL;
			
			inFields2FormLayout.remove(inFields2FormLayout.getChildren().collect(Collectors.toList()));
			outFields1FormLayout.remove(outFields1FormLayout.getChildren().collect(Collectors.toList()));
			outFields2FormLayout.remove(outFields2FormLayout.getChildren().collect(Collectors.toList()));
			outFields3FormLayout.remove(outFields3FormLayout.getChildren().collect(Collectors.toList()));
			
			try {
				investmentTransaction2VOL = miscService.fetchInvestmentTransaction(investmentTransactionIdIntegerField.getValue());
			} catch (Exception e ) {
				ViewFuncs.showError(UtilFuncs.messageFromException(e));
				return;
			}
			
			investmentTransaction2VOL.copyTo(investmentTransaction2VO); // To overcome "Local variable defined in an enclosing scope must be final or effectively final"
			investmentIdButton = new Button(String.valueOf(investmentTransaction2VOL.getInvestmentId()));
			investmentIdButton.addClickListener(e -> {
				investmentDetailComponent.showDetail(investmentTransaction2VOL.getInvestmentId());
			});
			outFields1FormLayout.addFormItem(investmentIdButton, "Investment Id");
			investorLabel = new NativeLabel(investmentTransaction2VOL.getInvestor());
			outFields1FormLayout.addFormItem(investorLabel, "Investor");
			productProviderLabel = new NativeLabel(investmentTransaction2VOL.getProductProvider());
			outFields1FormLayout.addFormItem(productProviderLabel, "Product Provider");
			
			productTypeLabel = new NativeLabel(investmentTransaction2VOL.getProductType());
			outFields2FormLayout.addFormItem(productTypeLabel, "Product Type");
			transactionTypeLabel = new NativeLabel(investmentTransaction2VOL.getTransactionType());
			outFields2FormLayout.addFormItem(transactionTypeLabel, "Transaction Type");
			
			dueAmountLabel = new NativeLabel(investmentTransaction2VOL.getDueAmount() == null ? " " : investmentTransaction2VOL.getDueAmount().toString());
			outFields3FormLayout.addFormItem(dueAmountLabel, "Due Amount");			
			statusLabel = new NativeLabel(investmentTransaction2VOL.getStatus());
			outFields3FormLayout.addFormItem(statusLabel, "Status");
		});
		
		realisationTypeDvSelect.addValueChangeListener(event -> {
			inFields2FormLayout.remove(inFields2FormLayout.getChildren().collect(Collectors.toList()));
		});
		
		proceedButton.addClickListener(event -> {
			try {
				inFields2FormLayout.remove(inFields2FormLayout.getChildren().collect(Collectors.toList()));
				if (investmentTransactionIdIntegerField.getValue() == null) {
					investmentTransaction2VO.setInvestmentTransactionId(0);
					ViewFuncs.showError("Provide Investment Transaction Id");
					return;
				} else {
					try {
						if (investmentTransaction2VO.getStatusDvId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
							ViewFuncs.showError("This transaction is currently not pending for realisation");
							return;
						}
						if (investmentTransaction2VO.getTransactionTypeDvId() != Constants.DVID_TRANSACTION_TYPE_ACCRUAL &&
								(realisationTypeDvSelect == null || realisationTypeDvSelect.getValue() == null)) {
							ViewFuncs.showError("Select Realisation Type");
							return;
						}
						handleRealisation2(inFields2FormLayout, realisationTypeDvSelect.getValue(), investmentTransaction2VO, savingsAccountTransactionVO);
					} catch (Exception e) {
						ViewFuncs.showError(UtilFuncs.messageFromException(e));
						return;
					}
				}
			} finally {
				proceedButton.setEnabled(true);
			}
		});
	}
	
	private void handleRealisation2(FormLayout formLayout, IdValueVO selectedRealisationIdValueVO, InvestmentTransaction2VO investmentTransaction2VO, SavingsAccountTransactionVO savingsAccountTransactionVO) {
		IntegerField realisationIdIntegerField;	// Should be converted to LongField
		DatePicker transactionDatePicker;
		Select<IdValueVO> closureTypeDvSelect, taxGroupDvSelect;
		Checkbox lastRealisationCheckbox;
		AmountComponent amountComponent;
		SbAcTxnComponent sbAcTxnComponent;
		
		// UI Elements
		amountComponent = new AmountComponent(investmentTransaction2VO.getTransactionTypeDvId());
		formLayout.addFormItem(amountComponent.getLayout(), "Realised Amount");
		if (savingsAccountTransactionVO != null) {
			amountComponent.setNetNumberField(savingsAccountTransactionVO.getAmount());
		}
		
		transactionDatePicker = new DatePicker();
		transactionDatePicker.setValue(investmentTransaction2VO.getDueDate());
		formLayout.addFormItem(transactionDatePicker, "Realised Date");
		if (savingsAccountTransactionVO == null) {
			transactionDatePicker.setEnabled(true);
		} else {
			transactionDatePicker.setValue(savingsAccountTransactionVO.getTransactionDate());
			transactionDatePicker.setEnabled(false);
		}
		
		if (investmentTransaction2VO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			lastRealisationCheckbox = null;
			closureTypeDvSelect = null;
			sbAcTxnComponent = null;
			realisationIdIntegerField = null;
		} else {
			lastRealisationCheckbox = new Checkbox();
			formLayout.addFormItem(lastRealisationCheckbox, "Last Realisation");
			lastRealisationCheckbox.setValue(true);

			closureTypeDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_CLOSURE_TYPE, false, false), "Account Closure Type", false, false);
			formLayout.addFormItem(closureTypeDvSelect, "Account Closure Type");

			realisationIdIntegerField = new IntegerField();
			sbAcTxnComponent = new SbAcTxnComponent(sbAcTxnService, () -> (investmentTransaction2VO == null || investmentTransaction2VO.getDefaultBankAccountIdValueVO() == null ? null : investmentTransaction2VO.getDefaultBankAccountIdValueVO().getId()), () -> transactionDatePicker.getValue());
			if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
				formLayout.addFormItem(sbAcTxnComponent.getLayout(), "SB A/c Txn Id");
				if (savingsAccountTransactionVO == null) {
					sbAcTxnComponent.setEnabled(true);
				} else {
					sbAcTxnComponent.getSbAcTxnIdIntegerField().setValue((int)savingsAccountTransactionVO.getSavingsAccountTransactionId());
					sbAcTxnComponent.setEnabled(false);
				}
			} else if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
				formLayout.addFormItem(realisationIdIntegerField, "Realisation Id");
			}
		}
		
		if (investmentTransaction2VO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT ||
				investmentTransaction2VO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			taxGroupDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_TAX_GROUP, false, false), "Tax Group", true, false);
			taxGroupDvSelect.setValue(investmentTransaction2VO.getDefaultTaxGroupIdValueVO());
			formLayout.addFormItem(taxGroupDvSelect, "Tax Group");
		} else {
			taxGroupDvSelect = null;
		}
		
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
					ViewFuncs.showError("Invalid Amount");
					return;
				}
				if (transactionDatePicker.getValue() == null) {
					ViewFuncs.showError("Date cannot be Empty");
					return;
				}
				if (investmentTransaction2VO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
					if (Objects.requireNonNullElse(amountComponent.getReturnedPrincipalAmount(), 0).doubleValue() > 0) {
						ViewFuncs.showError("Returned Principal Amount is not applicable for Accrual transactions");
						return;
					}
				} else if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
					if (sbAcTxnComponent.getSbAcTxnIdIntegerField().getValue() == null || sbAcTxnComponent.getSbAcTxnIdIntegerField().getValue() <= 0) {
						ViewFuncs.showError("Invalid SB A/c Txn Id");
						return;
					}
				} else if (selectedRealisationIdValueVO.getId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
					if (realisationIdIntegerField.getValue() != null && realisationIdIntegerField.getValue() <= 0) {
						ViewFuncs.showError("Invalid Realisation Id");
						return;
					}
				}
				
				// Back-end Call
				singleRealisationVO = new SingleRealisationVO(
						selectedRealisationIdValueVO == null ? null : Long.valueOf(selectedRealisationIdValueVO.getId()),
						investmentTransaction2VO.getInvestmentTransactionId(),
						(sbAcTxnComponent == null || sbAcTxnComponent.getSbAcTxnIdIntegerField().getValue() == null) ? null : Long.valueOf(sbAcTxnComponent.getSbAcTxnIdIntegerField().getValue()),
						(realisationIdIntegerField == null || realisationIdIntegerField.getValue() == null) ? null : Long.valueOf(realisationIdIntegerField.getValue()),
						amountComponent.getNetAmount(),
						amountComponent.getReturnedPrincipalAmount(),
						amountComponent.getInterestAmount(),
						amountComponent.getTdsAmount(),
						transactionDatePicker.getValue(),
						(lastRealisationCheckbox == null || lastRealisationCheckbox.getValue() == null || !lastRealisationCheckbox.getValue()) ? false : true,
						(closureTypeDvSelect == null || closureTypeDvSelect.getValue() == null) ? null : closureTypeDvSelect.getValue().getId(),
						(taxGroupDvSelect == null || taxGroupDvSelect.getValue() == null) ? null : taxGroupDvSelect.getValue().getId());
				try {
					moneyTransactionService.realisation(singleRealisationVO);
					notification = Notification.show("Realisation Saved Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
}
