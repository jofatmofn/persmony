package org.sakuram.persmony.bean;

import java.math.BigDecimal;

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
	private BigDecimal amount;

	public SbAcTxnCategory(SavingsAccountTransaction savingsAccountTransaction, DomainValue transactionCategory, String endAccountReference, Character groupId, Double amount) {
		this.savingsAccountTransaction = savingsAccountTransaction;
		this.transactionCategory = transactionCategory;
		this.endAccountReference = endAccountReference;
		this.groupId = groupId;
		this.amount = (amount == null ? null : BigDecimal.valueOf(amount));
	}
	
	public Double getAmount() {
		return (amount == null ? null : amount.doubleValue());
	}

	public void setAmount(Double amount) {
		this.amount = (amount == null ? null : BigDecimal.valueOf(amount));
	}
	
}
