package org.sakuram.persmony.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.util.FlaggedEnum;
import org.sakuram.persmony.valueobject.DvFlagsAccountVO;
import org.sakuram.persmony.valueobject.DvFlagsBranchVO;
import org.sakuram.persmony.valueobject.DvFlagsInvestorVO;
import org.sakuram.persmony.valueobject.DvFlagsSbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestmentTransaction2VO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.IsinActionSpecVO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MiscService {

	@Autowired
	DomainValueRepository domainValueRepository;

	@Autowired
	InvestmentTransactionRepository investmentTransactionRepository;
	
    public void loadCache() {
    	List<Long> categoryDvIdList;
    	
    	System.out.println("Loading Cache");
    	Constants.domainValueCache = new HashMap<Long, DomainValue>();
    	Constants.categoryDvIdCache = new HashMap<String, List<Long>>();
    	
    	for(DomainValue domainValue : domainValueRepository.findAllByOrderByValueAsc()) {
    		Constants.domainValueCache.put(domainValue.getId(), domainValue);
    		if (Constants.categoryDvIdCache.containsKey(domainValue.getCategory())) {
    			categoryDvIdList = Constants.categoryDvIdCache.get(domainValue.getCategory());
    		} else {
    			categoryDvIdList = new ArrayList<Long>();
    			Constants.categoryDvIdCache.put(domainValue.getCategory(), categoryDvIdList);
    		}
    		categoryDvIdList.add(domainValue.getId());
    	}
    	
    	try (CSVParser csvParser = new CSVParser(new BufferedReader(new InputStreamReader(new FileInputStream(new File(getClass().getClassLoader().getResource("IA Spec.csv").toURI())), "UTF-8")), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
    		for (CSVRecord csvRecord : csvParser.getRecords()) {
    	    	List<String> cellContentList;
    	    	Long actionTypeDvId;
    	    	List<IsinActionEntrySpecVO> isinActionEntrySpecVOList;
    	    	IsinActionEntrySpecVO isinActionEntrySpecVO;
    	    	int col;
    	    	
    	    	cellContentList = new ArrayList<String>();
    			csvRecord.iterator().forEachRemaining(cellContentList::add);
    			cellContentList.replaceAll(String::trim);
    			actionTypeDvId = Long.valueOf(cellContentList.get(0));
    			if (Constants.ISIN_ACTION_SPEC_MAP.containsKey(actionTypeDvId)) {
    				isinActionEntrySpecVOList = Constants.ISIN_ACTION_SPEC_MAP.get(actionTypeDvId).getIsinActionEntrySpecVOList();
    			} else {
    				isinActionEntrySpecVOList = new ArrayList<IsinActionEntrySpecVO>();
    				Constants.ISIN_ACTION_SPEC_MAP.put(actionTypeDvId, new IsinActionSpecVO(isinActionEntrySpecVOList));
    			}
    			col = 0;
    			isinActionEntrySpecVO = new IsinActionEntrySpecVO(
    					Long.valueOf(cellContentList.get(col++)),
    					cellContentList.get(col++),
    					Long.valueOf(cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IAIsinType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IASettlementDateType.class, cellContentList.get(col++)),
    					Boolean.valueOf(cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IAQuantityType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IAPriceType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IAFifoMappingType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IALotCreationType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IALotDateType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IALotQuantityType.class, cellContentList.get(col++)),
    					FlaggedEnum.fromFlag(IsinActionEntrySpecVO.IALotPriceType.class, cellContentList.get(col++))
    					);
    			isinActionEntrySpecVOList.add(isinActionEntrySpecVO);
    			/* for (String cellContent: cellContentList) {
    				System.out.print("<<" + cellContent + ">>");
    			} */
    		}
    		
    	}
    	catch(IOException e) {
    		throw new AppException("Error handling IA Spec CSV", e);
    	} catch (URISyntaxException e1) {
    		throw new AppException("Incorrect setup for IA Spec CSV", e1);
		}

    }
    
    public List<IdValueVO> fetchDvsOfCategory(String category) {
    	return fetchDvsOfCategory(category, true, true);
    }
    
    public List<IdValueVO> fetchDvsOfCategory(String category, boolean enhanced, boolean isOpenOnly) {
    	List<IdValueVO> idValueVOList;
    	
    	idValueVOList = new ArrayList<IdValueVO>();
    	for (String oneCategory : category.split("\\+")) {
    		idValueVOList.addAll(fetchDvsOfOneCategory(oneCategory, enhanced, isOpenOnly));
    	}
    	
    	return idValueVOList;
    }
    
    private List<IdValueVO> fetchDvsOfOneCategory(String category, boolean enhanced, boolean isOpenOnly) {
    	List<IdValueVO> idValueVOList;
    	DomainValue domainValue;
    	
    	idValueVOList = new ArrayList<IdValueVO>();
    	if (category.equals(Constants.CATEGORY_PRIMARY_INVESTOR)) {
        	for (Long dvId : Constants.categoryDvIdCache.get(Constants.CATEGORY_INVESTOR)) {
    			DvFlagsInvestorVO dvFlagsInvestorVO;
        		domainValue = Constants.domainValueCache.get(dvId);
    			dvFlagsInvestorVO = (DvFlagsInvestorVO) DomainValueFlags.getDvFlagsVO(domainValue);
    			if (dvFlagsInvestorVO == null) {
    				idValueVOList.add(new IdValueVO(domainValue.getId(), domainValue.getValue()));
    			}
        	}
    		return idValueVOList;
    	}
    	for (Long dvId : Constants.categoryDvIdCache.get(category)) {
    		String label;
    		domainValue = Constants.domainValueCache.get(dvId);
			label = domainValue.getValue();
    		if (enhanced) {
	    		switch(category) {
	    		case Constants.CATEGORY_ACCOUNT:
	    		case Constants.CATEGORY_DEMAT_ACCOUNT:
	    			try {
	    				DvFlagsAccountVO dvFlagsAccountVO;
		    			DvFlagsBranchVO dvFlagsBranchVO;
		    			DvFlagsInvestorVO dvFlagsInvestorVO;
		    			DomainValue branchDv, partyDv, investorDv;
		    			long investors[];
		    			StringBuffer labelSB;
		    			
		    			labelSB = new StringBuffer();
		    			labelSB.append(label);
		    			labelSB.append("::");
		    			dvFlagsAccountVO = (DvFlagsAccountVO) DomainValueFlags.getDvFlagsVO(domainValue);
		    			if (isOpenOnly && !dvFlagsAccountVO.isOpen())
		    				continue;
		    			investorDv = Constants.domainValueCache.get(dvFlagsAccountVO.getInvestorDvId());
		    			dvFlagsInvestorVO = (DvFlagsInvestorVO) DomainValueFlags.getDvFlagsVO(investorDv);
		    			if (dvFlagsInvestorVO == null) {
		    				investors = new long[1];
			    			investors[0] = dvFlagsAccountVO.getInvestorDvId();
		    			} else {
		    				investors = new long[dvFlagsInvestorVO.getRealInvestors().length];
		    				System.arraycopy(dvFlagsInvestorVO.getRealInvestors(), 0, investors, 0, dvFlagsInvestorVO.getRealInvestors().length);
		    			}
		    			for (int ind = 0; ind < investors.length; ind++) {
			    			labelSB.append(ind == 0 ? "" : " & ");
			    			labelSB.append(Constants.domainValueCache.get(investors[ind]).getValue());
		    			}
		    			labelSB.append("::");
		    			labelSB.append(dvFlagsAccountVO.getAccType());
		    			labelSB.append("::");
		    			if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_SAVINGS)) {
			    			branchDv = Constants.domainValueCache.get(dvFlagsAccountVO.getBranchDvId());
			    			dvFlagsBranchVO = (DvFlagsBranchVO) DomainValueFlags.getDvFlagsVO(branchDv);
			    			partyDv = Constants.domainValueCache.get(dvFlagsBranchVO.getPartyDvId());
			    			labelSB.append(partyDv.getValue());
			    			labelSB.append("::");
			    			labelSB.append(branchDv.getValue());
		    			} else if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_FUNDS) ||
								dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_DEMAT)) {
			    			partyDv = Constants.domainValueCache.get(dvFlagsAccountVO.getPartyDvId());
			    			labelSB.append(partyDv.getValue());
	    				}
		    			labelSB.append("::");
		    			labelSB.append(dvFlagsAccountVO.getAccId());
		    			label = labelSB.toString();
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
	    		}
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
    
    public InvestmentTransaction2VO fetchInvestmentTransaction(long investmentTransactionId) {
    	InvestmentTransaction investmentTransaction = investmentTransactionRepository.findById(investmentTransactionId)
    			.orElseThrow(() -> new AppException("Invalid Investment Transaction Id " + investmentTransactionId, null));
    	return new InvestmentTransaction2VO(
    			investmentTransaction.getInvestment().getId(),
    			investmentTransaction.getId(),
    			investmentTransaction.getTransactionType().getId(),
    			investmentTransaction.getTransactionType().getValue(),
    			investmentTransaction.getDueDate(),
    			investmentTransaction.getDueAmount(),
    			investmentTransaction.getStatus().getId(),
    			investmentTransaction.getStatus().getValue(),
    			new IdValueVO(investmentTransaction.getInvestment().getDefaultBankAccount()),
				new IdValueVO(investmentTransaction.getInvestment().getDefaultTaxGroup()),
				investmentTransaction.getInvestment().getInvestor().getValue(),
				investmentTransaction.getInvestment().getProductProvider().getValue(),
				investmentTransaction.getInvestment().getProductType().getValue()
				);
    }
    
    public RealisationVO fetchRealisationAmountSummary(InvestmentTransaction investmentTransaction) {
    	double returnedPrincipalAmount, interestAmount, tdsAmount, amount;
    	
    	returnedPrincipalAmount = 0;
    	interestAmount = 0;
    	tdsAmount = 0;
    	amount = 0;
		for (Realisation realisation : investmentTransaction.getRealisationList()) {
			returnedPrincipalAmount += ObjectUtils.defaultIfNull(realisation.getReturnedPrincipalAmount(), 0).doubleValue();
			interestAmount += ObjectUtils.defaultIfNull(realisation.getInterestAmount(), 0).doubleValue();
			tdsAmount += ObjectUtils.defaultIfNull(realisation.getTdsAmount(), 0).doubleValue();
			amount += ObjectUtils.defaultIfNull(realisation.getAmount(), 0).doubleValue();
		}
		return new RealisationVO(
				0,
				investmentTransaction.getId(),
				null,
				null,
				null,
				null,
				amount,
				returnedPrincipalAmount,
				interestAmount,
				tdsAmount,
				null
				);
    }
    
    public Map<Long, String> fetchDvCategoriesOfTxnCategories() {
    	Map<Long, String> txnCatToDvCatMap;
    	List<Long> categoryDvIdList;
    	
    	categoryDvIdList = Constants.categoryDvIdCache.get(Constants.CATEGORY_TRANSACTION_CATEGORY);
    	txnCatToDvCatMap = new HashMap<Long, String>(categoryDvIdList.size());
		for (Long dvId : categoryDvIdList) {
			try {
    			DvFlagsSbAcTxnCategoryVO dvFlagsSbAcTxnCategoryVO;
    			DomainValue transactionCategoryDv;
    			transactionCategoryDv = Constants.domainValueCache.get(dvId);
    			dvFlagsSbAcTxnCategoryVO = (DvFlagsSbAcTxnCategoryVO) DomainValueFlags.getDvFlagsVO(transactionCategoryDv);
    			txnCatToDvCatMap.put(transactionCategoryDv.getId(), dvFlagsSbAcTxnCategoryVO.getDvCategory());
			} catch (Exception e) {
				throw new AppException("Invalid Configuration of Transaction Category " + dvId, e);
	    	}
		} 
    	
		return txnCatToDvCatMap;
    }

}
