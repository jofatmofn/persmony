package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class SingleRealisationWithBankVO {
	long investmentTransactionId;
	Float amount;
	Date transactionDate;
	long bankAccountDvId;
	Long closureTypeDvId;
}
