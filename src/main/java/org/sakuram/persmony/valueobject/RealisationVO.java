package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RealisationVO {
	long realisationId;
	long investmentTransactionId;
	Date realisationDate;
	String realisationType;
	Long detailsReference;	/* Could be id of realisation or saving account transaction */
	Double amount;
	Double returnedPrincipalAmount;
	Double interestAmount;
	Double tdsAmount;
	String tdsReference;
}
