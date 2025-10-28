package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountingIsinActionEntryVO {
	Date settlementDate;
	String isin;
	Double transactionQuantity;
	IdValueVO bookingType;
	
	// For cloning during .addAll to beforeChange backup list
	public AccountingIsinActionEntryVO(AccountingIsinActionEntryVO accountingIsinActionEntryVO) {
		this.settlementDate = accountingIsinActionEntryVO.settlementDate;
		this.isin = accountingIsinActionEntryVO.isin;
		this.transactionQuantity = accountingIsinActionEntryVO.transactionQuantity;
		this.bookingType = accountingIsinActionEntryVO.bookingType;
	}
}
