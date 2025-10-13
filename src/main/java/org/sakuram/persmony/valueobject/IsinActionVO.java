package org.sakuram.persmony.valueobject;

import java.sql.Date;

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
public class IsinActionVO {
	Date settlementDate;
	String isin;
	String securityName;
	long isinActionId;
	Long tradeId;
	String actionType;
	Double transactionQuantity;
	Double balance;
	Double ppuBalance;
	String bookingType;
	String dematAccount;
	boolean isInternal;
}
