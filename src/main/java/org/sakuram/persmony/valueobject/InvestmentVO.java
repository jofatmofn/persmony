package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvestmentVO {
	private long investmentId;
	private String investor;
	private String productProvider;
	private String dematAccount;
	private String facilitator;
	private String investorIdWithProvider;
	private String productIdOfProvider;
	private String investmentIdWithProvider;
	private String productName;
	private String productType;
	private Double units;
	private Double worth;
	private Double cleanPrice;
	private Double accruedInterest;
	private Double charges;
	private Double rateOfInterest;
	private String taxability;
	private Long previousInvestment;
	private String newInvestmentReason;
	private Date investmentStartDate;
	private Date investmentEndDate;
	private boolean isClosed;
	private String closureType;
	private Date closureDate;
	private Boolean isAccrualApplicable;
	private Character dynamicReceiptPeriodicity;	
	private String providerBranch;
	
	public String toString() {
		return String.format("%s,%s,%s,\"%s\",%s,\"%s\",%s,\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
				investmentId,
				investor,
				productProvider,
				providerBranch,
				investmentIdWithProvider,
				investorIdWithProvider,
				productType,
				productName,
				productIdOfProvider,
				rateOfInterest,
				dematAccount,
				units,
				worth,
				cleanPrice,
				accruedInterest,
				charges,
				taxability,
				isAccrualApplicable,
				investmentStartDate,
				investmentEndDate,
				dynamicReceiptPeriodicity,
				previousInvestment,
				newInvestmentReason,
				isClosed,
				closureDate,
				closureType
				);
	}
	
	public InvestmentVO(Object[] columns) {
		int colPos = 0;
		this.investor = (String) columns[colPos++];
		this.productProvider = (String) columns[colPos++];
		this.dematAccount = (String) columns[colPos++];
		this.facilitator = (String) columns[colPos++];
		this.investorIdWithProvider = (String) columns[colPos++];
		this.productIdOfProvider = (String) columns[colPos++];
		this.investmentIdWithProvider = (String) columns[colPos++];
		this.productName = (String) columns[colPos++];
		this.productType = (String) columns[colPos++];
		this.units = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.worth = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.cleanPrice = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.accruedInterest = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.charges = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.rateOfInterest = (columns[colPos] == null ? null : ((BigDecimal) columns[colPos]).doubleValue()); colPos++;
		this.taxability = (String) columns[colPos++];
		this.previousInvestment = (columns[colPos] == null ? null : ((BigInteger) columns[colPos]).longValue()); colPos++;
		this.newInvestmentReason = (String) columns[colPos++];
		this.investmentStartDate = (Date) columns[colPos++];
		this.investmentEndDate = (Date) columns[colPos++];
		this.isClosed = (boolean) columns[colPos++];
		this.closureType = (String) columns[colPos++];
		this.closureDate = (Date) columns[colPos++];
		this.isAccrualApplicable = (Boolean) columns[colPos++];
		this.dynamicReceiptPeriodicity = (Character) columns[colPos++];
		this.providerBranch = (String) columns[colPos++];
		this.investmentId = ((BigInteger) columns[colPos++]).longValue();
	}
}
