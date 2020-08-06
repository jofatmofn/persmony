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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
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
	
	@Column(name="investment_id_with_provider", length=63, nullable=false)
	private String investmentIdWithProvider;
	
	@Column(name="product_name", length=127, nullable=false)
	private String productName;
	
	@ManyToOne
	@JoinColumn(name="product_type_fk", nullable=true)
	private DomainValue productType;
	
	@Column(name="worth", nullable=true)
	private Float worth;
	
	@Column(name="rate_of_interest", nullable=true)
	private Float rateOfInterest;
	
	@ManyToOne
	@JoinColumn(name="receipt_accounting_basis_fk", nullable=true)
	private DomainValue receiptAccountingBasis;
	
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
	private boolean isAccrualApplicable;

	@JsonIgnore
	@OneToMany(mappedBy = "investment", cascade = CascadeType.ALL)
	private List<InvestmentTransaction> investmentTransactionList;

}
