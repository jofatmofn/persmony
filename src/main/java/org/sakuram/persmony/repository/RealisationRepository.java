package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.bean.Realisation;

public interface RealisationRepository extends JpaRepository<Realisation, Long>, RealisationRepositoryCustom {

	@Query(nativeQuery = true, value =
			"SELECT R.*"
			+ " FROM realisation R"
			+ " LEFT OUTER JOIN investment_transaction IT ON R.investment_transaction_fk = IT.id"
			+ "	LEFT OUTER JOIN investment I ON IT.investment_fk = I.id"
			+ "	WHERE COALESCE(R.accounted_realisation_date,R.realisation_date) BETWEEN :#{#fromDate} AND :#{#toDate}"
			+ " AND I.investor_fk IN (:#{#investorId})"
			+ " ORDER BY COALESCE(R.accounted_realisation_date,R.realisation_date)")
	public List<Realisation> retrieveRealisationsForIt(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate, @Param("investorId") long[] investorId);
	
	List<Realisation> findByRealisationDateBetweenOrderByRealisationDateAscSavingsAccountTransactionIdAsc(LocalDate fromDate, LocalDate toDate);
}
