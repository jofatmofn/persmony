package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RenewalVO {
	// Being extended
	long investmentId;
	float realisationAmount;
	Date realisationDate;
	Float tdsAmount;
	Float interestAmount;
	// New
	String investmentIdWithProvider;
	Date productEndDate;
	Float rateOfInterest;
	Float maturityAmount;
}
