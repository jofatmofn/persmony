package org.sakuram.persmony.repository;

import java.util.List;
import java.util.Optional;

import org.sakuram.persmony.bean.Isin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinRepository extends JpaRepository<Isin, String>, IsinRepositoryCustom {
	List<Isin> findAllByOrderByStockId();
	default Optional<Isin> findByIdCaseInsensitive(String isinStr) {
		if (isinStr == null) {
			return Optional.empty();
		} else {
			return findById(isinStr.toUpperCase());
		}
	}
}
