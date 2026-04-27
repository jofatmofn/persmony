package org.sakuram.persmony.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sakuram.persmony.bean.CashFlow;
import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.bean.IncomeExpenditureMatchPlan;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.repository.CashFlowRepository;
import org.sakuram.persmony.repository.IncomeExpenditureMatchPlanRepository;
import org.sakuram.persmony.repository.InvestmentTransactionRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.CashFlowVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.PlanSearchCriteriaVO;
import org.sakuram.persmony.valueobject.PlanSearchResultVO;
import org.sakuram.persmony.valueobject.PlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlanService {

	@Autowired
	InvestmentTransactionRepository investmentTransactionRepository;
	@Autowired
	CashFlowRepository cashFlowRepository;
	@Autowired
	IncomeExpenditureMatchPlanRepository incomeExpenditureMatchPlanRepository;
	
	public List<PlanSearchResultVO> retrievePlanStatus(PlanSearchCriteriaVO planSearchCriteriaVO) {
		List<PlanSearchResultVO> planSearchResultVOList;
		List<IncomeExpenditureMatchPlan> incomeExpenditureMatchPlanList;
		
		incomeExpenditureMatchPlanList = incomeExpenditureMatchPlanRepository.searchIncomeExpenditureMatchPlans(planSearchCriteriaVO);
		planSearchResultVOList = new ArrayList<PlanSearchResultVO>(incomeExpenditureMatchPlanList.size());
		for (IncomeExpenditureMatchPlan iEMP: incomeExpenditureMatchPlanList) {
			planSearchResultVOList.add(new PlanSearchResultVO(
					iEMP.getId(),
					(iEMP.getIncomeInvestmentTransaction() == null ? iEMP.getIncomeCashFlow().toString() : iEMP.getIncomeInvestmentTransaction().toString()),
					(iEMP.getExpenditureInvestmentTransaction() == null ? iEMP.getExpenditureCashFlow().toString() : iEMP.getExpenditureInvestmentTransaction().toString()),
					iEMP.getMappedAmount(),
					new IdValueVO(iEMP.getStatus())
					));
		}
		
		return planSearchResultVOList;
	}
	
	public void createPlan(PlanVO planVO) {
		InvestmentTransaction incomeInvestmentTransaction, expenditureInvestmentTransaction;
		CashFlow incomeCashFlow, expenditureCashFlow;
		BigDecimal incomeSoFarMappedToExpenditure, expenditureSoFarMappedToIncome, overallIncome, overallExpenditure;
		List<IncomeExpenditureMatchPlan> mappedExpenditurePlanList, mappedIncomePlanList;
		DomainValue transactionTypeDv;
		LocalDate incomeDate, expenditureDate;
		
		if (planVO.getIncomeInvestmentTransactionId() != null) {
			incomeCashFlow = null;
			incomeInvestmentTransaction = investmentTransactionRepository.findById(planVO.getIncomeInvestmentTransactionId())
					.orElseThrow(() -> new AppException("Invalid Investment Transaction Id " + planVO.getIncomeInvestmentTransactionId(), null));
			overallIncome = BigDecimal.valueOf(Objects.requireNonNullElse(incomeInvestmentTransaction.getDueAmount(),
					Objects.requireNonNullElse(incomeInvestmentTransaction.getReturnedPrincipalAmount(), 0D)));
			transactionTypeDv = incomeInvestmentTransaction.getTransactionType();
			if (incomeInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_CANCELLED) {
				throw new AppException("Income Investment Transactio is in Cancelled state", null);
			}
			incomeDate = incomeInvestmentTransaction.getDueDate();
			mappedExpenditurePlanList = incomeExpenditureMatchPlanRepository.findByIncomeInvestmentTransaction(incomeInvestmentTransaction);
		} else {
			incomeInvestmentTransaction = null;
			incomeCashFlow = cashFlowRepository.findById(planVO.getIncomeCashFlowId())
					.orElseThrow(() -> new AppException("Invalid Cash Flow Id " + planVO.getIncomeCashFlowId(), null));
			overallIncome = incomeCashFlow.getFlowAmount();
			transactionTypeDv = incomeCashFlow.getTransactionType();
			incomeDate = incomeCashFlow.getFlowDate();
			mappedExpenditurePlanList = incomeExpenditureMatchPlanRepository.findByIncomeCashFlow(incomeCashFlow);
		}
		if (transactionTypeDv.getId() != Constants.DVID_TRANSACTION_TYPE_RECEIPT) {
			throw new AppException("Investment Transaction / Cash Flow specified is not an Income", null);
		}
		expenditureSoFarMappedToIncome = mappedExpenditurePlanList.stream()
                .map(IncomeExpenditureMatchPlan::getMappedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if (planVO.getExpenditureInvestmentTransactionId() != null) {
			expenditureCashFlow = null;
			expenditureInvestmentTransaction = investmentTransactionRepository.findById(planVO.getExpenditureInvestmentTransactionId())
					.orElseThrow(() -> new AppException("Invalid Investment Transaction Id " + planVO.getExpenditureInvestmentTransactionId(), null));
			overallExpenditure = BigDecimal.valueOf(expenditureInvestmentTransaction.getDueAmount());
			transactionTypeDv = expenditureInvestmentTransaction.getTransactionType();
			if (expenditureInvestmentTransaction.getStatus().getId() == Constants.DVID_TRANSACTION_STATUS_CANCELLED) {
				throw new AppException("Expenditure Investment Transaction is in Cancelled state", null);
			}
			expenditureDate = expenditureInvestmentTransaction.getDueDate();
			mappedIncomePlanList =  incomeExpenditureMatchPlanRepository.findByExpenditureInvestmentTransaction(expenditureInvestmentTransaction);
		} else {
			expenditureInvestmentTransaction = null;
			expenditureCashFlow = cashFlowRepository.findById(planVO.getExpenditureCashFlowId())
					.orElseThrow(() -> new AppException("Invalid Cash Flow Id " + planVO.getExpenditureCashFlowId(), null));
			overallExpenditure = expenditureCashFlow.getFlowAmount();
			transactionTypeDv = expenditureCashFlow.getTransactionType();
			expenditureDate = expenditureCashFlow.getFlowDate();
			mappedIncomePlanList = incomeExpenditureMatchPlanRepository.findByExpenditureCashFlow(expenditureCashFlow);
		}
		if (transactionTypeDv.getId() != Constants.DVID_TRANSACTION_TYPE_PAYMENT) {
			throw new AppException("Investment Transaction / Cash Flow specified is not an Expenditure", null);
		}
		incomeSoFarMappedToExpenditure = mappedIncomePlanList.stream()
                .map(IncomeExpenditureMatchPlan::getMappedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if (incomeDate == null || expenditureDate == null ) {
			throw new AppException("Both the income date as well as the expenditure date should be present to create a plan with them", null);
		}
		/* if (incomeDate.isAfter(expenditureDate)) {
			throw new AppException("Infeasible plan, as the Income comes after the Expenditure", null);
		} */
		if (overallIncome.compareTo(expenditureSoFarMappedToIncome.add(planVO.getMappedAmount())) < 0) {
			throw new AppException("Outflow exceeds the income", null);
		}
		if (overallExpenditure.compareTo(incomeSoFarMappedToExpenditure.add(planVO.getMappedAmount())) < 0) {
			throw new AppException("Inflow exceeds the expenditure", null);
		}
		
		incomeExpenditureMatchPlanRepository.save(
				new IncomeExpenditureMatchPlan(
						incomeInvestmentTransaction,
						incomeCashFlow,
						expenditureInvestmentTransaction,
						expenditureCashFlow,
						planVO.getMappedAmount(),
						Constants.domainValueCache.get(Constants.DVID_TRANSACTION_STATUS_PENDING)
						)
				);
	}
	
	public void createCashFlow(CashFlowVO cashFlowVO) {
		cashFlowRepository.save(
				new CashFlow(
						cashFlowVO.getFlowDate(),
						cashFlowVO.getFlowAmount(),
						cashFlowVO.getNarration(),
						cashFlowVO.getBankAccountOrInvestorDvId(),
						cashFlowVO.getTransactionTypeDvId(),
						cashFlowVO.getTransactionCategoryDvId(),
						cashFlowVO.getEndAccountReference()
						)
				);
	}
	
	public void updatePlanStatus(long incomeExpenditureMatchPlanId, long planStatusDvId) {
		IncomeExpenditureMatchPlan incomeExpenditureMatchPlan;
		
		incomeExpenditureMatchPlan = incomeExpenditureMatchPlanRepository.findById(incomeExpenditureMatchPlanId)
				.orElseThrow(() -> new AppException("Invalid Plan Id " + incomeExpenditureMatchPlanId, null));
		incomeExpenditureMatchPlan.setStatus(Constants.domainValueCache.get(planStatusDvId));
		incomeExpenditureMatchPlanRepository.save(incomeExpenditureMatchPlan);
	}
}
