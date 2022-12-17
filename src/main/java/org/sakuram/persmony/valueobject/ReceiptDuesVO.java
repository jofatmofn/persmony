package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ReceiptDuesVO {
	long investmentId;
	List<ScheduleVO> receiptScheduleVOList;
}
