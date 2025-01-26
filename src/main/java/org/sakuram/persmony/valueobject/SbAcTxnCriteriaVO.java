package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SbAcTxnCriteriaVO {
	Date fromDate;
	Date toDate;
	Double fromAmount;
	Double toAmount;
	String narration;
	String narrationOperator;
	Long bankAccountDvId;
	Long bookingDvId;
	Long transactionCategoryDvId;
	String endAccountReference;
	String endAccountReferenceOperator;
}
