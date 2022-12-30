package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentDetailsVO {
	List<InvestmentTransactionVO> investmentTransactionVOList;
	List<RealisationVO> realisationVOList;
	List<SavingsAccountTransactionVO> savingsAccountTransactionVOList;
}
