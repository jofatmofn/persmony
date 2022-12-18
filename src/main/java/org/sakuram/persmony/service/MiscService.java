package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.LongStream;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.valueobject.DvFlagsAccountVO;
import org.sakuram.persmony.valueobject.DvFlagsBranchVO;
import org.sakuram.persmony.valueobject.DvFlagsInvestorVO;
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
    	List<Long> categoryDvIdList;
    	
    	System.out.println("Loading Cache");
    	Constants.domainValueCache = new HashMap<Long, DomainValue>();
    	Constants.categoryDvIdCache = new HashMap<String, List<Long>>();
    	
    	for(DomainValue domainValue : domainValueRepository.findAll()) {
    		Constants.domainValueCache.put(domainValue.getId(), domainValue);
    		if (Constants.categoryDvIdCache.containsKey(domainValue.getCategory())) {
    			categoryDvIdList = Constants.categoryDvIdCache.get(domainValue.getCategory());
    		} else {
    			categoryDvIdList = new ArrayList<Long>();
    			Constants.categoryDvIdCache.put(domainValue.getCategory(), categoryDvIdList);
    		}
    		categoryDvIdList.add(domainValue.getId());
    	}
    }
    
    public List<IdValueVO> fetchDvsOfCategory(String category) {
    	List<IdValueVO> idValueVOList;
    	DomainValue domainValue;
    	
    	idValueVOList = new ArrayList<IdValueVO>();
    	for (Long dvId : Constants.categoryDvIdCache.get(category)) {
    		String label;
    		domainValue = Constants.domainValueCache.get(dvId);
    		label = "? ERROR ?";
    		switch(category) {
    		case Constants.CATEGORY_ACCOUNT:
    			try {
    				DvFlagsAccountVO dvFlagsAccountVO;
	    			DvFlagsBranchVO dvFlagsBranchVO;
	    			DomainValue branchDv, partyDv;
	    			dvFlagsAccountVO = (DvFlagsAccountVO) DomainValueFlags.getDvFlagsVO(domainValue);
	    			if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_SAVINGS)) {
		    			branchDv = Constants.domainValueCache.get(dvFlagsAccountVO.getBranchDvId());
		    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
		    			partyDv = Constants.domainValueCache.get(dvFlagsBranchVO.getPartyDvId());
		    			label = dvFlagsAccountVO.getAccType() + "::" +
		    					partyDv.getValue() + "::" +
		    					branchDv.getValue() + "::" +
		    					dvFlagsAccountVO.getAccId();
	    			} else if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_FUNDS)) {
		    			partyDv = Constants.domainValueCache.get(dvFlagsAccountVO.getPartyDvId());
		    			label =  dvFlagsAccountVO.getAccType() + "::" +
		    					partyDv.getValue();
    				}
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
    
    public List<Long> fetchBranchDvIdsOfParty(long partyDvId) {
    	List<Long> branchDVIdList;
    	
    	branchDVIdList = new ArrayList<Long>();
    	for (Long dvId : Constants.categoryDvIdCache.get(Constants.CATEGORY_BRANCH)) {
			try {
    			DvFlagsBranchVO dvFlagsBranchVO;
    			DomainValue branchDv;
    			branchDv = Constants.domainValueCache.get(dvId);
    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
    			if (dvFlagsBranchVO.getPartyDvId() == partyDvId) {
    				branchDVIdList.add(dvId);
    			}
			} catch (Exception e) {
				throw new AppException("Invalid Configuration of Branch " + dvId, null);
			}
    	}
    	return branchDVIdList;
    }
    
    public List<IdValueVO> fetchBranchesOfParty(long partyDvId) {
    	List<IdValueVO> idValueVOList;
    	
    	idValueVOList = new ArrayList<IdValueVO>();
    	for (Long dvId : Constants.categoryDvIdCache.get(Constants.CATEGORY_BRANCH)) {
			try {
    			DvFlagsBranchVO dvFlagsBranchVO;
    			DomainValue branchDv;
    			branchDv = Constants.domainValueCache.get(dvId);
    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
    			if (dvFlagsBranchVO.getPartyDvId() == partyDvId) {
    				idValueVOList.add(new IdValueVO(branchDv.getId(), branchDv.getValue()));
    			}
			} catch (Exception e) {
				throw new AppException("Invalid Configuration of Branch " + dvId, null);
			}
    	}
    	return idValueVOList;
    }
    
    public List<IdValueVO> fetchAccountsOfInvestor(long investorDvId) {
    	List<IdValueVO> idValueVOList;
    	DvFlagsInvestorVO dvFlagsInvestorVO;
		DomainValue investorDv;
		long investors[];
    	
		investorDv = Constants.domainValueCache.get(investorDvId);
		dvFlagsInvestorVO = (DvFlagsInvestorVO) DomainValueFlags.getDvFlagsVO(investorDv);
		if (dvFlagsInvestorVO == null) {
			investors = new long[1];
		} else {
			investors = new long[dvFlagsInvestorVO.getRealInvestors().length + 1];
			System.arraycopy(dvFlagsInvestorVO.getRealInvestors(), 0, investors, 1, dvFlagsInvestorVO.getRealInvestors().length);
		}
		investors[0] = investorDvId;
		
    	idValueVOList = new ArrayList<IdValueVO>();
    	for (Long dvId : Constants.categoryDvIdCache.get(Constants.CATEGORY_ACCOUNT)) {
			try {
    			DvFlagsAccountVO dvFlagsAccountVO;
    			DomainValue accountDv;
    			accountDv = Constants.domainValueCache.get(dvId);
    			dvFlagsAccountVO = (DvFlagsAccountVO) DomainValueFlags.getDvFlagsVO(accountDv);
    			if (LongStream.of(investors).anyMatch(x -> x == dvFlagsAccountVO.getInvestorDvId()) && dvFlagsAccountVO.isOpen()) {
    				idValueVOList.add(new IdValueVO(accountDv.getId(), accountDv.getValue()));
    			}
			} catch (Exception e) {
				throw new AppException("Invalid Configuration of Account " + dvId, null);
			}
    	}
    	return idValueVOList;
    }
    
}
