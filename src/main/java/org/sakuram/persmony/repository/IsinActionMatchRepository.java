package org.sakuram.persmony.repository;

import org.sakuram.persmony.bean.IsinActionMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinActionMatchRepository extends JpaRepository<IsinActionMatch, Long>{
	// public List<IsinActionMatch> findByFromIsinActionIsinInOrToIsinActionIsinIn(List<Isin> isinList, List<Isin> isinList2);
}
