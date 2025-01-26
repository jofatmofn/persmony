package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;

public interface SavingsAccountTransactionRepositoryCustom {
	public List<Object[]> searchSavingsAccountTransactions(SbAcTxnCriteriaVO sbAcTxnCriteriaVO);
}
