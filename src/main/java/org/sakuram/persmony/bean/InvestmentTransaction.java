package org.sakuram.persmony.bean;

import java.math.BigDecimal;
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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="investment_transaction")
public class InvestmentTransaction {

	@Id
	@SequenceGenerator(name="investment_transaction_seq_generator",sequenceName="investment_transaction_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="investment_transaction_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="investment_fk", nullable=false)
	private Investment investment;
	
	@ManyToOne
	@JoinColumn(name="transaction_type_fk", nullable=false)
	private DomainValue transactionType;
	
	@Column(name="due_date", nullable=true)
	private Date dueDate;
	
	@Column(name="due_amount", nullable=true)
	private Float dueAmount;
	
	@ManyToOne
	@JoinColumn(name="status_fk", nullable=false)
	private DomainValue status;
	
	@Column(name="settled_amount", nullable=true)
	private Float settledAmount; /* Only when not equal to dueAmount */
	
	@Column(name="returned_principal_amount", nullable=true)
	private Float returnedPrincipalAmount;
	
	@Column(name="interest_amount", nullable=true)
	private Float interestAmount;
	
	@Column(name="tds_amount", nullable=true)
	private Float tdsAmount; /* Only for Accruals and Receipts */
	
	@ManyToOne
	@JoinColumn(name="taxability_fk", nullable=true)
	private DomainValue taxability;	/* Only for Receipts; Overrides Investment */
	
	@Column(name="assessment_year", nullable=true, precision=4, scale=0)
	private BigDecimal assessmentYear;	/* Why not Short? */

	@JsonIgnore
	@OneToMany(mappedBy = "investmentTransaction", cascade = CascadeType.ALL)
	private List<Realisation> realisationList;

	public InvestmentTransaction(Investment investment, DomainValue transactionType, Date dueDate, Float dueAmount, DomainValue status, Float settledAmount, Float returnedPrincipalAmount, Float interestAmount, Float tdsAmount, DomainValue taxability, BigDecimal assessmentYear, List<Realisation> realisationList) {
		this.investment = investment;
		this.transactionType = transactionType;
		this.dueDate = dueDate;
		this.dueAmount = dueAmount;
		this.status = status;
		this.settledAmount = settledAmount;
		this.returnedPrincipalAmount = returnedPrincipalAmount;
		this.interestAmount = interestAmount;
		this.tdsAmount = tdsAmount;
		this.taxability = taxability;
		this.assessmentYear = assessmentYear;
		this.realisationList = realisationList;
	}
}
