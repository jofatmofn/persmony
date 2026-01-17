package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleVO {
	private LocalDate dueDate;
	private Double dueAmount;
	private Double returnedPrincipalAmount;
	private Double interestAmount;
	private Double tdsAmount;

}
