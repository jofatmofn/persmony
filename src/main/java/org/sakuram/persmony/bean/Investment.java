package org.sakuram.persmony.bean;

import java.sql.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
	
	@Column(name="product_id_of_provider", length=31, nullable=true)
	private String productIdOfProvider;
	
	@Column(name="investment_id_with_provider", length=63, nullable=true)
	private String investmentIdWithProvider;
	
	@Column(name="product_name", length=127, nullable=true)
	private String productName;
	
	@ManyToOne
	@JoinColumn(name="product_type_fk", nullable=true)
	private DomainValue productType;
	
	@Column(name="worth", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double worth;
	
	@Column(name="clean_price", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double cleanPrice;
	
	@Column(name="accrued_interest", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double accruedInterest;
	
	@Column(name="charges", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double charges;
	
	@Column(name="rate_of_interest", nullable=true, columnDefinition="NUMERIC", precision=6, scale=4)
	private Double rateOfInterest;
	
	@ManyToOne
	@JoinColumn(name="taxability_fk", nullable=true)
	private DomainValue taxability;
	
	@ManyToOne
	@JoinColumn(name="previous_investment_fk", nullable=true)
	private Investment previousInvestment;
	
	@ManyToOne
	@JoinColumn(name="new_investment_reason_fk", nullable=true)
	private DomainValue newInvestmentReason;
	
	@Column(name="product_end_date", nullable=true)
	private Date productEndDate;
	
	@Column(name="is_closed", nullable=false)
	private boolean isClosed;
	
	@ManyToOne
	@JoinColumn(name="closure_type_fk", nullable=true)
	private DomainValue closureType;
	
	@Column(name="closure_date", nullable=true)
	private Date closureDate;
	
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
	
	public Investment(DomainValue investor, DomainValue productProvider, DomainValue dematAccount, DomainValue facilitator, String investorIdWithProvider, String productIdOfProvider, String investmentIdWithProvider, String productName, DomainValue productType, Double worth, Double cleanPrice, Double accruedInterest, Double charges, Double rateOfInterest, DomainValue taxability, Investment previousInvestment, DomainValue newInvestmentReason, Date productEndDate, boolean isClosed, DomainValue closureType, Date closureDate, Boolean isAccrualApplicable, List<InvestmentTransaction> investmentTransactionList, Character dynamicReceiptPeriodicity, DomainValue providerBranch) {
		this.investor = investor;
		this.productProvider = productProvider;
		this.dematAccount = dematAccount;
		this.facilitator = facilitator;
		this.investorIdWithProvider = investorIdWithProvider;
		this.productIdOfProvider = productIdOfProvider;
		this.investmentIdWithProvider = investmentIdWithProvider;
		this.productName = productName;
		this.productType = productType;
		this.worth = worth;
		this.cleanPrice = cleanPrice;
		this.accruedInterest = accruedInterest;
		this.charges = charges;
		this.rateOfInterest = rateOfInterest;
		this.taxability = taxability;
		this.previousInvestment = previousInvestment;
		this.newInvestmentReason = newInvestmentReason;
		this.productEndDate = productEndDate;
		this.isClosed = isClosed;
		this.closureType = closureType;
		this.closureDate = closureDate;
		this.isAccrualApplicable = isAccrualApplicable;
		this.investmentTransactionList = investmentTransactionList;
		this.dynamicReceiptPeriodicity = dynamicReceiptPeriodicity;
		this.providerBranch = providerBranch;
	}
}
