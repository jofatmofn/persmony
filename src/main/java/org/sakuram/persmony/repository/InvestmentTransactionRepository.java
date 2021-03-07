package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, Long> {
	public List<InvestmentTransaction> findByInvestmentOrderByDueDateDesc(Investment investment);
}
