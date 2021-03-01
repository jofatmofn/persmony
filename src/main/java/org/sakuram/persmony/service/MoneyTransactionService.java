package org.sakuram.persmony.service;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.bean.SavingsAccountTransaction;
import org.sakuram.persmony.repository.DomainValueRepository;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.repository.RealisationRepository;
import org.sakuram.persmony.repository.SavingsAccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;

@Service
@Transactional
public class MoneyTransactionService implements MoneyTransactionServiceInterface {
	@Autowired
	DomainValueRepository domainValueRepository;
	@Autowired
	InvestmentRepository investmentRepository;
	@Autowired
	InvestmentTransactionRepository investmentTransactionRepository;
	@Autowired
	SavingsAccountTransactionRepository savingsAccountTransactionRepository;
	@Autowired
	RealisationRepository realisationRepository;
	
	public void singleRealisationWithBank(SingleRealisationWithBankVO singleRealisationWithBankVO) {
		// Investment investment;
		InvestmentTransaction investmentTransaction;
		SavingsAccountTransaction savingsAccountTransaction;
		Realisation realisation;
		DomainValue domainValue;
		
		investmentTransaction = investmentTransactionRepository.findById(singleRealisationWithBankVO.getInvestmentTransactionId())
			.orElseThrow(() -> new AppException("Invalid Group Type " + singleRealisationWithBankVO.getInvestmentTransactionId(), null));
		if (investmentTransaction.getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
			throw new AppException("Transaction " + singleRealisationWithBankVO.getInvestmentTransactionId() + " no longer Pending ", null);
		}
		domainValue = domainValueRepository.findById(Constants.DVID_TRANSACTION_STATUS_COMPLETED)
				.orElseThrow(() -> new AppException("Transaction Status could not be located: " + Constants.DVID_TRANSACTION_STATUS_COMPLETED, null));
		investmentTransaction.setStatus(domainValue);
		if (investmentTransaction.getDueAmount() == null) {
			investmentTransaction.setDueAmount(singleRealisationWithBankVO.getAmount());
		}
		// investment = investmentTransaction.getInvestment();
		// TODO: Close the investment if it's the last investmentTransaction?
		
		domainValue = domainValueRepository.findById(singleRealisationWithBankVO.getBankAccountDvId())
				.orElseThrow(() -> new AppException("Invalid Bank Account " + singleRealisationWithBankVO.getBankAccountDvId(), null));
		savingsAccountTransaction = new SavingsAccountTransaction(domainValue, singleRealisationWithBankVO.getTransactionDate(), singleRealisationWithBankVO.getAmount());
		savingsAccountTransaction = savingsAccountTransactionRepository.save(savingsAccountTransaction);

		domainValue = domainValueRepository.findById(Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT)
				.orElseThrow(() -> new AppException("Realisation Type could not be located: " + Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT, null));
		realisation = new Realisation(investmentTransaction, singleRealisationWithBankVO.getTransactionDate(), domainValue, savingsAccountTransaction.getId(), singleRealisationWithBankVO.getAmount());
		realisation = realisationRepository.save(realisation);
		
		System.out.println("singleRealisationWithBank completed.");
	}
}
