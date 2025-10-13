package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.IsinActionMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinActionMatchRepository extends JpaRepository<IsinActionMatch, Long>{
	public IsinActionMatch findByFromIsinActionAndToIsinAction(IsinAction fromIsinAction, IsinAction toIsinAction);
	public List<IsinActionMatch> findByFromIsinActionIsinInOrToIsinActionIsinIn(List<Isin> isinList, List<Isin> isinList2);
}
