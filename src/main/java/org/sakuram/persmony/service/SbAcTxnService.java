package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.SavingsAccountTransaction;
import org.sakuram.persmony.bean.SbAcTxnCategory;
import org.sakuram.persmony.repository.SavingsAccountTransactionRepository;
import org.sakuram.persmony.repository.SbAcTxnCategoryRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.valueobject.DvFlagsSbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SbAcTxnService {
	@Autowired
	SavingsAccountTransactionRepository savingsAccountTransactionRepository;
	@Autowired
	SbAcTxnCategoryRepository sbAcTxnCategoryRepository;
	
	public List<SavingsAccountTransactionVO> searchSavingsAccountTransactions(SbAcTxnCriteriaVO sbAcTxnCriteriaVO) {
		List<Object[]> savingsAccountTransactionList;
		List<SavingsAccountTransactionVO> savingsAccountTransactionVOList;
		SavingsAccountTransactionVO savingsAccountTransactionVO;
		
		savingsAccountTransactionList = savingsAccountTransactionRepository.searchSavingsAccountTransactions(sbAcTxnCriteriaVO);
		savingsAccountTransactionVOList = new ArrayList<SavingsAccountTransactionVO>(savingsAccountTransactionList.size());
		for(Object[] columns : savingsAccountTransactionList) {
			savingsAccountTransactionVO = new SavingsAccountTransactionVO(columns);
			savingsAccountTransactionVOList.add(savingsAccountTransactionVO);
		}
		return savingsAccountTransactionVOList;
	}

	public List<SbAcTxnCategoryVO> fetchSbAcTxnCategories(long savingsAccountTransactionId) {
		SavingsAccountTransaction savingsAccountTransaction;
		List<SbAcTxnCategoryVO> sbAcTxnCategoryVOList;
		DvFlagsSbAcTxnCategoryVO dvFlagsSbAcTxnCategoryVO;
		
		savingsAccountTransaction = savingsAccountTransactionRepository.findById(savingsAccountTransactionId)
			.orElseThrow(() -> new AppException("Invalid Savings Account Transaction Id " + savingsAccountTransactionId, null));
		if (savingsAccountTransaction.getTransactionCategory() != null) {
			dvFlagsSbAcTxnCategoryVO = (DvFlagsSbAcTxnCategoryVO) DomainValueFlags.getDvFlagsVO(savingsAccountTransaction.getTransactionCategory());
			sbAcTxnCategoryVOList = new ArrayList<SbAcTxnCategoryVO>(1);
			sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
					null,
					new IdValueVO(savingsAccountTransaction.getTransactionCategory().getId(), savingsAccountTransaction.getTransactionCategory().getValue()),
					(dvFlagsSbAcTxnCategoryVO == null || dvFlagsSbAcTxnCategoryVO.getDvCategory().equals(Constants.CATEGORY_NONE)) ?
							new IdValueVO(null, savingsAccountTransaction.getEndAccountReference()) :
							new IdValueVO(Long.parseLong(savingsAccountTransaction.getEndAccountReference()), Constants.domainValueCache.get(Long.parseLong(savingsAccountTransaction.getEndAccountReference())).getValue()),
					savingsAccountTransaction.getAmount()));
		} else if (savingsAccountTransaction.getSbAcTxnCategoryList() != null) {
			sbAcTxnCategoryVOList = new ArrayList<SbAcTxnCategoryVO>(savingsAccountTransaction.getSbAcTxnCategoryList().size());
			for (SbAcTxnCategory sbAcTxnCategory : savingsAccountTransaction.getSbAcTxnCategoryList()) {
				dvFlagsSbAcTxnCategoryVO = (DvFlagsSbAcTxnCategoryVO) DomainValueFlags.getDvFlagsVO(sbAcTxnCategory.getTransactionCategory());
				sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
						sbAcTxnCategory.getId(), 
						new IdValueVO(sbAcTxnCategory.getTransactionCategory().getId(), sbAcTxnCategory.getTransactionCategory().getValue()),
						(dvFlagsSbAcTxnCategoryVO == null || dvFlagsSbAcTxnCategoryVO.getDvCategory().equals(Constants.CATEGORY_NONE)) ?
								new IdValueVO(null, sbAcTxnCategory.getEndAccountReference()) :
								new IdValueVO(Long.parseLong(sbAcTxnCategory.getEndAccountReference()), Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue()),
						sbAcTxnCategory.getAmount()));
			}
		} else {
			sbAcTxnCategoryVOList = new ArrayList<SbAcTxnCategoryVO>(0);
		}

		return sbAcTxnCategoryVOList;
	}
	
	public void saveSbAcTxnCategories(long savingsAccountTransactionId, List<SbAcTxnCategoryVO> sbAcTxnCategoryVOFromUiList) {
		SavingsAccountTransaction savingsAccountTransaction;
		DomainValue transactionCategoryDvUi;
		boolean isFound;
		
		savingsAccountTransaction = savingsAccountTransactionRepository.findById(savingsAccountTransactionId)
			.orElseThrow(() -> new AppException("Invalid Savings Account Transaction Id " + savingsAccountTransactionId, null));
		
		if (sbAcTxnCategoryVOFromUiList.size() == 0) {
			savingsAccountTransaction.setTransactionCategory(null);
			savingsAccountTransaction.setEndAccountReference(null);
			savingsAccountTransaction.getSbAcTxnCategoryList().clear();
		} else if (sbAcTxnCategoryVOFromUiList.size() == 1) {
			savingsAccountTransaction.setTransactionCategory(Constants.domainValueCache.get(sbAcTxnCategoryVOFromUiList.get(0).getTransactionCategory().getId()));
			savingsAccountTransaction.setEndAccountReference(endAccountReferenceUiToDb(sbAcTxnCategoryVOFromUiList.get(0).getEndAccountReference()));
			savingsAccountTransaction.getSbAcTxnCategoryList().clear();
		} else {
			savingsAccountTransaction.setTransactionCategory(null);
			savingsAccountTransaction.setEndAccountReference(null);
			for (int i = 0; i < savingsAccountTransaction.getSbAcTxnCategoryList().size(); i++) {
				SbAcTxnCategory sbAcTxnCategoryDeleted = savingsAccountTransaction.getSbAcTxnCategoryList().get(i);
				if (!sbAcTxnCategoryVOFromUiList.stream().anyMatch(sbAcTxnCategoryVOFromUi -> sbAcTxnCategoryVOFromUi.getSbAcTxnCategoryId().equals(sbAcTxnCategoryDeleted.getId()))) {
					sbAcTxnCategoryRepository.delete(sbAcTxnCategoryDeleted);
					savingsAccountTransaction.getSbAcTxnCategoryList().remove(sbAcTxnCategoryDeleted); // Because of the bi-directional relationship, this additional step is required
					i--;
				}
			}
			for (SbAcTxnCategoryVO sbAcTxnCategoryVO : sbAcTxnCategoryVOFromUiList) {
				transactionCategoryDvUi = Constants.domainValueCache.get(sbAcTxnCategoryVO.getTransactionCategory().getId());
				if (sbAcTxnCategoryVO.getSbAcTxnCategoryId() == null) {
					SbAcTxnCategory sbAcTxnCategoryInserted = new SbAcTxnCategory(
							savingsAccountTransaction,
							transactionCategoryDvUi,
							endAccountReferenceUiToDb(sbAcTxnCategoryVO.getEndAccountReference()),
							sbAcTxnCategoryVO.getAmount()
							);
					sbAcTxnCategoryRepository.save(sbAcTxnCategoryInserted);
				} else {
					isFound = false;
					for (SbAcTxnCategory sbAcTxnCategoryUpdated : savingsAccountTransaction.getSbAcTxnCategoryList()) {
						if (sbAcTxnCategoryVO.getSbAcTxnCategoryId().equals(sbAcTxnCategoryUpdated.getId())) {
							if (sbAcTxnCategoryUpdated.getTransactionCategory().getId() != sbAcTxnCategoryVO.getTransactionCategory().getId() ||
									!sbAcTxnCategoryUpdated.getEndAccountReference().equals(sbAcTxnCategoryVO.getEndAccountReference()) ||
									!sbAcTxnCategoryUpdated.getAmount().equals(sbAcTxnCategoryVO.getAmount())) {
								sbAcTxnCategoryUpdated.setTransactionCategory(transactionCategoryDvUi);
								sbAcTxnCategoryUpdated.setEndAccountReference(endAccountReferenceUiToDb(sbAcTxnCategoryVO.getEndAccountReference()));
								sbAcTxnCategoryUpdated.setAmount(sbAcTxnCategoryVO.getAmount());
							}
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						throw new AppException("SbAcTxnCategory " + sbAcTxnCategoryVO.getSbAcTxnCategoryId() + " not found", null);
					}
				}
			}
		}
	}
	
	private String endAccountReferenceUiToDb(IdValueVO idValueVO) {
		if (idValueVO == null) {
			return null;
		} else if (idValueVO.getId() != null) {
			return idValueVO.getId().toString();
		} else if (idValueVO.getValue() != null) {
			return idValueVO.getValue();
		} else {
			throw new AppException("Unexpected End Account Reference - both id and value are NULLs", null);
		}
	}
}
