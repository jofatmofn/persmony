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
public class IsinActionVO {
	long isinActionId;
	Date settlementDate;
	String isin;
	String securityName;
	IdValueVO actionType;
	IdValueVO bookingType;
	IdValueVO dematAccount;
	boolean isInternal;
}
