package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ActionVO {
	IdValueVO actionType;
	String entitledIsin;
	Date recordDate;
	Short newSharesPerOld;
	Short oldSharesBase;
}
