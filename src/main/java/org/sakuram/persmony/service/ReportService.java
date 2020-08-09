package org.sakuram.persmony.service;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.Report01VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {
	@Autowired
	InvestmentRepository investmentRepository;
	
	public List<Report01VO> pendingTransactions() {
		return fetchRequiredTransactions();
	}
	
	private List<Report01VO> fetchRequiredTransactions() {
		// TODO: Filtering criteria as argument
		Report01VO report01VO;
		List<Report01VO> report01VOList;
		
		report01VOList = new ArrayList<Report01VO>();
		for (Investment investment : investmentRepository.findAllByOrderByIdAsc()) {
			report01VO = new Report01VO();
			report01VOList.add(report01VO);
			
			report01VO.setInvestmentId(investment.getId());
			report01VO.setInvestor(investment.getInvestor().getValue());
			report01VO.setProductProvider(investment.getProductProvider().getValue());
			report01VO.setProductName(investment.getProductName());
			report01VO.setInvestmentIdWithProvider(investment.getInvestmentIdWithProvider());
			report01VO.setIsClosed(investment.isClosed());
			
			for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
				if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
					report01VO = new Report01VO();
					report01VOList.add(report01VO);

					report01VO.setReceiptDate(investmentTransaction.getDueDate());
					report01VO.setReceiptAmout(investmentTransaction.getDueAmount());
					report01VO.setReceiptStatus(investmentTransaction.getStatus().getValue());
				}
			}
		}
		return report01VOList;
	}
}
