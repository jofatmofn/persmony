package org.sakuram.persmony.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
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
import org.sakuram.persmony.valueobject.InvestmentDetailsVO;
import org.sakuram.persmony.valueobject.InvestmentTransactionVO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.sakuram.persmony.valueobject.ReceiptDuesVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;

@Service
@Transactional
public class MoneyTransactionService {
	@Autowired
	InvestmentRepository investmentRepository;
	@Autowired
	InvestmentTransactionRepository investmentTransactionRepository;
	@Autowired
	SavingsAccountTransactionRepository savingsAccountTransactionRepository;
	@Autowired
	RealisationRepository realisationRepository;
	@Autowired
	MiscService miscService;
	
	public void realisation(SingleRealisationVO singleRealisationVO) {
		Investment investment;
		InvestmentTransaction investmentTransaction, dynamicReceiptIt;
		SavingsAccountTransaction savingsAccountTransaction;
		Realisation realisation, referencedRealisation;
		Date dynamicReceiptDueDate;
		
		investmentTransaction = investmentTransactionRepository.findById(singleRealisationVO.getInvestmentTransactionId())
			.orElseThrow(() -> new AppException("Invalid Investment Transaction Id " + singleRealisationVO.getInvestmentTransactionId(), null));
		if (investmentTransaction.getStatus().getId() != Constants.DVID_TRANSACTION_STATUS_PENDING) {
			throw new AppException("Transaction " + singleRealisationVO.getInvestmentTransactionId() + " no longer Pending ", null);
		}
		if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {	// TODO: Ability to realise an accrual transaction
			throw new AppException("Realisation of an Accrual transaction cannot be done with this feature", null);
		}

		realisation = new Realisation(investmentTransaction,
				singleRealisationVO.getTransactionDate(),
				Constants.domainValueCache.get(singleRealisationVO.getRealisationTypeDvId()),
				null,
				singleRealisationVO.getNetAmount(),
				singleRealisationVO.getReturnedPrincipalAmount(),
				singleRealisationVO.getInterestAmount(),
				singleRealisationVO.getTdsAmount());
		realisation = realisationRepository.save(realisation);
		if (singleRealisationVO.getRealisationTypeDvId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
			if (singleRealisationVO.getSavingsAccountTransactionId() == null) {
				savingsAccountTransaction = new SavingsAccountTransaction(Constants.domainValueCache.get(singleRealisationVO.getBankAccountDvId()), singleRealisationVO.getTransactionDate(), Math.abs(singleRealisationVO.getNetAmount()));
				savingsAccountTransaction = savingsAccountTransactionRepository.save(savingsAccountTransaction);
			} else {
				savingsAccountTransaction = savingsAccountTransactionRepository.findById(singleRealisationVO.getSavingsAccountTransactionId())
						.orElseThrow(() -> new AppException("Invalid Account Transaction Id " + singleRealisationVO.getSavingsAccountTransactionId(), null));
			}
			realisation.setDetailsReference(savingsAccountTransaction.getId());
		} else if (singleRealisationVO.getRealisationTypeDvId() == Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION) {
			if (singleRealisationVO.getRealisationId() != null) {
				referencedRealisation = realisationRepository.findById(singleRealisationVO.getRealisationId())
						.orElseThrow(() -> new AppException("Invalid Realisation Id " + singleRealisationVO.getRealisationId(), null));
				if (referencedRealisation.getDetailsReference() != null) {
					throw new AppException("Realisation Id " + singleRealisationVO.getRealisationId() + " is already mapped and cannot be reused.", null);
				}
				realisation.setDetailsReference(singleRealisationVO.getRealisationId());
				referencedRealisation.setDetailsReference(realisation.getId());
			}
		}
		
		if (singleRealisationVO.isLastRealisation()) {
			investmentTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED));
		}
		
		if (singleRealisationVO.getClosureTypeDvId() != null) {
			investment = investmentTransaction.getInvestment();
			investment.setClosed(true);
			investment.setClosureDate(singleRealisationVO.getTransactionDate());
			investment.setClosureType(Constants.domainValueCache.get(singleRealisationVO.getClosureTypeDvId()));

			for(InvestmentTransaction childInvestmentTransaction : investment.getInvestmentTransactionList()) {
				if(childInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING) {
					childInvestmentTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_CANCELLED));
				}
			}
		} else if (investmentTransaction.getTransactionType().getId() == Constants.DVID_TRANSACTION_TYPE_RECEIPT && investmentTransaction.getInvestment().getDynamicReceiptPeriodicity() != null) {
			if (investmentTransaction.getInvestment().getDynamicReceiptPeriodicity().equals(Constants.DYNAMIC_REALISATION_PERIODICITY_YEAR)) {
				dynamicReceiptDueDate = Date.valueOf(investmentTransaction.getDueDate().toLocalDate().plusYears(1));
				dynamicReceiptIt = new InvestmentTransaction(
						investmentTransaction.getInvestment(),
						investmentTransaction.getTransactionType(),
						dynamicReceiptDueDate,
						investmentTransaction.getDueAmount(),
						Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
						null,
						null,
						null,
						investmentTransaction.getTaxability(),
						UtilFuncs.computeAssessmentYear(dynamicReceiptDueDate),
						null);
				dynamicReceiptIt = investmentTransactionRepository.save(dynamicReceiptIt);
			}
			else {
				throw new AppException("Unsupported Dynamic Receipt Periodicity " + investmentTransaction.getInvestment().getDynamicReceiptPeriodicity(), null);
			}
			System.out.println("singleRealisation completed.");
		}
	}
	
	public void txnSingleRealisationWithBank(TxnSingleRealisationWithBankVO txnSingleRealisationWithBankVO) {
		Investment investment;
		InvestmentTransaction investmentTransaction;
		
		investment = investmentRepository.findById(txnSingleRealisationWithBankVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + txnSingleRealisationWithBankVO.getInvestmentId(), null));
		if (txnSingleRealisationWithBankVO.getTransactionTypeDvId() != Constants.DVID_TRANSACTION_TYPE_ACCRUAL && investment.isClosed()) {
			throw new AppException("Investment " + txnSingleRealisationWithBankVO.getInvestmentId() + " no longer Open", null);
		}
		
		if (txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL && (investment.getIsAccrualApplicable() == null || !investment.getIsAccrualApplicable())) {
			throw new AppException("Accrual is not applicable for the Investment " + txnSingleRealisationWithBankVO.getInvestmentId(), null);
		}
		
		investmentTransaction = new InvestmentTransaction(
				investment,
				Constants.domainValueCache.get(txnSingleRealisationWithBankVO.getTransactionTypeDvId()),
				txnSingleRealisationWithBankVO.getTransactionDate(),
				txnSingleRealisationWithBankVO.getNetAmount(),
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED) : Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
				null,
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? txnSingleRealisationWithBankVO.getInterestAmount() : null,
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? txnSingleRealisationWithBankVO.getTdsAmount() : null,
				null,
				UtilFuncs.computeAssessmentYear(txnSingleRealisationWithBankVO.getTransactionDate()),
				null);
		investmentTransaction = investmentTransactionRepository.save(investmentTransaction);
		investmentTransaction.setRealisationList(new ArrayList<Realisation>());

		if (txnSingleRealisationWithBankVO.getTransactionTypeDvId() != Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
			realisation(new SingleRealisationVO(
					Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT,
					investmentTransaction.getId(),
					null,
					txnSingleRealisationWithBankVO.getBankAccountDvId(),
					null,
					txnSingleRealisationWithBankVO.getNetAmount(),
					txnSingleRealisationWithBankVO.getReturnedPrincipalAmount(),
					txnSingleRealisationWithBankVO.getInterestAmount(),
					txnSingleRealisationWithBankVO.getTdsAmount(),
					txnSingleRealisationWithBankVO.getTransactionDate(),
					true,
					null));
		}
		
		System.out.println("txnSingleRealisationWithBank completed.");
	}
	
	public void renewal(RenewalVO renewalVO) {
		Investment renewedInvestment, newInvestment;
		List<InvestmentTransaction> investmentTransactionList;
		InvestmentTransaction riReceiptTransaction;
		Realisation riReceiptRealisation, niPaymentRealisation;
		Date realisationDate;
		Double realisationAmount;
		
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
		}
		riReceiptTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED));
		
		riReceiptRealisation = new Realisation(
				riReceiptTransaction,
				realisationDate,
				Constants.domainValueCache.get(Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
				null,
				realisationAmount,
				null,
				null,
				null);
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
				renewalVO.getFaceValue(),
				null,
				null,
				null,
				renewalVO.getRateOfInterest(),
				renewedInvestment.getTaxability(),
				renewedInvestment,
				Constants.domainValueCache.get(Constants.DVID_NEW_INVESTMENT_REASON_RENEWAL),
				renewalVO.getProductEndDate(),
				false,
				null,
				null,
				renewedInvestment.getIsAccrualApplicable(),
				null,
				renewedInvestment.getDynamicReceiptPeriodicity(),
				renewedInvestment.getProviderBranch());
		
		niPaymentRealisation = openNew(newInvestment, renewalVO.getPaymentScheduleVOList(), renewalVO.getReceiptScheduleVOList(), renewalVO.getAccrualScheduleVOList(), riReceiptRealisation.getId(), null);
		
		realisationRepository.flush();
		riReceiptRealisation.setDetailsReference(niPaymentRealisation.getId());
		realisationRepository.save(riReceiptRealisation);
		
		System.out.println("renewal completed.");
	}
	
	public void invest(InvestVO investVO) {
		Investment newInvestment;
		
		if (investVO.getProviderBranchDvId() != null) {
			if (!miscService.fetchBranchDvIdsOfParty(investVO.getProductProviderDvId()).contains(investVO.getProviderBranchDvId())) {
				throw new AppException("Given branch does not belong to the given Provider", null);
			}
		}
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
				investVO.getFaceValue(),
				investVO.getCleanPrice(),
				investVO.getAccruedInterest(),
				investVO.getCharges(),
				investVO.getRateOfInterest(),
				Constants.domainValueCache.get(investVO.getTaxabilityDvId()),
				null,
				null,
				investVO.getProductEndDate(),
				false,
				null,
				null,
				investVO.getIsAccrualApplicable(),
				null,
				investVO.getDynamicReceiptPeriodicity(),
				Constants.domainValueCache.get(investVO.getProviderBranchDvId()));
		
		openNew(newInvestment, investVO.getPaymentScheduleVOList(), investVO.getReceiptScheduleVOList(), investVO.getAccrualScheduleVOList(), null, investVO.getBankDvId());
		
		System.out.println("invest completed.");
	}

	public void addReceiptDues(ReceiptDuesVO receiptDuesVO) {
		Investment investment;
		
		investment = investmentRepository.findById(receiptDuesVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + receiptDuesVO.getInvestmentId(), null));
		if (investment.isClosed()) {
			throw new AppException("Investment " + receiptDuesVO.getInvestmentId() + " no longer Open", null);
		}
		
		saveSchedule(receiptDuesVO.getReceiptScheduleVOList(), investment, Constants.DVID_TRANSACTION_TYPE_RECEIPT);
	}

    public InvestmentDetailsVO fetchInvestmentDetails(long investmentId) {
    	InvestmentDetailsVO investmentDetailsVO;
    	List<InvestmentTransactionVO> investmentTransactionVOList;
    	List<RealisationVO> realisationVOList;
    	List<SavingsAccountTransactionVO> savingsAccountTransactionVOList;
		Investment investment;
		SavingsAccountTransaction savingsAccountTransaction;
		
		investmentDetailsVO = new InvestmentDetailsVO();
		investment = investmentRepository.findById(investmentId)
				.orElseThrow(() -> new AppException("Invalid Investment Id " + investmentId, null));
    	investmentTransactionVOList = new ArrayList<InvestmentTransactionVO>(investment.getInvestmentTransactionList().size());
    	investmentDetailsVO.setInvestmentTransactionVOList(investmentTransactionVOList);
    	realisationVOList = new ArrayList<RealisationVO>();
    	investmentDetailsVO.setRealisationVOList(realisationVOList);
    	savingsAccountTransactionVOList = new ArrayList<SavingsAccountTransactionVO>();
    	investmentDetailsVO.setSavingsAccountTransactionVOList(savingsAccountTransactionVOList);
    	
    	for (InvestmentTransaction investmentTransaction : investment.getInvestmentTransactionList()) {
    		investmentTransactionVOList.add(new InvestmentTransactionVO(
    				investmentTransaction.getId(),
    				investmentTransaction.getTransactionType().getId(),
    				investmentTransaction.getTransactionType().getValue(),
    				investmentTransaction.getDueDate(),
    				investmentTransaction.getDueAmount(),
    				investmentTransaction.getStatus().getId(),
    				investmentTransaction.getStatus().getValue(),
    				miscService.fetchRealisationAmountSummary(investmentTransaction).getAmount(),
    				investmentTransaction.getReturnedPrincipalAmount(),
    				investmentTransaction.getInterestAmount(),
    				investmentTransaction.getTdsAmount(),
        			investmentTransaction.getTaxability() == null ? null : investmentTransaction.getTaxability().getId(),
    				investmentTransaction.getTaxability() == null ? null : investmentTransaction.getTaxability().getValue(),
    				investmentTransaction.getAssessmentYear().shortValue()
    				));
    		for (Realisation realisation : investmentTransaction.getRealisationList()) {
    			// TODO: Handle possible Duplicates
    			realisationVOList.add(new RealisationVO(
    					realisation.getId(),
    					investmentTransaction.getId(),
    					realisation.getRealisationDate(),
    					realisation.getRealisationType() == null ? "Not available" : realisation.getRealisationType().getValue(),
    					realisation.getDetailsReference(),
    					realisation.getAmount(),
    					realisation.getReturnedPrincipalAmount(),
    					realisation.getInterestAmount(),
    					realisation.getTdsAmount()
    					));
    			if (realisation.getRealisationType() != null && realisation.getRealisationType().getId() == Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) {
        			// TODO: Handle possible Duplicates
    				savingsAccountTransaction = savingsAccountTransactionRepository.findById(realisation.getDetailsReference())
    						.orElseThrow(() -> new AppException("Invalid Savings Account Transaction Id " + realisation.getDetailsReference(), null));
    				savingsAccountTransactionVOList.add(new SavingsAccountTransactionVO(
    						savingsAccountTransaction.getId(),
    						savingsAccountTransaction.getBankAccount().getValue(),
    						savingsAccountTransaction.getTransactionDate(),
    						savingsAccountTransaction.getAmount()
    						));
    			}
    		}
    	}
    	return investmentDetailsVO;
    }
	
	private void saveSchedule(List<ScheduleVO> scheduleVOList, Investment investment, long transactionType) {
		InvestmentTransaction invesmentTransaction;
		for(ScheduleVO scheduleVO : scheduleVOList) {
			if (scheduleVO.getDueDate() == null ) {
				continue;
			}
			invesmentTransaction = new InvestmentTransaction(
					investment,
					Constants.domainValueCache.get(transactionType),
					scheduleVO.getDueDate(),
					(transactionType == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) ?
							Double.valueOf(ObjectUtils.defaultIfNull(scheduleVO.getInterestAmount(), 0).doubleValue() - ObjectUtils.defaultIfNull(scheduleVO.getTdsAmount(), 0).doubleValue()) :
							scheduleVO.getDueAmount(),
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
					scheduleVO.getReturnedPrincipalAmount(),
					scheduleVO.getInterestAmount(),
					scheduleVO.getTdsAmount(),
					null,
					UtilFuncs.computeAssessmentYear(scheduleVO.getDueDate()),
					null);
			investmentTransactionRepository.save(invesmentTransaction);
		}
	}
	
	private Realisation openNew(Investment newInvestment, List<ScheduleVO> paymentScheduleVOList, List<ScheduleVO> receiptScheduleVOList, List<ScheduleVO> accrualScheduleVOList, Long riReceiptRealisationId, Long bankDvId) {
		InvestmentTransaction niPaymentTransaction;
		Realisation niPaymentRealisation = null;
		SavingsAccountTransaction niSavingsAccountTransaction = null;
		boolean is_first;
		
		if (newInvestment.getIsAccrualApplicable() != null && !newInvestment.getIsAccrualApplicable() && !accrualScheduleVOList.isEmpty()) {
			throw new AppException("When accrual is not applicable, accrual schedule should not be there.", null);
		}
		newInvestment = investmentRepository.save(newInvestment);

		is_first = true;
		for(ScheduleVO paymentScheduleVO : paymentScheduleVOList) {
			if (paymentScheduleVO.getDueDate() == null ) {
				continue;
			}
			niPaymentTransaction = new InvestmentTransaction(
					newInvestment,
					Constants.domainValueCache.get(Constants.DVID_TRANSACTION_TYPE_PAYMENT),
					paymentScheduleVO.getDueDate(),
					paymentScheduleVO.getDueAmount(),
					Constants.domainValueCache.get(is_first && (bankDvId != null || riReceiptRealisationId != null) ? Constants.DVID_TRANSACTION_STATUS_COMPLETED : Constants.DVID_TRANSACTION_STATUS_PENDING),
					paymentScheduleVO.getReturnedPrincipalAmount(),
					paymentScheduleVO.getInterestAmount(),
					paymentScheduleVO.getTdsAmount(),
					null,
					UtilFuncs.computeAssessmentYear(paymentScheduleVO.getDueDate()),
					null);
			niPaymentTransaction = investmentTransactionRepository.save(niPaymentTransaction);
			
			if (is_first) {
				if (bankDvId != null) {
					niSavingsAccountTransaction = new SavingsAccountTransaction(
							Constants.domainValueCache.get(bankDvId),
							paymentScheduleVO.getDueDate(),
							paymentScheduleVO.getDueAmount());
					niSavingsAccountTransaction = savingsAccountTransactionRepository.save(niSavingsAccountTransaction);
				}
				if (bankDvId != null || riReceiptRealisationId != null) {
					niPaymentRealisation = new Realisation(
							niPaymentTransaction,
							paymentScheduleVO.getDueDate(),
							Constants.domainValueCache.get(riReceiptRealisationId == null? (bankDvId == null? Constants.DVID_REALISATION_TYPE_CASH : Constants.DVID_REALISATION_TYPE_SAVINGS_ACCOUNT) : Constants.DVID_REALISATION_TYPE_ANOTHER_REALISATION),
							riReceiptRealisationId == null? niSavingsAccountTransaction.getId() : riReceiptRealisationId,
							paymentScheduleVO.getDueAmount(),
							null,
							null,
							null);
					niPaymentRealisation = realisationRepository.save(niPaymentRealisation);
				}
				is_first = false;
			}
		}
		
		saveSchedule(receiptScheduleVOList, newInvestment, Constants.DVID_TRANSACTION_TYPE_RECEIPT);
		
		saveSchedule(accrualScheduleVOList, newInvestment, Constants.DVID_TRANSACTION_TYPE_ACCRUAL);
		
		return niPaymentRealisation;
	}
	
}
