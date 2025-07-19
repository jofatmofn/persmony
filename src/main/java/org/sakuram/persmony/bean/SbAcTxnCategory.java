package org.sakuram.persmony.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="sb_ac_txn_category")
public class SbAcTxnCategory {

	@Id
	@SequenceGenerator(name="sb_ac_txn_category_seq_generator",sequenceName="sb_ac_txn_category_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sb_ac_txn_category_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="savings_account_transaction_fk", nullable=false)
	private SavingsAccountTransaction savingsAccountTransaction;
	
	@Column(name="group_id", nullable=true)
	private Character groupId;

	@ManyToOne
	@JoinColumn(name="transaction_category_fk", nullable=false)
	private DomainValue transactionCategory;
	
	@Column(name="end_account_reference", length=127, nullable=true)
	private String endAccountReference;

	@Column(name="amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double amount;

	@Column(name="assessment_year", nullable=true, columnDefinition="NUMERIC", precision=4, scale=0)
	private Short assessmentYear;

	public SbAcTxnCategory(SavingsAccountTransaction savingsAccountTransaction, DomainValue transactionCategory, String endAccountReference, Character groupId, Double amount) {
		this.savingsAccountTransaction = savingsAccountTransaction;
		this.transactionCategory = transactionCategory;
		this.endAccountReference = endAccountReference;
		this.groupId = groupId;
		this.amount = amount;
	}
}
