package org.sakuram.persmony.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class SbAcTxnService {
	@Autowired
	SavingsAccountTransactionRepository savingsAccountTransactionRepository;
	@Autowired
	SbAcTxnCategoryRepository sbAcTxnCategoryRepository;
	
	public void importSavingsAccountTransactions(long bankAccountDvId, MultipartFile multipartFile) throws IOException, ParseException {
    	List<String> cellContentList;
    	String transactionDateStr, valueDateStr, reference, narration, transactionId, utrNumber, remitterBranch, transactionTime, endAccountReference;
    	Double amount, balance;
    	Long bookingDvId, transactionCodeDvId, costCenterDvId, voucherTypeDvId, transactionCategoryDvId;
    	Integer branchCode;
    	
		try (CSVParser csvParser = new CSVParser(new BufferedReader(new InputStreamReader(multipartFile.getInputStream())), CSVFormat.DEFAULT)) {
			for (CSVRecord csvRecord : csvParser.getRecords()) {
				cellContentList = new ArrayList<String>();
				csvRecord.iterator().forEachRemaining(cellContentList::add);
				cellContentList.replaceAll(String::trim);
				/* for (String cellContent: cellContentList) {
					System.out.print("<<" + cellContent + ">>");
				}
				System.out.println(); */
				transactionDateStr = null;
				valueDateStr = null;
				reference = null;
				narration = null;
				transactionId = null;
				utrNumber = null;
				remitterBranch = null;
				transactionTime = null;
				endAccountReference = null;
				amount = null;
				balance = null;
				bookingDvId = null;
				transactionCodeDvId = null;
				costCenterDvId = null;
				voucherTypeDvId = null;
				transactionCategoryDvId = null;
				branchCode = null;
				
				// TODO: Following hard-coded transformation is to be performed based on import-specification
				if (bankAccountDvId == 89 || bankAccountDvId == 98 || bankAccountDvId == 99) { // ICICI
					// dd/MM/yyyy
					valueDateStr = cellContentList.get(2).substring(6) + "-" + cellContentList.get(2).substring(3, 5) + "-" + cellContentList.get(2).substring(0, 2);
					transactionDateStr = cellContentList.get(3).substring(6) + "-" + cellContentList.get(3).substring(3, 5) + "-" + cellContentList.get(3).substring(0, 2);
					if (!cellContentList.get(4).equals("-")) {
						reference = cellContentList.get(4);
					}
					narration = cellContentList.get(5);
					if (cellContentList.get(6).equals("0.0")) {
						amount = Double.parseDouble(cellContentList.get(7));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(6));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(8));
				} else if (bankAccountDvId == 90 || bankAccountDvId == 91 || bankAccountDvId == 112) { // HDFC
					// dd/MM/yy
					transactionDateStr = "20" + cellContentList.get(0).substring(6) + "-" + cellContentList.get(0).substring(3, 5) + "-" + cellContentList.get(0).substring(0, 2);
					narration = cellContentList.get(1);
					reference = cellContentList.get(2);
					valueDateStr = "20" + cellContentList.get(3).substring(6) + "-" + cellContentList.get(3).substring(3, 5) + "-" + cellContentList.get(3).substring(0, 2);
					if (cellContentList.get(4).equals("")) {
						amount = Double.parseDouble(cellContentList.get(5));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(4));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(6));
				} else if (bankAccountDvId == 94 || bankAccountDvId == 100) { // UBoI
					// dd-MM-yyyy hh:mm:ss
					transactionDateStr = cellContentList.get(1).substring(6, 10) + "-" + cellContentList.get(1).substring(3, 5) + "-" + cellContentList.get(1).substring(0, 2);
					transactionTime = cellContentList.get(1).substring(11, 19);
					transactionId = cellContentList.get(2);
					narration = cellContentList.get(3);
					if (!cellContentList.get(5).equals("'-") && !cellContentList.get(5).equals("-") && !cellContentList.get(5).equals("")) {
						utrNumber = cellContentList.get(5).substring(10);
					}
					if (!cellContentList.get(6).equals("")) {
						reference = cellContentList.get(6);
					}
					if (cellContentList.get(7).equals("")) {
						amount = Double.parseDouble(cellContentList.get(8));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(7));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(9));
				} else if (bankAccountDvId == 95) { // Indian
					// dd/MM/yyyy
					valueDateStr = cellContentList.get(0).substring(6) + "-" + cellContentList.get(0).substring(3, 5) + "-" + cellContentList.get(0).substring(0, 2);
					transactionDateStr = cellContentList.get(1).substring(6) + "-" + cellContentList.get(1).substring(3, 5) + "-" + cellContentList.get(1).substring(0, 2);
					remitterBranch = cellContentList.get(2);
					narration = cellContentList.get(3);
					if (!cellContentList.get(4).equals("")) {
						reference = cellContentList.get(4);
					}
					if (cellContentList.get(5).equals("")) {
						amount = Double.parseDouble(cellContentList.get(6));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(5));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(7).substring(0, cellContentList.get(7).length() - 2));
				} else if (bankAccountDvId == 97) { // IOB
				} else if (bankAccountDvId == 108) { // PO
				} else if (bankAccountDvId == 135 || bankAccountDvId == 204) { // Canara
				} else if (bankAccountDvId == 157) { // ESAF
				} else if (bankAccountDvId == 165 || bankAccountDvId == 207 || bankAccountDvId == 208) { // Zerodha
				} else if (bankAccountDvId == 178) { // Kotak Mahindra
				} else if (bankAccountDvId == 239 || bankAccountDvId == 241) { // ICICI-PPF
				} else if (bankAccountDvId == 240) { // HDFC-PPF
				} else if (bankAccountDvId == 242) { // PO-PPF
				} else {
					throw new AppException("Unexpected bank account", null);
				}
				
				savingsAccountTransactionRepository.save(new SavingsAccountTransaction(
						bankAccountDvId, transactionDateStr, amount, bookingDvId, valueDateStr, reference, narration, balance, transactionId, utrNumber, remitterBranch, transactionCodeDvId, branchCode, transactionTime, costCenterDvId, voucherTypeDvId, transactionCategoryDvId, endAccountReference
						));
			}
		}
	}
	
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
