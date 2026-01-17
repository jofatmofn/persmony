package org.sakuram.persmony.valueobject;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class InvestVO {
	long investorDvId;
	long productProviderDvId;
	Long providerBranchDvId;
	String productIdOfProvider;
	String investorIdWithProvider;
	String productName;
	long productTypeDvId;
	Long dematAccountDvId;
	Long taxabilityDvId;
	Boolean isAccrualApplicable;
	Long savingsAccountTransactionId;
	String investmentIdWithProvider;
	Double units;
	Double faceValue, cleanPrice, accruedInterest, charges;
	Double rateOfInterest;
	LocalDate investmentStartDate;
	LocalDate investmentEndDate;
	List<ScheduleVO> paymentScheduleVOList;
	List<ScheduleVO> receiptScheduleVOList;
	Character dynamicReceiptPeriodicity;
	List<ScheduleVO> accrualScheduleVOList;
	Long defaultBankAccountDvId;
	Long defaultTaxGroupDvId;
}
