package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.sakuram.persmony.bean.DomainValue;

public interface DomainValueRepository extends JpaRepository<DomainValue, Long> {
	public List<DomainValue> findAllByOrderByValueAsc();
}
