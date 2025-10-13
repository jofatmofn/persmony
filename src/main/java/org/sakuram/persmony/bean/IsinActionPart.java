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
@Table(name="isin_action_part")
public class IsinActionPart {

	@Id
	@SequenceGenerator(name="isin_action_part_seq_generator",sequenceName="isin_action_part_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="isin_action_part_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="isin_action_fk", nullable=false)
	private IsinAction isinAction;
	
	@Column(name="acquisition_date", nullable=true)	// TODO: nullable=false
	private Date acquisitionDate;
	
	@Column(name="quantity", nullable=false, columnDefinition="NUMERIC", precision=11, scale=5)
	private Double quantity;

	@Column(name="price_per_unit", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double pricePerUnit;
	
}
