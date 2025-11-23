package org.sakuram.persmony.bean;

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
@Table(name="isin_action_match")
public class IsinActionMatch {

	@Id
	@SequenceGenerator(name="isin_action_match_seq_generator",sequenceName="isin_action_match_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="isin_action_match_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="from_isin_action_part_fk", nullable=true)	// Only for match reason FIFO, this is false
	private IsinActionPart fromIsinActionPart;
	
	@ManyToOne
	@JoinColumn(name="to_isin_action_part_fk", nullable=true) // Only for match reason FIFO, this is false
	private IsinActionPart toIsinActionPart;
	
	@Column(name="quantity", nullable=true, columnDefinition="NUMERIC", precision=11, scale=5)
	private Double quantity;

	@Column(name="price_per_unit", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double pricePerUnit;
	
	@ManyToOne
	@JoinColumn(name="match_reason_fk", nullable=false)
	private DomainValue matchReason;
	
}
