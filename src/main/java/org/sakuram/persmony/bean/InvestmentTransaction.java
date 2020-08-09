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
import lombok.Setter;

@Getter @Setter
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
	private BigDecimal assessmentYear;

	@JsonIgnore
	@OneToMany(mappedBy = "investmentTransaction", cascade = CascadeType.ALL)
	private List<Realisation> realisationList;

}
