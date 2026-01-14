package org.sakuram.persmony.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
	@JoinColumn(name="from_isin_action_part_fk", nullable=false)
	private IsinActionPart fromIsinActionPart;
	
	@ManyToOne
	@JoinColumn(name="to_isin_action_part_fk", nullable=false)
	private IsinActionPart toIsinActionPart;
	
	@Column(name="quantity", nullable=true, columnDefinition="NUMERIC", precision=11, scale=5)
	private Double quantity;

	@ManyToOne
	@JoinColumn(name="match_reason_fk", nullable=false)
	private DomainValue matchReason;
	
}
