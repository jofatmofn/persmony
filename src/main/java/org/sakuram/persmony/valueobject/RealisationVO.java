package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class RealisationVO {
	long realisationId;
	long investmentTransactionId;
	LocalDate realisationDate;
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
	
	public void copyTo(RealisationVO realisationVO) {	// TODO: Do this in LOMBOK way; Beware, reference copies!!!
		realisationVO.realisationId = this.realisationId;
		realisationVO.investmentTransactionId = this.investmentTransactionId;
		realisationVO.realisationDate = this.realisationDate;
		realisationVO.realisationType = this.realisationType;
		realisationVO.savingsAccountTransactionId = this.savingsAccountTransactionId;
		realisationVO.referredRealisationId = this.referredRealisationId;
		realisationVO.amount = this.amount;
		realisationVO.returnedPrincipalAmount = this.returnedPrincipalAmount;
		realisationVO.interestAmount = this.interestAmount;
		realisationVO.tdsAmount = this.tdsAmount;
		realisationVO.tdsReference = this.tdsReference;
	}
	
}
