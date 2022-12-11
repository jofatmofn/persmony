package org.sakuram.persmony.valueobject;

import java.sql.Date;
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
	Long bankDvId;
	String investmentIdWithProvider;
	Float faceValue, cleanPrice, accruedInterest, charges;
	Float rateOfInterest;
	Date productEndDate;
	List<ScheduleVO> paymentScheduleVOList;
	List<ScheduleVO> receiptScheduleVOList;
	Character dynamicReceiptPeriodicity;
	List<ScheduleVO> accrualScheduleVOList;
}
