package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.CashFlow;
import org.sakuram.persmony.bean.IncomeExpenditureMatchPlan;
import org.sakuram.persmony.bean.InvestmentTransaction;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.PlanSearchCriteriaVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IncomeExpenditureMatchPlanRepository extends JpaRepository<IncomeExpenditureMatchPlan, Long> {
	public List<IncomeExpenditureMatchPlan> findByIncomeInvestmentTransaction(InvestmentTransaction incomeInvestmentTransaction);
	public List<IncomeExpenditureMatchPlan> findByIncomeCashFlow(CashFlow incomeCashFlow);
	public List<IncomeExpenditureMatchPlan> findByExpenditureInvestmentTransaction(InvestmentTransaction expenditureInvestmentTransaction);
	public List<IncomeExpenditureMatchPlan> findByExpenditureCashFlow(CashFlow expenditureCashFlow);

	@Query(nativeQuery = true, value =
			"SELECT IEMP.* "
					+ "FROM income_expenditure_match_plan IEMP "
					+ "LEFT OUTER JOIN investment_transaction IIT ON IEMP.income_investment_transaction_fk = IIT.id "
					+ "LEFT OUTER JOIN investment II ON IIT.investment_fk = II.id "
					+ "LEFT OUTER JOIN cash_flow ICF ON IEMP.income_cash_flow_fk = ICF.id "
					+ "LEFT OUTER JOIN investment_transaction EIT ON IEMP.expenditure_investment_transaction_fk = EIT.id "
					+ "LEFT OUTER JOIN cash_flow ECF ON IEMP.expenditure_cash_flow_fk = ECF.id "
					+ "LEFT OUTER JOIN domain_value sDV ON IEMP.status_fk = sDV.id "
					+ "WHERE true "

					// Income Investment Transaction
					+ "AND ((IIT.id IS NOT NULL "
					+ "AND (IIT.due_date IS NULL OR CAST(:#{#planSearchCriteriaVO.incomeFromDate} AS DATE) IS NULL OR IIT.due_date >= :#{#planSearchCriteriaVO.incomeFromDate}) "
					+ "AND (IIT.due_date IS NULL OR CAST(:#{#planSearchCriteriaVO.incomeToDate} AS DATE) IS NULL OR IIT.due_date <= :#{#planSearchCriteriaVO.incomeToDate}) "
					+ "AND (CAST(:#{#planSearchCriteriaVO.incomeBankAccountOrInvestorDvId} AS BIGINT) IS NULL OR II.default_bank_account_fk = :#{#planSearchCriteriaVO.incomeBankAccountOrInvestorDvId})) "
					// Income Cash Flow
					+ "OR (ICF.id IS NOT NULL "
					+ "AND (ICF.flow_date IS NULL OR CAST(:#{#planSearchCriteriaVO.incomeFromDate} AS DATE) IS NULL OR ICF.flow_date >= :#{#planSearchCriteriaVO.incomeFromDate}) "
					+ "AND (ICF.flow_date IS NULL OR CAST(:#{#planSearchCriteriaVO.incomeToDate} AS DATE) IS NULL OR ICF.flow_date <= :#{#planSearchCriteriaVO.incomeToDate}) "
					+ "AND (CAST(:#{#planSearchCriteriaVO.incomeBankAccountOrInvestorDvId} AS BIGINT) IS NULL OR ICF.bank_account_or_investor_fk = :#{#planSearchCriteriaVO.incomeBankAccountOrInvestorDvId}))) "
					// Expenditure Investment Transaction
					+ "AND ((EIT.id IS NOT NULL "
					+ "AND (EIT.due_date IS NULL OR CAST(:#{#planSearchCriteriaVO.expenditureFromDate} AS DATE) IS NULL OR EIT.due_date >= :#{#planSearchCriteriaVO.expenditureFromDate}) "
					+ "AND (EIT.due_date IS NULL OR CAST(:#{#planSearchCriteriaVO.expenditureToDate} AS DATE) IS NULL OR EIT.due_date <= :#{#planSearchCriteriaVO.expenditureToDate}) "
					+ "AND (CAST(:#{#planSearchCriteriaVO.expenditureBankAccountOrInvestorDvId} AS BIGINT) IS NULL OR II.default_bank_account_fk = :#{#planSearchCriteriaVO.expenditureBankAccountOrInvestorDvId})) "
					// Expenditure Cash Flow
					+ "OR (ECF.id IS NOT NULL "
					+ "AND (ECF.flow_date IS NULL OR CAST(:#{#planSearchCriteriaVO.expenditureFromDate} AS DATE) IS NULL OR ECF.flow_date >= :#{#planSearchCriteriaVO.expenditureFromDate}) "
					+ "AND (ECF.flow_date IS NULL OR CAST(:#{#planSearchCriteriaVO.expenditureToDate} AS DATE) IS NULL OR ECF.flow_date <= :#{#planSearchCriteriaVO.expenditureToDate}) "
					+ "AND (CAST(:#{#planSearchCriteriaVO.expenditureBankAccountOrInvestorDvId} AS BIGINT) IS NULL OR ECF.bank_account_or_investor_fk = :#{#planSearchCriteriaVO.expenditureBankAccountOrInvestorDvId}))) "
					
					+ "AND (CAST(:#{#planSearchCriteriaVO.mappedFromAmount} AS NUMERIC) IS NULL OR IEMP.mapped_amount >= :#{#planSearchCriteriaVO.mappedFromAmount}) "
					+ "AND (CAST(:#{#planSearchCriteriaVO.mappedToAmount} AS NUMERIC) IS NULL OR IEMP.mapped_amount <= :#{#planSearchCriteriaVO.mappedToAmount}) "
					
					+ "	AND (:#{#planSearchCriteriaVO.isStatusPending} AND IEMP.status_fk = " + Constants.DVID_TRANSACTION_STATUS_PENDING
					+ "	OR :#{#planSearchCriteriaVO.isStatusCancelled} AND IEMP.status_fk = " + Constants.DVID_TRANSACTION_STATUS_CANCELLED
					+ "	OR :#{#planSearchCriteriaVO.isStatusCompleted} AND IEMP.status_fk = " + Constants.DVID_TRANSACTION_STATUS_COMPLETED
					+ ") "

					+ "ORDER BY COALESCE(IIT.due_date, ICF.flow_date) "
					)
	public List<IncomeExpenditureMatchPlan> searchIncomeExpenditureMatchPlans(PlanSearchCriteriaVO planSearchCriteriaVO);
}
