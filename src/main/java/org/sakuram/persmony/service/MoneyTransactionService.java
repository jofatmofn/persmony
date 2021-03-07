package org.sakuram.persmony.service;

import java.util.List;

import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.bean.Realisation;
import org.sakuram.persmony.bean.SavingsAccountTransaction;
import org.sakuram.persmony.repository.InvestmentRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.repository.RealisationRepository;
import org.sakuram.persmony.repository.SavingsAccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;

@Service
@Transactional
public class MoneyTransactionService implements MoneyTransactionServiceInterface {
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
		
		investmentTransaction = investmentTransactionRepository.findById(singleRealisationWithBankVO.getInvestmentTransactionId())
			.orElseThrow(() -> new AppException("Invalid Group Type " + singleRealisationWithBankVO.getInvestmentTransactionId(), null));
		if (investmentTransaction.getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
			throw new AppException("Transaction " + singleRealisationWithBankVO.getInvestmentTransactionId() + " no longer Pending ", null);
		}
		investmentTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED));
		if (investmentTransaction.getDueAmount() == null) {
			investmentTransaction.setDueAmount(singleRealisationWithBankVO.getAmount());
		}
		// investment = investmentTransaction.getInvestment();
		// TODO: Close the investment if it's the last investmentTransaction?
		
		savingsAccountTransaction = new SavingsAccountTransaction(Constants.domainValueCache.get(singleRealisationWithBankVO.getBankAccountDvId()), singleRealisationWithBankVO.getTransactionDate(), singleRealisationWithBankVO.getAmount());
		savingsAccountTransaction = savingsAccountTransactionRepository.save(savingsAccountTransaction);

		realisation = new Realisation(investmentTransaction, singleRealisationWithBankVO.getTransactionDate(), Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT), savingsAccountTransaction.getId(), singleRealisationWithBankVO.getAmount());
		realisation = realisationRepository.save(realisation);
		
		System.out.println("singleRealisationWithBank completed.");
	}
	
	public void renewal(RenewalVO renewalVO) {
		Investment renewedInvestment, newInvestment;
		List<InvestmentTransaction> investmentTransactionList;
		InvestmentTransaction riReceiptTransaction, niPaymentTransaction, niReceiptTransaction;
		Realisation riReceiptRealisation, niPaymentRealisation;
		
		renewedInvestment = investmentRepository.findById(renewalVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Group Type " + renewalVO.getInvestmentId(), null));
		if (renewedInvestment.isClosed()) {
			throw new AppException("Investment " + renewalVO.getInvestmentId() + " no longer Open", null);
		}
		renewedInvestment.setClosed(true);
		renewedInvestment.setClosureDate(renewalVO.getRealisationDate());
		renewedInvestment.setClosureType(Constants.domainValueCache.get(Constants.DVID_CLOSURE_TYPE_MATURITY));
		
		newInvestment = new Investment(
				renewedInvestment.getInvestor(), 
				renewedInvestment.getProductProvider(),
				renewedInvestment.getDematAccount(),
				renewedInvestment.getFacilitator(),
				renewedInvestment.getInvestorIdWithProvider(),
				renewedInvestment.getProductIdOfProvider(),
				renewalVO.getInvestmentIdWithProvider(),
				renewedInvestment.getProductName(),
				renewedInvestment.getProductType(),
				renewalVO.getRealisationAmount(),
				renewalVO.getRateOfInterest(),
				renewedInvestment.getReceiptAccountingBasis(),
				renewedInvestment.getTaxability(),
				renewedInvestment,
				Constants.domainValueCache.get(Constants.DVID_NEW_INVESTMENT_REASON_RENEWAL),
				renewalVO.getProductEndDate(),
				false,
				null,
				null,
				renewedInvestment.isAccrualApplicable(),
				null);
		newInvestment = investmentRepository.save(newInvestment);

		investmentTransactionList = investmentTransactionRepository.findByInvestmentOrderByDueDateDesc(renewedInvestment);
		if(investmentTransactionList.size() == 0) {
			throw new AppException("No Investment Transactions found for the investment", null);
		}
		if(investmentTransactionList.get(0).getTransactionType().getId() != Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			throw new AppException("Last Transaction is not RECEIPT for the investment", null);
		}
		if(investmentTransactionList.get(0).getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
			throw new AppException("Last RECEIPT Transaction is not PENDING for the investment", null);
		}
		for (int i = 1; i < investmentTransactionList.size(); i++) {
			if(investmentTransactionList.get(i).getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING) {
				throw new AppException("A Transaction is still PENDING against the investment", null);
			}
		}
		
		riReceiptTransaction = investmentTransactionList.get(0);
		if(riReceiptTransaction.getDueAmount() == null) {
			riReceiptTransaction.setDueAmount(renewalVO.getRealisationAmount());
		}
		riReceiptTransaction.setTdsAmount(renewalVO.getTdsAmount());
		riReceiptTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED));
		riReceiptTransaction.setInterestAmount(renewalVO.getInterestAmount());
		if (riReceiptTransaction.getDueAmount() != renewalVO.getRealisationAmount()) {
			riReceiptTransaction.setSettledAmount(renewalVO.getRealisationAmount());
		}
		
		niPaymentTransaction = new InvestmentTransaction(
				newInvestment,
				Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_PAYMENT),
				renewalVO.getRealisationDate(),
				renewalVO.getRealisationAmount(),
				Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED),
				null,
				null,
				null,
				null,
				null,
				UtilFuncs.computeAssessmentYear(renewalVO.getRealisationDate()),
				null);
		niPaymentTransaction = investmentTransactionRepository.save(niPaymentTransaction);
		
		niReceiptTransaction = new InvestmentTransaction(
				newInvestment,
				Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_RECEIPT),
				renewalVO.getProductEndDate(),
				renewalVO.getMaturityAmount(),
				Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
				null,
				renewalVO.getRealisationAmount(),
				null,
				null,
				riReceiptTransaction.getTaxability(),
				UtilFuncs.computeAssessmentYear(renewalVO.getProductEndDate()),
				null);
		niReceiptTransaction = investmentTransactionRepository.save(niReceiptTransaction);
		
		riReceiptRealisation = new Realisation(
				riReceiptTransaction,
				renewalVO.getRealisationDate(),
				Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
				null,
				renewalVO.getRealisationAmount());
		riReceiptRealisation = realisationRepository.save(riReceiptRealisation);
		
		niPaymentRealisation = new Realisation(
				niPaymentTransaction,
				renewalVO.getRealisationDate(),
				Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
				null,
				renewalVO.getRealisationAmount());
		niPaymentRealisation = realisationRepository.save(niPaymentRealisation);
		
		realisationRepository.flush();
		riReceiptRealisation.setDetailsReference(niPaymentRealisation.getId());
		realisationRepository.save(riReceiptRealisation);
		niPaymentRealisation.setDetailsReference(riReceiptRealisation.getId());
		realisationRepository.save(niPaymentRealisation);
		
		System.out.println("renewal completed.");
	}
}
