package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.sakuram.persmony.util.Constants;

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
	LocalDate dueDate;
	Double dueAmount;
	Double investmentTransactionInterestAmount;
	Double investmentTransactionTdsAmount;
	String accrualTdsReference;
	Boolean investmentTransactionInAis;
	LocalDate investmentTransactionForm26asBookingDate;
	
	Long realisationId;
	LocalDate realisationDate;
	Double realisationAmount;
	Double realisationInterestAmount;
	Double realisationTdsAmount;
	String realisationTdsReference;
	Boolean realisationInAis;
	LocalDate realisationForm26asBookingDate;
	
	transient Double interestAmount;
	transient Double tdsAmount;
	transient String tdsReference;
	transient Boolean inAis;
	transient LocalDate form26asBookingDate;	
	
	public String toString() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
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
				realisationId,
				realisationDate,
				realisationAmount,
				getInterestAmount(),
				getTdsAmount(),
				getTdsReference(),
				getInAis(),
				getForm26asBookingDate()
				);
	}
	public DueRealisationVO(Object[] record) {
		int colPos = 0;
		investmentId = ((Long) record[colPos++]).longValue();
		investor = (String) record[colPos++];
		productProvider = (String) record[colPos++];
		investmentIdWithProvider = (String) record[colPos++];
		productType = (String) record[colPos++];
		worth = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		
		investmentTransactionId = ((Long) record[colPos++]).longValue();
		transactionTypeDvId = ((Long) record[colPos++]).longValue();
		transactionType = (String) record[colPos++];
		taxGroup = (String) record[colPos++];
		dueDate = (LocalDate) record[colPos++];
		dueAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		investmentTransactionInterestAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		investmentTransactionTdsAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		accrualTdsReference = (String) record[colPos++];
		investmentTransactionInAis = (Boolean) record[colPos++];
		investmentTransactionForm26asBookingDate = (LocalDate) record[colPos++];
		
		realisationId = record[colPos] == null ? null : ((Long) record[colPos]).longValue(); colPos++;
		realisationDate = (LocalDate) record[colPos++];
		realisationAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationInterestAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationTdsAmount = record[colPos] == null ? null : ((BigDecimal) record[colPos]).doubleValue(); colPos++;
		realisationTdsReference = (String) record[colPos++];
		realisationInAis = (Boolean) record[colPos++];
		realisationForm26asBookingDate = (LocalDate) record[colPos++];
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

	public Double getInterestAmount() {
		return transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? investmentTransactionInterestAmount : realisationInterestAmount;
	}
	public Double getTdsAmount() {
		return transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? investmentTransactionTdsAmount : realisationTdsAmount;
	}
	public String getTdsReference() {
		return transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? accrualTdsReference : realisationTdsReference;
	}
	public Boolean getInAis() {
		return transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? investmentTransactionInAis : realisationInAis;
	}
	public LocalDate getForm26asBookingDate() {
		return transactionTypeDvId == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? investmentTransactionForm26asBookingDate : realisationForm26asBookingDate;
	}
}
