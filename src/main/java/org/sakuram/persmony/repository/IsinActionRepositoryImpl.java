package org.sakuram.persmony.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.bean.IsinAction;

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
}
