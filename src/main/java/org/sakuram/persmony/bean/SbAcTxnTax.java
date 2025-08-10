package org.sakuram.persmony.bean;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="sb_ac_txn_tax")
public class SbAcTxnTax {

	@Id
	@SequenceGenerator(name="sb_ac_txn_tax_seq_generator",sequenceName="sb_ac_txn_tax_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sb_ac_txn_tax_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

    @OneToOne
	@JoinColumn(name="savings_account_transaction_fk", nullable=false, unique=true)
	private SavingsAccountTransaction savingsAccountTransaction;
    
	@Column(name="tds_reference", length=31, nullable=true)
	private String tdsReference;
	
	@Column(name="in_ais", nullable=true)
	private Boolean inAis;
	
	@Column(name="form26as_booking_date", nullable=true)
	private Date Form26asBookingDate;
	
	@Column(name="assessment_year", nullable=true, columnDefinition="NUMERIC", precision=4, scale=0)
	private Short assessmentYear;

}
