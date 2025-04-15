package org.sakuram.persmony.service;

import java.math.BigInteger;
import java.sql.Date;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.bean.SavingsAccountTransaction;
import org.sakuram.persmony.bean.SbAcTxnCategory;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.repository.RealisationRepository;
import org.sakuram.persmony.repository.SbAcTxnCategoryRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.DomainValueFlags;
import org.sakuram.persmony.valueobject.DetailsForTaxFilingRequestVO;
import org.sakuram.persmony.valueobject.DvFlagsAccountVO;
import org.sakuram.persmony.valueobject.DvFlagsInvestorVO;
import org.sakuram.persmony.valueobject.DvFlagsSbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.DvFlagsVO;
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
	@Autowired
	SbAcTxnCategoryRepository sbAcTxnCategoryRepository;
	@Autowired
	RealisationRepository realisationRepository;

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
	
	
	public List<List<Object[]>> incomeVsSpend(PeriodSummaryCriteriaVO periodSummaryCriteriaVO) {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		DvFlagsSbAcTxnCategoryVO dvFlagsSbAcTxnCategoryVO;
		double incomeAmount, spendAmount, otherAmount, amount;
		String treatment;
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);
		
		recordList.add(new Object[] {"Income Vs. Spend Analysis"});
		recordList.add(new Object[] {"Period", periodSummaryCriteriaVO.getFromDate().toString(), periodSummaryCriteriaVO.getToDate().toString()});
		recordList.add(new Object[1]);
		
		recordList.add(new Object[] {}); // Indices 3 to 5 to be replaced later
		recordList.add(new Object[] {});
		recordList.add(new Object[] {});
		recordList.add(new Object[] {});
		
		incomeAmount = 0;
		spendAmount= 0;
		otherAmount = 0;
		recordList.add(new Object[] {"CatFlag", "Account", "SAT Id", "SATC/R Id", "Category", "EAR", "Booking", "SATC Amount", "Treatment", "Amount"});
		for (SbAcTxnCategory sbAcTxnCategory : sbAcTxnCategoryRepository.findBySavingsAccountTransactionTransactionDateBetweenOrderBySavingsAccountTransactionTransactionDateAscSavingsAccountTransactionIdAsc(
				periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			dvFlagsSbAcTxnCategoryVO = (DvFlagsSbAcTxnCategoryVO) DomainValueFlags.getDvFlagsVO(sbAcTxnCategory.getTransactionCategory());
			if (dvFlagsSbAcTxnCategoryVO.getIOrC() == DvFlagsSbAcTxnCategoryVO.IOrC.INCOME) {
			}
			amount = Math.abs(sbAcTxnCategory.getAmount());
			switch(dvFlagsSbAcTxnCategoryVO.getIOrC()) {
			case INCOME:
				incomeAmount += amount;
				treatment = "I";
				break;
			case EXPENSE:
				spendAmount += amount;
				treatment = "S";
				break;
			case BOOKING_DEPENDENT:
				if (sbAcTxnCategory.getSavingsAccountTransaction().getBooking().getId() == Constants.DVID_BOOKING_CREDIT &&
						sbAcTxnCategory.getAmount() >= 0 || sbAcTxnCategory.getSavingsAccountTransaction().getBooking().getId() == Constants.DVID_BOOKING_DEBIT &&
						sbAcTxnCategory.getAmount() < 0) {
					incomeAmount += amount;
					treatment = "I";
				} else {
					spendAmount += amount;
					treatment = "S";
				}
				break;
			default: 	// case NONE
				if (sbAcTxnCategory.getSavingsAccountTransaction().getBooking().getId() == Constants.DVID_BOOKING_CREDIT &&
						sbAcTxnCategory.getAmount() >= 0 || sbAcTxnCategory.getSavingsAccountTransaction().getBooking().getId() == Constants.DVID_BOOKING_DEBIT &&
						sbAcTxnCategory.getAmount() < 0) {
					otherAmount += amount;
					treatment = "O+";
				} else {
					otherAmount -= amount;
					treatment = "O-";
				}
				break;
			}
			recordList.add(new Object[] {dvFlagsSbAcTxnCategoryVO.getIorCString(), sbAcTxnCategory.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), sbAcTxnCategory.getSavingsAccountTransaction().getId(), sbAcTxnCategory.getId(),
					sbAcTxnCategory.getTransactionCategory().getValue(), sbAcTxnCategory.getEndAccountReference(),
					sbAcTxnCategory.getSavingsAccountTransaction().getBooking().getValue(), sbAcTxnCategory.getAmount(), treatment, amount});
		}
		
		// DTI transactions
		for (Realisation realisation : realisationRepository.findByRealisationDateBetweenOrderByRealisationDateAscSavingsAccountTransactionIdAsc(
				periodSummaryCriteriaVO.getFromDate(), periodSummaryCriteriaVO.getToDate())) {
			if (realisation.getRealisationType().getId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
				continue;
			}
			
			if (realisation.getInvestmentTransaction().getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_PAYMENT) {
				// Investment, not Expense
				otherAmount -= realisation.getAmount();
				recordList.add(new Object[] {"DTI", realisation.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), realisation.getSavingsAccountTransaction().getId(), realisation.getId(),
						"Investment", null,
						realisation.getSavingsAccountTransaction().getBooking().getValue(), realisation.getAmount(), "O-", realisation.getAmount()});
			} else if (realisation.getInvestmentTransaction().getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
				if (realisation.getReturnedPrincipalAmount() == null && realisation.getInterestAmount() == null && realisation.getTdsAmount() != null) {
					incomeAmount += realisation.getAmount();
					recordList.add(new Object[] {"DTI", realisation.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), realisation.getSavingsAccountTransaction().getId(), realisation.getId(),
							"Approx. Interest", null,
							realisation.getSavingsAccountTransaction().getBooking().getValue(), realisation.getAmount(), "I", realisation.getAmount()});
				} else {
					if (realisation.getReturnedPrincipalAmount() != null) {
						otherAmount += realisation.getReturnedPrincipalAmount();
						recordList.add(new Object[] {"DTI", realisation.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), realisation.getSavingsAccountTransaction().getId(), realisation.getId(),
								"Principal", null,
								realisation.getSavingsAccountTransaction().getBooking().getValue(), realisation.getReturnedPrincipalAmount(), "O+", realisation.getReturnedPrincipalAmount()});
					}
					if (realisation.getInterestAmount() != null) {
						incomeAmount += realisation.getInterestAmount();
						recordList.add(new Object[] {"DTI", realisation.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), realisation.getSavingsAccountTransaction().getId(), realisation.getId(),
								"Interest", null,
								realisation.getSavingsAccountTransaction().getBooking().getValue(), realisation.getInterestAmount(), "I", realisation.getInterestAmount()});
					}
					if (realisation.getTdsAmount() != null) {
						spendAmount += realisation.getTdsAmount();
						recordList.add(new Object[] {"DTI", realisation.getSavingsAccountTransaction().getBankAccountOrInvestor().getValue(), realisation.getSavingsAccountTransaction().getId(), realisation.getId(),
								"TDS", null,
								realisation.getSavingsAccountTransaction().getBooking().getValue(), realisation.getTdsAmount(), "S", realisation.getTdsAmount()});
					}
				}
			}
		}
		
		recordList.set(3, new Object[] {"Income", incomeAmount});
		recordList.set(4, new Object[] {"Expense", spendAmount});
		recordList.set(5, new Object[] {"Other", otherAmount});
		
		return reportList;
	}
	
	public List<List<Object[]>> advanceTaxLiability(int fyStartYear) throws ParseException {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		Map<Long, Double[]> investorDvIdToTaxLiabilitysMap;
		final Object headerArray[] = {"Investor", "By Date", "Income", "Tax Liability", "TDS"};

		investorDvIdToTaxLiabilitysMap = fetchAccrualForFy(fyStartYear, null).getValue0();
		
		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);
		
		recordList.add(headerArray);
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

	public List<List<Object[]>> detailsForTaxFiling(DetailsForTaxFilingRequestVO incomeTaxFilingDetailsRequestVO) throws ParseException {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		List<List<Object[]>> reportTableList;
		Object[] previousReportRow;
		java.sql.Date fyStartDate, fyEndDate, realisationDate;
		DvFlagsVO dvFlagsVO;
		DvFlagsAccountVO dvFlagsAccountVO;
		DomainValue investorDomainValue;
		SavingsAccountTransaction savingsAccountTransaction;
		List<List<Object[]>> accrualDetailsForInvestor;

		final int TABLE_IND_HP_RENT = 0;
		final int TABLE_IND_HP_TAX_RENTAL_INCOME = 1;
		final int TABLE_IND_HP_TAX_PROPERTY_WATER = 2;
		final int TABLE_IND_OS_DIVIDEND = 3;
		final int TABLE_IND_OS_SB_INTEREST = 4;
		final int TABLE_IND_OS_BANK_PO_DEPOSIT_INTEREST = 5;
		final int TABLE_IND_OS_IT_REFUND_INTEREST = 6;
		final int TABLE_IND_OS_OTHER_INTEREST = 7;
		final int TABLE_IND_VI_A_INVESTMENT = 8;
		final int TABLE_IND_80G = 9;
		final int TABLE_IND_EI_INTEREST = 10;
		final int TABLE_IND_EI_OTHER = 11;
		final int TABLE_IND_TAX_PAYMENT_ADV_SA = 12;
		final int TABLE_IND_80D_HEALTH_INSURANCE = 13;
		final int NO_OF_TABLES = 14;

		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);

		reportTableList = new ArrayList<List<Object[]>>(NO_OF_TABLES);
		for(int i = 0; i < NO_OF_TABLES; i++) {
			reportTableList.add(new ArrayList<Object[]>());
		}

		fyStartDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(incomeTaxFilingDetailsRequestVO.getFyStartYear() + "-04-01").getTime());
		fyEndDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse((incomeTaxFilingDetailsRequestVO.getFyStartYear() + 1) + "-03-31").getTime());
		// TODO: What-if the date and FY as per the provider don't match?

		for (SbAcTxnCategory sbAcTxnCategory : sbAcTxnCategoryRepository.findBySavingsAccountTransactionTransactionDateBetweenOrderBySavingsAccountTransactionTransactionDateAscSavingsAccountTransactionIdAsc(
				java.sql.Date.valueOf(fyStartDate.toLocalDate().minusDays(10)),
				java.sql.Date.valueOf(fyEndDate.toLocalDate().plusDays(10)))) {
			// To use value date, if available, 10 days on each side
			
			savingsAccountTransaction = sbAcTxnCategory.getSavingsAccountTransaction();

			if (ObjectUtils.defaultIfNull(savingsAccountTransaction.getValueDate(), savingsAccountTransaction.getTransactionDate()).before(fyStartDate) ||
					ObjectUtils.defaultIfNull(savingsAccountTransaction.getValueDate(), savingsAccountTransaction.getTransactionDate()).after(fyEndDate)) {
				continue;
			}
			
			dvFlagsAccountVO = null;
			investorDomainValue = savingsAccountTransaction.getBankAccountOrInvestor();
			dvFlagsVO = DomainValueFlags.getDvFlagsVO(savingsAccountTransaction.getBankAccountOrInvestor());
			if (dvFlagsVO instanceof DvFlagsAccountVO) {
				dvFlagsAccountVO = (DvFlagsAccountVO) dvFlagsVO;
				investorDomainValue = Constants.domainValueCache.get(dvFlagsAccountVO.getInvestorDvId());
			} else if (dvFlagsVO != null && !(dvFlagsVO instanceof DvFlagsInvestorVO)) {
				throw new AppException("Unexpected Bank Account / Investor", null);
			}

			if (Constants.INVESTOR_TO_PRIMARY_MAP.get(investorDomainValue.getId()) != incomeTaxFilingDetailsRequestVO.getInvestorDvId() &&
					(sbAcTxnCategory.getEndAccountReference() == null ||
					!sbAcTxnCategory.getEndAccountReference().equals(String.valueOf(incomeTaxFilingDetailsRequestVO.getInvestorDvId())))) {
				continue;
			}

			if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_PROPERTY_RENT) {
				reportTableList.get(TABLE_IND_HP_RENT).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_TAX_RENTAL_INCOME) {
				reportTableList.get(TABLE_IND_HP_TAX_RENTAL_INCOME).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_TAX_PROPERTY ||
					sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_TAX_WATER) {
				reportTableList.get(TABLE_IND_HP_TAX_PROPERTY_WATER).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(),
						(sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_TAX_PROPERTY ? "Property" : "Water"),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_EQUITY_DIVIDEND ||
					sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_MUTUAL_FUND_DIVIDEND) {
				if (reportTableList.get(TABLE_IND_OS_DIVIDEND).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_OS_DIVIDEND).get(reportTableList.get(TABLE_IND_OS_DIVIDEND).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals(sbAcTxnCategory.getEndAccountReference()) &&
						previousReportRow[4].equals("")) {
					previousReportRow[4] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1);
				} else {
					reportTableList.get(TABLE_IND_OS_DIVIDEND).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							sbAcTxnCategory.getEndAccountReference(),
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1), ""});
				}
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_EQUITY_DIVIDEND_TDS ||
					sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_MUTUAL_FUND_DIVIDEND_TDS) {
				if (reportTableList.get(TABLE_IND_OS_DIVIDEND).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_OS_DIVIDEND).get(reportTableList.get(TABLE_IND_OS_DIVIDEND).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals(sbAcTxnCategory.getEndAccountReference()) &&
						previousReportRow[5].equals("")) {
					previousReportRow[5] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1);
				} else {
					reportTableList.get(TABLE_IND_OS_DIVIDEND).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							sbAcTxnCategory.getEndAccountReference(), "",
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
				}
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_SB_INTEREST &&
					dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_SAVINGS)) {
				reportTableList.get(TABLE_IND_OS_SB_INTEREST).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						savingsAccountTransaction.getBankAccountOrInvestor().getValue(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_SB_INTEREST &&
					dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_PPF)) {
				reportTableList.get(TABLE_IND_EI_INTEREST).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"PPF Interest",
						savingsAccountTransaction.getBankAccountOrInvestor().getValue(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_IT_REFUND_INTEREST) {
				if (reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).get(reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals(Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue()) &&
						previousReportRow[4].equals("")) {
					previousReportRow[4] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1);
				} else {
					reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(),
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1), ""});
				}
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_IT_REFUND_INTEREST_TDS) {
				if (reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).get(reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals(Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue()) &&
						previousReportRow[5].equals("")) {
					previousReportRow[5] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1);
				} else {
					reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(), "",
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
				}
			} else if (dvFlagsAccountVO != null && dvFlagsAccountVO.getAccType().equals(Constants.ACCOUNT_TYPE_PPF)) {
				reportTableList.get(TABLE_IND_VI_A_INVESTMENT).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"PPF – 80C",
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_NPS_TIER_1) {
				reportTableList.get(TABLE_IND_VI_A_INVESTMENT).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"NPS Tier 1 – 80CCD(1B)",
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_NPS_TIER_2) {
				reportTableList.get(TABLE_IND_VI_A_INVESTMENT).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"NPS Tier 2 – 80C",
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_DONATION) {
				reportTableList.get(TABLE_IND_80G).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						Constants.domainValueCache.get(Long.parseLong(sbAcTxnCategory.getEndAccountReference())).getValue(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_EQUITY_EXEMPT_DIVIDEND) {
				reportTableList.get(TABLE_IND_EI_OTHER).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"Exempt Dividend",
						sbAcTxnCategory.getEndAccountReference(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1), ""});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_EQUITY_BUYBACK) {
				if (reportTableList.get(TABLE_IND_EI_OTHER).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_EI_OTHER).get(reportTableList.get(TABLE_IND_EI_OTHER).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals("Buyback CG - 10(34A)") &&
						previousReportRow[4].equals(sbAcTxnCategory.getEndAccountReference()) &&
						previousReportRow[5].equals("")) {
					previousReportRow[5] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1);
				} else {
					reportTableList.get(TABLE_IND_EI_OTHER).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							"Buyback CG - 10(34A)",
							sbAcTxnCategory.getEndAccountReference(),
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1), ""});
				}
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_EQUITY_BUYBACK_TDS) {
				if (reportTableList.get(TABLE_IND_EI_OTHER).size() > 0) {
					previousReportRow = reportTableList.get(TABLE_IND_EI_OTHER).get(reportTableList.get(TABLE_IND_EI_OTHER).size() - 1);
				} else {
					previousReportRow = new Object[] {"", "", "", "", "", "", ""};
				}
				if (previousReportRow[2].equals(savingsAccountTransaction.getTransactionDate()) &&
						previousReportRow[3].equals("Buyback CG - 10(34A)") &&
						previousReportRow[4].equals(sbAcTxnCategory.getEndAccountReference()) &&
						previousReportRow[6].equals("")) {
					previousReportRow[6] = sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1);
				} else {
					reportTableList.get(TABLE_IND_EI_OTHER).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
							"Buyback CG - 10(34A)",
							sbAcTxnCategory.getEndAccountReference(), "",
							sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
				}
			} else if ((sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_GIFT ||
					sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_ON_BEHALF_GIFT) &&
					(savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ||
					savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_DEBIT &&
					sbAcTxnCategory.getEndAccountReference().equals(String.valueOf(incomeTaxFilingDetailsRequestVO.getInvestorDvId())))) {
				reportTableList.get(TABLE_IND_EI_OTHER).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						"Gift Received – 56(2)(vi)(a)", "", sbAcTxnCategory.getAmount(), ""});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_INCOME_TAX) {
				reportTableList.get(TABLE_IND_TAX_PAYMENT_ADV_SA).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			} else if (sbAcTxnCategory.getTransactionCategory().getId() == Constants.DVID_TRANSACTION_CATEGORY_OTHERS &&
					sbAcTxnCategory.getEndAccountReference().equals(Constants.END_ACCOUNT_REFERENCE_HEALTH_INSURANCE)) {
				reportTableList.get(TABLE_IND_80D_HEALTH_INSURANCE).add(new Object[] {"", "", savingsAccountTransaction.getTransactionDate(),
						sbAcTxnCategory.getAmount() * (savingsAccountTransaction.getBooking().getId() == Constants.DVID_BOOKING_CREDIT ? -1 : 1)});
			}
		}

		for (Realisation realisation : realisationRepository.retrieveRealisationsForIt(fyStartDate, fyEndDate, incomeTaxFilingDetailsRequestVO.getInvestorDvId())) {
			realisationDate = ObjectUtils.defaultIfNull(realisation.getAccountedRealisationDate(), realisation.getRealisationDate());

			if (realisation.getInvestmentTransaction().getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
				if (realisation.getInvestmentTransaction().getTaxGroup() != null && Constants.DVID_TAX_GROUP_EXEMPTED_LIST.contains(realisation.getInvestmentTransaction().getTaxGroup().getId())) {
					reportTableList.get(TABLE_IND_EI_INTEREST).add(new Object[] {"", "", realisationDate,
							realisation.getInvestmentTransaction().getInvestment().getProductProvider().getValue(),
							formAccountNo(realisation.getInvestmentTransaction().getInvestment()),
							ObjectUtils.defaultIfNull(realisation.getInterestAmount(), realisation.getAmount())
							});
				} else if (realisation.getInvestmentTransaction().getTaxGroup() != null && Constants.DVID_TAX_GROUP_PO_BANK_DEPOSIT_INTEREST == realisation.getInvestmentTransaction().getTaxGroup().getId()) {
					reportTableList.get(TABLE_IND_OS_BANK_PO_DEPOSIT_INTEREST).add(new Object[] {"", "", realisationDate,
							realisation.getInvestmentTransaction().getInvestment().getProductProvider().getValue(),
							formAccountNo(realisation.getInvestmentTransaction().getInvestment()),
							ObjectUtils.defaultIfNull(realisation.getInterestAmount(), realisation.getAmount()),
							realisation.getTdsAmount()
							});
				} else {
					reportTableList.get(TABLE_IND_OS_OTHER_INTEREST).add(new Object[] {"", "", realisationDate,
							realisation.getInvestmentTransaction().getInvestment().getProductProvider().getValue(),
							formAccountNo(realisation.getInvestmentTransaction().getInvestment()),
							ObjectUtils.defaultIfNull(realisation.getInterestAmount(), realisation.getAmount()),
							realisation.getTdsAmount()
							});
				}
			}
		}

		accrualDetailsForInvestor = fetchAccrualForFy(incomeTaxFilingDetailsRequestVO.getFyStartYear(), incomeTaxFilingDetailsRequestVO.getInvestorDvId()).getValue1();

		recordList.add(new Object[] {"Details for filing Income Tax Returns"});
		recordList.add(new Object[] {"Assessee", Constants.domainValueCache.get(incomeTaxFilingDetailsRequestVO.getInvestorDvId()).getValue()});
		recordList.add(new Object[] {"Period", fyStartDate.toString(), fyEndDate.toString()});
		recordList.add(new Object[1]);
		
		recordList.add(new Object[] {"SCHEDULE SALARY"});
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE HP"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Rent"});
		recordList.add(new Object[] {"", "", "Date", "Property", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_HP_RENT));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Tax On Rent"});
		recordList.add(new Object[] {"", "", "Date", "Property", "Tax"});
		recordList.addAll(reportTableList.get(TABLE_IND_HP_TAX_RENTAL_INCOME));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Property/Water Tax"});
		recordList.add(new Object[] {"", "", "Date", "Property", "Type", "Tax"});
		recordList.addAll(reportTableList.get(TABLE_IND_HP_TAX_PROPERTY_WATER));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE CG"});
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE OS"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Dividend"});
		recordList.add(new Object[] {"", "", "Date", "ISIN", "Amount", "TDS"});
		recordList.addAll(reportTableList.get(TABLE_IND_OS_DIVIDEND));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "SB Interest"});
		recordList.add(new Object[] {"", "", "Date", "Account No.", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_OS_SB_INTEREST));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Bank/PO Deposit Interest"});
		recordList.add(new Object[] {"", "", "Date", "Provider", "Account No.", "Amount", "TDS"});
		recordList.addAll(reportTableList.get(TABLE_IND_OS_BANK_PO_DEPOSIT_INTEREST));
		recordList.add(new Object[1]);
		recordList.addAll(accrualDetailsForInvestor.get(0));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "IT Refund Interest"});
		recordList.add(new Object[] {"", "", "Date", "PAN", "Amount", "TDS"}); // PAN is not really required, it's used for programming convenience
		recordList.addAll(reportTableList.get(TABLE_IND_OS_IT_REFUND_INTEREST));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Other Interest"});
		recordList.add(new Object[] {"", "", "Date", "Provider", "Account No.", "Amount", "TDS"});
		recordList.addAll(reportTableList.get(TABLE_IND_OS_OTHER_INTEREST));
		recordList.add(new Object[1]);
		recordList.addAll(accrualDetailsForInvestor.get(1));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE VI-A"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Part B"});
		recordList.add(new Object[] {"", "", "Date", "Type", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_VI_A_INVESTMENT));
		recordList.add(new Object[1]);
		recordList.add(new Object[] {"", "", "Refer Schedule 80D for Health Insurance, 80G for Donations"});
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Part C"});
		recordList.add(new Object[1]);
		recordList.add(new Object[] {"", "", "Refer Schedule OS - SB Interest for 80TTA"});
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE 80G"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Donation"});
		recordList.add(new Object[] {"", "", "Date", "Donee", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_80G));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE SI"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "", "Refer Schedule CG for LTCG, STCG"});
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE EI"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Interest"});
		recordList.add(new Object[] {"", "", "Date", "Provider", "Account No.", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_EI_INTEREST));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Other"});
		recordList.add(new Object[] {"", "", "Date", "Type", "Reference", "Amount", "TDS"});
		recordList.addAll(reportTableList.get(TABLE_IND_EI_OTHER));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"TAX PAYMENTS"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Advance/Self Assessment Tax"});
		recordList.add(new Object[] {"", "", "Date", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_TAX_PAYMENT_ADV_SA));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"SCHEDULE 80D"});
		recordList.add(new Object[1]);

		recordList.add(new Object[] {"", "Health Insurance"});
		recordList.add(new Object[] {"", "", "Date", "Amount"});
		recordList.addAll(reportTableList.get(TABLE_IND_80D_HEALTH_INSURANCE));
		recordList.add(new Object[1]);
		recordList.add(new Object[1]);

		return reportList;
	}

	public List<List<Object[]>> readinessForTaxFiling(DetailsForTaxFilingRequestVO incomeTaxFilingDetailsRequestVO) throws ParseException {
		List<List<Object[]>> reportList;
		List<Object[]> recordList;
		java.sql.Date fyStartDate, fyEndDate;
		double expectedPrincipal, expectedInterest, expectedTds, actualAmount;
		InvestmentTransaction lastInvestmentTransaction;

		reportList = new ArrayList<List<Object[]>>(1);
		recordList = new ArrayList<Object[]>();
		reportList.add(recordList);

		fyStartDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(incomeTaxFilingDetailsRequestVO.getFyStartYear() + "-04-01").getTime());
		fyEndDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse((incomeTaxFilingDetailsRequestVO.getFyStartYear() + 1) + "-03-31").getTime());

		recordList.add(new Object[] {"Readiness for filing Income Tax Returns"});
		recordList.add(new Object[] {"Assessee", Constants.domainValueCache.get(incomeTaxFilingDetailsRequestVO.getInvestorDvId()).getValue()});
		recordList.add(new Object[] {"Period", fyStartDate.toString(), fyEndDate.toString()});
		recordList.add(new Object[1]);
		recordList.add(new Object[] {"I.id", "IT.id", "R.id", "Type", "Date", "Remarks", "Details..."});

		for (Investment investment : investmentRepository.retrieveInvestmentActiveWithinPeriod(fyStartDate, fyEndDate)) {
			// Additional Filters not used in DB, now applied in Java

			if (investment.getClosureDate() != null && investment.getClosureDate().before(fyStartDate)) {
				continue;
			}

			if (Constants.INVESTOR_TO_PRIMARY_MAP.get(investment.getInvestor().getId()) != incomeTaxFilingDetailsRequestVO.getInvestorDvId()) {
				continue;
			}

			expectedPrincipal = 0;
			expectedInterest = 0;
			expectedTds = 0;
			actualAmount = 0;
			lastInvestmentTransaction = null;

			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {

				if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING &&
						investmentTransaction.getDueDate().compareTo(fyStartDate) >= 0 &&
						investmentTransaction.getDueDate().compareTo(fyEndDate) <= 0) {
					recordList.add(new Object[] {investment.getId(), investmentTransaction.getId(), null,
							investmentTransaction.getTransactionType().getValue(),
							investmentTransaction.getDueDate(),
							"Pending for Realisation",
							});
				} else if (investmentTransaction.getDueDate().compareTo(fyEndDate) > 0) {
					break;
				} else if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_COMPLETED) {
					if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
						if (lastInvestmentTransaction != null &&
								lastInvestmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
							if (Math.abs(actualAmount - expectedPrincipal - expectedInterest + expectedTds) > Constants.TOLERATED_DIFFERENCE_AMOUNT) {
								recordList.add(new Object[] {investment.getId(), lastInvestmentTransaction.getId(), null,
										lastInvestmentTransaction.getTransactionType().getValue(),
										lastInvestmentTransaction.getDueDate(),
										"Incomplete Data",
										expectedPrincipal,
										expectedInterest,
										expectedTds,
										actualAmount});
							}
							expectedPrincipal = 0;
							expectedInterest = 0;
							expectedTds = 0;
						}
						actualAmount = 0;
						expectedPrincipal += ObjectUtils.defaultIfNull(investmentTransaction.getReturnedPrincipalAmount(), 0D);
						expectedInterest += ObjectUtils.defaultIfNull(investmentTransaction.getInterestAmount(), 0D);
						expectedTds += ObjectUtils.defaultIfNull(investmentTransaction.getTdsAmount(), 0D).doubleValue();
						lastInvestmentTransaction = investmentTransaction;
					} else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
						for (Realisation realisation : investmentTransaction.getRealisationList()) {
							expectedPrincipal += ObjectUtils.defaultIfNull(realisation.getReturnedPrincipalAmount(), 0D);
							expectedInterest += ObjectUtils.defaultIfNull(realisation.getInterestAmount(), 0D);
							expectedTds += ObjectUtils.defaultIfNull(realisation.getTdsAmount(), 0D).doubleValue();
							actualAmount += ObjectUtils.defaultIfNull(realisation.getAmount(), 0D);
						}
						lastInvestmentTransaction = investmentTransaction;
					}
				}
			}

			if (lastInvestmentTransaction != null &&
					lastInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_COMPLETED &&
					lastInvestmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT &&
					Math.abs(actualAmount - expectedPrincipal - expectedInterest + expectedTds) > Constants.TOLERATED_DIFFERENCE_AMOUNT) {
				recordList.add(new Object[] {investment.getId(), lastInvestmentTransaction.getId(), null,
						lastInvestmentTransaction.getTransactionType().getValue(),
						lastInvestmentTransaction.getDueDate(),
						"Incomplete Data",
						expectedPrincipal,
						expectedInterest,
						expectedTds,
						actualAmount});
			}
		}

		recordList.addAll(fetchAccrualForFy(incomeTaxFilingDetailsRequestVO.getFyStartYear(), incomeTaxFilingDetailsRequestVO.getInvestorDvId()).getValue2());


		return reportList;
	}

	private Triplet<Map<Long, Double[]>, List<List<Object[]>>, List<Object[]>> fetchAccrualForFy(int fyStartYear, Long investorDvId) throws ParseException {
		Double investorSummary[];
		java.sql.Date fyStartDate, fyEndDate, forInterestStartDate, forInterestEndDate;
		long fyDays, interestDays, investmentPrimaryInvestorDvId;
		Map<Long, Double[]> investorDvIdToTaxLiabilitysMap;
		List<List<Object[]>> accrualDetailsForInvestor;
		List<Object[]> anticipatedAccrualDetailsForInvestor;
		Date realisationDate, investmentLastDate;
		double investmentYearEndAccrual, investmentTransactionAmount;
		int tdsGroupInd;

		investorDvIdToTaxLiabilitysMap = new HashMap<Long, Double[]>();
		accrualDetailsForInvestor = new ArrayList<List<Object[]>>(2);
		accrualDetailsForInvestor.add(new ArrayList<Object[]>());
		accrualDetailsForInvestor.add(new ArrayList<Object[]>());
		anticipatedAccrualDetailsForInvestor = new ArrayList<Object[]>();

		fyStartDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(fyStartYear + "-04-01").getTime());
		fyEndDate = new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse((fyStartYear + 1) + "-03-31").getTime());
		fyDays = Duration.between(fyStartDate.toLocalDate().atStartOfDay(), fyEndDate.toLocalDate().atStartOfDay()).toDays() + 1;
		for (Investment investment : investmentRepository.retrieveInvestmentActiveWithinPeriod(fyStartDate, fyEndDate)) {
			// Additional Filters not used in DB, now applied in Java

			if (investment.getClosureDate() != null && investment.getClosureDate().before(fyStartDate)) {
				continue;
			}

			if (investment.getDefaultTaxGroup() != null && Constants.DVID_TAX_GROUP_EXEMPTED_LIST.contains(investment.getDefaultTaxGroup().getId())) {
				continue;
			}

			investmentPrimaryInvestorDvId = Constants.INVESTOR_TO_PRIMARY_MAP.get(investment.getInvestor().getId());
			if (investorDvId != null && investmentPrimaryInvestorDvId != investorDvId.longValue()) {
				continue;
			}

			// Income
			forInterestStartDate = (investment.getInvestmentStartDate() == null || investment.getInvestmentStartDate().before(fyStartDate) ? fyStartDate : investment.getInvestmentStartDate());
			investmentLastDate = ObjectUtils.defaultIfNull(investment.getClosureDate(), investment.getInvestmentEndDate());
			forInterestEndDate = (investmentLastDate == null || investmentLastDate.after(fyEndDate) ? fyEndDate : investmentLastDate);
			interestDays = Duration.between(forInterestStartDate.toLocalDate().atStartOfDay(), forInterestEndDate.toLocalDate().atStartOfDay()).toDays();

			investorSummary = investorDvIdToTaxLiabilitysMap.get(investmentPrimaryInvestorDvId);
			if (investorSummary == null) {
				investorSummary = new Double[] {0D, 0D};
			}
			investmentYearEndAccrual = ObjectUtils.defaultIfNull(investment.getWorth(), 0).doubleValue() *
					ObjectUtils.defaultIfNull(investment.getRateOfInterest(), 0).doubleValue() / 100 *
					interestDays / fyDays;
			investorSummary[0] += investmentYearEndAccrual;
			if (investment.getDefaultTaxGroup() != null && Constants.DVID_TAX_GROUP_PO_BANK_DEPOSIT_INTEREST == investment.getDefaultTaxGroup().getId()) {
				tdsGroupInd = 0;
			} else {
				tdsGroupInd = 1;
			}	// TDS Group defined at investment transaction level is not utilised!!!

			// TDS
			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
				if (investmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_COMPLETED) {
					if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL &&
							investmentTransaction.getDueDate().compareTo(fyStartDate) >= 0 &&
							investmentTransaction.getDueDate().compareTo(fyEndDate) <= 0) {
						investorSummary[1] += ObjectUtils.defaultIfNull(investmentTransaction.getTdsAmount(), 0D).doubleValue();
						investmentTransactionAmount = ObjectUtils.defaultIfNull(investmentTransaction.getInterestAmount(), 0D);
						investmentYearEndAccrual -= investmentTransactionAmount;
						accrualDetailsForInvestor.get(tdsGroupInd).add(new Object[] {"", "", investmentTransaction.getDueDate(),
								investment.getProductProvider().getValue(),
								formAccountNo(investment),
								investmentTransactionAmount,
								investmentTransaction.getTdsAmount()
								});
					}
					else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
						for (Realisation realisation : investmentTransaction.getRealisationList()) {
							realisationDate = ObjectUtils.defaultIfNull(realisation.getAccountedRealisationDate(), realisation.getRealisationDate());
							if (realisationDate.compareTo(fyStartDate) >= 0 &&
									realisationDate.compareTo(fyEndDate) <= 0) {
								investorSummary[1] += ObjectUtils.defaultIfNull(realisation.getTdsAmount(), 0D).doubleValue();
								investmentYearEndAccrual -= ObjectUtils.defaultIfNull(realisation.getInterestAmount(), 0D);
							}
						}
					}
				}
			}
			if (investment.getIsAccrualApplicable() != null && investment.getIsAccrualApplicable() &&
					investmentYearEndAccrual > Constants.TOLERATED_DIFFERENCE_AMOUNT) {
				anticipatedAccrualDetailsForInvestor.add(new Object[] {investment.getId(), null, null,
						"Accrual",
						fyEndDate,
						"Missing Entry",
						investment.getProductProvider().getValue(),
						formAccountNo(investment),
						investmentYearEndAccrual
						});
			}
			investorDvIdToTaxLiabilitysMap.put(investmentPrimaryInvestorDvId, investorSummary);
		}

		return Triplet.with(investorDvIdToTaxLiabilitysMap, accrualDetailsForInvestor, anticipatedAccrualDetailsForInvestor);
	}

	private String formAccountNo(Investment investment) {
		return investment.getId() + "/" +
				ObjectUtils.defaultIfNull(investment.getInvestmentIdWithProvider(), "");
	}
}
