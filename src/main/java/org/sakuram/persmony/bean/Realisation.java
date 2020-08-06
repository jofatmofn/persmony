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
@Table(name="realisation")
public class Realisation {

	@Id
	@SequenceGenerator(name="realisation_seq_generator",sequenceName="realisation_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="realisation_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="investment_transaction_fk", nullable=false)
	private InvestmentTransaction investmentTransaction;
	
	@Column(name="realisation_date", nullable=true)
	private Date realisationDate;
	
	@ManyToOne
	@JoinColumn(name="realisation_type_fk", nullable=true)
	private DomainValue realisationType;
	
	@Column(name="details_reference", nullable=true)
	private Long detailsReference;	/* Could be id of realisation or saving account transaction */
	
	@Column(name="amount", nullable=true)
	private Float amount;

}
