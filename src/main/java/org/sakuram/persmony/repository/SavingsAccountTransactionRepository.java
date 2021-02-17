package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sakuram.persmony.bean.SavingsAccountTransaction;

public interface SavingsAccountTransactionRepository extends JpaRepository<SavingsAccountTransaction, Long> {
	
}
