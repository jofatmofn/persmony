package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentTransaction2VO {
	long investmentId;
	long investmentTransactionId;
	long transactionTypeDvId;
	String transactionType;
	LocalDate dueDate;
	Double dueAmount;
	long statusDvId;
	String status;
	IdValueVO defaultBankAccountIdValueVO;
	IdValueVO defaultTaxGroupIdValueVO;
	String investor;
	String productProvider;
	String productType;
	
	public void copyTo(InvestmentTransaction2VO investmentTransactionVO) {	// TODO: Do this in LOMBOK way
		investmentTransactionVO.investmentId = this.investmentId;
		investmentTransactionVO.investmentTransactionId = this.investmentTransactionId;
		investmentTransactionVO.transactionTypeDvId = this.transactionTypeDvId;
		investmentTransactionVO.transactionType = this.transactionType;
		investmentTransactionVO.dueDate = this.dueDate;
		investmentTransactionVO.dueAmount = this.dueAmount;
		investmentTransactionVO.statusDvId = this.statusDvId;
		investmentTransactionVO.status = this.status;
		investmentTransactionVO.defaultBankAccountIdValueVO = this.defaultBankAccountIdValueVO;
		investmentTransactionVO.defaultTaxGroupIdValueVO = this.defaultTaxGroupIdValueVO;
		investmentTransactionVO.investor = this.investor;
		investmentTransactionVO.productProvider = this.productProvider;
		investmentTransactionVO.productType = this.productType;
	}
}
