package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.math.NumberUtils;
import org.sakuram.persmony.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowVO {
	Long id;
	IdValueVO bankAccountOrInvestorIdValueVO;
	LocalDate flowDate;
	IdValueVO transactionTypeIdValueVO;
	BigDecimal flowAmount;
	String narration;
	IdValueVO transactionCategoryIdValueVO;
	String endAccountReference;
	
	public static String[] gridColumns() {
		return new String[] {"flowDate", "id", "bankAccountOrInvestorIdValueVO.value", "transactionTypeIdValueVO.value", "narration", "flowAmount", "transactionCategoryIdValueVO.value", "endAccountReference"};
	}
	
	public CashFlowVO(Object[] columns) {
		int colPos = 0;
		this.id = ((Long) columns[colPos++]).longValue();
		this.bankAccountOrInvestorIdValueVO = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		this.flowDate = (LocalDate) columns[colPos++];
		this.transactionTypeIdValueVO = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		this.flowAmount = new BigDecimal(columns[colPos++].toString());
		this.narration = columns[colPos++].toString();
		this.transactionCategoryIdValueVO = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		if (NumberUtils.isDigits(columns[colPos].toString())) {
			this.endAccountReference = Constants.domainValueCache.get(Long.parseLong(columns[colPos].toString())).getValue();
		} else {
			this.endAccountReference = columns[colPos].toString();
		}
	}
}
