package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanSearchResultVO {
	long planId;
	String incomeDetail;
	String expenditureDetail;
	BigDecimal mappedAmount;
	IdValueVO statusIdValueVO;
}
