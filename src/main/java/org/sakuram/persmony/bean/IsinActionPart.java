package org.sakuram.persmony.bean;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.sakuram.persmony.util.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	@Column(name="ownership_change_date", nullable=false)
	private Date ownershipChangeDate;
	
	@Column(name="quantity", nullable=false, columnDefinition="NUMERIC", precision=11, scale=5)
	private Double quantity;

	@Column(name="price_per_unit", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double pricePerUnit;
	
	@JsonIgnore
	@OneToOne(mappedBy="isinActionPart", cascade=CascadeType.ALL)
	private Trade trade;

	@JsonIgnore
    @OneToMany(mappedBy="fromIsinActionPart")
    private List<IsinActionMatch> toIsinActionMatchList;
    
	@JsonIgnore
    @OneToMany(mappedBy="toIsinActionPart")
    private List<IsinActionMatch> fromIsinActionMatchList;

	public double getInQuantity() {
	    return Optional.ofNullable(fromIsinActionMatchList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(isinActionMatch -> isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO &&
                		isinActionMatch.getFromIsinActionPart().getIsinAction().getDematAccount().getId() == this.getIsinAction().getDematAccount().getId() ||
                		isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_OTHERS &&
                		isinActionMatch.getFromIsinActionPart().getIsinAction().getDematAccount().getId() != this.getIsinAction().getDematAccount().getId())	// TODO: There should not be a need for demat based criteria
                .mapToDouble(isinActionMatch -> isinActionMatch.getQuantity())
                .sum();
	}
	
	public double getOutQuantity() {
	    return Optional.ofNullable(toIsinActionMatchList)
                .orElse(Collections.emptyList())
                .stream()
                .filter(isinActionMatch -> isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO &&
                		isinActionMatch.getToIsinActionPart().getIsinAction().getDematAccount().getId() == this.getIsinAction().getDematAccount().getId() ||
                		isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_OTHERS &&
                		isinActionMatch.getToIsinActionPart().getIsinAction().getDematAccount().getId() != this.getIsinAction().getDematAccount().getId())	// TODO: There should not be a need for demat based criteria
                .mapToDouble(isinActionMatch -> isinActionMatch.getQuantity())
                .sum();
	}
}
