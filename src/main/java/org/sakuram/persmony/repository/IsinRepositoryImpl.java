package org.sakuram.persmony.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

public class IsinRepositoryImpl implements IsinRepositoryCustom {
    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Object[]> searchIsins(IsinCriteriaVO isinCriteriaVO) {
    	Query query;
    	StringBuffer queryStringBuffer;
		String queryString;
		
		queryStringBuffer = new StringBuffer();
		
		queryStringBuffer.append("SELECT isin.isin, isin.company_name, isin.security_name, DV.value AS security_type ");
		queryStringBuffer.append("FROM isin ");
		queryStringBuffer.append("LEFT OUTER JOIN domain_value DV ON isin.security_type_fk = DV.id ");
		queryStringBuffer.append("WHERE 1 = 1 ");
		
		if (UtilFuncs.toFormQueryForNullableField(isinCriteriaVO.getIsinOperator(), isinCriteriaVO.getIsin())) {
			queryStringBuffer.append("AND ");
			queryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("isin.isin", isinCriteriaVO.getIsinOperator(), isinCriteriaVO.getIsin())));
		}
		if (UtilFuncs.toFormQueryForNullableField(isinCriteriaVO.getCompanyNameOperator(), isinCriteriaVO.getCompanyName())) {
			queryStringBuffer.append("AND ");
			queryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("isin.company_name", isinCriteriaVO.getCompanyNameOperator(), isinCriteriaVO.getCompanyName())));
		}
		if (UtilFuncs.toFormQueryForNullableField(isinCriteriaVO.getSecurityNameOperator(), isinCriteriaVO.getSecurityName())) {
			queryStringBuffer.append("AND ");
			queryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("isin.security_name", isinCriteriaVO.getSecurityNameOperator(), isinCriteriaVO.getSecurityName())));
		}
		if (isinCriteriaVO.getSecurityTypeDvId() != null) {
			queryStringBuffer.append("AND isin.security_type_fk = ");
			queryStringBuffer.append(isinCriteriaVO.getSecurityTypeDvId());
			queryStringBuffer.append(" ");
		}
		
		queryStringBuffer.append("ORDER BY isin.isin ");
		
		queryString = queryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}
}
