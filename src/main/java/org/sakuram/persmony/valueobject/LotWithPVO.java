package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
@ToString
public class LotWithPVO {
	IsinActionVO isinActionVO;
	LotVO lotVO;
	
	// For cloning during .addAll to beforeChange backup list
	public LotWithPVO(LotWithPVO lotWithPVO) {
		this.isinActionVO = lotWithPVO.isinActionVO; // This is still a reference, not a copy. As isinActionVO is read-only, this is fine.
		this.lotVO.isinActionPartId = lotWithPVO.lotVO.isinActionPartId;
		this.lotVO.transactionQuantity = lotWithPVO.lotVO.transactionQuantity;
		this.lotVO.balance = lotWithPVO.lotVO.balance;
		this.lotVO.holdingChangeDate = lotWithPVO.lotVO.holdingChangeDate;
		this.lotVO.pricePerUnit = lotWithPVO.lotVO.pricePerUnit;
	}
	
	public static String[] gridColumnsH() {
		return new String[] {"isinActionVO.internal", "isinActionVO.settlementDate", "lotVO.holdingChangeDate", "isinActionVO.isin", "isinActionVO.securityName", "isinActionVO.isinActionId", "lotVO.isinActionPartId", "isinActionVO.actionType.value", "isinActionVO.bookingType.value", "isinActionVO.dematAccount.value", "lotVO.pricePerUnit", "lotVO.transactionQuantity"};
	}
	
	public static String[] gridColumnsB() {
		return new String[] {"isinActionVO.settlementDate", "lotVO.holdingChangeDate", "isinActionVO.isin", "isinActionVO.securityName", "isinActionVO.isinActionId", "lotVO.isinActionPartId", "isinActionVO.actionType.value", "isinActionVO.dematAccount.value", "lotVO.pricePerUnit", "lotVO.transactionQuantity", "lotVO.balance"};
	}
	
}
