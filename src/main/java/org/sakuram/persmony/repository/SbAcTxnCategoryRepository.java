package org.sakuram.persmony.repository;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.bean.SbAcTxnCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SbAcTxnCategoryRepository  extends JpaRepository<SbAcTxnCategory, Long> {
	List<SbAcTxnCategory> findBySavingsAccountTransactionTransactionDateBetweenOrderBySavingsAccountTransactionTransactionDateAscSavingsAccountTransactionIdAsc(LocalDate fromDate, LocalDate toDate);
}
