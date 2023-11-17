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
	
	public InvestmentVO(Object[] columns) {
		this.investor = (String) columns[0];
		this.productProvider = (String) columns[1];
		this.dematAccount = (String) columns[2];	
		this.facilitator = (String) columns[3];
		this.investorIdWithProvider = (String) columns[4];
		this.productIdOfProvider = (String) columns[5];
		this.investmentIdWithProvider = (String) columns[6];
		this.productName = (String) columns[7];
		this.productType = (String) columns[8];
		this.worth = (columns[9] == null ? null : ((BigDecimal) columns[9]).doubleValue());
		this.cleanPrice = (columns[10] == null ? null : ((BigDecimal) columns[10]).doubleValue());
		this.accruedInterest = (columns[11] == null ? null : ((BigDecimal) columns[11]).doubleValue());
		this.charges = (columns[12] == null ? null : ((BigDecimal) columns[12]).doubleValue());
		this.rateOfInterest = (columns[13] == null ? null : ((BigDecimal) columns[13]).doubleValue());	
		this.taxability = (String) columns[14];
		this.previousInvestment = (columns[15] == null ? null : ((BigInteger) columns[15]).longValue());
		this.newInvestmentReason = (String) columns[16];
		this.investmentStartDate = (Date) columns[17];
		this.investmentEndDate = (Date) columns[18];
		this.isClosed = (boolean) columns[19];
		this.closureType = (String) columns[20];
		this.closureDate = (Date) columns[21];
		this.isAccrualApplicable = (Boolean) columns[22];
		this.dynamicReceiptPeriodicity = (Character) columns[23];
		this.providerBranch = (String) columns[24];
		this.investmentId = ((BigInteger) columns[25]).longValue();
	}
}
