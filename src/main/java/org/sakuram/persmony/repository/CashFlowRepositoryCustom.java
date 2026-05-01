package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;

public interface CashFlowRepositoryCustom {
	public List<Object[]> searchCashFlows(SbAcTxnCriteriaVO cfCriteriaVO);
}
