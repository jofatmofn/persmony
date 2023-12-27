package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class DuesVO {
	long investmentId;
	List<ScheduleVO> paymentScheduleVOList;
	List<ScheduleVO> receiptScheduleVOList;
	List<ScheduleVO> accrualScheduleVOList;
}
