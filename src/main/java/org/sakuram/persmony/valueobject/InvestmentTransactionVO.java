package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentTransactionVO {
	long investmentTransactionId;
	long transactionTypeDvId;
	String transactionType;
	Date dueDate;
	Double dueAmount;
	long statusDvId;
	String status;
	Double settledAmount;
	Double returnedPrincipalAmount;
	Double interestAmount;
	Double tdsAmount;
	String accrualTdsReference;
	Long taxabilityDvId;
	String taxability;
	short assessmentYear;
	
	public void copyTo(InvestmentTransactionVO investmentTransactionVO) {	// TODO: Do this in LOMBOK way
		investmentTransactionVO.investmentTransactionId = this.investmentTransactionId;
		investmentTransactionVO.transactionTypeDvId = this.transactionTypeDvId;
		investmentTransactionVO.transactionType = this.transactionType;
		investmentTransactionVO.dueDate = this.dueDate;
		investmentTransactionVO.dueAmount = this.dueAmount;
		investmentTransactionVO.statusDvId = this.statusDvId;
		investmentTransactionVO.status = this.status;
		investmentTransactionVO.settledAmount = this.settledAmount;
		investmentTransactionVO.returnedPrincipalAmount = this.returnedPrincipalAmount;
		investmentTransactionVO.interestAmount = this.interestAmount;
		investmentTransactionVO.tdsAmount = this.tdsAmount;
		investmentTransactionVO.accrualTdsReference = this.accrualTdsReference;
		investmentTransactionVO.taxabilityDvId = this.taxabilityDvId;
		investmentTransactionVO.taxability = this.taxability;
		investmentTransactionVO.assessmentYear = this.assessmentYear;
	}
}
