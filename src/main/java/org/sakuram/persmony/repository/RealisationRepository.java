package org.sakuram.persmony.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Realisation;

public interface RealisationRepository extends JpaRepository<Realisation, Long> {
	
	@Query(nativeQuery = true, value =
			"SELECT I.id i_id, iDV.value investor, pDV.value product_provider, I.investment_id_with_provider, ptDV.value product_type, I.worth,"
			+ " IT.id it_id, IT.transaction_type_fk, ttDV.value transaction_type, tgDV.value tax_group, COALESCE(IT. accounted_transaction_date, IT.due_date) it_due_date, IT.due_amount, IT.interest_amount it_interest_amount, IT.tds_amount it_tds_amount, IT.accrual_tds_reference, IT.in_ais it_in_ais, IT.form26as_booking_date it_form26as_booking_date,"
			+ " R.id r_id, COALESCE(R.accounted_realisation_date, R.realisation_date) r_realisation_date, R.amount r_realisation_amount, R.interest_amount r_interest_amount, R.tds_amount r_tds_amount, R.tds_reference, R.in_ais r_in_ais, R.form26as_booking_date r_form26as_booking_date"
			+ " FROM realisation R"
			+ " RIGHT OUTER JOIN investment_transaction IT ON R.investment_transaction_fk = IT.id"
			+ "	LEFT OUTER JOIN investment I ON IT.investment_fk = I.id"
			+ "	LEFT OUTER JOIN domain_value pDV ON I.product_provider_fk = pDV.id"
			+ "	LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id"
			+ "	LEFT OUTER JOIN domain_value ptDV ON I.product_type_fk = ptDV.id"
			+ "	LEFT OUTER JOIN domain_value ttDV ON IT.transaction_type_fk = ttDV.id"
			+ "	LEFT OUTER JOIN domain_value tgDV ON IT.tax_group_fk = tgDV.id"
			+ " WHERE status_fk IN (69, 71)"			// Only PENDING and COMPLETED, not CANCELLED
			+ "	AND transaction_type_fk IN (73, 74)"	// Only RECEIPTs and ACCRUALs, not PAYMENTs
			+ "	AND COALESCE(COALESCE(R.accounted_realisation_date,R.realisation_date), IT.due_date) BETWEEN :#{#fromDate} AND :#{#toDate}"
			+ " AND CASE WHEN (:#{#investorId} = -1) THEN TRUE ELSE I.investor_fk = :#{#investorId} END"
			+ " AND CASE WHEN (:#{#productProviderId} = -1) THEN TRUE ELSE I.product_provider_fk = :#{#productProviderId} END"
			+ " AND (NOT :#{#taxDetailNotInForm26as} OR (IT.transaction_type_fk = 73 AND R.form26as_booking_date IS NULL)"
			+ "		OR (IT.transaction_type_fk = 74 AND IT.form26as_booking_date IS NULL))"
			+ " AND (NOT :#{#taxDetailNotInAis} OR (IT.transaction_type_fk = 73 AND NOT COALESCE(R.in_ais, false))"
			+ "		OR (IT.transaction_type_fk = 74 AND NOT COALESCE(IT.in_ais, false)))"
			+ " ORDER BY I.product_provider_fk, IT.due_date DESC")
	public List<Object[]> retrieveAccrualsRealisations(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("investorId") Long investorId, @Param("productProviderId") Long productProviderId, @Param("taxDetailNotInForm26as") boolean taxDetailNotInForm26as, @Param("taxDetailNotInAis") boolean taxDetailNotInAis);
	
}
