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
	
	public enum IASettlementDateType implements FlaggedEnum {
		RECORD_DATE,
		OTHER_DATE
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
		NULL;
	}
	
	long actionDvId;
	String entrySpecName;
	long bookingTypeDvId;
	IAIsinType isinInputType;
	IASettlementDateType settlementDateInputType;
	boolean isFactorOfExistingQuantity;
	IAQuantityType quantityInputType;
	IAPriceType priceInputType;
	IAFifoMappingType fifoMappingType;
	IALotCreationType lotCreationType;
	IALotDateType lotDateType;
	IALotQuantityType lotQuantityType;
	IALotPriceType lotPriceType;
}
