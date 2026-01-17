package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ActionVO {
	IdValueVO actionType;
	String entitledIsin;
	LocalDate recordDate;
	Short newSharesPerOld;
	Short oldSharesBase;
}
