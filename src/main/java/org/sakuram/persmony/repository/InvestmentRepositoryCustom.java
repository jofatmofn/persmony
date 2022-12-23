package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

public interface InvestmentRepositoryCustom {
	public List<Investment> executeDynamicQuery(String query);
	public List<Object[]> searchInvestments(List<SearchCriterionVO> searchCriterionVOList);
}
