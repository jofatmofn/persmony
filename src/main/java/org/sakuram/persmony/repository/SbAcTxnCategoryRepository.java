package org.sakuram.persmony.repository;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.SbAcTxnCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SbAcTxnCategoryRepository  extends JpaRepository<SbAcTxnCategory, Long> {
	List<SbAcTxnCategory> findBySavingsAccountTransactionTransactionDateBetweenOrderBySavingsAccountTransactionTransactionDateAscSavingsAccountTransactionIdAsc(Date fromDate, Date toDate);
}
