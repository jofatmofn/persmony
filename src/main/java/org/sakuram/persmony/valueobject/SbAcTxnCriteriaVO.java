package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SbAcTxnCriteriaVO {
	Long fromId;
	Long toId;
	LocalDate fromDate;
	LocalDate toDate;
	Double fromAmount;
	Double toAmount;
	String narration;
	IdValueVO narrationOperatorIdValueVO;
	IdValueVO bankAccountOrInvestorIdValueVO;
	Long bookingDvId;
	Long transactionCategoryDvId;
	String endAccountReference;
	String endAccountReferenceOperator;
}
