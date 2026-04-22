package org.sakuram.persmony.bean;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.sakuram.persmony.util.Constants;

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
@Table(name="cash_flow")
public class CashFlow {

	@Id
	@SequenceGenerator(name="cash_flow_seq_generator",sequenceName="cash_flow_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cash_flow_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@Column(name="flowDate", nullable=false)
	private LocalDate flowDate;
	
	@Column(name="flow_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal flowAmount;
	
	@Column(name="narration", length=255, nullable=false)
	private String narration;
	
	@ManyToOne
	@JoinColumn(name="bank_account_or_investor_fk", nullable=false)
	private DomainValue bankAccountOrInvestor;
	
	@ManyToOne
	@JoinColumn(name="transaction_type_fk", nullable=false)
	private DomainValue transactionType;
	
	@ManyToOne
	@JoinColumn(name="transaction_category_fk", nullable=false)
	private DomainValue transactionCategory;
	
	@Column(name="end_account_reference", length=127, nullable=false)
	private String endAccountReference;

	public CashFlow(LocalDate flowDate, BigDecimal flowAmount, String narration, long bankAccountOrInvestorDvId, long transactionTypeDvId, long transactionCategoryDvId, String endAccountReference) {
		this.flowDate = flowDate;
		this.flowAmount = flowAmount;
		this.narration = narration;
		this.bankAccountOrInvestor = Constants.domainValueCache.get(bankAccountOrInvestorDvId);
		this.transactionType = Constants.domainValueCache.get(transactionTypeDvId);
		this.transactionCategory = Constants.domainValueCache.get(transactionCategoryDvId);
		this.endAccountReference = endAccountReference;
	}
	
	public String toString() {
		String dvCategory, endAccountReference;
		dvCategory = Constants.TXN_CAT_TO_DV_CAT_MAP.get(transactionCategory.getId());
		if (dvCategory == null || dvCategory.equals("") || dvCategory.equals(Constants.CATEGORY_NONE)) {
			endAccountReference = this.endAccountReference;
		} else {
			endAccountReference = Constants.domainValueCache.get(Long.parseLong(this.endAccountReference)).getValue();
		}
		return bankAccountOrInvestor.getValue() + "::" + flowDate.format(Constants.ISO_LOCAL_DATE_FORMATTER) + "::" + transactionType.getValue().substring(0, 1) + "::" + transactionCategory.getValue() + "::" + endAccountReference + "::" + narration;
	}
	
}
