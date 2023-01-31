package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.PeriodSummaryCriteriaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {
	@Autowired
	InvestmentRepository investmentRepository;
	@Autowired
	InvestmentTransactionRepository investmentTransactionRepository;

	final Integer CRITERION_INVESTOR = 1;
	final Integer CRITERION_IS_CLOSED = 2;
	final Integer CRITERION_TRANSACTION_TYPE = 3;
	final Integer CRITERION_TRANSACTION_STATUS = 4;
	
	public List<Object[]> investmentsWithPendingTransactions() {
		Map<Integer, List<String>> criteriaMap;
		
		criteriaMap = new HashMap<Integer, List<String>>();
		criteriaMap.put(CRITERION_IS_CLOSED, Arrays.asList(new String[]{"false"}));
		criteriaMap.put(CRITERION_TRANSACTION_TYPE, Arrays.asList(new String[]{"73"}));
		criteriaMap.put(CRITERION_TRANSACTION_STATUS, Arrays.asList(new String[]{"69"}));
		return fetchRequiredTransactions(criteriaMap);
	}
	
	private List<Object[]> fetchRequiredTransactions(Map<Integer, List<String>> criteriaMap) {
		List<Object[]> recordList;
		
		recordList = new ArrayList<Object[]>();
		recordList.add(new Object[]{"Sl. No.", "Investor", "Product Provider", "Product Name", "Account No.", "Is Closed?", "Receipt Date", "Receipt Amount", "Returned Principal", "Receipt Status"});
		for (Investment investment : investmentRepository.findAllByOrderByIdAsc()) {

			if (criteriaMap.containsKey(CRITERION_INVESTOR) &&
					!criteriaMap.get(CRITERION_INVESTOR).contains(String.valueOf(investment.getInvestor().getId())))
				continue;
			if (criteriaMap.containsKey(CRITERION_IS_CLOSED) &&
					!criteriaMap.get(CRITERION_IS_CLOSED).contains(String.valueOf(investment.isClosed())))
				continue;
			recordList.add(new Object[]{investment.getId(), investment.getInvestor().getValue(), investment.getProductProvider().getValue() + " - " + (investment.getProviderBranch() == null ? "Central" : investment.getProviderBranch().getValue()),
					investment.getProductName() == null ? investment.getProductType().getValue() : investment.getProductName(),
					investment.getInvestmentIdWithProvider(), investment.isClosed()});
			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
				if (criteriaMap.containsKey(CRITERION_TRANSACTION_TYPE) &&
						!criteriaMap.get(CRITERION_TRANSACTION_TYPE).contains(String.valueOf(investmentTransaction.getTransactionType().getId())))
					continue;
				if (criteriaMap.containsKey(CRITERION_TRANSACTION_STATUS) &&
						!criteriaMap.get(CRITERION_TRANSACTION_STATUS).contains(String.valueOf(investmentTransaction.getStatus().getId())))
					continue;
				if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
					recordList.add(new Object[]{null, null, null, null, null, null,
							investmentTransaction.getDueDate(), investmentTransaction.getDueAmount(), 
							investmentTransaction.getReturnedPrincipalAmount(), investmentTransaction.getStatus().getValue()});
				}
				// TODO: Update existing record, than always adding to recordList
			}
		}
		return recordList;
	}
	
	public List<Object[]> pendingTransactions() {
		List<Object[]> recordList;
		
		recordList = investmentTransactionRepository.findPendingTransactions();
		recordList.add(0, new Object[]{"Date", "Txn. Id", "Investment Id", "Investor", "Product Provider", "Product Name", "Account No.", "Amount", "Returned Principal"});
		
		return recordList;
	}
	
	public List<Object[]> periodSummary(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<Object[]> recordList;
		List<Long> investorList;
		Object headerArray[], dataArray[][];
		int investorInd, dataRowInd;
		double investmentTransactionAmount, notRealisedPayment, notRealisedReceipt, notRealisedAccrual;
		final int DATA_COLUMNS_COUNT = 8;

		investorList = Constants.categoryDvIdCache.get(Constants.CATEGORY_INVESTOR);
		headerArray = new Object[2 + investorList.size() * DATA_COLUMNS_COUNT];
		dataArray = new Object[3][2 + investorList.size() * DATA_COLUMNS_COUNT];
		
		headerArray[0] = "Due";
		headerArray[1] = "Realisation";
		for (int ind = 0; ind < investorList.size(); ind++) {
			headerArray[ind * DATA_COLUMNS_COUNT + 2] = (ind + 1) + ". Payment";
			headerArray[ind * DATA_COLUMNS_COUNT + 3] = (ind + 1) + ". Receipt";
			headerArray[ind * DATA_COLUMNS_COUNT + 4] = (ind + 1) + ". Receipt-Principal";
			headerArray[ind * DATA_COLUMNS_COUNT + 5] = (ind + 1) + ". Receipt-Interest";
			headerArray[ind * DATA_COLUMNS_COUNT + 6] = (ind + 1) + ". Receipt-TDS";
			headerArray[ind * DATA_COLUMNS_COUNT + 7] = (ind + 1) + ". Accrual";
			headerArray[ind * DATA_COLUMNS_COUNT + 8] = (ind + 1) + ". Accrual-Interest";
			headerArray[ind * DATA_COLUMNS_COUNT + 9] = (ind + 1) + ". Accrual-TDS";
		}
		dataArray[0][0] = "Within Period";
		dataArray[0][1] = "Within Period";
		dataArray[1][0] = "Within Period";
		dataArray[1][1] = "Outside Period";
		dataArray[2][0] = "Within Period";
		dataArray[2][1] = "Not Realised";
		for (int ind = 2; ind < 2 + investorList.size() * DATA_COLUMNS_COUNT; ind++) {
			dataArray[0][ind] = 0D;
			dataArray[1][ind] = 0D;
			dataArray[2][ind] = 0D;
		}
		
		for (InvestmentTransaction investmentTransaction : investmentTransactionRepository.findByDueDateBetween(periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_CANCELLED) {
				continue;
			}
			investorInd = investorList.indexOf(investmentTransaction.getInvestment().getInvestor().getId());
			if (investmentTransaction.getSettledAmount() == null && investmentTransaction.getDueAmount() == null && investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
				investmentTransactionAmount = MiscService.zeroIfNull(investmentTransaction.getInterestAmount()) -
						MiscService.zeroIfNull(investmentTransaction.getTdsAmount());
			} else if (investmentTransaction.getSettledAmount() != null) {
				investmentTransactionAmount = investmentTransaction.getSettledAmount();
			} else if (investmentTransaction.getDueAmount() != null) {
				investmentTransactionAmount = investmentTransaction.getDueAmount();
			} else if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING) {
				investmentTransactionAmount = MiscService.zeroIfNull(investmentTransaction.getReturnedPrincipalAmount()) +
						MiscService.zeroIfNull(investmentTransaction.getInterestAmount()) -
						MiscService.zeroIfNull(investmentTransaction.getTdsAmount()); // Just Approximation
			} else {
				throw new AppException("Invalid Data in Investment Transaction " + investmentTransaction.getId(), null);
			}
			notRealisedPayment = 0;
			notRealisedReceipt = 0;
			notRealisedAccrual = 0;
			if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_PAYMENT) {
				notRealisedPayment = investmentTransactionAmount;
			} else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
				notRealisedReceipt = investmentTransactionAmount;
			} else { /* Constants.DVID_TRANSACTION_TYPE_ACCRUAL */
				notRealisedAccrual = investmentTransactionAmount;
			}
			for (Realisation realisation : investmentTransaction.getRealisationList()) {
				if (realisation.getRealisationDate().before(periodSummaryCriteriaVO.getFromDate()) ||
						realisation.getRealisationDate().after(periodSummaryCriteriaVO.getToDate())) {
					dataRowInd = 1;
				} else {
					dataRowInd = 0;
				}
				if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_PAYMENT) {
					dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 2] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 2] + realisation.getAmount();
					notRealisedPayment -= realisation.getAmount();
				} else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
					dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 3] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 3] + realisation.getAmount();
					notRealisedReceipt -= realisation.getAmount();
					if (investmentTransaction.getReturnedPrincipalAmount() != null) {
						dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 4] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 4] + realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getReturnedPrincipalAmount();
					}
					if (investmentTransaction.getInterestAmount() != null) {
						dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 5] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 5] + realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getInterestAmount();
					}
					if (investmentTransaction.getTdsAmount() != null) {
						dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 6] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 6] + realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getTdsAmount();
					}
				} else { /* Constants.DVID_TRANSACTION_TYPE_ACCRUAL */
					dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 7] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 7] + realisation.getAmount();
					notRealisedAccrual -= realisation.getAmount();
					if (investmentTransaction.getInterestAmount() != null) {
						dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 8] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 8] + realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getInterestAmount();
					}
					if (investmentTransaction.getTdsAmount() != null) {
						dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 9] = (double)dataArray[dataRowInd][investorInd * DATA_COLUMNS_COUNT + 9] + realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getTdsAmount();
					}
				}
			}
			dataArray[2][investorInd * DATA_COLUMNS_COUNT + 2] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 2] + notRealisedPayment;
			dataArray[2][investorInd * DATA_COLUMNS_COUNT + 3] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 3] + notRealisedReceipt;
			dataArray[2][investorInd * DATA_COLUMNS_COUNT + 7] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 7] + notRealisedAccrual;
			if (investmentTransaction.getReturnedPrincipalAmount() != null) {
				dataArray[2][investorInd * DATA_COLUMNS_COUNT + 4] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 4] + notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getReturnedPrincipalAmount();
			}
			if (investmentTransaction.getInterestAmount() != null) {
				dataArray[2][investorInd * DATA_COLUMNS_COUNT + 5] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 5] + notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getInterestAmount();
				dataArray[2][investorInd * DATA_COLUMNS_COUNT + 8] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 8] + notRealisedAccrual / investmentTransactionAmount * investmentTransaction.getInterestAmount();
			}
			if (investmentTransaction.getTdsAmount() != null) {
				dataArray[2][investorInd * DATA_COLUMNS_COUNT + 6] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 6] + notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getTdsAmount();
				dataArray[2][investorInd * DATA_COLUMNS_COUNT + 9] = (double)dataArray[2][investorInd * DATA_COLUMNS_COUNT + 9] + notRealisedAccrual / investmentTransactionAmount * investmentTransaction.getTdsAmount();
			}
		}
		
		recordList = new ArrayList<Object[]>(5);
		recordList.add(new Object[]{"Between", periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate()});
		recordList.add(headerArray);
		recordList.add(dataArray[0]);
		recordList.add(dataArray[1]);
		recordList.add(dataArray[2]);
		return recordList;
	}
}
