package org.sakuram.persmony.bean;

import java.sql.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
@Table(name="contract")
public class Contract {
	
	@Id
	@SequenceGenerator(name="contract_seq_generator",sequenceName="contract_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="contract_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@Column(name="contract_no", length=31, nullable=false)
	private String contractNo;
	
	@Column(name="contract_date", nullable=false)
	private Date contractDate;
	
	@Column(name="settlement_no", length=31, nullable=false)	// Settlement Date in ISIN Action
	private String settlementNo;
	
	@Column(name="brokerage", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double brokerage;
	
	@Column(name="exchange_transaction_charge", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double exchangeTransactionCharge;
	
	@Column(name="clearing_charge", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double clearingCharge;
	
	@Column(name="gst", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double gst;
	
	@Column(name="stt", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double stt;

	@Column(name="sebi_turnover_fee", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double sebiTurnoverFee;

	@Column(name="stamp_duty", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private Double stampDuty;

	@Column(name="net_amount", nullable=false, columnDefinition="NUMERIC", precision=11, scale=3)
	private Double netAmount;

	@JsonIgnore
	@ManyToMany(mappedBy="contractList")
	@OrderBy("transaction_date")
	private List<SavingsAccountTransaction> savingsAccountTransactionList;
	
	@JsonIgnore
	@OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
	private List<IsinAction> isinActionList;

}
