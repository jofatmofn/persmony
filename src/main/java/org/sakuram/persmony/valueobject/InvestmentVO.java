package org.sakuram.persmony.valueobject;

import java.math.BigInteger;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvestmentVO {
	private String investor;
	private String productProvider;
	private String dematAccount;	
	private String facilitator;
	private String investorIdWithProvider;
	private String productIdOfProvider;
	private String investmentIdWithProvider;
	private String productName;
	private String productType;
	private Float worth;
	private Float cleanPrice;
	private Float accruedInterest;
	private Float charges;
	private Float rateOfInterest;	
	private String taxability;
	private Long previousInvestment;
	private String newInvestmentReason;
	private Date productEndDate;
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
		this.worth = (Float) columns[9];
		this.cleanPrice = (Float) columns[10];
		this.accruedInterest = (Float) columns[11];
		this.charges = (Float) columns[12];
		this.rateOfInterest = (Float) columns[13];	
		this.taxability = (String) columns[14];
		this.previousInvestment = (columns[15] == null ? null : ((BigInteger) columns[15]).longValue());
		this.newInvestmentReason = (String) columns[16];
		this.productEndDate = (Date) columns[17];
		this.isClosed = (boolean) columns[18];
		this.closureType = (String) columns[19];
		this.closureDate = (Date) columns[20];
		this.isAccrualApplicable = (Boolean) columns[21];
		this.dynamicReceiptPeriodicity = (Character) columns[22];	
		this.providerBranch = (String) columns[23];
	}
}
