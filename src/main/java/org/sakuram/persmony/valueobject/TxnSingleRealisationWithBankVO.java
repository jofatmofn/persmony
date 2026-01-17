package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class TxnSingleRealisationWithBankVO {
	long investmentId;
	long transactionTypeDvId;
	Double netAmount, returnedPrincipalAmount, interestAmount, tdsAmount;
	LocalDate transactionDate;
	Long savingsAccountTransactionId;
	Long taxGroupDvId;
}
