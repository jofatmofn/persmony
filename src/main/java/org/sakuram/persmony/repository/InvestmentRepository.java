package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Investment;

public interface InvestmentRepository extends JpaRepository<Investment, Long>, InvestmentRepositoryCustom {
	List<Investment> findAllByOrderByIdAsc();

	public List<Investment> findByInvestmentEndDateGreaterThanEqualAndInvestmentStartDateLessThanEqual(Date investmentStartDate, Date investmentEndDate);
}
