package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsinActionVO {
	Date settlementDate;
	String isin;
	String securityName;
	long isinActionId;
	String actionType;
	Double quantity;
	String bookingType;
	String dematAccount;
}
