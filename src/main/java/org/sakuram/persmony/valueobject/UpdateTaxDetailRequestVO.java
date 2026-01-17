package org.sakuram.persmony.valueobject;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateTaxDetailRequestVO {
	long id;
	long transactionTypeDvId;
	LocalDate accountedDate;
	Double interestAmount;
	Double tdsAmount;
	String tdsReference;
	Boolean inAis;
	LocalDate form26asBookingDate;	
}
