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

	// TODO: Avoid following class level variables
	final String col0Values[] = {"Within Period"};
	final String col1Values[] = {"Within Period", "Outside Period", "Not Realised"};
	List<Long> col2Values;
	final String col3Values[] = {"Payment", "Receipt", "Receipt-Principal", "Receipt-Interest", "Receipt-TDS", "Accrual", "Accrual-Interest", "Accrual-TDS"};
	int col0Ind, col1Ind, col2Ind;
	Object dataArray[][];
	
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
		int dataRowInd;
		double investmentTransactionAmount, notRealisedPayment, notRealisedReceipt, notRealisedAccrual;
		
		final Object headerArray[] = {"Due", "Realisation", "Investor", "Detail", "Amount"};
		
		col2Values = Constants.categoryDvIdCache.get(Constants.CATEGORY_INVESTOR);
		dataArray = new Object[col0Values.length * col1Values.length * col2Values.size() *  col3Values.length][5];
		
		dataRowInd = -1;
		for (String col0 : col0Values) {
			for (String col1 : col1Values) {
				for (long col2 : col2Values) {
					for (String col3 : col3Values) {
						dataRowInd++;
						dataArray[dataRowInd][0] = col0;
						dataArray[dataRowInd][1] = col1;
						dataArray[dataRowInd][2] = col2;
						dataArray[dataRowInd][3] = col3;
						dataArray[dataRowInd][4] = 0D;
					}
				}
			}
		}
		
		col0Ind = 0;
		for (InvestmentTransaction investmentTransaction : investmentTransactionRepository.findByDueDateBetween(periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_CANCELLED) {
				continue;
			}
			col2Ind = col2Values.indexOf(investmentTransaction.getInvestment().getInvestor().getId());
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
					col1Ind = 1;
				} else {
					col1Ind = 0;
				}
				if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_PAYMENT) {
					accumulateAmount(0, realisation.getAmount());
					notRealisedPayment -= realisation.getAmount();
				} else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
					accumulateAmount(1, realisation.getAmount());
					notRealisedReceipt -= realisation.getAmount();
					if (investmentTransaction.getReturnedPrincipalAmount() != null) {
						accumulateAmount(2, realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getReturnedPrincipalAmount());
					}
					if (investmentTransaction.getInterestAmount() != null) {
						accumulateAmount(3, realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getInterestAmount());
					}
					if (investmentTransaction.getTdsAmount() != null) {
						accumulateAmount(4, realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getTdsAmount());
					}
				} else { /* Constants.DVID_TRANSACTION_TYPE_ACCRUAL */
					accumulateAmount(5, realisation.getAmount());
					notRealisedAccrual -= realisation.getAmount();
					if (investmentTransaction.getInterestAmount() != null) {
						accumulateAmount(6, realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getInterestAmount());
					}
					if (investmentTransaction.getTdsAmount() != null) {
						accumulateAmount(7, realisation.getAmount() / investmentTransactionAmount * investmentTransaction.getTdsAmount());
					}
				}
			}
			col1Ind = 2;
			accumulateAmount(0, notRealisedPayment);
			accumulateAmount(1, notRealisedReceipt);
			accumulateAmount(5, notRealisedAccrual);
			if (investmentTransaction.getReturnedPrincipalAmount() != null) {
				accumulateAmount(2, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getReturnedPrincipalAmount());
			}
			if (investmentTransaction.getInterestAmount() != null) {
				accumulateAmount(3, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getInterestAmount());
				accumulateAmount(6, notRealisedAccrual / investmentTransactionAmount * investmentTransaction.getInterestAmount());
			}
			if (investmentTransaction.getTdsAmount() != null) {
				accumulateAmount(4, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getTdsAmount());
				accumulateAmount(7, notRealisedAccrual / investmentTransactionAmount * investmentTransaction.getTdsAmount());
			}
		}
		
		recordList = new ArrayList<Object[]>(dataArray.length);
		recordList.add(new Object[]{"Between", periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate()});
		recordList.add(headerArray);
		for (Object[] dataRow : dataArray) {
			recordList.add(dataRow);
		}
		return recordList;
	 
	}
	
	private void accumulateAmount(int ind3, double amount) {
		int ind;
		ind = col0Ind * col1Values.length * col2Values.size() * col3Values.length +
				col1Ind * col2Values.size() * col3Values.length +
				col2Ind * col3Values.length +
				ind3;
		dataArray[ind][4] = (double)dataArray[ind][4] + amount;
	}
}
