package org.sakuram.persmony.bean;

import java.time.LocalDate;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
	private LocalDate settlementDate;
	
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
	@OrderBy("holdingChangeDate")
	private List<IsinActionPart> isinActionPartList;

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

	public IsinAction(IsinAction other) {
		// All except id and isinActionPartList
		this.isin = other.isin;
		this.settlementDate = other.settlementDate;
		this.settlementSequence = other.settlementSequence;
		this.contract = other.contract;
		this.contractEq = other.contractEq;
		this.dematAccount = other.dematAccount;
		this.quantityBooking = other.quantityBooking;
		this.action = other.action;
		this.actionType = other.actionType;
		this.isInternal = other.isInternal;
		this.investment = other.investment;	
	}
}
