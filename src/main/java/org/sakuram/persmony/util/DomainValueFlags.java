package org.sakuram.persmony.util;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.valueobject.DvFlagsAccountVO;
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
		case Constants.CATEGORY_ACCOUNT:
			DvFlagsAccountVO dvFlagsAccountVO;
			
			dvFlagsAccountVO = new DvFlagsAccountVO();
			// TODO: Validation
			if (flagsArr.length > Constants.FLAG_POSITION_ACCOUNT_TYPE) {
				dvFlagsAccountVO.setAccType(flagsArr[Constants.FLAG_POSITION_ACCOUNT_TYPE]);
				
				if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_SAVINGS)) {
					if (flagsArr.length > Constants.FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID) {
						dvFlagsAccountVO.setBranchDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID]));
					}
					if (flagsArr.length > Constants.FLAG_POSITION_SAVINGS_ACCOUNT_ID) {
						dvFlagsAccountVO.setAccId(flagsArr[Constants.FLAG_POSITION_SAVINGS_ACCOUNT_ID]);
					}
					if (flagsArr.length > Constants.FLAG_POSITION_SAVINGS_ACCOUNT_INVESTOR_DVID) {
						dvFlagsAccountVO.setInvestorDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_SAVINGS_ACCOUNT_INVESTOR_DVID]));
					}
					if (flagsArr.length > Constants.FLAG_POSITION_SAVINGS_ACCOUNT_OPEN_OR_CLOSED) {
						dvFlagsAccountVO.setOpenOrClosed(flagsArr[Constants.FLAG_POSITION_SAVINGS_ACCOUNT_OPEN_OR_CLOSED].charAt(0));
					}
				} else if (dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_FUNDS)) {
					if (flagsArr.length > Constants.FLAG_POSITION_FUNDS_ACCOUNT_PARTY_DVID) {
						dvFlagsAccountVO.setPartyDvId(Long.parseLong(flagsArr[Constants.FLAG_POSITION_FUNDS_ACCOUNT_PARTY_DVID]));
					}
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
		default:
			return null;
		}
		
	}
	
}
