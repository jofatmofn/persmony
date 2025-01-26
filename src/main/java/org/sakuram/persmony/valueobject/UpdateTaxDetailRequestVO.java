package org.sakuram.persmony.valueobject;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UpdateTaxDetailRequestVO {
	long id;
	long transactionTypeDvId;
	Date accountedDate;
	Double interestAmount;
	Double tdsAmount;
	String tdsReference;
	Boolean inAis;
	Date form26asBookingDate;	
}
