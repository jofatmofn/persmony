package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.List;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.valueobject.InvestmentVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
	@Autowired
	InvestmentRepository investmentRepository;
	
	public List<InvestmentVO> searchInvestments(List<SearchCriterionVO> searchCriterionVOList) {
		List<Object[]> investments;
		List<InvestmentVO> investmentVOList;
		InvestmentVO investmentVO;
		
		investments = investmentRepository.searchInvestments(searchCriterionVOList);
		investmentVOList = new ArrayList<InvestmentVO>(investments.size());
		for(Object[] columns : investments) {
			investmentVO = new InvestmentVO(columns);
			investmentVOList.add(investmentVO);
		}
		return investmentVOList;
	}
}
