package org.sakuram.persmony.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.sakuram.persmony.bean.Contract;
import org.sakuram.persmony.bean.ContractEq;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.bean.SavingsAccountTransaction;
import org.sakuram.persmony.bean.SbAcTxnCategory;
import org.sakuram.persmony.bean.Trade;
import org.sakuram.persmony.repository.RealisationRepository;
import org.sakuram.persmony.repository.SavingsAccountTransactionRepository;
import org.sakuram.persmony.repository.SbAcTxnCategoryRepository;
import org.sakuram.persmony.repository.TradeRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.valueobject.DvFlagsSbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.valueobject.SbAcTxnImportStatsVO;
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
	@Autowired
	RealisationRepository realisationRepository;
	@Autowired
	TradeRepository tradeRepository;
		
	public SbAcTxnImportStatsVO importSavingsAccountTransactions(long bankAccountDvId, MultipartFile multipartFile) throws IOException, ParseException {
    	List<String> cellContentList;
    	String transactionDateStr, valueDateStr, reference, narration, transactionId, utrNumber, remitterBranch, transactionTime;
    	Double amount, balance;
    	Long bookingDvId, transactionCodeDvId, costCenterDvId, voucherTypeDvId;
    	Integer branchCode;
    	SimpleDateFormat targetDateFormat, targetTimeFormat, sourceFormat01, sourceFormat02, sourceFormat03, sourceFormat04, sourceFormat05, sourceFormat06;
    	int debitCount, creditCount;
    	double debitTotal, creditTotal;
    	
    	debitCount = 0;
    	creditCount = 0;
    	debitTotal = 0;
    	creditTotal = 0;
    	targetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	targetTimeFormat = new SimpleDateFormat("HH:mm:ss");
    	sourceFormat01 = new SimpleDateFormat("dd-MMM-yyyy");
    	sourceFormat02 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	sourceFormat03 = new SimpleDateFormat("dd MMM yyyy");
    	sourceFormat04 = new SimpleDateFormat("dd/MM/yyyy");
    	sourceFormat05 = new SimpleDateFormat("dd/MM/yy");
    	sourceFormat06 = new SimpleDateFormat("dd-MM-yyyy");
    	
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
				amount = null;
				balance = null;
				bookingDvId = null;
				transactionCodeDvId = null;
				costCenterDvId = null;
				voucherTypeDvId = null;
				branchCode = null;
				
				// TODO: Following hard-coded transformation is to be performed based on import-specification
				if (bankAccountDvId == 89 || bankAccountDvId == 98 || bankAccountDvId == 99) { // ICICI
					// dd/MM/yyyy
					valueDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(2)));
					transactionDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(3)));
					if (!cellContentList.get(4).equals("-")) {
						reference = cellContentList.get(4);
					}
					narration = cellContentList.get(5);
					if (Double.parseDouble(cellContentList.get(6)) == 0D) {
						amount = Double.parseDouble(cellContentList.get(7));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(6));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(8));
				} else if (bankAccountDvId == 90 || bankAccountDvId == 91 || bankAccountDvId == 112) { // HDFC
					// dd/MM/yy
					transactionDateStr = targetDateFormat.format(sourceFormat05.parse(cellContentList.get(0)));
					narration = cellContentList.get(1);
					reference = cellContentList.get(2);
					valueDateStr = targetDateFormat.format(sourceFormat05.parse(cellContentList.get(3)));
					if (cellContentList.get(4).equals("")) {
						amount = Double.parseDouble(cellContentList.get(5));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(4));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(6));
				} else if (bankAccountDvId == 94 || bankAccountDvId == 100) { // UBoI
					// dd-MM-yyyy HH:mm:ss
					transactionDateStr = targetDateFormat.format(sourceFormat02.parse(cellContentList.get(1)));
					transactionTime = targetTimeFormat.format(sourceFormat02.parse(cellContentList.get(1)));
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
					valueDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(0)));
					transactionDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(1)));
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
					// dd-MMM-yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat01.parse(cellContentList.get(0)));
					valueDateStr = targetDateFormat.format(sourceFormat01.parse(cellContentList.get(1)));
					if (!cellContentList.get(2).equals("")) {
						reference = cellContentList.get(2);
					}
					narration = cellContentList.get(3);
					transactionCodeDvId = Constants.DESC_TO_ID_MAP.get(Constants.CATEGORY_TRANSACTION_CODE + ":" + cellContentList.get(4));
					if (transactionCodeDvId == null) {
						throw new AppException("Invalid Transaction Code", null);
					}
					if (cellContentList.get(5).equals("")) {
						amount = Double.parseDouble(cellContentList.get(6));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(5));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(7));
				} else if (bankAccountDvId == 108) { // PO
					// dd-MM-yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat06.parse(cellContentList.get(2)));
					valueDateStr = targetDateFormat.format(sourceFormat06.parse(cellContentList.get(3)));
					narration = cellContentList.get(7);
					if (cellContentList.get(11).equals("Cr.")) {
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					amount = Double.parseDouble(cellContentList.get(12).replace(",", ""));
					balance = Double.parseDouble(cellContentList.get(14).replace(",", ""));
				} else if (bankAccountDvId == 135 || bankAccountDvId == 204) { // Canara
					cellContentList.replaceAll( cellContent -> {
						return (cellContent.startsWith("=\"") ? cellContent.substring(2, cellContent.length() - 1) : cellContent);
					});
					// dd-MM-yyyy HH:mm:ss
					transactionDateStr = targetDateFormat.format(sourceFormat02.parse(cellContentList.get(0)));
					transactionTime = targetTimeFormat.format(sourceFormat02.parse(cellContentList.get(0)));
					// dd MMM yyyy
					valueDateStr = targetDateFormat.format(sourceFormat03.parse(cellContentList.get(1)));
					if (!cellContentList.get(2).equals("") && !cellContentList.get(2).equals(" ") && !cellContentList.get(2).equals("0") && !cellContentList.get(2).equals("000000000000")) {
						reference = cellContentList.get(2);
					}
					narration = cellContentList.get(3);
					branchCode = Integer.valueOf(cellContentList.get(4));
					if (cellContentList.get(5).equals("")) {
						amount = Double.parseDouble(cellContentList.get(6).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(5).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(7).replace(",", ""));
				} else if (bankAccountDvId == 157) { // ESAF
					// dd/MM/yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(1)));
					narration = cellContentList.get(3);
					if (cellContentList.get(11).equals("")) {
						amount = Double.parseDouble(cellContentList.get(14).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(11).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(19).replace(",", ""));
				} else if (bankAccountDvId == 165 || bankAccountDvId == 207 || bankAccountDvId == 208) { // Zerodha
					narration = cellContentList.get(0);
					// yyyy-MM-dd
					transactionDateStr = cellContentList.get(1);
					costCenterDvId = Constants.DESC_TO_ID_MAP.get(Constants.CATEGORY_COST_CENTER + ":" + cellContentList.get(2));
					if (costCenterDvId == null) {
						throw new AppException("Invalid Cost Center", null);
					}
					voucherTypeDvId = Constants.DESC_TO_ID_MAP.get(Constants.CATEGORY_VOUCHER_TYPE + ":" + cellContentList.get(3));
					if (voucherTypeDvId == null) {
						throw new AppException("Invalid Voucher Type", null);
					}
					if (cellContentList.get(4).equals("") || Double.parseDouble(cellContentList.get(4)) == 0) {
						amount = Double.parseDouble(cellContentList.get(5));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(4));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(6));
				} else if (bankAccountDvId == 178) { // Kotak Mahindra
					// dd-MM-yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat06.parse(cellContentList.get(1)));
					valueDateStr = targetDateFormat.format(sourceFormat06.parse(cellContentList.get(2)));
					narration = cellContentList.get(3);
					if (!cellContentList.get(4).equals("")) {
						reference = cellContentList.get(4);
					}
					amount = Double.parseDouble(cellContentList.get(5).replace(",", ""));
					if (cellContentList.get(6).equals("CR")) {
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(7).replace(",", ""));
				} else if (bankAccountDvId == 239 || bankAccountDvId == 241) { // ICICI-PPF
					// dd/MM/yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat04.parse(cellContentList.get(2)));
					if (!cellContentList.get(3).equals("-")) {
						reference = cellContentList.get(3);
					}
					narration = cellContentList.get(4);
					if (cellContentList.get(6).equals("-")) {
						amount = Double.parseDouble(cellContentList.get(7).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(6).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(8).replace(",", ""));
				/* } else if (bankAccountDvId == 240) { // HDFC-PPF
					
				} else if (bankAccountDvId == 242) { // PO-PPF */
					
				} else if (bankAccountDvId == 296) { // Canara-CC
					// dd-MM-yyyy
					transactionDateStr = targetDateFormat.format(sourceFormat06.parse(cellContentList.get(0)));
					reference = cellContentList.get(1);
					narration = cellContentList.get(2);
					amount = Double.parseDouble(cellContentList.get(3).substring(0, cellContentList.get(3).length() - 3).replace(",", ""));
					if (cellContentList.get(3).endsWith("Cr")) {
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(4).substring(0, cellContentList.get(4).length() - 3).replace(",", ""));
					if (cellContentList.get(4).endsWith("Dr") && balance != 0) {
						balance = -1 * balance;
					}
				} else if (bankAccountDvId == 302) {	// SIB
					transactionDateStr = targetDateFormat.format(sourceFormat01.parse(cellContentList.get(1)));
					valueDateStr = targetDateFormat.format(sourceFormat01.parse(cellContentList.get(2)));
					narration = cellContentList.get(3);
					reference = cellContentList.get(6);
					if (cellContentList.get(7).equals("")) {
						amount = Double.parseDouble(cellContentList.get(8).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_CREDIT;
					} else {
						amount = Double.parseDouble(cellContentList.get(7).replace(",", ""));
						bookingDvId = Constants.DVID_BOOKING_DEBIT;
					}
					balance = Double.parseDouble(cellContentList.get(9).replace(",", ""));
				} else {
					throw new AppException("Unexpected bank account", null);
				}
				
				savingsAccountTransactionRepository.save(new SavingsAccountTransaction(
						bankAccountDvId, transactionDateStr, amount, bookingDvId, valueDateStr, reference, narration, balance, transactionId, utrNumber, remitterBranch, transactionCodeDvId, branchCode, transactionTime, costCenterDvId, voucherTypeDvId
						));
				if (bookingDvId == Constants.DVID_BOOKING_DEBIT) {
					debitCount++;
					debitTotal += amount;
				} else {
					creditCount++;
					creditTotal += amount;
				}
			}
		}
		return new SbAcTxnImportStatsVO(debitCount, debitTotal, creditCount, creditTotal);
	}

	public SavingsAccountTransactionVO fetchLastSavingsAccountTransaction(long bankAccountDvId) {
		SavingsAccountTransaction savingsAccountTransaction;
		SavingsAccountTransactionVO savingsAccountTransactionVO;
		savingsAccountTransaction = savingsAccountTransactionRepository.findLastSbAcTxnInBankAccount(bankAccountDvId);
		savingsAccountTransactionVO = new SavingsAccountTransactionVO();
		if (savingsAccountTransaction != null) {
			savingsAccountTransactionVO.setSavingsAccountTransactionId(savingsAccountTransaction.getId());
			savingsAccountTransactionVO.setTransactionDate(savingsAccountTransaction.getTransactionDate());
			savingsAccountTransactionVO.setNarration(savingsAccountTransaction.getNarration());
			savingsAccountTransactionVO.setBooking(new IdValueVO(savingsAccountTransaction.getBooking().getId(), savingsAccountTransaction.getBooking().getValue()));
			savingsAccountTransactionVO.setAmount(savingsAccountTransaction.getAmount());
			savingsAccountTransactionVO.setBalance(savingsAccountTransaction.getBalance());
		}
		return savingsAccountTransactionVO;
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
		double amount;
		
		sbAcTxnCategoryVOList = new ArrayList<SbAcTxnCategoryVO>();
		savingsAccountTransaction = savingsAccountTransactionRepository.findById(savingsAccountTransactionId)
			.orElseThrow(() -> new AppException("Invalid Savings Account Transaction Id " + savingsAccountTransactionId, null));
		if (savingsAccountTransaction.getSbAcTxnCategoryList() != null) {
			for (SbAcTxnCategory sbAcTxnCategory : savingsAccountTransaction.getSbAcTxnCategoryList()) {
				dvFlagsSbAcTxnCategoryVO = (DvFlagsSbAcTxnCategoryVO) DomainValueFlags.getDvFlagsVO(sbAcTxnCategory.getTransactionCategory());
				sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
						sbAcTxnCategory.getId(), 
						new IdValueVO(sbAcTxnCategory.getTransactionCategory().getId(), sbAcTxnCategory.getTransactionCategory().getValue()),
						(dvFlagsSbAcTxnCategoryVO.getDvCategory() == null || dvFlagsSbAcTxnCategoryVO.getDvCategory().equals(Constants.CATEGORY_NONE)) ?
								new IdValueVO(null, sbAcTxnCategory.getEndAccountReference()) :
								new IdValueVO(Long.parseLong(sbAcTxnCategory.getEndAccountReference()), Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue()),
						sbAcTxnCategory.getGroupId(),
						sbAcTxnCategory.getAmount()));
			}
		}
		for (Realisation realisation : savingsAccountTransaction.getRealisationList()) {
			sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
					Constants.NON_SATC_ID,
					new IdValueVO(Constants.DVID_TRANSACTION_CATEGORY_DTI, Constants.domainValueCache.get(Constants.DVID_TRANSACTION_CATEGORY_DTI).getValue()),
					new IdValueVO(realisation.getInvestmentTransaction().getInvestment().getId(),
							realisation.getInvestmentTransaction().getInvestment().getId() +
							"/" + realisation.getInvestmentTransaction().getId() +
							"/" + realisation.getId() +
							"/" + realisation.getInvestmentTransaction().getInvestment().getProductName()),
					null,
					realisation.getAmount()));
		}
		for (Contract contract : savingsAccountTransaction.getContractList()) {
			sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
					Constants.NON_SATC_ID,
					new IdValueVO(null, "Security Contract"),
					new IdValueVO(null, contract.getContractNo()),
					'A',
					contract.getNetAmount()));
			for (IsinAction isinAction : contract.getIsinActionList()) {
				if (isinAction.getIsin().getSecurityType().getId() != Constants.DVID_TRANSACTION_CATEGORY_DTI) {
					amount = 0;
					for (Trade trade : tradeRepository.findByIsinActionPart_IsinAction(isinAction)) {
						amount += trade.getIsinActionPart().getQuantity() * (trade.getIsinActionPart().getPricePerUnit() + trade.getBrokeragePerUnit());
					}
					sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
							Constants.NON_SATC_ID,
							new IdValueVO(isinAction.getIsin().getSecurityType()),
							new IdValueVO(null,
									isinAction.getEffectiveActionType().getValue() +
									"/" + isinAction.getIsin().getIsin()),
							'B',
							amount));
				}
			}
		}
		for (ContractEq contractEq : savingsAccountTransaction.getContractEqList()) {
			sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
					Constants.NON_SATC_ID,
					new IdValueVO(null, "Security Contract Equivalent"),
					new IdValueVO(null, String.valueOf(contractEq.getId())),
					'A',
					contractEq.getNetAmount()));
			for (IsinAction isinAction : contractEq.getIsinActionList()) {
				if (isinAction.getIsin().getSecurityType().getId() != Constants.DVID_TRANSACTION_CATEGORY_DTI) {
					sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
							Constants.NON_SATC_ID,
							new IdValueVO(isinAction.getIsin().getSecurityType()),
							new IdValueVO(null,
									isinAction.getEffectiveActionType().getValue() +
									"/" + isinAction.getIsin().getIsin()),
							'B',
							isinAction.getBasePrice()));
				}
			}
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
			savingsAccountTransaction.getSbAcTxnCategoryList().clear();
		} else {
			for (int i = 0; i < savingsAccountTransaction.getSbAcTxnCategoryList().size(); i++) {
				SbAcTxnCategory sbAcTxnCategoryDeleted = savingsAccountTransaction.getSbAcTxnCategoryList().get(i);
				if (!sbAcTxnCategoryVOFromUiList.stream().anyMatch(sbAcTxnCategoryVOFromUi -> (sbAcTxnCategoryVOFromUi.getSbAcTxnCategoryId() == null ? false : sbAcTxnCategoryVOFromUi.getSbAcTxnCategoryId().equals(sbAcTxnCategoryDeleted.getId())))) {
					sbAcTxnCategoryRepository.delete(sbAcTxnCategoryDeleted);
					savingsAccountTransaction.getSbAcTxnCategoryList().remove(sbAcTxnCategoryDeleted); // Because of the bi-directional relationship, this additional step is required
					i--;
				}
			}
			for (SbAcTxnCategoryVO sbAcTxnCategoryVO : sbAcTxnCategoryVOFromUiList) {
				if (sbAcTxnCategoryVO.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_DTI) {
					continue;
				}
				transactionCategoryDvUi = Constants.domainValueCache.get(sbAcTxnCategoryVO.getTransactionCategory().getId());
				if (sbAcTxnCategoryVO.getSbAcTxnCategoryId() == null) {
					SbAcTxnCategory sbAcTxnCategoryInserted = new SbAcTxnCategory(
							savingsAccountTransaction,
							transactionCategoryDvUi,
							endAccountReferenceUiToDb(sbAcTxnCategoryVO.getEndAccountReference()),
							sbAcTxnCategoryVO.getGroupId(),
							sbAcTxnCategoryVO.getAmount()
							);
					sbAcTxnCategoryRepository.save(sbAcTxnCategoryInserted);
				} else {
					isFound = false;
					for (SbAcTxnCategory sbAcTxnCategoryUpdated : savingsAccountTransaction.getSbAcTxnCategoryList()) {
						if (sbAcTxnCategoryVO.getSbAcTxnCategoryId().equals(sbAcTxnCategoryUpdated.getId())) {
							if (sbAcTxnCategoryUpdated.getTransactionCategory().getId() != sbAcTxnCategoryVO.getTransactionCategory().getId() ||
									!sbAcTxnCategoryUpdated.getEndAccountReference().equals(sbAcTxnCategoryVO.getEndAccountReference().getValue()) ||
									!sbAcTxnCategoryUpdated.getAmount().equals(sbAcTxnCategoryVO.getAmount())) {
								sbAcTxnCategoryUpdated.setTransactionCategory(transactionCategoryDvUi);
								sbAcTxnCategoryUpdated.setEndAccountReference(endAccountReferenceUiToDb(sbAcTxnCategoryVO.getEndAccountReference()));
								sbAcTxnCategoryUpdated.setGroupId(sbAcTxnCategoryVO.getGroupId());
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
	
	public void createSavingsAccountTransaction(SavingsAccountTransactionVO savingsAccountTransactionVO) {
		savingsAccountTransactionRepository.save(new SavingsAccountTransaction(
				savingsAccountTransactionVO.getBankAccountOrInvestor() == null ? null : savingsAccountTransactionVO.getBankAccountOrInvestor().getId(),
				savingsAccountTransactionVO.getTransactionDate(),
				savingsAccountTransactionVO.getAmount(),
				savingsAccountTransactionVO.getBooking() == null ? null : savingsAccountTransactionVO.getBooking().getId(),
				savingsAccountTransactionVO.getValueDate(),
				savingsAccountTransactionVO.getReference(),
				savingsAccountTransactionVO.getNarration(),
				savingsAccountTransactionVO.getBalance(),
				savingsAccountTransactionVO.getTransactionId(),
				savingsAccountTransactionVO.getUtrNumber(),
				savingsAccountTransactionVO.getRemitterBranch(),
				savingsAccountTransactionVO.getTransactionCode() == null ? null : savingsAccountTransactionVO.getTransactionCode().getId(),
				savingsAccountTransactionVO.getBranchCode(),
				savingsAccountTransactionVO.getTransactionTime(),
				savingsAccountTransactionVO.getCostCenter() == null ? null : savingsAccountTransactionVO.getCostCenter().getId(),
				savingsAccountTransactionVO.getVoucherType() == null ? null : savingsAccountTransactionVO.getVoucherType().getId()
		));
	}
	
	private String endAccountReferenceUiToDb(IdValueVO idValueVO) {
		if (idValueVO == null) {
			return null;
		} else if (idValueVO.getId() != null) {
			return idValueVO.getId().toString();
		} else if (idValueVO.getValue() != null) {
			return idValueVO.getValue();
		} else {
			return null;
		}
	}
}
