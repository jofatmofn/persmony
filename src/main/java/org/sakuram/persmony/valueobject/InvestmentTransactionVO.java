package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class InvestmentTransactionVO {
	long investmentTransactionId;
	String transactionType;
	Date dueDate;
	Double dueAmount;
	String status;
	Double settledAmount;
	Double returnedPrincipalAmount;
	Double interestAmount;
	Double tdsAmount;
	String taxability;
	short assessmentYear;
}
