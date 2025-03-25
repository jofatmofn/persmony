package org.sakuram.persmony.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsRequestVO;

public class RealisationRepositoryImpl implements RealisationRepositoryCustom {
	
    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Object[]> retrieveAccrualsRealisations(RetrieveAccrualsRealisationsRequestVO retrieveAccrualsRealisationsRequestVO) {
    	Query query;
    	StringBuffer queryStringBuffer;
		String queryString;

		queryStringBuffer = new StringBuffer();

		queryStringBuffer.append("SELECT I.id i_id, iDV.value investor, pDV.value product_provider, I.investment_id_with_provider, ptDV.value product_type, I.worth,");
		queryStringBuffer.append(" IT.id it_id, IT.transaction_type_fk, ttDV.value transaction_type, tgDV.value tax_group, COALESCE(IT. accounted_transaction_date, IT.due_date) it_due_date, IT.due_amount, IT.interest_amount it_interest_amount, IT.tds_amount it_tds_amount, IT.accrual_tds_reference, IT.in_ais it_in_ais, IT.form26as_booking_date it_form26as_booking_date,");
		queryStringBuffer.append(" R.id r_id, COALESCE(R.accounted_realisation_date, R.realisation_date) r_realisation_date, R.amount r_realisation_amount, R.interest_amount r_interest_amount, R.tds_amount r_tds_amount, R.tds_reference, R.in_ais r_in_ais, R.form26as_booking_date r_form26as_booking_date");
		queryStringBuffer.append(" FROM realisation R");
		queryStringBuffer.append(" RIGHT OUTER JOIN investment_transaction IT ON R.investment_transaction_fk = IT.id");
		queryStringBuffer.append(" LEFT OUTER JOIN investment I ON IT.investment_fk = I.id");
		queryStringBuffer.append(" LEFT OUTER JOIN domain_value pDV ON I.product_provider_fk = pDV.id");
		queryStringBuffer.append(" LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id");
		queryStringBuffer.append(" LEFT OUTER JOIN domain_value ptDV ON I.product_type_fk = ptDV.id");
		queryStringBuffer.append(" LEFT OUTER JOIN domain_value ttDV ON IT.transaction_type_fk = ttDV.id");
		queryStringBuffer.append(" LEFT OUTER JOIN domain_value tgDV ON IT.tax_group_fk = tgDV.id");
		queryStringBuffer.append(" WHERE status_fk = ");			// Only COMPLETED, not PENDING and CANCELLED
		queryStringBuffer.append(Constants.DVID_TRANSACTION_STATUS_COMPLETED);
		queryStringBuffer.append(" AND transaction_type_fk IN (");	// Only RECEIPTs and ACCRUALs, not PAYMENTs
		queryStringBuffer.append(Constants.DVID_TRANSACTION_TYPE_RECEIPT);
		queryStringBuffer.append(", ");
		queryStringBuffer.append(Constants.DVID_TRANSACTION_TYPE_ACCRUAL);
		queryStringBuffer.append(")");
		queryStringBuffer.append(" AND COALESCE(COALESCE(R.accounted_realisation_date,R.realisation_date), IT.due_date) BETWEEN '");
		queryStringBuffer.append(retrieveAccrualsRealisationsRequestVO.getFyStartYear() + "-04-01'");
		queryStringBuffer.append(" AND '");
		queryStringBuffer.append((retrieveAccrualsRealisationsRequestVO.getFyStartYear() + 1) + "-03-31'");
		
		if (retrieveAccrualsRealisationsRequestVO.getInvestorDvId() != null) {
			queryStringBuffer.append(" AND I.investor_fk IN (");
			queryStringBuffer.append(StringUtils.join(Constants.PRIMARY_TO_INVESTOR_LIST_MAP.get(retrieveAccrualsRealisationsRequestVO.getInvestorDvId()), ','));
			queryStringBuffer.append(")");
		}
		if (retrieveAccrualsRealisationsRequestVO.getProductProviderDvId() != null) {
			queryStringBuffer.append(" AND I.product_provider_fk = ");
			queryStringBuffer.append(retrieveAccrualsRealisationsRequestVO.getProductProviderDvId());
		}
		if (retrieveAccrualsRealisationsRequestVO.getInForm26as() != null) {
			queryStringBuffer.append(" AND ");
			queryStringBuffer.append(presenceAbsenceCheck(retrieveAccrualsRealisationsRequestVO.getInForm26as(), "form26as_booking_date"));
		}
		if (retrieveAccrualsRealisationsRequestVO.getInAis() != null) {
			queryStringBuffer.append(" AND ");
			queryStringBuffer.append(presenceAbsenceCheck(retrieveAccrualsRealisationsRequestVO.getInAis(), "in_ais"));
		}
		if (retrieveAccrualsRealisationsRequestVO.getWithInterest() != null) {
			queryStringBuffer.append(" AND ");
			queryStringBuffer.append(presenceAbsenceCheck(retrieveAccrualsRealisationsRequestVO.getWithInterest(), "interest_amount"));
		}
		if (retrieveAccrualsRealisationsRequestVO.getWithTds() != null) {
			queryStringBuffer.append(" AND ");
			queryStringBuffer.append(presenceAbsenceCheck(retrieveAccrualsRealisationsRequestVO.getWithTds(), "tds_amount"));
		}

		queryString = queryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();

	}
	
	private StringBuffer presenceAbsenceCheck(boolean toBePresent, String columnChecked) {
		StringBuffer stringBuffer;
		stringBuffer = new StringBuffer();
		stringBuffer.append("(IT.transaction_type_fk = ");
		stringBuffer.append(Constants.DVID_TRANSACTION_TYPE_ACCRUAL);
		stringBuffer.append(" AND IT.");
		stringBuffer.append(columnChecked);
		stringBuffer.append(" IS");
		if (toBePresent) {
			stringBuffer.append(" NOT");
		}
		stringBuffer.append(" NULL");
		stringBuffer.append(" OR IT.transaction_type_fk = ");
		stringBuffer.append(Constants.DVID_TRANSACTION_TYPE_RECEIPT);
		stringBuffer.append(" AND R.");
		stringBuffer.append(columnChecked);
		stringBuffer.append(" IS");
		if (toBePresent) {
			stringBuffer.append(" NOT");
		}
		stringBuffer.append(" NULL)");
		return stringBuffer;
	}

}
