package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.DomainValueVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MiscService {

	@Autowired
	DomainValueRepository domainValueRepository;
	
    public void loadCache() {
    	List<Long> categoryDvList;
    	
    	System.out.println("Loading Cache");
    	Constants.domainValueCache = new HashMap<Long, DomainValue>();
    	Constants.categoryDvCache = new HashMap<String, List<Long>>();
    	
    	for(DomainValue domainValue : domainValueRepository.findAll()) {
    		Constants.domainValueCache.put(domainValue.getId(), domainValue);
    		if (Constants.categoryDvCache.containsKey(domainValue.getCategory())) {
    			categoryDvList = Constants.categoryDvCache.get(domainValue.getCategory());
    		} else {
    			categoryDvList = new ArrayList<Long>();
    			Constants.categoryDvCache.put(domainValue.getCategory(), categoryDvList);
    		}
    		categoryDvList.add(domainValue.getId());
    	}
    }
    
    public List<DomainValueVO> fetchDvOfCategory(String category) {
    	List<DomainValueVO> domainValueVOList;
    	DomainValue domainValue;
    	
    	domainValueVOList = new ArrayList<DomainValueVO>();
    	for (Long dvId : Constants.categoryDvCache.get(category)) {
    		domainValue = Constants.domainValueCache.get(dvId);
    		domainValueVOList.add(new DomainValueVO(domainValue.getId(), domainValue.getCategory(), domainValue.getValue()));
    	}
    	return domainValueVOList;
    }
}
