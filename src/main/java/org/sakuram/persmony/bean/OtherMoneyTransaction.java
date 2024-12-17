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

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name="other_money_transaction")
public class OtherMoneyTransaction {
	// Only realised transactions
	
	@Id
	@SequenceGenerator(name="other_money_transaction_seq_generator",sequenceName="other_money_transaction_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="other_money_transaction_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="investor_fk", nullable=false)
	private DomainValue investor;
	
	@Column(name="realisation_date", nullable=false)
	private Date realisationDate;
	
	@ManyToOne
	@JoinColumn(name="transaction_type_fk", nullable=false)
	private DomainValue transactionType;
	
	@ManyToOne
	@JoinColumn(name="bank_account_fk", nullable=false)
	private DomainValue bankAccount;
	
	@ManyToOne
	@JoinColumn(name="end_account_type_fk", nullable=false)
	private DomainValue endAccountType;
	
	@Column(name="end_account_reference", length=127, nullable=true)
	private String endAccountReference;
	
	@Column(name="amount", nullable=false, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double amount;

}
