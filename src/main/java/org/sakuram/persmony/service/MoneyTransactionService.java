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
import org.sakuram.persmony.valueobject.ReceiptSingleRealisationIntoBankVO;

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
	
	public void receiptSingleRealisationIntoBank(ReceiptSingleRealisationIntoBankVO receiptSingleRealisationIntoBankVO) {
		// Investment investment;
		InvestmentTransaction investmentTransaction;
		SavingsAccountTransaction savingsAccountTransaction;
		Realisation realisation;
		DomainValue domainValue;
		
		investmentTransaction = investmentTransactionRepository.findById(receiptSingleRealisationIntoBankVO.getInvestmentTransactionId())
			.orElseThrow(() -> new AppException("Invalid Group Type " + receiptSingleRealisationIntoBankVO.getInvestmentTransactionId(), null));
		if (investmentTransaction.getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
			throw new AppException("Transaction " + receiptSingleRealisationIntoBankVO.getInvestmentTransactionId() + " no longer Pending ", null);
		}
		domainValue = domainValueRepository.findById(Constants.DVID_TRANSACTION_STATUS_COMPLETED)
				.orElseThrow(() -> new AppException("Transaction Status could not be located: " + Constants.DVID_TRANSACTION_STATUS_COMPLETED, null));
		investmentTransaction.setStatus(domainValue);
		if (investmentTransaction.getDueAmount() == null) {
			investmentTransaction.setDueAmount(receiptSingleRealisationIntoBankVO.getAmount());
		}
		// investment = investmentTransaction.getInvestment();
		
		savingsAccountTransaction = new SavingsAccountTransaction();
		domainValue = domainValueRepository.findById(receiptSingleRealisationIntoBankVO.getBankAccountDvId())
				.orElseThrow(() -> new AppException("Invalid Bank Account " + receiptSingleRealisationIntoBankVO.getBankAccountDvId(), null));
		savingsAccountTransaction.setBankAccount(domainValue);
		savingsAccountTransaction.setTransactionDate(receiptSingleRealisationIntoBankVO.getTransactionDate());
		savingsAccountTransaction.setAmount(receiptSingleRealisationIntoBankVO.getAmount());
		savingsAccountTransaction = savingsAccountTransactionRepository.save(savingsAccountTransaction);

		realisation = new Realisation();
		realisation.setAmount(receiptSingleRealisationIntoBankVO.getAmount());
		realisation.setDetailsReference(savingsAccountTransaction.getId());
		realisation.setRealisationDate(receiptSingleRealisationIntoBankVO.getTransactionDate());
		realisation.setInvestmentTransaction(investmentTransaction);
		domainValue = domainValueRepository.findById(Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT)
				.orElseThrow(() -> new AppException("Realisation Type could not be located: " + Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT, null));
		realisation.setRealisationType(domainValue);
		realisation = realisationRepository.save(realisation);
		
		System.out.println("receiptSingleRealisationIntoBank completed.");
	}
}
