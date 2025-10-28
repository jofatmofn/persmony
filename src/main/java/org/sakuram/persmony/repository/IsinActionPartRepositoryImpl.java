package org.sakuram.persmony.repository;

import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.bean.IsinActionPart;
import org.sakuram.persmony.util.Constants;

public class IsinActionPartRepositoryImpl implements IsinActionPartRepositoryCustom {

    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<IsinActionPart> findMatchingIsinActionParts(String isin, Date sellDate, Long dematAccount, boolean isIsinIndependent, String orderBy) {
    	Query query;
    	StringBuffer mainQueryStringBuffer;
		String queryString;
		
		mainQueryStringBuffer = new StringBuffer();

		mainQueryStringBuffer.append("SELECT IAP.* ");
		mainQueryStringBuffer.append("FROM isin_action_part IAP JOIN isin_action IA ON IAP.isin_action_fk = IA.id ");
		mainQueryStringBuffer.append("WHERE (IA.isin_fk = '");
		mainQueryStringBuffer.append(isin.trim().toUpperCase());
		mainQueryStringBuffer.append("' ");
		if (isIsinIndependent) {
			mainQueryStringBuffer.append("OR IA.isin_fk IN ");
			mainQueryStringBuffer.append("(SELECT I2.isin ");
			mainQueryStringBuffer.append("FROM isin_action IA1 ");
			mainQueryStringBuffer.append("JOIN isin I1 ON I1.isin = IA1.isin_fk ");
			mainQueryStringBuffer.append("JOIN isin I2 ON I2.stock_id = I1.stock_id ");
			mainQueryStringBuffer.append("WHERE IA1.isin_fk = '");
			mainQueryStringBuffer.append(isin.trim().toUpperCase());
			mainQueryStringBuffer.append("') ");
		}
		mainQueryStringBuffer.append(") ");
		if (dematAccount != null) {
			mainQueryStringBuffer.append("AND IA.demat_account_fk = ");
			mainQueryStringBuffer.append(String.valueOf(dematAccount));
			mainQueryStringBuffer.append(" ");
		}
		mainQueryStringBuffer.append("AND IAP.acquisition_date <= '");
		mainQueryStringBuffer.append(Constants.ANSI_DATE_FORMAT.format(sellDate));
		mainQueryStringBuffer.append("' ");
		mainQueryStringBuffer.append("ORDER BY ");
		if (orderBy.equals("A")) {
			mainQueryStringBuffer.append("IAP.acquisition_date");
		} else {
			mainQueryStringBuffer.append("IA.settlement_date");
		}
		mainQueryStringBuffer.append(", IAP.id ");
		
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString, IsinActionPart.class);
    	return query.getResultList();
	}
	
}
