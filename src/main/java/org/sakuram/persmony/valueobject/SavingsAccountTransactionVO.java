package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SavingsAccountTransactionVO {
	long savingsAccountTransactionId;
	String bankAccount;
	Date transactionDate;
	Double amount;
}
