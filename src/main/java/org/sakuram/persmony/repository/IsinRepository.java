package org.sakuram.persmony.repository;

import org.sakuram.persmony.bean.Isin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinRepository extends JpaRepository<Isin, String> {

}
