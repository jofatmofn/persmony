package org.sakuram.persmony.bean;

import java.sql.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="contract_eq")
public class ContractEq {

    @Id
    @Column(name="isin_action_fk")
    private long id;

    @OneToOne
    @MapsId
    @JoinColumn(name="isin_action_fk")
    private IsinAction isinAction;
    
	@Column(name="price_per_unit", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	// Applicable to Share IPO and MF
	private Double pricePerUnit;
	
	@Column(name="stamp_duty", nullable=true, columnDefinition="NUMERIC", precision=6, scale=3)	// Applicable to MF
	private Double stampDuty;

	@Column(name="allotment_date", nullable=true)	// Applicable to MF
	private Date allotmentDate;
	
	@Column(name="net_amount", nullable=false, columnDefinition="NUMERIC", precision=11, scale=3)
	private Double netAmount;

	@JsonIgnore
	@ManyToMany(mappedBy="contractEqList")
	@OrderBy("transaction_date")
	private List<SavingsAccountTransaction> savingsAccountTransactionList;
	
}
