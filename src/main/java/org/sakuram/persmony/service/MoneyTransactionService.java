package org.sakuram.persmony.service;

import java.sql.Date;
import java.text.ParseException;
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
import org.sakuram.persmony.valueobject.DuesVO;
import org.sakuram.persmony.valueobject.DueRealisationVO;
import org.sakuram.persmony.valueobject.RenewalVO;
import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsRequestVO;
import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsResponseVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.ScheduleVO;
import org.sakuram.persmony.valueobject.SingleRealisationVO;
import org.sakuram.persmony.valueobject.TransferVO;
import org.sakuram.persmony.valueobject.TxnSingleRealisationWithBankVO;
import org.sakuram.persmony.valueobject.UpdateTaxDetailRequestVO;

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
			investmentTransaction.setTaxGroup(Constants.domainValueCache.get(singleRealisationVO.getTaxGroupDvId()));
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
						investmentTransaction.getTaxGroup(),
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
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? null : txnSingleRealisationWithBankVO.getNetAmount(),
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_COMPLETED) : Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
				null,
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? txnSingleRealisationWithBankVO.getInterestAmount() : null,
				txnSingleRealisationWithBankVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL ? txnSingleRealisationWithBankVO.getTdsAmount() : null,
				Constants.domainValueCache.get(txnSingleRealisationWithBankVO.getTaxGroupDvId()),
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
					null,
					txnSingleRealisationWithBankVO.getTaxGroupDvId()));
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
				renewedInvestment.getUnits(),
				renewalVO.getFaceValue(),
				null,
				null,
				null,
				renewalVO.getRateOfInterest(),
				renewedInvestment.getTaxability(),
				renewedInvestment,
				Constants.domainValueCache.get(Constants.DVID_NEW_INVESTMENT_REASON_RENEWAL),
				renewedInvestment.getInvestmentEndDate(),
				renewalVO.getInvestmentEndDate(),
				false,
				null,
				null,
				renewedInvestment.getIsAccrualApplicable(),
				null,
				renewedInvestment.getDynamicReceiptPeriodicity(),
				renewedInvestment.getProviderBranch(),
				renewedInvestment.getDefaultBankAccount(),
				renewedInvestment.getDefaultTaxGroup()
				);
		
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
				investVO.getUnits(),
				investVO.getFaceValue(),
				investVO.getCleanPrice(),
				investVO.getAccruedInterest(),
				investVO.getCharges(),
				investVO.getRateOfInterest(),
				Constants.domainValueCache.get(investVO.getTaxabilityDvId()),
				null,
				null,
				investVO.getInvestmentStartDate(),
				investVO.getInvestmentEndDate(),
				false,
				null,
				null,
				investVO.getIsAccrualApplicable(),
				null,
				investVO.getDynamicReceiptPeriodicity(),
				Constants.domainValueCache.get(investVO.getProviderBranchDvId()),
				Constants.domainValueCache.get(investVO.getDefaultBankAccountDvId()),
				Constants.domainValueCache.get(investVO.getDefaultTaxGroupDvId())
				);
		
		openNew(newInvestment, investVO.getPaymentScheduleVOList(), investVO.getReceiptScheduleVOList(), investVO.getAccrualScheduleVOList(), null, investVO.getBankDvId());
		
		System.out.println("invest completed.");
	}

	public void transfer(TransferVO transferVO) {
		Investment transferredInvestment, balanceInvestment, newInvestment;
		InvestmentTransaction balanceInvestmentTransaction, newInvestmentTransaction;
		double transferFaceValue, transferProportion;
		Double transferUnits;
		
		transferredInvestment = investmentRepository.findById(transferVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + transferVO.getInvestmentId(), null));
		if (transferredInvestment.isClosed()) {
			throw new AppException("Investment " + transferVO.getInvestmentId() + " no longer Open", null);
		}
		if (transferredInvestment.getUnits() == null && transferVO.getUnits() != null) {
			throw new AppException("Transfer cannot be expressed in no. of units for the Investment " + transferVO.getInvestmentId(), null);
		}
		transferredInvestment.setClosed(true);
		transferredInvestment.setClosureDate(transferVO.getInvestmentStartDate());
		transferredInvestment.setClosureType(Constants.domainValueCache.get(Constants.DVID_CLOSURE_TYPE_TRANSFER_OUT));
		
		transferProportion = 1;
		if (transferVO.getUnits() == null && transferVO.getFaceValue() == null ||
				transferVO.getUnits() != null && transferVO.getFaceValue() != null) {
			throw new AppException("Either No. of units Or Value (not both) should be provided", null);
		} else if (transferVO.getFaceValue() != null) {
			transferFaceValue = transferVO.getFaceValue();
			if (transferredInvestment.getWorth() == transferVO.getFaceValue()) {
				transferUnits = transferredInvestment.getUnits();	// Redundant logic, just to handle rounding-off differences
			} else {
				transferProportion = transferVO.getFaceValue() / transferredInvestment.getWorth();
				transferUnits = (transferredInvestment.getUnits() == null ? null : transferredInvestment.getUnits() * transferProportion);
			}
		} else {
			transferUnits = transferVO.getUnits();
			if (transferredInvestment.getUnits() == transferVO.getUnits()) {
				transferFaceValue = transferredInvestment.getWorth();	// Redundant logic, just to handle rounding-off differences
			} else {
				transferProportion = transferVO.getUnits() / transferredInvestment.getUnits();
				transferFaceValue = transferredInvestment.getWorth() * transferProportion;
			}
		}
		
		balanceInvestment = null;
		if (transferProportion < 1) {
			balanceInvestment = new Investment(
					transferredInvestment.getInvestor(),
					transferredInvestment.getProductProvider(),
					transferredInvestment.getDematAccount(),
					transferredInvestment.getFacilitator(),
					transferredInvestment.getInvestorIdWithProvider(),
					transferredInvestment.getProductIdOfProvider(),
					transferredInvestment.getInvestmentIdWithProvider(),
					transferredInvestment.getProductName(),
					transferredInvestment.getProductType(),
					(transferredInvestment.getUnits() == null ? null : transferredInvestment.getUnits() - transferUnits),
					transferredInvestment.getWorth() - transferFaceValue,
					null,
					null,
					null,
					transferredInvestment.getRateOfInterest(),
					transferredInvestment.getTaxability(),
					transferredInvestment,
					Constants.domainValueCache.get(Constants.DVID_NEW_INVESTMENT_REASON_TRANSFER_BALANCE),
					transferVO.getInvestmentStartDate(),
					transferredInvestment.getInvestmentEndDate(),
					false,
					null,
					null,
					transferredInvestment.getIsAccrualApplicable(),
					null,
					transferredInvestment.getDynamicReceiptPeriodicity(),
					transferredInvestment.getProviderBranch(),
					transferredInvestment.getDefaultBankAccount(),
					transferredInvestment.getDefaultTaxGroup()
					);
			balanceInvestment = investmentRepository.save(balanceInvestment);
		}
		
		newInvestment = new Investment(
				Constants.domainValueCache.get(transferVO.getInvestorDvId()),
				transferredInvestment.getProductProvider(),
				Constants.domainValueCache.get(transferVO.getDematAccountDvId()),
				transferredInvestment.getFacilitator(),
				transferVO.getInvestorIdWithProvider(),
				transferredInvestment.getProductIdOfProvider(),
				transferVO.getInvestmentIdWithProvider(),
				transferredInvestment.getProductName(),
				transferredInvestment.getProductType(),
				transferUnits,
				transferFaceValue,
				null,
				null,
				null,
				transferredInvestment.getRateOfInterest(),
				transferredInvestment.getTaxability(),
				transferredInvestment,
				Constants.domainValueCache.get(Constants.DVID_NEW_INVESTMENT_REASON_TRANSFER_IN),
				transferVO.getInvestmentStartDate(),
				transferredInvestment.getInvestmentEndDate(),
				false,
				null,
				null,
				transferredInvestment.getIsAccrualApplicable(),
				null,
				transferredInvestment.getDynamicReceiptPeriodicity(),
				transferredInvestment.getProviderBranch(),
				transferredInvestment.getDefaultBankAccount(),
				transferredInvestment.getDefaultTaxGroup()
				);
		newInvestment = investmentRepository.save(newInvestment);
		
		for (InvestmentTransaction transferredInvestmentTransaction : transferredInvestment.getInvestmentTransactionList()) {
			if (transferredInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_PENDING) {
				transferredInvestmentTransaction.setStatus(Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_CANCELLED));
				if (transferProportion < 1) {
					balanceInvestmentTransaction = new InvestmentTransaction(
							balanceInvestment,
							transferredInvestmentTransaction.getTransactionType(),
							transferredInvestmentTransaction.getDueDate(),
							transferredInvestmentTransaction.getDueAmount() == null ? null : transferredInvestmentTransaction.getDueAmount() * (1 - transferProportion),
							Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
							transferredInvestmentTransaction.getReturnedPrincipalAmount() == null ? null : transferredInvestmentTransaction.getReturnedPrincipalAmount() * (1 - transferProportion),
							transferredInvestmentTransaction.getInterestAmount() == null ? null : transferredInvestmentTransaction.getInterestAmount() * (1 - transferProportion),
							transferredInvestmentTransaction.getTdsAmount() == null ? null : transferredInvestmentTransaction.getTdsAmount() * (1 - transferProportion),
							transferredInvestmentTransaction.getTaxGroup(),
							transferredInvestmentTransaction.getAssessmentYear(),
							null);
					balanceInvestmentTransaction = investmentTransactionRepository.save(balanceInvestmentTransaction);
				}
				newInvestmentTransaction = new InvestmentTransaction(
						newInvestment,
						transferredInvestmentTransaction.getTransactionType(),
						transferredInvestmentTransaction.getDueDate(),
						transferredInvestmentTransaction.getDueAmount() == null ? null : transferredInvestmentTransaction.getDueAmount() * transferProportion,
						Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING),
						transferredInvestmentTransaction.getReturnedPrincipalAmount() == null ? null : transferredInvestmentTransaction.getReturnedPrincipalAmount() * transferProportion,
						transferredInvestmentTransaction.getInterestAmount() == null ? null : transferredInvestmentTransaction.getInterestAmount() * transferProportion,
						transferredInvestmentTransaction.getTdsAmount() == null ? null : transferredInvestmentTransaction.getTdsAmount() * transferProportion,
						transferredInvestmentTransaction.getTaxGroup(),
						transferredInvestmentTransaction.getAssessmentYear(),
						null);
				newInvestmentTransaction = investmentTransactionRepository.save(newInvestmentTransaction);
			}
		}
	}
	
	public void addDues(DuesVO duesVO) {
		Investment investment;
		
		investment = investmentRepository.findById(duesVO.getInvestmentId())
				.orElseThrow(() -> new AppException("Invalid Investment Id " + duesVO.getInvestmentId(), null));
		if (investment.isClosed()) {
			throw new AppException("Investment " + duesVO.getInvestmentId() + " no longer Open", null);
		}
		
		saveSchedule(duesVO.getPaymentScheduleVOList(), investment, Constants.DVID_TRANSACTION_TYPE_PAYMENT);
		saveSchedule(duesVO.getReceiptScheduleVOList(), investment, Constants.DVID_TRANSACTION_TYPE_RECEIPT);
		saveSchedule(duesVO.getAccrualScheduleVOList(), investment, Constants.DVID_TRANSACTION_TYPE_ACCRUAL);
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
    				investmentTransaction.getAccrualTdsReference(),
        			investmentTransaction.getTaxGroup() == null ? null : investmentTransaction.getTaxGroup().getId(),
    				investmentTransaction.getTaxGroup() == null ? null : investmentTransaction.getTaxGroup().getValue(),
    				investmentTransaction.getAssessmentYear().shortValue(),
    				null,
    				null
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
    					realisation.getTdsAmount(),
    					realisation.getTdsReference()
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
	
    public RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisations(RetrieveAccrualsRealisationsRequestVO retrieveAccrualsRealisationsRequestVO) throws ParseException{
    	RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisationsResponseVO;
    	List<DueRealisationVO> dueRealisationVOList;
    	
    	retrieveAccrualsRealisationsResponseVO = new RetrieveAccrualsRealisationsResponseVO();
    	dueRealisationVOList = new ArrayList<DueRealisationVO>();
    	retrieveAccrualsRealisationsResponseVO.setDueRealisationVOList(dueRealisationVOList);
    	for(Object[] record : realisationRepository.retrieveAccrualsRealisations(
    			new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse(retrieveAccrualsRealisationsRequestVO.getFyStartYear() + "-04-01").getTime()),
    			new java.sql.Date(Constants.ANSI_DATE_FORMAT.parse((retrieveAccrualsRealisationsRequestVO.getFyStartYear() + 1) + "-03-31").getTime()),
    			retrieveAccrualsRealisationsRequestVO.getInvestorDvId() == null ? -1 : retrieveAccrualsRealisationsRequestVO.getInvestorDvId(),
    			retrieveAccrualsRealisationsRequestVO.getProductProviderDvId() == null ? -1 : retrieveAccrualsRealisationsRequestVO.getProductProviderDvId(),
    			retrieveAccrualsRealisationsRequestVO.isTaxDetailNotInForm26as(),
				retrieveAccrualsRealisationsRequestVO.isTaxDetailNotInAis(),
				retrieveAccrualsRealisationsRequestVO.isInterestAvailable(),
				retrieveAccrualsRealisationsRequestVO.isTdsAvailable())) {
    		dueRealisationVOList.add(new DueRealisationVO(record));
    		
    	}
    	return retrieveAccrualsRealisationsResponseVO;
    }
    
    public void updateTaxDetail(UpdateTaxDetailRequestVO updateTaxDetailRequestVO) {
    	if (updateTaxDetailRequestVO.getTransactionTypeDvId() == Constants.DVID_TRANSACTION_TYPE_ACCRUAL) {
    		InvestmentTransaction investmentTransaction;
    		investmentTransaction = investmentTransactionRepository.findById(updateTaxDetailRequestVO.getId())
    				.orElseThrow(() -> new AppException("Invalid Investment Transaction Id " + updateTaxDetailRequestVO.getId(), null));
    		if (investmentTransaction.getDueDate().equals(updateTaxDetailRequestVO.getAccountedDate())) {
	    		investmentTransaction.setAccountedTransactionDate(null);
    		} else {
	    		investmentTransaction.setAccountedTransactionDate(updateTaxDetailRequestVO.getAccountedDate());
	    	}
    		investmentTransaction.setInterestAmount(updateTaxDetailRequestVO.getInterestAmount());
    		investmentTransaction.setTdsAmount(updateTaxDetailRequestVO.getTdsAmount());
    		investmentTransaction.setAccrualTdsReference(updateTaxDetailRequestVO.getTdsReference());
    		investmentTransaction.setInAis(updateTaxDetailRequestVO.getInAis());
    		investmentTransaction.setForm26asBookingDate(updateTaxDetailRequestVO.getForm26asBookingDate());
    	} else {
    		Realisation realisation;
    		realisation = realisationRepository.findById(updateTaxDetailRequestVO.getId())
    				.orElseThrow(() -> new AppException("Invalid Realisation Id " + updateTaxDetailRequestVO.getId(), null));
    		if (realisation.getRealisationDate().equals(updateTaxDetailRequestVO.getAccountedDate())) {
    			realisation.setAccountedRealisationDate(null);
    		} else {
    			realisation.setAccountedRealisationDate(updateTaxDetailRequestVO.getAccountedDate());
	    	}
    		realisation.setInterestAmount(updateTaxDetailRequestVO.getInterestAmount());
    		realisation.setTdsAmount(updateTaxDetailRequestVO.getTdsAmount());
    		realisation.setTdsReference(updateTaxDetailRequestVO.getTdsReference());
    		realisation.setInAis(updateTaxDetailRequestVO.getInAis());
    		realisation.setForm26asBookingDate(updateTaxDetailRequestVO.getForm26asBookingDate());
    	}
    	
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
