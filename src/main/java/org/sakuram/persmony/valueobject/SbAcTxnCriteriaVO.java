package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SbAcTxnCriteriaVO {
	Long fromId;
	Long toId;
	Date fromDate;
	Date toDate;
	Double fromAmount;
	Double toAmount;
	String narration;
	String narrationOperator;
	Long bankAccountOrInvestorDvId;
	Long bookingDvId;
	Long transactionCategoryDvId;
	String endAccountReference;
	String endAccountReferenceOperator;
}
