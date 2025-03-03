package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.sakuram.persmony.bean.SavingsAccountTransaction;

public interface SavingsAccountTransactionRepository extends JpaRepository<SavingsAccountTransaction, Long>, SavingsAccountTransactionRepositoryCustom {
	
	@Query(nativeQuery = true, value =
			"SELECT * FROM savings_account_transaction WHERE id = "
			+ "(SELECT MAX(id) FROM savings_account_transaction	WHERE bank_account_fk = :#{#bankAccountDvId} AND transaction_date = "
			+ "(SELECT MAX(transaction_date) FROM savings_account_transaction WHERE bank_account_fk = :#{#bankAccountDvId}))")
	public SavingsAccountTransaction findLastSbAcTxnInBankAccount(@Param("bankAccountDvId") long bankAccountDvId);
}
