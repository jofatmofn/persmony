package org.sakuram.persmony.valueobject;

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
	private long previousInvestment;
	private String newInvestmentReason;
	private Date productEndDate;
	private boolean isClosed;
	private String closureType;
	private Date closureDate;
	private Boolean isAccrualApplicable;
	private Character dynamicReceiptPeriodicity;	
	private String providerBranch;
}
