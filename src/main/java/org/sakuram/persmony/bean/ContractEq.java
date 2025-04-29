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
@Table(name="contract_eq")
public class ContractEq {

	@Id
	@SequenceGenerator(name="contract_eq_seq_generator",sequenceName="contract_eq_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="contract_eq_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@JsonIgnore
	@OneToMany(mappedBy="contractEq", cascade=CascadeType.ALL)
	private List<IsinAction> isinActionList;
	
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
