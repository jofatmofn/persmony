package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvestmentTransaction3VO {
	LocalDate dueDate;
	long id;
	long investmentId;
	String investor;
	String productProvider;
	String productName;
	String investmentIdWithProvider;
	Double dueAmount;
	String basedOn;
	Double returnedPrincipalAmount;
	
	public InvestmentTransaction3VO(Object[] columns) {
		int colPos = 0;
		this.dueDate = (LocalDate) columns[colPos++];
		this.id = ((Long) columns[colPos++]).longValue();
		this.investmentId = ((Long) columns[colPos++]).longValue();
		this.investor = (String) columns[colPos++];
		this.productProvider = (String) columns[colPos++];
		this.productName = (String) columns[colPos++];
		this.investmentIdWithProvider = (String) columns[colPos++];
		this.dueAmount = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.basedOn = (columns[colPos] == null ? null : ((String) columns[colPos])); colPos++;
		this.returnedPrincipalAmount = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;;
	}
	
	public static String[] gridColumns() {
		return new String[] {"dueDate", "id", "investmentId", "investor", "productProvider", "productName", "investmentIdWithProvider", "dueAmount", "basedOn", "returnedPrincipalAmount"};
	}
	
}
