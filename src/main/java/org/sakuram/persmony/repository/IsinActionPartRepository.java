package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinActionPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinActionPartRepository extends JpaRepository<IsinActionPart, Long>{
	public List<IsinActionPart> findByIsinActionIsinIn(List<Isin> isinList);
}