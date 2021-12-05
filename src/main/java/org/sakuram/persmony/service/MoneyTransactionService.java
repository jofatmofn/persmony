package org.sakuram.persmony.service;

import java.sql.Date;
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
import org.sakuram.persmony.valueobject.InvestVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationWithBankVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;

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
		} else if (investmentTransaction.getDueAmount() != singleRealisationWithBankVO.getAmount()) {
			investmentTransaction.setSettledAmount(singleRealisationWithBankVO.getAmount());
		}
		
		savingsAccountTransaction = new SavingsAccountTransaction(Constants.domainValueCache.get(singleRealisationWithBankVO.getBankAccountDvId()), singleRealisationWithBankVO.getTransactionDate(), singleRealisationWithBankVO.getAmount());
		savingsAccountTransaction = savingsAccountTransactionRepository.save(savingsAccountTransaction);

		realisation = new Realisation(investmentTransaction, singleRealisationWithBankVO.getTransactionDate(), Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT), savingsAccountTransaction.getId(), singleRealisationWithBankVO.getAmount());
		realisation = realisationRepository.save(realisation);
		
		System.out.println("singleRealisationWithBank completed.");
	}
	
	public void txnSingleRealisationWithBank(TxnSingleRealisationWithBankVO txnSingleRealisationWithBankVO) {
		Investment investment;
		InvestmentTransaction investmentTransaction;
		
		investment = investmentRepository.findById(txnSingleRealisationWithBankVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + txnSingleRealisationWithBankVO.getInvestmentId(), null));
		if (investment.isClosed()) {
			throw new AppException("Investment " + txnSingleRealisationWithBankVO.getInvestmentId() + " no longer Open", null);
		}
		
		investmentTransaction = new InvestmentTransaction(
				investment,
				Constants.domainValueCache.get(txnSingleRealisationWithBankVO.getTransactionTypeDvId()),
				txnSingleRealisationWithBankVO.getTransactionDate(),
				txnSingleRealisationWithBankVO.getAmount(),
				Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
				null,
				null,
				null,
				null,
				null,
				UtilFuncs.computeAssessmentYear(txnSingleRealisationWithBankVO.getTransactionDate()),
				null);
		investmentTransaction = investmentTransactionRepository.save(investmentTransaction);
		
		singleRealisationWithBank(new SingleRealisationWithBankVO(
				investmentTransaction.getId(),
				txnSingleRealisationWithBankVO.getAmount(),
				txnSingleRealisationWithBankVO.getTransactionDate(),
				txnSingleRealisationWithBankVO.getBankAccountDvId(),
				null));
		System.out.println("txnSingleRealisationWithBank completed.");
	}
	
	public void singleLastRealisationWithBank(SingleRealisationWithBankVO singleRealisationWithBankVO) {
		Investment investment;
		InvestmentTransaction investmentTransaction;
		
		singleRealisationWithBank(singleRealisationWithBankVO);
		investmentTransaction = investmentTransactionRepository.findById(singleRealisationWithBankVO.getInvestmentTransactionId())
				.orElseThrow(() -> new AppException("Invalid Group Type " + singleRealisationWithBankVO.getInvestmentTransactionId(), null));
		investment = investmentTransaction.getInvestment();
		investment.setClosed(true);
		investment.setClosureDate(singleRealisationWithBankVO.getTransactionDate());
		investment.setClosureType(Constants.domainValueCache.get(singleRealisationWithBankVO.getClosureTypeDvId()));
		
		for(InvestmentTransaction childInvestmentTransaction : investment.getInvestmentTransactionList()) {
			if(childInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING) {
				childInvestmentTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_CANCELLED));
			}
		}
	}
	
	public void renewal(RenewalVO renewalVO) {
		Investment renewedInvestment, newInvestment;
		List<InvestmentTransaction> investmentTransactionList;
		InvestmentTransaction riReceiptTransaction;
		Realisation riReceiptRealisation, niPaymentRealisation;
		Date realisationDate;
		Float realisationAmount;
		
		realisationDate = renewalVO.getPaymentScheduleVOList().get(0).getDueDate();
		realisationAmount = renewalVO.getPaymentScheduleVOList().get(0).getDueAmount();
		
		renewedInvestment = investmentRepository.findById(renewalVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + renewalVO.getInvestmentId(), null));
		if (renewedInvestment.isClosed()) {
			throw new AppException("Investment " + renewalVO.getInvestmentId() + " no longer Open", null);
		}
		renewedInvestment.setClosed(true);
		renewedInvestment.setClosureDate(realisationDate);
		renewedInvestment.setClosureType(Constants.domainValueCache.get(Constants.DVID_CLOSURE_TYPE_MATURITY));
		
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
			riReceiptTransaction.setDueAmount(realisationAmount);
		} else if (riReceiptTransaction.getDueAmount() != realisationAmount) {
			riReceiptTransaction.setSettledAmount(realisationAmount);
		}
		riReceiptTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED));
		
		riReceiptRealisation = new Realisation(
				riReceiptTransaction,
				realisationDate,
				Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
				null,
				realisationAmount);
		riReceiptRealisation = realisationRepository.save(riReceiptRealisation);
		
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
				null,
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
		
		niPaymentRealisation = openNew(newInvestment, renewalVO.getPaymentScheduleVOList(), renewalVO.getReceiptScheduleVOList(), renewalVO.getAccrualScheduleVOList(), riReceiptRealisation.getId(), null);
		
		realisationRepository.flush();
		riReceiptRealisation.setDetailsReference(niPaymentRealisation.getId());
		realisationRepository.save(riReceiptRealisation);
		
		System.out.println("renewal completed.");
	}
	
	public void invest(InvestVO investVO) {
		Investment newInvestment;
		
		newInvestment = new Investment(
				Constants.domainValueCache.get(investVO.getInvestorDvId()),
				Constants.domainValueCache.get(investVO.getProductProviderDvId()),
				Constants.domainValueCache.get(investVO.getDematAccountDvId()),
				null,
				investVO.getInvestorIdWithProvider(),
				investVO.getProductIdOfProvider(),
				investVO.getInvestmentIdWithProvider(),
				investVO.getProductName(),
				Constants.domainValueCache.get(investVO.getProductTypeDvId()),
				null,
				investVO.getRateOfInterest(),
				null,
				Constants.domainValueCache.get(investVO.getTaxabilityDvId()),
				null,
				null,
				investVO.getProductEndDate(),
				false,
				null,
				null,
				investVO.getIsAccrualApplicable(),
				null);
		
		openNew(newInvestment, investVO.getPaymentScheduleVOList(), investVO.getReceiptScheduleVOList(), investVO.getAccrualScheduleVOList(), null, investVO.getBankDvId());
		
		System.out.println("invest completed.");
	}
	
	private Realisation openNew(Investment newInvestment, List<ScheduleVO> paymentScheduleVOList, List<ScheduleVO> receiptScheduleVOList, List<ScheduleVO> accrualScheduleVOList, Long riReceiptRealisationId, Long bankDvId) {
		InvestmentTransaction niPaymentTransaction, niReceiptTransaction, niAccrualTransaction;
		Realisation niPaymentRealisation = null;
		SavingsAccountTransaction niSavingsAccountTransaction = null;
		boolean is_first;
		Float worth;
		
		newInvestment = investmentRepository.save(newInvestment);

		is_first = true;
		worth = 0F;
		for(ScheduleVO paymentScheduleVO : paymentScheduleVOList) {
			niPaymentTransaction = new InvestmentTransaction(
					newInvestment,
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_PAYMENT),
					paymentScheduleVO.getDueDate(),
					paymentScheduleVO.getDueAmount(),
					Constants.domainValueCache.get(is_first? Constants.DVID_TRANSACTION_STATUS_COMPLETED : Constants.DVID_TRANSACTION_STATUS_PENDING),
					null,
					null,
					null,
					null,
					null,
					UtilFuncs.computeAssessmentYear(paymentScheduleVO.getDueDate()),
					null);
			niPaymentTransaction = investmentTransactionRepository.save(niPaymentTransaction);
			
			worth += paymentScheduleVO.getDueAmount();
			
			if (is_first) {
				if (bankDvId != null) {
					niSavingsAccountTransaction = new SavingsAccountTransaction(
							Constants.domainValueCache.get(bankDvId),
							paymentScheduleVO.getDueDate(),
							paymentScheduleVO.getDueAmount());
					niSavingsAccountTransaction = savingsAccountTransactionRepository.save(niSavingsAccountTransaction);
				}
				niPaymentRealisation = new Realisation(
						niPaymentTransaction,
						paymentScheduleVO.getDueDate(),
						Constants.domainValueCache.get(riReceiptRealisationId == null? (bankDvId == null? Constants.DVID_REALISATION_TYPE_CASH : Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) : Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
						riReceiptRealisationId == null? (niSavingsAccountTransaction == null ? null : niSavingsAccountTransaction.getId()) : riReceiptRealisationId,
						paymentScheduleVO.getDueAmount());
				niPaymentRealisation = realisationRepository.save(niPaymentRealisation);
				is_first = false;
			}
		}
		
		if (worth > 0) {
			newInvestment.setWorth(worth);
			investmentRepository.save(newInvestment);
		}
		
		for(ScheduleVO receiptScheduleVO : receiptScheduleVOList) {
			niReceiptTransaction = new InvestmentTransaction(
					newInvestment,
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_RECEIPT),
					receiptScheduleVO.getDueDate(),
					receiptScheduleVO.getDueAmount(),
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
					null,
					null,
					receiptScheduleVO.getDueAmount(),
					null,
					null,
					UtilFuncs.computeAssessmentYear(receiptScheduleVO.getDueDate()),
					null);
			niReceiptTransaction = investmentTransactionRepository.save(niReceiptTransaction);
		}
		
		for(ScheduleVO accrualScheduleVO : accrualScheduleVOList) {
			niAccrualTransaction = new InvestmentTransaction(
					newInvestment,
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_ACCRUAL),
					accrualScheduleVO.getDueDate(),
					accrualScheduleVO.getDueAmount(),
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
					null,
					null,
					accrualScheduleVO.getDueAmount(),
					null,
					null,
					UtilFuncs.computeAssessmentYear(accrualScheduleVO.getDueDate()),
					null);
			niAccrualTransaction = investmentTransactionRepository.save(niAccrualTransaction);
		}
		
		return niPaymentRealisation;
	}
}
