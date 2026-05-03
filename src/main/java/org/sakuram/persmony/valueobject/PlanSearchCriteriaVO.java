package org.sakuram.persmony.valueobject;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlanSearchCriteriaVO {
	LocalDate incomeFromDate, incomeToDate, expenditureFromDate, expenditureToDate;
	Long incomeBankAccountOrInvestorDvId, expenditureBankAccountOrInvestorDvId;
	BigDecimal mappedFromAmount, mappedToAmount;
	boolean isStatusPending, isStatusCompleted, isStatusCancelled;

}
