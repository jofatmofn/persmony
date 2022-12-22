package org.sakuram.persmony.service;

import java.util.List;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.valueobject.SearchCriterionFEVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
	@Autowired
	InvestmentRepository investmentRepository;
	
	public List<Object[]> searchInvestments(List<SearchCriterionFEVO> searchCriterionFEVOList) {
		return investmentRepository.searchInvestments();
	}
}
