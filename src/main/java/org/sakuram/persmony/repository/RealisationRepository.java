package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sakuram.persmony.bean.Realisation;

public interface RealisationRepository extends JpaRepository<Realisation, Long> {
	
}
