package org.sakuram.persmony.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.valueobject.IsinActionCriteriaVO;

public class IsinActionRepositoryImpl implements IsinActionRepositoryCustom {

    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<IsinAction> findIsinIndependentIsinActions(String isin, LocalDate sellDate, Long dematAccount) {
    	Query query;
    	StringBuffer mainQueryStringBuffer;
		String queryString;
		
		mainQueryStringBuffer = new StringBuffer();

		mainQueryStringBuffer.append("SELECT IA.* ");
		mainQueryStringBuffer.append("FROM isin_action IA ");
		mainQueryStringBuffer.append("WHERE (IA.isin_fk = '");
		mainQueryStringBuffer.append(isin.trim().toUpperCase());
		mainQueryStringBuffer.append("' ");
		mainQueryStringBuffer.append("OR IA.isin_fk IN ");
		mainQueryStringBuffer.append("(SELECT I2.isin ");
		mainQueryStringBuffer.append("FROM isin_action IA1 ");
		mainQueryStringBuffer.append("JOIN isin I1 ON I1.isin = IA1.isin_fk ");
		mainQueryStringBuffer.append("JOIN isin I2 ON I2.stock_id = I1.stock_id ");
		mainQueryStringBuffer.append("WHERE IA1.isin_fk = '");
		mainQueryStringBuffer.append(isin.trim().toUpperCase());
		mainQueryStringBuffer.append("')) ");
		mainQueryStringBuffer.append("AND IA.settlement_date <= '");
		mainQueryStringBuffer.append(sellDate.toString());
		mainQueryStringBuffer.append("' ");
		if (dematAccount != null) {
			mainQueryStringBuffer.append("AND IA.demat_account_fk = ");
			mainQueryStringBuffer.append(String.valueOf(dematAccount));
			mainQueryStringBuffer.append(" ");
		}
		mainQueryStringBuffer.append("ORDER BY IA.settlement_date, settlement_sequence NULLS LAST ");
		
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString, IsinAction.class);
    	return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> searchIsinActions(IsinActionCriteriaVO isinActionCriteriaVO) {
    	Query query;
    	StringBuffer mainQueryStringBuffer;
		String queryString;
		
		mainQueryStringBuffer = new StringBuffer();
		
		mainQueryStringBuffer.append("SELECT IA.id AS isinActionId, IA.settlement_date, IA.isin_fk, I.security_name, atDV.id AS atId, atDV.value AS atValue, qbDV.id AS qbId, qbDV.value AS qbValue, daDV.id AS daId, daDV.value AS daValue, IA.is_internal ");
		mainQueryStringBuffer.append("FROM isin_action IA ");
		mainQueryStringBuffer.append("JOIN action A ON IA.action_fk = A.id ");
		mainQueryStringBuffer.append("JOIN isin I ON IA.isin_fk = I.isin ");
		mainQueryStringBuffer.append("JOIN domain_value atDV ON A.action_type_fk = atDV.id ");
		mainQueryStringBuffer.append("JOIN domain_value qbDV ON IA.quantity_booking_fk = qbDV.id ");
		mainQueryStringBuffer.append("JOIN domain_value daDV ON IA.demat_account_fk = daDV.id ");
		
		mainQueryStringBuffer.append("WHERE true ");

		if (isinActionCriteriaVO.getFromId() != null) {
			mainQueryStringBuffer.append("AND IA.id >= ");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getFromId());
			mainQueryStringBuffer.append(" ");
		}
		if (isinActionCriteriaVO.getToId() != null) {
			mainQueryStringBuffer.append("AND IA.id <= ");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getToId());
			mainQueryStringBuffer.append(" ");
		}
		if (isinActionCriteriaVO.getFromDate() != null) {
			mainQueryStringBuffer.append("AND IA.settlement_date >= '");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getFromDate().toString());
			mainQueryStringBuffer.append("' ");
		}
		if (isinActionCriteriaVO.getToDate() != null) {
			mainQueryStringBuffer.append("AND IA.settlement_date <= '");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getToDate().toString());
			mainQueryStringBuffer.append("' ");
		}
		if (isinActionCriteriaVO.getIsin() != null) {
			mainQueryStringBuffer.append("AND IA.isin_fk = '");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getIsin().trim().toUpperCase());
			mainQueryStringBuffer.append("' ");
		}
		if (isinActionCriteriaVO.getDematAccountDvId() != null) {
			mainQueryStringBuffer.append("AND IA.demat_account_fk = '");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getDematAccountDvId());
			mainQueryStringBuffer.append("' ");
		}
		if (isinActionCriteriaVO.getActionTypeDvId() != null) {
			mainQueryStringBuffer.append("AND atDV.id = ");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getActionTypeDvId());
			mainQueryStringBuffer.append(" ");
		}
		if (isinActionCriteriaVO.getQuantityBookingDvId() != null) {
			mainQueryStringBuffer.append("AND qbDV.id = ");
			mainQueryStringBuffer.append(isinActionCriteriaVO.getQuantityBookingDvId());
			mainQueryStringBuffer.append(" ");
		}
		if (isinActionCriteriaVO.getIsInternal() != null) {
			mainQueryStringBuffer.append("AND ");
			if (!isinActionCriteriaVO.getIsInternal()) {
				mainQueryStringBuffer.append("NOT ");
			}
			mainQueryStringBuffer.append("is_internal ");
		}
		
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}
}
