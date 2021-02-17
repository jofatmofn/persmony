package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sakuram.persmony.bean.DomainValue;

public interface DomainValueRepository extends JpaRepository<DomainValue, Long> {
	
}
