package org.sakuram.persmony.service;

import java.math.BigInteger;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.javatuples.Pair;
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
		List<Object[]> recordList;
		
		recordList = investmentTransactionRepository.findPendingTransactions();
		recordList.add(0, new Object[]{"Date", "Txn. Id", "Investment Id", "Investor", "Product Provider", "Product Name", "Account No.", "Amount", "Based On", "Returned Principal"});
		return listTransactions(recordList);
	}
	
	public List<List<Object[]>> receiptTransactions(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<Object[]> recordList;
		
		recordList = investmentTransactionRepository.findReceiptTransactionsWithinPeriod(periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate());
		recordList.add(0, new Object[]{"Date", "Txn. Id", "Investment Id", "Investor", "Product Provider", "Product Name", "Account No.", "Amount", "Based On", "Returned Principal", "Realised Date", "Realised Amount", "Realised Principal"});
		return listTransactions(recordList);
	}
	
	public List<List<Object[]>> listTransactions(List<Object[]> recordList) {
		List<List<Object[]>> reportList;
		Map<Long, Pair<String, Double>> lastCompletedReceiptsMap;
		Long investmentId;
		
		reportList = new ArrayList<List<Object[]>>(1);
		reportList.add(recordList);

		lastCompletedReceiptsMap = fetchLastCompletedReceipts();
		for (Object[] fields : recordList.subList(1, recordList.size())) {
			if (fields[7] == null) {
				investmentId = ((BigInteger) fields[2]).longValue();
				if (lastCompletedReceiptsMap.containsKey(investmentId)) {
					fields[7] = lastCompletedReceiptsMap.get(investmentId).getValue1();
					fields[8] = lastCompletedReceiptsMap.get(investmentId).getValue0();
				}
			} else {
				fields[8] = "Due";
			}
		}
		
		return reportList;
	}
	
	public List<List<Object[]>> periodSummary(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		int dataRowInd;
		double investmentTransactionAmount, notRealisedPayment, notRealisedReceipt, notRealisedAccrual, settledAmount;
		Map<Long, Pair<String, Double>> lastCompletedReceiptsMap;
		
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
				investmentTransactionAmount = lastCompletedReceiptsMap.get(investmentTransaction.getInvestment().getId()).getValue1();
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

	private Map<Long, Pair<String, Double>> fetchLastCompletedReceipts() {
		Map<Long, Pair<String, Double>> lastCompletedReceiptsMap;
		List<InvestmentTransaction> lastCompletedReceiptItList;
		RealisationVO realisationAmountSummary;
		
		lastCompletedReceiptItList = investmentTransactionRepository.findLastCompletedReceipts();
		lastCompletedReceiptsMap = new HashMap<Long, Pair<String, Double>>(lastCompletedReceiptItList.size());
		for (InvestmentTransaction it : lastCompletedReceiptItList) {
			realisationAmountSummary = miscService.fetchRealisationAmountSummary(it);
			lastCompletedReceiptsMap.put(it.getInvestment().getId(),
					(realisationAmountSummary.getInterestAmount() > 0 ?
							Pair.with("Last Interest", realisationAmountSummary.getInterestAmount()) :
							(realisationAmountSummary.getAmount() > 0 ?
									Pair.with("Last Credit", realisationAmountSummary.getAmount()) :
									Pair.with("Due", it.getDueAmount())
							)
					)
			);
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
	
	public List<List<Object[]>> advanceTaxLiability(int fyStartYear) throws ParseException {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		Double[] investorSummary;
		java.sql.Date fyStartDate, fyEndDate, forInterestStartDate, forInterestEndDate;
		long fyDays, interestDays, investor;
		Map<Long, Double[]> investorDvIdToTaxLiabilitysMap;
		final Object headerArray[] = {"Investor", "By Date", "Income", "Tax Liability", "TDS"};
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);
		
		recordList.add(headerArray);
		investorDvIdToTaxLiabilitysMap = new HashMap<Long, Double[]>();
		fyStartDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(fyStartYear + "-04-01").getTime());
		fyEndDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse((fyStartYear + 1) + "-03-31").getTime());
		fyDays = Duration.between(fyStartDate.toLocalDate().atStartOfDay(), fyEndDate.toLocalDate().atStartOfDay()).toDays() + 1;
		for (Investment investment : investmentRepository.findByInvestmentEndDateGreaterThanEqualAndInvestmentStartDateLessThanEqual(fyStartDate, fyEndDate)) {
			// Additional Filters not used in DB, now applied in Java
			
			if (investment.getClosureDate() != null && investment.getClosureDate().before(fyStartDate)) {
				continue;
			}
		
			if (investment.getDefaultTaxGroup() != null && Constants.TAXFREE_GROUP_LIST.contains(investment.getDefaultTaxGroup().getId())) {
				continue;
			}
			
			// Income
			forInterestStartDate = investment.getInvestmentStartDate().before(fyStartDate) ? fyStartDate : investment.getInvestmentStartDate();
			forInterestEndDate = investment.getInvestmentEndDate().after(fyEndDate) ? fyEndDate : investment.getInvestmentEndDate();
			interestDays = Duration.between(forInterestStartDate.toLocalDate().atStartOfDay(), forInterestEndDate.toLocalDate().atStartOfDay()).toDays() + 1;
			
			if (Constants.INVESTOR_MAP.containsKey(investment.getInvestor().getId())) {
				investor = Constants.INVESTOR_MAP.get(investment.getInvestor().getId());
			} else { // Joint investors, take the first one
				investor = investment.getInvestor().getId();
			}
			investorSummary = investorDvIdToTaxLiabilitysMap.get(investor);
			if (investorSummary == null) {
				investorSummary = new Double[] {0D, 0D};
			}
			// System.out.print(investor + "\t" + investment.getId() + "\t" + investment.getWorth() + "\t" + investment.getRateOfInterest() + "\t" + interestDays + "\t" + fyDays + "\t");
			investorSummary[0] += ObjectUtils.defaultIfNull(investment.getWorth(), 0).doubleValue() *
					ObjectUtils.defaultIfNull(investment.getRateOfInterest(), 0).doubleValue() / 100 *
					interestDays / fyDays;
			// TDS
			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
				if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_COMPLETED) {
					if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL &&
							investmentTransaction.getDueDate().compareTo(fyStartDate) >= 0 &&
							investmentTransaction.getDueDate().compareTo(fyEndDate) <= 0) {
						investorSummary[1] += ObjectUtils.defaultIfNull(investmentTransaction.getTdsAmount(), 0D).doubleValue();
					}
					else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
						for (Realisation realisation : investmentTransaction.getRealisationList()) {
							if (realisation.getRealisationDate().compareTo(fyStartDate) >= 0 &&
								realisation.getRealisationDate().compareTo(fyEndDate) <= 0) {
								investorSummary[1] += ObjectUtils.defaultIfNull(realisation.getTdsAmount(), 0D).doubleValue();
							}
						}
					}
				}
				// System.out.print(investorSummary[1]);
			}
			// System.out.println();
			investorDvIdToTaxLiabilitysMap.put(investor, investorSummary);
		}
		
		for (Map.Entry<Long, Double[]> investorEntry : investorDvIdToTaxLiabilitysMap.entrySet()) {
			recordList.add(new Object[] {
					Constants.domainValueCache.get(investorEntry.getKey()).getValue(),
					"",
					String.format("%.0f", investorEntry.getValue()[0]),
					String.format("%.0f", investorEntry.getValue()[0] * 0.3),
					String.format("%.0f", investorEntry.getValue()[1])
			});
			for (Object[] taxPercentage : Constants.TAX_PERCENTAGE_ARRAY) {
				recordList.add(new Object[] {
						"",
						taxPercentage[0],
						"",
						String.format("%.0f", Double.parseDouble(taxPercentage[1].toString()) * investorEntry.getValue()[0] * 0.3)
				});
			}			
		}
		
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);
		recordList.add(new Object[] {"Figures are just indicative."});
		recordList.add(new Object[] {"Tax Slabs are not taken into consideration. Flat 30% is used to compute the tax. There's no surcharge."});
		recordList.add(new Object[] {"Some of the income might be post TDS, but not known yet, and hence treated as 0 TDS."});
		recordList.add(new Object[] {"Incomes and TDS outside PersMony are not included."});
		return reportList;
	}
}
