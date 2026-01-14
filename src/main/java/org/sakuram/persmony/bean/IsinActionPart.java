package org.sakuram.persmony.bean;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
	
	@Column(name="ownership_change_date", nullable=true)
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
