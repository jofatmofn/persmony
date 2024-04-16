package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.util.Constants;

public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransaction, Long> {
	public List<InvestmentTransaction> findByInvestmentOrderByDueDateDesc(Investment investment);
	public List<InvestmentTransaction> findByDueDateBetween(Date fromDate, Date toDate);
	
	@Query(nativeQuery = true, value =
			"SELECT IT.due_date, IT.id t_id, I.id, iDV.value AS investor, CONCAT(pDV.value, ' - ', COALESCE(bDV.value, 'Central')) AS provider, I.product_name, I.investment_id_with_provider, IT.due_amount, null, IT.returned_principal_amount "
			+ "FROM investment_transaction IT "
			+ "	INNER JOIN investment I ON IT.investment_fk = I.id "
			+ "	LEFT OUTER JOIN domain_value pDV ON I.product_provider_fk = pDV.id "
			+ "	LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id "
			+ "	LEFT OUTER JOIN domain_value bDV ON I.provider_branch_fk = bDV.id "
			+ "WHERE status_fk = 69 "
			+ "	AND transaction_type_fk = 73 "
			+ "ORDER BY due_date, t_id")
	public List<Object[]> findPendingTransactions();
	
	@Query(nativeQuery = true, value =
			"SELECT IT.due_date, IT.id t_id, I.id, iDV.value AS investor, CONCAT(pDV.value, ' - ', COALESCE(bDV.value, 'Central')) AS provider, I.product_name, I.investment_id_with_provider, IT.due_amount, null, IT.returned_principal_amount, MIN(R.realisation_date) AS realised_date, SUM(R.amount) AS realised_amount, SUM(R.returned_principal_amount) AS realised_principal "
			+ "FROM investment_transaction IT "
			+ "	INNER JOIN investment I ON IT.investment_fk = I.id "
			+ "	LEFT OUTER JOIN domain_value pDV ON I.product_provider_fk = pDV.id "
			+ "	LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id "
			+ "	LEFT OUTER JOIN domain_value bDV ON I.provider_branch_fk = bDV.id "
			+ "	LEFT OUTER JOIN realisation R ON R.investment_transaction_fk = IT.id "
			+ "WHERE status_fk IN (69, 71) "
			+ "	AND transaction_type_fk = 73 "
			+ "	AND COALESCE(realisation_date, IT.due_date) BETWEEN :#{#fromDate} AND :#{#toDate} "
			+ "GROUP BY IT.due_date, IT.id, I.id, iDV.value, pDV.value, bDV.value, I.product_name, I.investment_id_with_provider, IT.due_amount, IT.returned_principal_amount "
			+ "ORDER BY due_date, t_id")
	public List<Object[]> findReceiptTransactionsWithinPeriod(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
	
	@Query(nativeQuery = true, value =
			"SELECT ITO.*"
			+ " FROM investment_transaction ITO"
			+ "	WHERE transaction_type_fk = " + Constants.DVID_TRANSACTION_TYPE_RECEIPT
			+ "	AND status_fk = " + Constants.DVID_TRANSACTION_STATUS_COMPLETED
			+ "	AND due_date = (SELECT max(due_date)"
			+ "	FROM investment_transaction ITI"
			+ "	WHERE ITI.investment_fk = ITO.investment_fk"
			+ "	AND transaction_type_fk = " + Constants.DVID_TRANSACTION_TYPE_RECEIPT
			+ "	AND status_fk = " + Constants.DVID_TRANSACTION_STATUS_COMPLETED
			+ ")")
	public List<InvestmentTransaction> findLastCompletedReceipts();
	
	@Query(nativeQuery = true, value =
			"SELECT ITO.*"
			+ " FROM investment_transaction ITO"
			+ "	WHERE investment_fk = :iId"
			+ "	AND status_fk = " + Constants.DVID_TRANSACTION_STATUS_COMPLETED
			+ "	AND due_date = (SELECT max(due_date)"
			+ "	FROM investment_transaction ITI"
			+ "	WHERE ITI.investment_fk = ITO.investment_fk"
			+ "	AND status_fk = " + Constants.DVID_TRANSACTION_STATUS_COMPLETED
			+ " AND due_date < :dueDate"
			+ ")")
	public InvestmentTransaction findPreviousCompletedTransaction(@Param("iId") long iId, @Param("dueDate") Date dueDate);
}
