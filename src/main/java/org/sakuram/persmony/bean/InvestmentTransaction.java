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
public class InvestmentTransaction { /* Dues of Payments, and Receipts <-- One record per transaction instance
										Dues, and Realisations of Accruals <-- One record per accrual instance */

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
	
	@Column(name="due_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double dueAmount;
	
	@ManyToOne
	@JoinColumn(name="status_fk", nullable=false)
	private DomainValue status;
	
	@Column(name="returned_principal_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double returnedPrincipalAmount;
	
	@Column(name="interest_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double interestAmount;
	
	@Column(name="tds_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double tdsAmount; /* Only for Accruals and Receipts */
	
	@Column(name="accrual_tds_reference", length=31, nullable=true)
	private String accrualTdsReference;
	
	@ManyToOne
	@JoinColumn(name="tax_group_fk", nullable=true)
	private DomainValue taxGroup;	/* Only for Receipts; Overrides Investment */
	
	@Column(name="assessment_year", nullable=true, precision=4, scale=0)
	private BigDecimal assessmentYear;	/* Why not Short? */

	@JsonIgnore
	@OneToMany(mappedBy = "investmentTransaction", cascade = CascadeType.ALL)
	private List<Realisation> realisationList;

	public InvestmentTransaction(Investment investment, DomainValue transactionType, Date dueDate, Double dueAmount, DomainValue status, Double returnedPrincipalAmount, Double interestAmount, Double tdsAmount, DomainValue taxGroup, BigDecimal assessmentYear, List<Realisation> realisationList) {
		this.investment = investment;
		this.transactionType = transactionType;
		this.dueDate = dueDate;
		this.dueAmount = dueAmount;
		this.status = status;
		this.returnedPrincipalAmount = returnedPrincipalAmount;
		this.interestAmount = interestAmount;
		this.tdsAmount = tdsAmount;
		this.taxGroup = taxGroup;
		this.assessmentYear = assessmentYear;
		this.realisationList = realisationList;
	}
}
