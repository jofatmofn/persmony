package org.sakuram.persmony.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
class IsinActionMatchKey implements Serializable {

	private static final long serialVersionUID = -7261761931493608663L;

	@Column(name="from_isin_action_fk")
    long fromIsinActionFk;

    @Column(name="to_isin_action_fk")
    long toIsinActionFk;

}

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="isin_action_match")
public class IsinActionMatch {

    @EmbeddedId
    IsinActionMatchKey id;
    
	@ManyToOne
	@MapsId("fromIsinActionFk")
	@JoinColumn(name="from_isin_action_fk", nullable=false)
	private IsinAction fromIsinAction;
	
	@ManyToOne
	@MapsId("toIsinActionFk")
	@JoinColumn(name="to_isin_action_fk", nullable=false)
	private IsinAction toIsinAction;
	
	@Column(name="quantity", nullable=false, columnDefinition="NUMERIC", precision=11, scale=5)
	private Double quantity;

	@ManyToOne
	@JoinColumn(name="match_reason_fk", nullable=false)
	private DomainValue matchReason;
	
}
