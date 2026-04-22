package org.sakuram.persmony.bean;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="income_expenditure_match_plan")
public class IncomeExpenditureMatchPlan {

	@Id
	@SequenceGenerator(name="income_expenditure_match_plan_seq_generator",sequenceName="income_expenditure_match_plan_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="income_expenditure_match_plan_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@ManyToOne
	@JoinColumn(name="income_investment_transaction_fk", nullable=true)
	private InvestmentTransaction incomeInvestmentTransaction;
	
	@ManyToOne
	@JoinColumn(name="income_cash_flow_fk", nullable=true)
	private CashFlow incomeCashFlow;
	
	@ManyToOne
	@JoinColumn(name="expenditure_investment_transaction_fk", nullable=true)
	private InvestmentTransaction expenditureInvestmentTransaction;
	
	@ManyToOne
	@JoinColumn(name="expenditure_cash_flow_fk", nullable=true)
	private CashFlow expenditureCashFlow;
	
	@Column(name="mapped_amount", nullable=true, columnDefinition="NUMERIC", precision=13, scale=4)
	private BigDecimal mappedAmount;
	
	@ManyToOne
	@JoinColumn(name="status_fk", nullable=false)
	private DomainValue status;
	
	public IncomeExpenditureMatchPlan(InvestmentTransaction incomeInvestmentTransaction, CashFlow incomeCashFlow, InvestmentTransaction expenditureInvestmentTransaction, CashFlow expenditureCashFlow, BigDecimal mappedAmount, DomainValue status) {
		this.incomeInvestmentTransaction = incomeInvestmentTransaction;
		this.incomeCashFlow = incomeCashFlow;
		this.expenditureInvestmentTransaction = expenditureInvestmentTransaction;
		this.expenditureCashFlow = expenditureCashFlow;
		this.mappedAmount = mappedAmount;
		this.status = status;
	}
}
