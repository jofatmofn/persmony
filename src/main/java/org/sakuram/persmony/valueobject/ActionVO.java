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
public class ActionVO {
	IdValueVO actionType;
	String entitledIsin;
	LocalDate recordDate;
	Short newSharesPerOld;
	Short oldSharesBase;
	Double costRetainedFraction;
}
