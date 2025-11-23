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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="isin_action")
public class IsinAction {

	@Id
	@SequenceGenerator(name="isin_action_seq_generator",sequenceName="isin_action_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="isin_action_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="isin_fk", nullable=false)
	private Isin isin;
	
	@Column(name="settlement_date", nullable=false)	// Better to be in Contract, however there can be trans(ISIN) actions without Contract
	private Date settlementDate;
	
	@Column(name="settlement_sequence", nullable=true)	// Within the same date, action
	private Short settlementSequence;
	
	@ManyToOne
	@JoinColumn(name="contract_fk", nullable=true)
	private Contract contract;
	// For an ISIN action, either contract OR contractEq is applicable
	@ManyToOne
	@JoinColumn(name="contract_eq_fk", nullable=true)
	private ContractEq contractEq;
	
	@ManyToOne
	@JoinColumn(name="demat_account_fk", nullable=false)
	private DomainValue dematAccount;
	
	@Column(name="quantity", nullable=false, columnDefinition="NUMERIC", precision=11, scale=5)	// TODO: Delete
	private Double quantity;

	@ManyToOne
	@JoinColumn(name="quantity_booking_fk", nullable=false)
	private DomainValue quantityBooking;
	
	@ManyToOne
	@JoinColumn(name="action_fk", nullable=true) // When more than one isinAction for a given action
	private Action action;
	
	@ManyToOne
	@JoinColumn(name="action_type_fk", nullable=true) // When action is not applicable
	private DomainValue actionType;
	
	@Column(name="is_internal", nullable=false)
	private boolean isInternal;

	@ManyToOne
	@JoinColumn(name="investment_fk", nullable=true)	// Applicable to Debt instruments
	private Investment investment;
	
	@JsonIgnore
	@OneToMany(mappedBy="isinAction", cascade=CascadeType.ALL)
	@OrderBy("acquisitionDate")
	private List<IsinActionPart> isinActionPartList;

	@Column(name="price_per_unit", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)	// TODO: Delete
	private Double pricePerUnit;
	
	public double getComputedQuantity() {
	    return Optional.ofNullable(isinActionPartList)
                .orElse(Collections.emptyList())
                .stream()
                .mapToDouble(isinActionPart -> isinActionPart.getQuantity())
                .sum();
	}

	public double getBasePrice() {
	    return Optional.ofNullable(isinActionPartList)
                .orElse(Collections.emptyList())
                .stream()
                .mapToDouble(isinActionPart -> isinActionPart.getQuantity() * Optional.ofNullable(isinActionPart.getPricePerUnit()).orElse(0D))
                .sum();
	}
	
	public DomainValue getEffectiveActionType() {
		if (action == null) {
			return actionType;
		} else {
			return action.getActionType();
		}
	}

}
