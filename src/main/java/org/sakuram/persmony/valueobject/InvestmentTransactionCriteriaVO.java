package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class InvestmentTransactionCriteriaVO {
	LocalDate dueDateFrom, dueDateTo;
	boolean isStatusPending, isStatusCompleted, isStatusCancelled, isTypePayment, isTypeReceipt, isTypeAccrual;
	Long investorDvId, productProviderDvId;
}
