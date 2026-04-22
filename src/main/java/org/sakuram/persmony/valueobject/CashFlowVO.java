package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowVO {
	Long id;
	long bankAccountOrInvestorDvId;
	LocalDate flowDate;
	long transactionTypeDvId;
	BigDecimal flowAmount;
	String narration;
	long transactionCategoryDvId;
	String endAccountReference;
	
}
