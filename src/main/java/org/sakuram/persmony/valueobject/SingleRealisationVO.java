package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SingleRealisationVO {
	Long realisationTypeDvId;
	long investmentTransactionId;
	Long savingsAccountTransactionId;
	Long realisationId;
	Double netAmount, returnedPrincipalAmount, interestAmount, tdsAmount;
	Date transactionDate;
	boolean isLastRealisation;
	Long closureTypeDvId;
	Long taxGroupDvId;
}
