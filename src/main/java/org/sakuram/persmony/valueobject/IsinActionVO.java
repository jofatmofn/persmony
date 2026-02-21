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
public class IsinActionVO {
	long isinActionId;
	LocalDate settlementDate;
	String isin;
	String securityName;
	IdValueVO actionType;
	IdValueVO bookingType;
	IdValueVO dematAccount;
	boolean isInternal;
	
	public IsinActionVO(Object[] columns) {
		int colPos = 0;
		this.isinActionId = ((Long) columns[colPos++]).longValue();
		this.settlementDate = (LocalDate) columns[colPos++];
		this.isin =(String) columns[colPos++];
		this.securityName =(String) columns[colPos++];
		this.actionType = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		this.bookingType = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		this.dematAccount = new IdValueVO(((Long) columns[colPos++]).longValue(), (String) columns[colPos++]);
		this.isInternal = (boolean) columns[colPos++];

	}
	
	public static String[] gridColumns() {
		return new String[] {"isinActionId", "settlementDate", "isin", "securityName", "actionType.value", "bookingType.value", "dematAccount.value", "internal"};
	}
	
}
