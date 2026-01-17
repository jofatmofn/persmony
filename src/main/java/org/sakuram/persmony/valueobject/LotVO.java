package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

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
public class LotVO {
	IsinActionVO isinActionVO;
	Long tradeId;
	Long isinActionPartId;
	Double transactionQuantity;
	Double balance;
	LocalDate ownershipChangeDate;
	Double pricePerUnit;
	
	// For cloning during .addAll to beforeChange backup list
	public LotVO(LotVO lotVO) {
		this.isinActionVO = lotVO.isinActionVO; // This is still a reference, not a copy. As isinActionVO is read-only, this is fine.
		this.tradeId = lotVO.tradeId;
		this.isinActionPartId = lotVO.isinActionPartId;
		this.transactionQuantity = lotVO.transactionQuantity;
		this.balance = lotVO.balance;
		this.ownershipChangeDate = lotVO.ownershipChangeDate;
		this.pricePerUnit = lotVO.pricePerUnit;
	}
}
