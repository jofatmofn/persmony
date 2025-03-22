package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Investment;

public interface InvestmentRepository extends JpaRepository<Investment, Long>, InvestmentRepositoryCustom {
	List<Investment> findAllByOrderByIdAsc();

	@Query(nativeQuery = true, value =
			"SELECT *"
			+ " FROM investment"
			+ " WHERE (investment_end_date IS NULL OR CAST(:#{#fromDate} AS DATE) IS NULL OR investment_end_date >= :#{#fromDate})"
			+ " AND (investment_start_date IS NULL OR CAST(:#{#toDate} AS DATE) IS NULL OR investment_start_date <= :#{#toDate})"
			+ " ORDER BY id"
			)
	public List<Investment> retrieveInvestmentActiveWithinPeriod(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
