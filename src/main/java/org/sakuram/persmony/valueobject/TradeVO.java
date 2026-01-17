package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TradeVO {
	Long tradeId;
	Double quantity;
	Double pricePerUnit;
	Double brokeragePerUnit;
	LocalDate orderDate;
	String orderTime;
	String orderNo;
	LocalDate tradeDate;
	String tradeTime;
	String tradeNo;
	
	// For cloning during .addAll to beforeChange backup list
	public TradeVO(TradeVO tradeVO) {
		this.tradeId = tradeVO.tradeId;
		this.quantity = tradeVO.quantity;
		this.pricePerUnit = tradeVO.pricePerUnit;
		this.brokeragePerUnit = tradeVO.brokeragePerUnit;
		this.orderDate = tradeVO.orderDate;
		this.orderTime = tradeVO.orderTime;
		this.orderNo = tradeVO.orderNo;
		this.tradeDate = tradeVO.tradeDate;
		this.tradeTime = tradeVO.tradeTime;
		this.tradeNo = tradeVO.tradeNo;
	}

	public boolean isEmpty() {
		return (orderDate == null && tradeDate == null);
	}
}
