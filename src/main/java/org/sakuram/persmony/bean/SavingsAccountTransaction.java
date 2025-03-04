package org.sakuram.persmony.bean;

import java.sql.Date;
import java.text.ParseException;
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

import org.sakuram.persmony.util.Constants;

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
	@JoinColumn(name="bank_account_fk", nullable=true) // Ideally speaking this should NOT be NULLable. But this table accommodates entries which does not involve a bank account.
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
	
	@Column(name="balance", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	//Balance is not applicable for non-banking transaction
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
	
	@JsonIgnore
	@OneToMany(mappedBy = "savingsAccountTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<SbAcTxnCategory> sbAcTxnCategoryList;
	
	public SavingsAccountTransaction(DomainValue bankAccount, Date transactionDate, Double amount) {
		this.bankAccount = bankAccount;
		this.transactionDate = transactionDate;
		this.amount = amount;
	}
	
	public SavingsAccountTransaction(Long bankAccountDvId, String transactionDateStr, Double amount, Long bookingDvId, String valueDateStr, String reference, String narration, Double balance, String transactionId, String utrNumber, String remitterBranch, Long transactionCodeDvId, Integer branchCode, String transactionTime, Long costCenterDvId, Long voucherTypeDvId) throws ParseException {
		this(bankAccountDvId,
				new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(transactionDateStr).getTime()),
				amount,
				bookingDvId,
				(valueDateStr == null || valueDateStr.equals("") ? null : new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(valueDateStr).getTime())),
				reference,
				narration,
				balance,
				transactionId,
				utrNumber,
				remitterBranch,
				transactionCodeDvId,
				branchCode,
				transactionTime,
				costCenterDvId,
				voucherTypeDvId
		);
	}

	public SavingsAccountTransaction(Long bankAccountDvId, Date transactionDate, Double amount, Long bookingDvId, Date valueDate, String reference, String narration, Double balance, String transactionId, String utrNumber, String remitterBranch, Long transactionCodeDvId, Integer branchCode, String transactionTime, Long costCenterDvId, Long voucherTypeDvId) 
	{
		this.bankAccount = (bankAccountDvId == null ? null : Constants.domainValueCache.get(bankAccountDvId));
		this.transactionDate = transactionDate;
		this.amount = amount;
		this.booking = Constants.domainValueCache.get(bookingDvId);
		this.valueDate = valueDate;
		this.reference = (reference == null || reference.equals("") ? null : reference);
		this.narration = narration;
		this.balance = balance;
		this.transactionId = (transactionId == null || transactionId.equals("") ? null : transactionId);
		this.utrNumber = (utrNumber == null || utrNumber.equals("") ? null : utrNumber);
		this.remitterBranch = (remitterBranch == null || remitterBranch.equals("") ? null : remitterBranch);
		this.transactionCode = (transactionCodeDvId == null ? null : Constants.domainValueCache.get(transactionCodeDvId));
		this.branchCode = branchCode;
		this.transactionTime = (transactionTime == null || transactionTime.equals("") ? null : transactionTime);
		this.costCenter = (costCenterDvId == null ? null : Constants.domainValueCache.get(costCenterDvId));
		this.voucherType = (voucherTypeDvId == null ? null : Constants.domainValueCache.get(voucherTypeDvId));
	}
}
