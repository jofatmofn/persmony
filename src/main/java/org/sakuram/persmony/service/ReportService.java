package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {
	@Autowired
	InvestmentRepository investmentRepository;
	
	public List<Object[]> pendingTransactions() {
		return fetchRequiredTransactions();
	}
	
	private List<Object[]> fetchRequiredTransactions() {
		List<Object[]> recordList;
		
		recordList = new ArrayList<Object[]>();
		recordList.add(new Object[]{"Sl. No.", "Investor", "Product Provider", "Product Name", "Account No.", "Is Closed?", "Receipt Date", "Receipt Amount", "Receipt Status"});
		for (Investment investment : investmentRepository.findAllByOrderByIdAsc()) {
			
			recordList.add(new Object[]{investment.getId(), investment.getInvestor().getValue(), investment.getProductProvider().getValue(),
					investment.getProductName(), investment.getInvestmentIdWithProvider(), investment.isClosed()});
			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
				if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
					recordList.add(new Object[]{null, null, null, null, null, null,
							investmentTransaction.getDueDate(), investmentTransaction.getDueAmount(), 
							investmentTransaction.getStatus().getValue()});
				}
			}
		}
		return recordList;
	}
}
