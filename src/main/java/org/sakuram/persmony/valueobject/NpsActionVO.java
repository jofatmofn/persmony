package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class NpsActionVO {
	long npsAccountDvId;
	int tier;
	long actionTypeDvId;
	LocalDate settlementDate;
	double eNav, eUnits, cNav, cUnits, gNav, gUnits, paymentCharge, amount;
}
