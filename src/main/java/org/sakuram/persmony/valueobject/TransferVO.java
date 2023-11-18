package org.sakuram.persmony.valueobject;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class TransferVO {
	// Being transferred
	long investmentId;
	// New
	long investorDvId;
	String investorIdWithProvider;
	Long dematAccountDvId;
	String investmentIdWithProvider;
	Double units;
	Double faceValue;
	Date investmentStartDate;
}
