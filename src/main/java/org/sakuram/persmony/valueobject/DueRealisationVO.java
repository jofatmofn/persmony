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
	
	public DueRealisationVO(Object[] record) {
		investmentId = ((BigInteger) record[0]).longValue();
		investor = (String) record[1];
		productProvider = (String) record[2];
		investmentIdWithProvider = (String) record[3];
		productType = (String) record[4];
		worth = record[5] == null ? null : ((BigDecimal) record[5]).doubleValue();
		
		investmentTransactionId = ((BigInteger) record[6]).longValue();
		transactionTypeDvId = ((BigInteger) record[7]).longValue();
		transactionType = (String) record[8];
		dueDate = (Date) record[9];
		dueAmount = record[10] == null ? null : ((BigDecimal) record[10]).doubleValue();
		investmentTransactionInterestAmount = record[11] == null ? null : ((BigDecimal) record[11]).doubleValue();
		investmentTransactionTdsAmount = record[12] == null ? null : ((BigDecimal) record[12]).doubleValue();
		accrualTdsReference = (String) record[13];
		investmentTransactionInAis = (Boolean) record[14];
		investmentTransactionForm26asBookingDate = (Date) record[15];
		
		realisationId = record[16] == null ? null : ((BigInteger) record[16]).longValue();;
		realisationDate = (Date) record[17];
		realisationAmount = record[18] == null ? null : ((BigDecimal) record[18]).doubleValue();
		realisationInterestAmount = record[19] == null ? null : ((BigDecimal) record[19]).doubleValue();
		realisationTdsAmount = record[20] == null ? null : ((BigDecimal) record[20]).doubleValue();
		realisationTdsReference = (String) record[21];
		realisationInAis = (Boolean) record[22];
		realisationForm26asBookingDate = (Date) record[23];
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
