package org.sakuram.persmony.bean;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
