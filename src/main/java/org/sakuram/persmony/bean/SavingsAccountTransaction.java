package org.sakuram.persmony.bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.text.ParseException;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
	@JoinColumn(name="bank_account_or_investor_fk", nullable=false)
	private DomainValue bankAccountOrInvestor;
	
	@Column(name="transaction_date", nullable=false)
	private LocalDate transactionDate;
	
	@Column(name="amount", nullable=false, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal amount;

	@ManyToOne
	@JoinColumn(name="booking_fk", nullable=true)	//TOD: false
	private DomainValue booking;
	
	@Column(name="value_date", nullable=true)
	private LocalDate valueDate;
	
	@Column(name="reference", length=63, nullable=true)	// Cheque No. / Reference No. / Instrument Id
	private String reference;
	
	@Column(name="narration", length=255, nullable=true)	//TOD: false
	private String narration;
	
	@Column(name="balance", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	//Balance is not applicable for non-banking transaction
	private BigDecimal balance;

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
	@OrderBy("group_id, id")
	private List<SbAcTxnCategory> sbAcTxnCategoryList;
	
	@JsonIgnore
	@OneToMany(mappedBy = "savingsAccountTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("realisation_date")
	private List<Realisation> realisationList;
	
	@JsonIgnore
	@ManyToMany
	@JoinTable(
			  name = "contract_join_sb_ac_txn",
			  joinColumns = @JoinColumn(name = "savings_account_transaction_fk"),
			  inverseJoinColumns = @JoinColumn(name = "contract_fk"))
	@OrderBy("contract_date")
	private List<Contract> contractList;

	@JsonIgnore
	@ManyToMany
	@JoinTable(
			  name = "contract_eq_join_sb_ac_txn",
			  joinColumns = @JoinColumn(name = "savings_account_transaction_fk"),
			  inverseJoinColumns = @JoinColumn(name = "contract_eq_fk"))
	private List<ContractEq> contractEqList;

    @OneToOne(mappedBy="savingsAccountTransaction", fetch=FetchType.LAZY)
    private SbAcTxnTax sbAcTxnTax;
    
	public SavingsAccountTransaction(DomainValue bankAccountOrInvestor, LocalDate transactionDate, Double amount) {
		this.bankAccountOrInvestor = bankAccountOrInvestor;
		this.transactionDate = transactionDate;
		this.amount = (amount == null ? null : BigDecimal.valueOf(amount));
	}
	
	public SavingsAccountTransaction(Long bankAccountOrInvestorDvId, String transactionDateStr, Double amount, Long bookingDvId, String valueDateStr, String reference, String narration, Double balance, String transactionId, String utrNumber, String remitterBranch, Long transactionCodeDvId, Integer branchCode, String transactionTime, Long costCenterDvId, Long voucherTypeDvId) throws ParseException {
		this(bankAccountOrInvestorDvId,
				LocalDate.parse(transactionDateStr),
				amount,
				bookingDvId,
				(valueDateStr == null || valueDateStr.equals("")) ? null : LocalDate.parse(valueDateStr),
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

	public SavingsAccountTransaction(Long bankAccountOrInvestorDvId, LocalDate transactionDate, Double amount, Long bookingDvId, LocalDate valueDate, String reference, String narration, Double balance, String transactionId, String utrNumber, String remitterBranch, Long transactionCodeDvId, Integer branchCode, String transactionTime, Long costCenterDvId, Long voucherTypeDvId) 
	{
		this.bankAccountOrInvestor = (bankAccountOrInvestorDvId == null ? null : Constants.domainValueCache.get(bankAccountOrInvestorDvId));
		this.transactionDate = transactionDate;
		this.amount = (amount == null ? null : BigDecimal.valueOf(amount));
		this.booking = Constants.domainValueCache.get(bookingDvId);
		this.valueDate = valueDate;
		this.reference = (reference == null || reference.equals("") ? null : reference);
		this.narration = narration;
		this.balance = (balance == null ? null : BigDecimal.valueOf(balance));
		this.transactionId = (transactionId == null || transactionId.equals("") ? null : transactionId);
		this.utrNumber = (utrNumber == null || utrNumber.equals("") ? null : utrNumber);
		this.remitterBranch = (remitterBranch == null || remitterBranch.equals("") ? null : remitterBranch);
		this.transactionCode = (transactionCodeDvId == null ? null : Constants.domainValueCache.get(transactionCodeDvId));
		this.branchCode = branchCode;
		this.transactionTime = (transactionTime == null || transactionTime.equals("") ? null : transactionTime);
		this.costCenter = (costCenterDvId == null ? null : Constants.domainValueCache.get(costCenterDvId));
		this.voucherType = (voucherTypeDvId == null ? null : Constants.domainValueCache.get(voucherTypeDvId));
	}
	
	public Double getAmount() {
		return (amount == null ? null : amount.doubleValue());
	}
	
	public Double getBalance() {
		return (balance == null ? null : balance.doubleValue());
	}
	
}
