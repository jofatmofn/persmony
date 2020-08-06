package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sakuram.persmony.bean.Investment;

public interface InvestmentRepository extends JpaRepository<Investment, Integer> {
	
}
