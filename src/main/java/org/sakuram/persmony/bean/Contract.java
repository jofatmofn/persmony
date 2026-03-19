package org.sakuram.persmony.bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
	private LocalDate contractDate;
	
	@Column(name="settlement_no", length=31, nullable=false)	// Settlement Date in ISIN Action
	private String settlementNo;
	
	@Column(name="brokerage", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal brokerage;
	
	@Column(name="exchange_transaction_charge", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal exchangeTransactionCharge;
	
	@Column(name="clearing_charge", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal clearingCharge;
	
	@Column(name="gst", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal gst;
	
	@Column(name="stt", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal stt;

	@Column(name="sebi_turnover_fee", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal sebiTurnoverFee;

	@Column(name="stamp_duty", nullable=false, columnDefinition="NUMERIC", precision=6, scale=3)
	private BigDecimal stampDuty;

	@Column(name="net_amount", nullable=false, columnDefinition="NUMERIC", precision=11, scale=3)
	private BigDecimal netAmount;

	@JsonIgnore
	@OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
	private List<Action> actionList;

	public Double getNetAmount() {
		return (netAmount == null ? null : netAmount.doubleValue());
	}
	
	public Double getExtraAmount() {
		return Optional.ofNullable(brokerage).orElse(BigDecimal.ZERO)
				.add(Optional.ofNullable(exchangeTransactionCharge).orElse(BigDecimal.ZERO))
				.add(Optional.ofNullable(clearingCharge).orElse(BigDecimal.ZERO))
				.add(Optional.ofNullable(gst).orElse(BigDecimal.ZERO))
				.add(Optional.ofNullable(stt).orElse(BigDecimal.ZERO))
				.add(Optional.ofNullable(sebiTurnoverFee).orElse(BigDecimal.ZERO))
				.add(Optional.ofNullable(stampDuty).orElse(BigDecimal.ZERO))
				.doubleValue();
	}
}
