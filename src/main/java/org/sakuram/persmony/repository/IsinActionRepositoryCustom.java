package org.sakuram.persmony.repository;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.valueobject.IsinActionCriteriaVO;

public interface IsinActionRepositoryCustom {
	public List<IsinAction> findIsinIndependentIsinActions(String isin, LocalDate sellDate, Long dematAccount);
	public List<Object[]> searchIsinActions(IsinActionCriteriaVO isinActionCriteriaVO);
}
