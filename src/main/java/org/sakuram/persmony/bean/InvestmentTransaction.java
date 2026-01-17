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
	private LocalDate dueDate;
	
	@Column(name="accounted_transaction_date", nullable=true)
	private LocalDate accountedTransactionDate;
	
	@Column(name="due_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal dueAmount;
	
	@ManyToOne
	@JoinColumn(name="status_fk", nullable=false)
	private DomainValue status;
	
	@Column(name="returned_principal_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal returnedPrincipalAmount;
	
	@Column(name="interest_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal interestAmount;
	
	@Column(name="tds_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal tdsAmount; /* Only for Accruals and Receipts */
	
	@Column(name="accrual_tds_reference", length=31, nullable=true)
	private String accrualTdsReference;
	
	@ManyToOne
	@JoinColumn(name="tax_group_fk", nullable=true)
	private DomainValue taxGroup;	/* Only for Receipts; Overrides Investment */
	
	@Column(name="in_ais", nullable=true)
	private Boolean inAis;
	
	@Column(name="form26as_booking_date", nullable=true)
	private LocalDate form26asBookingDate;
	
	@Column(name="assessment_year", nullable=true, precision=4, scale=0)
	private BigDecimal assessmentYear;	/* Why not Short? */

	@JsonIgnore
	@OneToMany(mappedBy = "investmentTransaction", cascade = CascadeType.ALL)
	@OrderBy("realisationDate")
	private List<Realisation> realisationList;

	public InvestmentTransaction(Investment investment, DomainValue transactionType, LocalDate dueDate, Double dueAmount, DomainValue status, Double returnedPrincipalAmount, Double interestAmount, Double tdsAmount, DomainValue taxGroup, BigDecimal assessmentYear, List<Realisation> realisationList) {
		this(investment,
				transactionType,
				dueDate,
				(dueAmount == null ? null : BigDecimal.valueOf(dueAmount)),
				status,
				(returnedPrincipalAmount == null ? null: BigDecimal.valueOf(returnedPrincipalAmount)),
				(interestAmount == null ? null : BigDecimal.valueOf(interestAmount)),
				(tdsAmount == null ? null : BigDecimal.valueOf(tdsAmount)),
				taxGroup,
				assessmentYear,
				realisationList);
	}
	
	public InvestmentTransaction(Investment investment, DomainValue transactionType, LocalDate dueDate, BigDecimal dueAmount, DomainValue status, BigDecimal returnedPrincipalAmount, BigDecimal interestAmount, BigDecimal tdsAmount, DomainValue taxGroup, BigDecimal assessmentYear, List<Realisation> realisationList) {
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
	
	public Double getDueAmount() {
		return (dueAmount == null ? null : dueAmount.doubleValue());
	}
	
	public void setDueAmount(Double dueAmount) {
		this.dueAmount = (dueAmount == null ? null : BigDecimal.valueOf(dueAmount));
	}

	public Double getReturnedPrincipalAmount() {
		return (returnedPrincipalAmount == null ? null : returnedPrincipalAmount.doubleValue());
	}
	
	public Double getInterestAmount() {
		return (interestAmount == null ? null : interestAmount.doubleValue());
	}
	
	public void setInterestAmount(Double interestAmount) {
		this.interestAmount = (interestAmount == null ? null : BigDecimal.valueOf(interestAmount));
	}
	
	public Double getTdsAmount() {
		return (tdsAmount == null ? null : tdsAmount.doubleValue());
	}
	
	public void setTdsAmount(Double tdsAmount) {
		this.tdsAmount = (tdsAmount == null ? null : BigDecimal.valueOf(tdsAmount));
	}
	
}
