package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class TxnSingleRealisationWithBankVO {
	long investmentId;
	long transactionTypeDvId;
	Double amount;
	Date transactionDate;
	Long bankAccountDvId;
}
