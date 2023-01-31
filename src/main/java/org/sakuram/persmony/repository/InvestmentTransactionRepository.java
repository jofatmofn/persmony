package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, Long> {
	public List<InvestmentTransaction> findByInvestmentOrderByDueDateDesc(Investment investment);
	public List<InvestmentTransaction> findByDueDateBetween(Date fromDate, Date toDate);
	
	@Query(nativeQuery = true, value =
			"SELECT IT.due_date, IT.id t_id, I.id, iDV.value AS investor, CONCAT(pDV.value, ' - ', COALESCE(bDV.value, 'Central')) AS provider, I.product_name, I.investment_id_with_provider, IT.due_amount, IT.returned_principal_amount "
			+ "FROM investment_transaction IT "
			+ "	INNER JOIN investment I ON IT.investment_fk = I.id "
			+ "	LEFT OUTER JOIN domain_value pDV ON I.product_provider_fk = pDV.id "
			+ "	LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id "
			+ "	LEFT OUTER JOIN domain_value bDV ON I.provider_branch_fk = bDV.id "
			+ "WHERE status_fk = 69 "
			+ "	AND transaction_type_fk = 73 "
			+ "ORDER BY due_date")
	public List<Object[]> findPendingTransactions();
}
