package org.sakuram.persmony.view;

import org.apache.commons.lang3.ObjectUtils;
import org.sakuram.persmony.util.Constants;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter(AccessLevel.NONE)
public class AmountComponent {
	Double netAmount, returnedPrincipalAmount, interestAmount, tdsAmount;
	@Getter(AccessLevel.NONE)
	boolean amountsToAddup;
	HorizontalLayout layout;
	@Getter(AccessLevel.NONE)
	long transactionTypeDvId;

	public AmountComponent(long transactionTypeDvId) {
		NumberField netNumberField, returnedPrincipalNumberField, interestNumberField, tdsNumberField;
		Checkbox amountsToAddupCheckbox;

		layout = new HorizontalLayout();
		amountsToAddup = false;
		this.transactionTypeDvId = transactionTypeDvId;
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			returnedPrincipalNumberField = new NumberField("Returned Principal");
			returnedPrincipalNumberField.addValueChangeListener(event -> {
				returnedPrincipalAmount = returnedPrincipalNumberField.getValue();
			});
			layout.add(returnedPrincipalNumberField);
		}
		
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT || transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			interestNumberField = new NumberField("Interest");
			interestNumberField.addValueChangeListener(event -> {
				interestAmount = interestNumberField.getValue();
				if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
					netAmount = ObjectUtils.defaultIfNull(interestAmount, 0).doubleValue() - ObjectUtils.defaultIfNull(tdsAmount, 0).doubleValue();
				}
			});
			tdsNumberField = new NumberField("TDS");
			tdsNumberField.addValueChangeListener(event -> {
				tdsAmount = tdsNumberField.getValue();
				if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
					netAmount = ObjectUtils.defaultIfNull(interestAmount, 0).doubleValue() - ObjectUtils.defaultIfNull(tdsAmount, 0).doubleValue();
				}
			});
			layout.add(interestNumberField, tdsNumberField);
		}
		
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_PAYMENT || transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			netNumberField = new NumberField("Net");
			netNumberField.addValueChangeListener(event -> {
				netAmount = netNumberField.getValue();
			});
			layout.add(netNumberField);
		}
			
		if (transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			amountsToAddupCheckbox = new Checkbox("Net = ReturnedPrincipal + Interest - TDS");
			amountsToAddupCheckbox.setValue(true);
			amountsToAddup = true;
			amountsToAddupCheckbox.addValueChangeListener(event -> {
				amountsToAddup = amountsToAddupCheckbox.getValue();
			});
			layout.add(amountsToAddupCheckbox);
		}
	}

	public boolean isInputValid() {
		double netAmountL, returnedPrincipalAmountL, interestAmountL, tdsAmountL;
		netAmountL = ObjectUtils.defaultIfNull(netAmount, 0).doubleValue();
		returnedPrincipalAmountL = ObjectUtils.defaultIfNull(returnedPrincipalAmount, 0).doubleValue();
		interestAmountL = ObjectUtils.defaultIfNull(interestAmount, 0).doubleValue();
		tdsAmountL = ObjectUtils.defaultIfNull(tdsAmount, 0).doubleValue();
		if ((netAmountL <= 0 && transactionTypeDvId != Constants.DVID_TRANSACTION_TYPE_RECEIPT) || returnedPrincipalAmountL < 0 || tdsAmountL < 0 || amountsToAddup && netAmountL != (returnedPrincipalAmountL + interestAmountL - tdsAmountL)) {
			return false;
		}
		return true;
	}
}
