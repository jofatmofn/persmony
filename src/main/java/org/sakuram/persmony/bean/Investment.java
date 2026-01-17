package org.sakuram.persmony.bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="investment")
public class Investment {

	@Id
	@SequenceGenerator(name="investment_seq_generator",sequenceName="investment_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="investment_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="investor_fk", nullable=false)
	private DomainValue investor;
	
	@ManyToOne
	@JoinColumn(name="product_provider_fk", nullable=false)
	private DomainValue productProvider;
	
	@ManyToOne
	@JoinColumn(name="demat_account_fk", nullable=true)
	private DomainValue dematAccount;
	
	@ManyToOne
	@JoinColumn(name="facilitator_fk", nullable=true)
	private DomainValue facilitator;
	
	@Column(name="investor_id_with_provider", length=31, nullable=true)
	private String investorIdWithProvider;
	
	@Column(name="product_id_of_provider", length=63, nullable=true)
	private String productIdOfProvider;
	
	@Column(name="investment_id_with_provider", length=63, nullable=true)
	private String investmentIdWithProvider;
	
	@Column(name="product_name", length=127, nullable=true)
	private String productName;
	
	@ManyToOne
	@JoinColumn(name="product_type_fk", nullable=true)
	private DomainValue productType;
	
	@Column(name="units", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	// Non-portable way of setting the default
	private BigDecimal units;
	
	@Column(name="worth", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal worth;
	
	@Column(name="clean_price", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal cleanPrice;
	
	@Column(name="accrued_interest", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal accruedInterest;
	
	@Column(name="charges", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal charges;
	
	@Column(name="rate_of_interest", nullable=true, columnDefinition="NUMERIC", precision=6, scale=4)
	private BigDecimal rateOfInterest;
	
	@ManyToOne
	@JoinColumn(name="taxability_fk", nullable=true)
	private DomainValue taxability;
	
	@ManyToOne
	@JoinColumn(name="previous_investment_fk", nullable=true)
	private Investment previousInvestment;
	
	@ManyToOne
	@JoinColumn(name="new_investment_reason_fk", nullable=true)
	private DomainValue newInvestmentReason;
	
	@Column(name="investment_start_date", nullable=true)
	private LocalDate investmentStartDate;
	
	@Column(name="investment_end_date", nullable=true)
	private LocalDate investmentEndDate;
	
	@Column(name="is_closed", nullable=false)
	private boolean isClosed;
	
	@ManyToOne
	@JoinColumn(name="closure_type_fk", nullable=true)
	private DomainValue closureType;
	
	@Column(name="closure_date", nullable=true)
	private LocalDate closureDate;
	
	@Column(name="is_accrual_applicable", nullable=true)
	private Boolean isAccrualApplicable;

	@JsonIgnore
	@OneToMany(mappedBy = "investment", cascade = CascadeType.ALL)
	@OrderBy("dueDate")
	private List<InvestmentTransaction> investmentTransactionList;

	@Column(name="dynamic_receipt_periodicity", nullable=true)
	private Character dynamicReceiptPeriodicity;
	
	@ManyToOne
	@JoinColumn(name="provider_branch_fk", nullable=true)
	private DomainValue providerBranch;

	@ManyToOne
	@JoinColumn(name="default_bank_account_fk", nullable=true)
	private DomainValue defaultBankAccount;

	@ManyToOne
	@JoinColumn(name="default_tax_group_fk", nullable=true)
	private DomainValue defaultTaxGroup;
	
	public Investment(DomainValue investor, DomainValue productProvider, DomainValue dematAccount, DomainValue facilitator, String investorIdWithProvider, String productIdOfProvider, String investmentIdWithProvider, String productName, DomainValue productType, Double units, Double worth, Double cleanPrice, Double accruedInterest, Double charges, Double rateOfInterest, DomainValue taxability, Investment previousInvestment, DomainValue newInvestmentReason, LocalDate investmentStartDate, LocalDate investmentEndDate, boolean isClosed, DomainValue closureType, LocalDate closureDate, Boolean isAccrualApplicable, List<InvestmentTransaction> investmentTransactionList, Character dynamicReceiptPeriodicity, DomainValue providerBranch, DomainValue defaultBankAccount, DomainValue defaultTaxGroup) {
		this(investor,
				productProvider,
				dematAccount,
				facilitator,
				investorIdWithProvider,
				productIdOfProvider,
				investmentIdWithProvider,
				productName,
				productType,
				(units == null ? null : BigDecimal.valueOf(units)),
				(worth == null ? null : BigDecimal.valueOf(worth)),
				(cleanPrice == null ? null : BigDecimal.valueOf(cleanPrice)),
				(accruedInterest == null ? null : BigDecimal.valueOf(accruedInterest)),
				(charges == null ? null : BigDecimal.valueOf(charges)),
				(rateOfInterest == null ? null : BigDecimal.valueOf(rateOfInterest)),
				taxability,
				previousInvestment,
				newInvestmentReason,
				investmentStartDate,
				investmentEndDate,
				isClosed,
				closureType,
				closureDate,
				isAccrualApplicable,
				investmentTransactionList,
				dynamicReceiptPeriodicity,
				providerBranch,
				defaultBankAccount,
				defaultTaxGroup);
	}
	
	public Investment(DomainValue investor, DomainValue productProvider, DomainValue dematAccount, DomainValue facilitator, String investorIdWithProvider, String productIdOfProvider, String investmentIdWithProvider, String productName, DomainValue productType, BigDecimal units, BigDecimal worth, BigDecimal cleanPrice, BigDecimal accruedInterest, BigDecimal charges, BigDecimal rateOfInterest, DomainValue taxability, Investment previousInvestment, DomainValue newInvestmentReason, LocalDate investmentStartDate, LocalDate investmentEndDate, boolean isClosed, DomainValue closureType, LocalDate closureDate, Boolean isAccrualApplicable, List<InvestmentTransaction> investmentTransactionList, Character dynamicReceiptPeriodicity, DomainValue providerBranch, DomainValue defaultBankAccount, DomainValue defaultTaxGroup) {
		this.investor = investor;
		this.productProvider = productProvider;
		this.dematAccount = dematAccount;
		this.facilitator = facilitator;
		this.investorIdWithProvider = investorIdWithProvider;
		this.productIdOfProvider = productIdOfProvider;
		this.investmentIdWithProvider = investmentIdWithProvider;
		this.productName = productName;
		this.productType = productType;
		this.units = units;
		this.worth = worth;
		this.cleanPrice = cleanPrice;
		this.accruedInterest = accruedInterest;
		this.charges = charges;
		this.rateOfInterest = rateOfInterest;
		this.taxability = taxability;
		this.previousInvestment = previousInvestment;
		this.newInvestmentReason = newInvestmentReason;
		this.investmentStartDate = investmentStartDate;
		this.investmentEndDate = investmentEndDate;
		this.isClosed = isClosed;
		this.closureType = closureType;
		this.closureDate = closureDate;
		this.isAccrualApplicable = isAccrualApplicable;
		this.investmentTransactionList = investmentTransactionList;
		this.dynamicReceiptPeriodicity = dynamicReceiptPeriodicity;
		this.providerBranch = providerBranch;
		this.defaultBankAccount = defaultBankAccount;
		this.defaultTaxGroup = defaultTaxGroup;
	}
	
	public Double getUnits() {
		return (units == null ? null : units.doubleValue());
	}
	
	public Double getWorth() {
		return (worth == null ? null : worth.doubleValue());
	}
	
	public Double getAccruedInterest() {
		return (accruedInterest == null ? null : accruedInterest.doubleValue());
	}
	
	public Double getRateOfInterest() {
		return (rateOfInterest == null ? null : rateOfInterest.doubleValue());
	}
	
}
