package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sakuram.persmony.bean.InvestmentTransaction;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, Long> {
	
}
