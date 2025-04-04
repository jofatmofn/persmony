package org.sakuram.persmony.util;

import java.util.Arrays;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.valueobject.DvFlagsAccountVO;
import org.sakuram.persmony.valueobject.DvFlagsBranchVO;
import org.sakuram.persmony.valueobject.DvFlagsInvestorVO;
import org.sakuram.persmony.valueobject.DvFlagsPartyVO;
import org.sakuram.persmony.valueobject.DvFlagsSbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.DvFlagsVO;

public class DomainValueFlags {
	
	public static DvFlagsVO getDvFlagsVO(DomainValue domainValue) {
    	String flagsArr[];
    	
		if (domainValue.getFlagsCsv() == null || domainValue.getFlagsCsv().equals("")) {
			return null;
		}
		flagsArr = domainValue.getFlagsCsv().split(Constants.DV_FLAGS_LEVEL1_SEPARATOR);
		switch(domainValue.getCategory()) {
		case Constants.CATEGORY_ACCOUNT:
		case Constants.CATEGORY_DEMAT_ACCOUNT:
			DvFlagsAccountVO dvFlagsAccountVO;
			
			dvFlagsAccountVO = new DvFlagsAccountVO();
			// TODO: Validation
			if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_TYPE) {
				dvFlagsAccountVO.setAccType(flagsArr[Constants.FLAG_POSITION_ACCOUNT_TYPE]);
				
				if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_SAVINGS)) {
					if (flagsArr.length > Constants.FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID) {
						dvFlagsAccountVO.setBranchDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID]));
					}
				} else if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_FUNDS) ||
						dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_DEMAT)) {
					if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_PARTY_DVID) {
						dvFlagsAccountVO.setPartyDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_ACCOUNT_PARTY_DVID]));
					}
				}
				if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_ID) {
					dvFlagsAccountVO.setAccId(flagsArr[Constants.FLAG_POSITION_ACCOUNT_ID]);
				}
				if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_INVESTOR_DVID) {
					dvFlagsAccountVO.setInvestorDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_ACCOUNT_INVESTOR_DVID]));
				}
				if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_STATUS) {
					dvFlagsAccountVO.setOpen(flagsArr[Constants.FLAG_POSITION_ACCOUNT_STATUS].charAt(0) == Constants.ACCOUNT_STATUS_OPEN);
				}
			}
			
			return dvFlagsAccountVO;
		case Constants.CATEGORY_BRANCH:
			DvFlagsBranchVO dvFlagsBranchVO;
			dvFlagsBranchVO = new DvFlagsBranchVO();
			if (flagsArr.length > Constants.FLAG_POSITION_BRANCH_BANK_DVID) {
				dvFlagsBranchVO.setPartyDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_BRANCH_BANK_DVID]));
			}
			if (flagsArr.length > Constants.FLAG_POSITION_BRANCH_IFSC) {
				dvFlagsBranchVO.setIfsc(flagsArr[Constants.FLAG_POSITION_BRANCH_IFSC]);
			}
			return dvFlagsBranchVO;
		case Constants.CATEGORY_PARTY:
			DvFlagsPartyVO dvFlagsPartyVO;
			dvFlagsPartyVO = new DvFlagsPartyVO();
			if (flagsArr.length > Constants.FLAG_POSITION_PARTY_ROLES) {
				dvFlagsPartyVO.setRoles(flagsArr[Constants.FLAG_POSITION_PARTY_ROLES].split(Constants.DV_FLAGS_LEVEL2_SEPARATOR));
			}
			return dvFlagsPartyVO;
		case Constants.CATEGORY_INVESTOR:
			DvFlagsInvestorVO dvFlagsInvestorVO;
			dvFlagsInvestorVO = new DvFlagsInvestorVO();
			if (flagsArr.length > Constants.FLAG_POSITION_REAL_INVESTORS) {
				dvFlagsInvestorVO.setRealInvestors(Arrays
						.stream(flagsArr[Constants.FLAG_POSITION_REAL_INVESTORS].split(Constants.DV_FLAGS_LEVEL2_SEPARATOR))
						.mapToLong(Long::parseLong)
						.toArray());
			}
			return dvFlagsInvestorVO;
		case Constants.CATEGORY_TRANSACTION_CATEGORY:
			DvFlagsSbAcTxnCategoryVO dvFlagsSbAcTxnCategoryVO;
			dvFlagsSbAcTxnCategoryVO = new DvFlagsSbAcTxnCategoryVO();
			if (flagsArr.length > Constants.FLAG_POSITION_DV_CATEGORY) {
				dvFlagsSbAcTxnCategoryVO.setDvCategory(flagsArr[Constants.FLAG_POSITION_DV_CATEGORY].equals("") ? null : 
					flagsArr[Constants.FLAG_POSITION_DV_CATEGORY]);
			}
			if (flagsArr.length > Constants.FLAG_POSITION_INCOME_OR_EXPENSE) {
				dvFlagsSbAcTxnCategoryVO.setIorCString(flagsArr[Constants.FLAG_POSITION_INCOME_OR_EXPENSE]);
			}
			return dvFlagsSbAcTxnCategoryVO;
		default:
			return null;
		}
		
	}
	
}
