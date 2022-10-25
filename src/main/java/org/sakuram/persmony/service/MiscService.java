package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.valueobject.DvFlagsBankAccVO;
import org.sakuram.persmony.valueobject.DvFlagsBranchVO;
import org.sakuram.persmony.valueobject.IdValueVO;
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
    
    public List<IdValueVO> fetchDvsOfCategory(String category) {
    	List<IdValueVO> idValueVOList;
    	DomainValue domainValue;
    	
    	idValueVOList = new ArrayList<IdValueVO>();
    	for (Long dvId : Constants.categoryDvCache.get(category)) {
    		String label;
    		domainValue = Constants.domainValueCache.get(dvId);
    		switch(category) {
    		case Constants.CATEGORY_BANK_ACCOUNT:
    			try {
    				DvFlagsBankAccVO dvFlagsBankAccVO;
	    			DvFlagsBranchVO dvFlagsBranchVO;
	    			DomainValue branchDv, partyDv;
	    			dvFlagsBankAccVO = (DvFlagsBankAccVO) DomainValueFlags.getDvFlagsVO(domainValue);
	    			branchDv = Constants.domainValueCache.get(dvFlagsBankAccVO.getBranchDvId());
	    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
	    			partyDv = Constants.domainValueCache.get(dvFlagsBranchVO.getPartyDvId());
	    			label = dvFlagsBankAccVO.getAccType() + "::" +
	    					partyDv.getValue() + "::" +
	    					branchDv.getValue() + "::" +
	    					dvFlagsBankAccVO.getAccId();
    			} catch (Exception e) {
    				throw new AppException("Invalid Configuration of Bank Account " + dvId, null);
    			}
    			break;
    		case Constants.CATEGORY_BRANCH:
    			try {
        			DvFlagsBranchVO dvFlagsBranchVO;
        			DomainValue branchDv, partyDv;
        			branchDv = Constants.domainValueCache.get(dvId);
        			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
        			partyDv = Constants.domainValueCache.get(dvFlagsBranchVO.getPartyDvId());
        			label = partyDv.getValue() + "::" +
        					branchDv.getValue();
        			} catch (Exception e) {
        				throw new AppException("Invalid Configuration of Branch " + dvId, null);
        			}
    			break;
    		default:
    				label = domainValue.getValue();
    		}
    		idValueVOList.add(new IdValueVO(domainValue.getId(), label));
    	}
    	return idValueVOList;
    }
    
    public List<Long> fetchBranchDvsOfBank(long partyDvId) {
    	List<Long> branchDVIdList;
    	
    	branchDVIdList = new ArrayList<Long>();
    	for (Long dvId : Constants.categoryDvCache.get(Constants.CATEGORY_BRANCH)) {
			try {
    			DvFlagsBranchVO dvFlagsBranchVO;
    			DomainValue branchDv;
    			branchDv = Constants.domainValueCache.get(dvId);
    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
    			if (dvFlagsBranchVO.getPartyDvId() == partyDvId) {
    				branchDVIdList.add(dvId);
    			}
			} catch (Exception e) {
				throw new AppException("Invalid Configuration of Bank Account " + dvId, null);
			}
    	}
    	return branchDVIdList;
    }
    
}
