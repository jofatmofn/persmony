package org.sakuram.persmony.valueobject;

import org.sakuram.persmony.util.FlaggedEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class IsinActionSpecVO {
	public enum IAQuantityType implements FlaggedEnum {
		ZERO,
		INPUT,
		PREVIOUS_INPUT,
		FULL_BALANCE,
		FACTOR_OF_BALANCE;
	}
		
	public enum IAFifoMappingType implements FlaggedEnum {
		USER_CHOICE,
		ALL_WITH_BALANCE,
		NOT_APPLICABLE;
	}
		
	public enum IALotCreationType implements FlaggedEnum {
		ONE,
		ONE_WITH_NULL_PRICE,
		TRADE,
		PROPAGATION,
		PROPAGATION_WITH_ZERO_PRICE,
		NEW_DATE_WITH_NULL_PRICE,
		NEW_DATE_AND_PRICE;
	}
		
	String actionName;
	long bookingTypeDvId;
	IAQuantityType quantityType;
	IAFifoMappingType fifoMappingType;
	IALotCreationType lotCreationType;
}
