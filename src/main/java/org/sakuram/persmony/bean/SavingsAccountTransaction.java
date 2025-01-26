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
	
	@Column(name="transaction_date", nullable=false)
	private Date transactionDate;
	
	@Column(name="amount", nullable=false, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double amount;

	@ManyToOne
	@JoinColumn(name="booking_fk", nullable=true)	//TOD: false
	private DomainValue booking;
	
	@Column(name="value_date", nullable=true)
	private Date valueDate;
	
	@Column(name="reference", length=63, nullable=true)	// Cheque No. / Reference No. / Instrument Id
	private String reference;
	
	@Column(name="narration", length=255, nullable=true)	//TOD: false
	private String narration;
	
	@Column(name="balance", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	//TOD: false
	private Double balance;

	@Column(name="transaction_id", length=15, nullable=true)
	private String transactionId;
	
	@Column(name="utr_number", length=31, nullable=true)
	private String utrNumber;
	
	@Column(name="remitter_branch", length=31, nullable=true)	// Can we maintain Domain Value for this?
	private String remitterBranch;
	
	@ManyToOne
	@JoinColumn(name="transaction_code_fk", nullable=true)
	private DomainValue transactionCode;
	
	@Column(name="branch_code", nullable=true, columnDefinition="INTEGER")	// IFSC without the Bank Code prefix
	private Integer branchCode;
	
	@Column(name="transaction_time", length=8, nullable=true)	// As a string in hh:mm:ss (24-Hour) format
	private String transactionTime;
	
	@ManyToOne
	@JoinColumn(name="cost_center_fk", nullable=true)
	private DomainValue costCenter;
	
	@ManyToOne
	@JoinColumn(name="voucher_type_fk", nullable=true)
	private DomainValue voucherType;
	
	@ManyToOne
	@JoinColumn(name="transaction_category_fk", nullable=true)
	private DomainValue transactionCategory;
	
	@Column(name="end_account_reference", length=127, nullable=true)
	private String endAccountReference;
	
	@JsonIgnore
	@OneToMany(mappedBy = "savingsAccountTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<SbAcTxnCategory> sbAcTxnCategoryList;
	
	public SavingsAccountTransaction(DomainValue bankAccount, Date transactionDate, Double amount) {
		this.bankAccount = bankAccount;
		this.transactionDate = transactionDate;
		this.amount = amount;
	}
}
