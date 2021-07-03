package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.util.Constants;
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
			recordList.add(new Object[]{investment.getId(), investment.getInvestor().getValue(), investment.getProductProvider().getValue(),
					investment.getProductName(), investment.getInvestmentIdWithProvider(), investment.isClosed()});
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
		recordList.add(0, new Object[]{"Date", "Investor", "Product Provider", "Product Name", "Account No.", "Amount", "Returned Principal"});
		
		return recordList;
	}
}
