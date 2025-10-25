package org.sakuram.persmony.valueobject;

import java.sql.Date;

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
	Date orderDate;
	String orderTime;
	String orderNo;
	Date tradeDate;
	String tradeTime;
	String tradeNo;
}
