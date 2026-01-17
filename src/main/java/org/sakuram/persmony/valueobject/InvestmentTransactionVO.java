package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentTransactionVO {
	long investmentTransactionId;
	String transactionType;
	LocalDate dueDate;
	Double dueAmount;
	String status;
	Double settledAmount;
	Double returnedPrincipalAmount;
	Double interestAmount;
	Double tdsAmount;
	String accrualTdsReference;
	String taxGroup;
	short assessmentYear;
	
	public void copyTo(InvestmentTransactionVO investmentTransactionVO) {	// TODO: Do this in LOMBOK way
		investmentTransactionVO.investmentTransactionId = this.investmentTransactionId;
		investmentTransactionVO.transactionType = this.transactionType;
		investmentTransactionVO.dueDate = this.dueDate;
		investmentTransactionVO.dueAmount = this.dueAmount;
		investmentTransactionVO.status = this.status;
		investmentTransactionVO.settledAmount = this.settledAmount;
		investmentTransactionVO.returnedPrincipalAmount = this.returnedPrincipalAmount;
		investmentTransactionVO.interestAmount = this.interestAmount;
		investmentTransactionVO.tdsAmount = this.tdsAmount;
		investmentTransactionVO.accrualTdsReference = this.accrualTdsReference;
		investmentTransactionVO.taxGroup = this.taxGroup;
		investmentTransactionVO.assessmentYear = this.assessmentYear;
	}
}
