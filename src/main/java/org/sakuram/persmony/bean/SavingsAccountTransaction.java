package org.sakuram.persmony.bean;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="savings_account_transaction")
public class SavingsAccountTransaction {

	@Id
	@SequenceGenerator(name="savings_account_transaction_seq_generator",sequenceName="savings_account_transaction_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="savings_account_transaction_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="bank_account_fk", nullable=false)
	private DomainValue bankAccount;
	
	@Column(name="transaction_date", nullable=true)
	private Date transactionDate;
	
	@Column(name="amount", nullable=true)
	private Float amount;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DomainValue getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(DomainValue bankAccount) {
		this.bankAccount = bankAccount;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}
	
}
