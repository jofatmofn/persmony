package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import org.sakuram.persmony.bean.Investment;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {
	List<Investment> findAllByOrderByIdAsc();
	
	@Query(nativeQuery = true, value =
			"SELECT iDV.value AS investor, ppDV.value AS provider, daDV.value AS dematAccount, fDV.value AS facilitator, I.investor_id_with_provider, I.product_id_of_provider, I.investment_id_with_provider, I.product_name, ptDV.value AS productType, I.worth, I.clean_price, I.accrued_interest, I.charges, I.rate_of_interest, tDV.value AS taxability, I.previous_investment_fk, nirDV.value AS newInvestmentReason, I.product_end_date, I.is_closed, ctDV.value AS closureType, I.closure_date, I.is_accrual_applicable, I.dynamic_receipt_periodicity, pbDV.value AS providerBranch "
			+ "FROM investment I "
			+ "	LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id "
			+ "	LEFT OUTER JOIN domain_value ppDV ON I.product_provider_fk = ppDV.id "
			+ "	LEFT OUTER JOIN domain_value daDV ON I.demat_account_fk = daDV.id "
			+ "	LEFT OUTER JOIN domain_value fDV ON I.facilitator_fk = fDV.id "
			+ "	LEFT OUTER JOIN domain_value ptDV ON I.product_type_fk = ptDV.id "
			+ "	LEFT OUTER JOIN domain_value tDV ON I.taxability_fk = tDV.id "
			+ "	LEFT OUTER JOIN domain_value nirDV ON I.new_investment_reason_fk = nirDV.id "
			+ "	LEFT OUTER JOIN domain_value ctDV ON I.closure_type_fk = ctDV.id "
			+ "	LEFT OUTER JOIN domain_value pbDV ON I.provider_branch_fk = pbDV.id ")
	public List<Object[]> searchInvestments();
}
