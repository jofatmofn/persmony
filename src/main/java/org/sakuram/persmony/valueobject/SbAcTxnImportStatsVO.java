package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SbAcTxnImportStatsVO {
	Integer debitCount;
	Double debitTotal;
	Integer creditCount;
	Double creditTotal;
}
