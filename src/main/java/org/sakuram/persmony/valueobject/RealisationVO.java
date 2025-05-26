package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RealisationVO {
	long realisationId;
	long investmentTransactionId;
	Date realisationDate;
	String realisationType;
	Long savingsAccountTransactionId;
	Long referredRealisationId;
	Double amount;
	Double returnedPrincipalAmount;
	Double interestAmount;
	Double tdsAmount;
	String tdsReference;
	
	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
				realisationId,
				investmentTransactionId,
				realisationDate,
				realisationType,
				amount,
				returnedPrincipalAmount,
				interestAmount,
				tdsAmount,
				tdsReference,
				savingsAccountTransactionId == null ? referredRealisationId : savingsAccountTransactionId
				);
	}
}
