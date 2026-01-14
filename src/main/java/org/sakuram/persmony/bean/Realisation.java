package org.sakuram.persmony.bean;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="realisation")
public class Realisation {

	@Id
	@SequenceGenerator(name="realisation_seq_generator",sequenceName="realisation_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="realisation_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="investment_transaction_fk", nullable=false)
	private InvestmentTransaction investmentTransaction;
	
	@Column(name="realisation_date", nullable=true)
	private Date realisationDate;
	
	@Column(name="accounted_realisation_date", nullable=true)
	private Date accountedRealisationDate;
	
	@ManyToOne
	@JoinColumn(name="realisation_type_fk", nullable=true)
	private DomainValue realisationType;
	
	@ManyToOne
	@JoinColumn(name="savings_account_transaction_fk", nullable=true)
	private SavingsAccountTransaction savingsAccountTransaction;
	
	@ManyToOne
	@JoinColumn(name="referred_realisation_fk", nullable=true)
	private Realisation referredRealisation;
	
	@Column(name="amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double amount;

	@Column(name="returned_principal_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double returnedPrincipalAmount;
	
	@Column(name="interest_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double interestAmount;
	
	@Column(name="tds_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double tdsAmount; /* Only for Accruals and Receipts */
	
	@Column(name="tds_reference", length=31, nullable=true)
	private String tdsReference;
	
	@Column(name="in_ais", nullable=true)
	private Boolean inAis;
	
	@Column(name="form26as_booking_date", nullable=true)
	private Date Form26asBookingDate;
	
	public Realisation(InvestmentTransaction investmentTransaction, Date realisationDate, DomainValue realisationType, SavingsAccountTransaction savingsAccountTransaction, Realisation realisation, Double amount, Double returnedPrincipalAmount, Double interestAmount, Double tdsAmount) {
		this.investmentTransaction = investmentTransaction;
		this.realisationDate = realisationDate;
		this.realisationType = realisationType;
		this.savingsAccountTransaction = savingsAccountTransaction;
		this.referredRealisation = realisation;
		this.amount = amount;
		this.returnedPrincipalAmount = returnedPrincipalAmount;
		this.interestAmount = interestAmount;
		this.tdsAmount = tdsAmount;
		this.tdsReference = null;
	}
}
