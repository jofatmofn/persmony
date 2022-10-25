package org.sakuram.persmony.util;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.valueobject.DvFlagsBankAccVO;
import org.sakuram.persmony.valueobject.DvFlagsBranchVO;
import org.sakuram.persmony.valueobject.DvFlagsPartyVO;
import org.sakuram.persmony.valueobject.DvFlagsVO;

public class DomainValueFlags {
	
	public static DvFlagsVO getDvFlagsVO(DomainValue domainValue) {
    	String flagsArr[];
    	
		if (domainValue.getFlagsCsv() == null || domainValue.getFlagsCsv().equals("")) {
			return null;
		}
		flagsArr = domainValue.getFlagsCsv().split(Constants.DV_FLAGS_LEVEL1_SEPARATOR);
		switch(domainValue.getCategory()) {
		case Constants.CATEGORY_BANK_ACCOUNT:
			DvFlagsBankAccVO dvFlagsBankAccVO;
			
			dvFlagsBankAccVO = new DvFlagsBankAccVO();
			// TODO: Validation
			if (flagsArr.length > Constants.FLAG_POSITION_BANK_ACCOUNT_TYPE) {
				dvFlagsBankAccVO.setAccType(flagsArr[Constants.FLAG_POSITION_BANK_ACCOUNT_TYPE]);
			}
			if (flagsArr.length > Constants.FLAG_POSITION_BANK_ACCOUNT_BRANCH_DVID) {
				dvFlagsBankAccVO.setBranchDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_BANK_ACCOUNT_BRANCH_DVID]));
			}
			if (flagsArr.length > Constants.FLAG_POSITION_BANK_ACCOUNT_ID) {
				dvFlagsBankAccVO.setAccId(flagsArr[Constants.FLAG_POSITION_BANK_ACCOUNT_ID]);
			}
			if (flagsArr.length > Constants.FLAG_POSITION_BANK_ACCOUNT_INVESTOR_DVID) {
				dvFlagsBankAccVO.setInvestorDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_BANK_ACCOUNT_INVESTOR_DVID]));
			}
			if (flagsArr.length > Constants.FLAG_POSITION_BANK_ACCOUNT_OPEN_OR_CLOSED) {
				dvFlagsBankAccVO.setOpenOrClosed(flagsArr[Constants.FLAG_POSITION_BANK_ACCOUNT_OPEN_OR_CLOSED].charAt(0));
			}
			return dvFlagsBankAccVO;
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
		default:
			return null;
		}
		
	}
	
}
