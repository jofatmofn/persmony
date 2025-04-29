package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DueRealisationVO {
	long investmentId;
	String investor;
	String productProvider;
	String investmentIdWithProvider;
	String productType;
	Double worth;
	
	long investmentTransactionId;
	long transactionTypeDvId;
	String transactionType;
	String taxGroup;
	Date dueDate;
	Double dueAmount;
	Double investmentTransactionInterestAmount;
	Double investmentTransactionTdsAmount;
	String accrualTdsReference;
	Boolean investmentTransactionInAis;
	Date investmentTransactionForm26asBookingDate;
	
	Long realisationId;
	Date realisationDate;
	Double realisationAmount;
	Double realisationInterestAmount;
	Double realisationTdsAmount;
	String realisationTdsReference;
	Boolean realisationInAis;
	Date realisationForm26asBookingDate;
	
	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
				investmentId,
				investor,
				productProvider,
				investmentIdWithProvider,
				productType,
				worth,
				investmentTransactionId,
				transactionType,
				taxGroup,
				dueDate,
				dueAmount,
				investmentTransactionInterestAmount,
				investmentTransactionTdsAmount,
				accrualTdsReference,
				investmentTransactionInAis,
				investmentTransactionForm26asBookingDate,
				realisationId,
				realisationDate,
				realisationAmount,
				realisationInterestAmount,
				realisationTdsAmount,
				realisationTdsReference,
				realisationInAis,
				realisationForm26asBookingDate
				);
	}
	public DueRealisationVO(Object[] record) {
		int colPos = 0;
		investmentId = ((BigInteger) record[colPos++]).longValue();
		investor = (String) record[colPos++];
		productProvider = (String) record[colPos++];
		investmentIdWithProvider = (String) record[colPos++];
		productType = (String) record[colPos++];
		worth = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		
		investmentTransactionId = ((BigInteger) record[colPos++]).longValue();
		transactionTypeDvId = ((BigInteger) record[colPos++]).longValue();
		transactionType = (String) record[colPos++];
		taxGroup = (String) record[colPos++];
		dueDate = (Date) record[colPos++];
		dueAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		investmentTransactionInterestAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		investmentTransactionTdsAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		accrualTdsReference = (String) record[colPos++];
		investmentTransactionInAis = (Boolean) record[colPos++];
		investmentTransactionForm26asBookingDate = (Date) record[colPos++];
		
		realisationId = record[colPos] == null ? null : ((BigInteger) record[colPos]).longValue(); colPos++;
		realisationDate = (Date) record[colPos++];
		realisationAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationInterestAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationTdsAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationTdsReference = (String) record[colPos++];
		realisationInAis = (Boolean) record[colPos++];
		realisationForm26asBookingDate = (Date) record[colPos++];
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DueRealisationVO other = (DueRealisationVO) obj;
		return investmentTransactionId == other.investmentTransactionId && Objects.equals(realisationId, other.realisationId);
	}

}
