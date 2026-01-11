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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="action")
public class Action {

	@Id
	@SequenceGenerator(name="action_seq_generator",sequenceName="action_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="action_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="entitled_isin_fk", nullable=true)
	private Isin entitledIsin;
	
	@ManyToOne
	@JoinColumn(name="action_type_fk", nullable=false)
	private DomainValue actionType;
	
	@Column(name="record_date", nullable=true)	// TODO: Nullable false
	private Date recordDate;
	
	@Column(name="new_shares_per_old", nullable=true)
	private Short newSharesPerOld;
	
	@Column(name="old_shares_base", nullable=true)
	private Short oldSharesBase;
	
	@Column(name="fractional_entitlement_cash", nullable=true, columnDefinition="NUMERIC", precision=8, scale=3)
	private Double fractionalEntitlementCash;
	
}
