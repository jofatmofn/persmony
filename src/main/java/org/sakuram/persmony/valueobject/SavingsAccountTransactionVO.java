package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccountTransactionVO {
	long savingsAccountTransactionId;
	IdValueVO bankAccountOrInvestor;
	Date transactionDate;
	Double amount;
	IdValueVO booking;
	Date valueDate;
	String reference;
	String narration;
	Double balance;
	String transactionId;
	String utrNumber;
	String remitterBranch;
	IdValueVO transactionCode;
	Integer branchCode;
	String transactionTime;
	IdValueVO costCenter;
	IdValueVO voucherType;

	public SavingsAccountTransactionVO(long savingsAccountTransactionId, String bankAccountOrInvestor, Date transactionDate, Double amount) {
		this.savingsAccountTransactionId = savingsAccountTransactionId;
		this.bankAccountOrInvestor = new IdValueVO(null, bankAccountOrInvestor);
		this.transactionDate = transactionDate;
		this.amount = amount;
	}
	
	public SavingsAccountTransactionVO(Object[] columns) {
		int colPos = 0;
		this.savingsAccountTransactionId = ((BigInteger) columns[colPos++]).longValue();
		if (columns[colPos] == null) {
			colPos = colPos + 2;
		} else {
			this.bankAccountOrInvestor = new IdValueVO(((BigInteger) columns[colPos++]).longValue(), (String) columns[colPos++]);
		}
		this.transactionDate = (Date) columns[colPos++];
		this.amount = ((BigDecimal) columns[colPos++]).doubleValue();
		if (columns[colPos] == null) {
			colPos = colPos + 2;
		} else {
			this.booking = new IdValueVO(((BigInteger) columns[colPos++]).longValue(), (String) columns[colPos++]);
		}
		this.valueDate = columns[colPos] == null ? null : (Date) columns[colPos]; colPos++;
		this.reference = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		this.narration = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		this.balance = columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue(); colPos++;
		this.transactionId = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		this.utrNumber = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		this.remitterBranch = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		if (columns[colPos] == null) {
			colPos = colPos + 2;
		} else {
			this.transactionCode = new IdValueVO(((BigInteger) columns[colPos++]).longValue(), (String) columns[colPos++]);
		}
		this.branchCode = columns[colPos] == null ? null : ((Integer) columns[colPos]).intValue(); colPos++;
		this.transactionTime = columns[colPos] == null ? null : (String) columns[colPos]; colPos++;
		if (columns[colPos] == null) {
			colPos = colPos + 2;
		} else {
			this.costCenter = new IdValueVO(((BigInteger) columns[colPos++]).longValue(), (String) columns[colPos++]);
		}
		if (columns[colPos] == null) {
			colPos = colPos + 2;
		} else {
			this.voucherType = new IdValueVO(((BigInteger) columns[colPos++]).longValue(), (String) columns[colPos++]);
		}
	}
}
