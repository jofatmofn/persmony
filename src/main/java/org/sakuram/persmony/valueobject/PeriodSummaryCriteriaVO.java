package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PeriodSummaryCriteriaVO {
	LocalDate fromDate;
	LocalDate toDate;
}
