package org.sakuram.persmony.valueobject;

import org.sakuram.persmony.util.FlaggedEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@AllArgsConstructor
@ToString
public class IsinActionEntrySpecVO {
	// TODO Replace all ENUMs with BOOLEAN when there are just two values

	public enum IAIsinType implements FlaggedEnum {
		ENTITLED_ISIN,
		OTHER_ISIN
	}
	
	public enum IADateType implements FlaggedEnum {
		ACQUISITION,
		DISPOSAL,
		NONE
	}
	
	public enum IAQuantityType implements FlaggedEnum {
		ZERO,
		INPUT,
		PREVIOUS_INPUT,
		BALANCE;
	}
		
	public enum IAPriceType implements FlaggedEnum {
		ZERO,
		NULL,
		INPUT,
		FACTOR;
	}
		
	public enum IAFifoMappingType implements FlaggedEnum {
		USER_CHOICE,
		PREVIOUS_USER_CHOICE,
		FULL_BALANCE,
		NOT_APPLICABLE;
	}
	
	public enum IALotCreationType implements FlaggedEnum {
		TRADE,
		PROPAGATION,
		ONE;
	}
	
	public enum IALotDateType implements FlaggedEnum {
		OLD,
		INPUT,
		SETTLEMENT_DATE;
	}
	
	public enum IALotQuantityType implements FlaggedEnum {
		BALANCE,
		FACTOR_OF_BALANCE,
		INPUT;
	}
	
	public enum IALotPriceType implements FlaggedEnum {
		ZERO,
		COMPUTED,
		INPUT,
		OLD,
		PLUS,
		SPLIT,
		NULL;
	}
	
	String entrySpecName;
	boolean isMandatory;
	long bookingTypeDvId;
	IAIsinType isinInputType;
	IADateType dateType;
	IAQuantityType quantityInputType;
	IAPriceType priceInputType;
	IAFifoMappingType fifoMappingType;
	IALotCreationType lotCreationType;
	IALotDateType lotDateType;
	IALotQuantityType lotQuantityType;
	IALotPriceType lotPriceType;
}
