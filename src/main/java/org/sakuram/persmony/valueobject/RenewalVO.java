package org.sakuram.persmony.valueobject;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RenewalVO {
	// Being extended
	long investmentId;
	// New
	String investmentIdWithProvider;
	Double faceValue;
	Double rateOfInterest;
	Date investmentEndDate;
	List<ScheduleVO> paymentScheduleVOList;
	List<ScheduleVO> receiptScheduleVOList;
	List<ScheduleVO> accrualScheduleVOList;
}
