package org.sakuram.persmony.service;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.PeriodSummaryCriteriaVO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {
	
	@Autowired
	MiscService miscService;
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
	List<Object[]> breakupRecordList;
	String itId, rId;
	
	public List<List<Object[]>> investmentsWithPendingTransactions() {
		Map<Integer, List<String>> criteriaMap;
		
		criteriaMap = new HashMap<Integer, List<String>>();
		criteriaMap.put(CRITERION_IS_CLOSED, Arrays.asList(new String[]{"false"}));
		criteriaMap.put(CRITERION_TRANSACTION_TYPE, Arrays.asList(new String[]{"73"}));
		criteriaMap.put(CRITERION_TRANSACTION_STATUS, Arrays.asList(new String[]{"69"}));
		return fetchRequiredTransactions(criteriaMap);
	}
	
	private List<List<Object[]>> fetchRequiredTransactions(Map<Integer, List<String>> criteriaMap) {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);
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
		return reportList;
	}
	
	public List<List<Object[]>> pendingTransactions() {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		Map<Long, Double> lastCompletedReceiptsMap;
		Long investmentId;
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = investmentTransactionRepository.findPendingTransactions();
		reportList.add(recordList);

		lastCompletedReceiptsMap = fetchLastCompletedReceipts();
		for (Object[] fields : recordList) {
			if (fields[7] == null) {
				investmentId = ((BigInteger) fields[2]).longValue();
				if (lastCompletedReceiptsMap.containsKey(investmentId)) {
					fields[7] = lastCompletedReceiptsMap.get(investmentId);
					fields[8] = "Last Received";
				}
			} else {
				fields[8] = "Due";
			}
		}
		
		recordList.add(0, new Object[]{"Date", "Txn. Id", "Investment Id", "Investor", "Product Provider", "Product Name", "Account No.", "Amount", "Based On", "Returned Principal"});
		return reportList;
	}
	
	public List<List<Object[]>> periodSummary(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		int dataRowInd;
		double investmentTransactionAmount, notRealisedPayment, notRealisedReceipt, notRealisedAccrual, settledAmount;
		Map<Long, Double> lastCompletedReceiptsMap;
		
		final Object headerArray[] = {"Due", "Realisation", "Investor", "Detail", "Amount"};
		
		breakupRecordList = new ArrayList<Object[]>();
		breakupRecordList.add(new Object[]{"Between", periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate()});
		breakupRecordList.add(new Object[]{"IT Id", "R Id", "Due", "Realisation", "Investor", "Detail", "Amount"});
		
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

		lastCompletedReceiptsMap = fetchLastCompletedReceipts();
		col0Ind = 0;
		for (InvestmentTransaction investmentTransaction : investmentTransactionRepository.findByDueDateBetween(periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_CANCELLED) {
				continue;
			}
			col2Ind = col2Values.indexOf(investmentTransaction.getInvestment().getInvestor().getId());
			settledAmount = miscService.fetchRealisationAmountSummary(investmentTransaction).getAmount();
			if (settledAmount != 0) {
				investmentTransactionAmount = settledAmount;
			} else if (investmentTransaction.getDueAmount() != null) {
				investmentTransactionAmount = investmentTransaction.getDueAmount();
			// Else Approximations
			} else if ((investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING  &&
					investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT || investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) &&
					(investmentTransaction.getReturnedPrincipalAmount() != null || investmentTransaction.getInterestAmount() != null || investmentTransaction.getTdsAmount() != null)) {
				investmentTransactionAmount = ObjectUtils.defaultIfNull(investmentTransaction.getReturnedPrincipalAmount(), 0).doubleValue() +
						ObjectUtils.defaultIfNull(investmentTransaction.getInterestAmount(), 0).doubleValue() -
						ObjectUtils.defaultIfNull(investmentTransaction.getTdsAmount(), 0).doubleValue();
				if (investmentTransactionAmount == 0) {
					System.out.println("Skipped Investment Transaction " + investmentTransaction.getId());
					continue;
				}
			} else if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING  && investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT &&
					lastCompletedReceiptsMap.containsKey(investmentTransaction.getInvestment().getId())) {
				investmentTransactionAmount = lastCompletedReceiptsMap.get(investmentTransaction.getInvestment().getId());
			} else {
				System.out.println("Skipped Investment Transaction " + investmentTransaction.getId());
				continue;
			}
			itId = String.valueOf(investmentTransaction.getId());
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
				rId = String.valueOf(realisation.getId());
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
					if (realisation.getReturnedPrincipalAmount() != null) {
						accumulateAmount(2, realisation.getReturnedPrincipalAmount());
					}
					if (realisation.getInterestAmount() != null) {
						accumulateAmount(3, realisation.getInterestAmount());
					}
					if (realisation.getTdsAmount() != null) {
						accumulateAmount(4, realisation.getTdsAmount());
					}
				}
			}
			rId = "N/A";
			col1Ind = 2;
			accumulateAmount(0, notRealisedPayment);
			accumulateAmount(1, notRealisedReceipt);
			accumulateAmount(5, notRealisedAccrual);
			if (investmentTransaction.getReturnedPrincipalAmount() != null) {
				accumulateAmount(2, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getReturnedPrincipalAmount());
			}
			if (investmentTransaction.getInterestAmount() != null) {
				accumulateAmount(3, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getInterestAmount());
			}
			if (investmentTransaction.getTdsAmount() != null) {
				accumulateAmount(4, notRealisedReceipt / investmentTransactionAmount * investmentTransaction.getTdsAmount());
			}
		}
		
		reportList = new ArrayList<List<Object[]>>(2);
		recordList = new ArrayList<Object[]>(dataArray.length);
		reportList.add(recordList);
		reportList.add(breakupRecordList);
		recordList.add(new Object[]{"Between", periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate()});
		recordList.add(headerArray);
		for (Object[] dataRow : dataArray) {
			recordList.add(dataRow);
		}
		return reportList;
	 
	}

	private Map<Long, Double> fetchLastCompletedReceipts() {
		Map<Long, Double> lastCompletedReceiptsMap;
		List<InvestmentTransaction> lastCompletedReceiptItList;
		RealisationVO realisationAmountSummary;
		
		lastCompletedReceiptItList = investmentTransactionRepository.findLastCompletedReceipts();
		lastCompletedReceiptsMap = new HashMap<Long, Double>(lastCompletedReceiptItList.size());
		for (InvestmentTransaction it : lastCompletedReceiptItList) {
			realisationAmountSummary = miscService.fetchRealisationAmountSummary(it);
			lastCompletedReceiptsMap.put(it.getInvestment().getId(), realisationAmountSummary.getInterestAmount() > 0 ? realisationAmountSummary.getInterestAmount() : it.getDueAmount());
		}
		
		return lastCompletedReceiptsMap;
	}

	private void accumulateAmount(int ind3, double amount) {
		int ind;
		ind = col0Ind * col1Values.length * col2Values.size() * col3Values.length +
				col1Ind * col2Values.size() * col3Values.length +
				col2Ind * col3Values.length +
				ind3;
		dataArray[ind][4] = (double)dataArray[ind][4] + amount;
		breakupRecordList.add(new Object[]{itId, rId, col0Values[col0Ind], col1Values[col1Ind], col2Values.get(col2Ind), col3Values[ind3], amount});
	}
	
	public List<List<Object[]>> anticipatedVsActual(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		Object[] dataArray;
		InvestmentTransaction previousCompletedIt;
		long duration;
		double anticipatedAmount;
		RealisationVO realisationAmountSummary;
		
		final Object headerArray[] = {"Investment", "Txn. Id", "Anticipated", "Actual"};
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);
		
		recordList.add(headerArray);
		for (InvestmentTransaction investmentTransaction : investmentTransactionRepository.findByDueDateBetween(periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			if (investmentTransaction.getTransactionType().getId() != Constants.DVID_TRANSACTION_TYPE_RECEIPT || investmentTransaction.getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_COMPLETED) {
				continue;
			}
			
			previousCompletedIt = investmentTransactionRepository.findPreviousCompletedTransaction(investmentTransaction.getInvestment().getId(), investmentTransaction.getDueDate());
			
			if (previousCompletedIt == null) {
				throw new AppException("No candidate previous IT!", null);
			}
			duration = Duration.between(previousCompletedIt.getDueDate().toLocalDate().atStartOfDay(), investmentTransaction.getDueDate().toLocalDate().atStartOfDay()).toDays();
			if (investmentTransaction.getInvestment().getWorth() != null && investmentTransaction.getInvestment().getRateOfInterest() != null) {
				anticipatedAmount = investmentTransaction.getInvestment().getWorth() * duration / 365 * investmentTransaction.getInvestment().getRateOfInterest() / 100;
				
				dataArray = new Object[4];
				recordList.add(dataArray);
				dataArray[0] = investmentTransaction.getInvestment().getId();
				dataArray[1] = investmentTransaction.getId();
				dataArray[2] = anticipatedAmount;
				realisationAmountSummary = miscService.fetchRealisationAmountSummary(investmentTransaction);
				dataArray[3] = (realisationAmountSummary.getAmount() == 0) ?
						(ObjectUtils.defaultIfNull(investmentTransaction.getDueAmount(), 0).doubleValue() - ObjectUtils.defaultIfNull(investmentTransaction.getReturnedPrincipalAmount(), 0).doubleValue())
						: (realisationAmountSummary.getAmount() - realisationAmountSummary.getReturnedPrincipalAmount());
			}
		}
		
		return reportList;
	}
	
}
