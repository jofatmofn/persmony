package org.sakuram.persmony.repository;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class CashFlowRepositoryImpl implements CashFlowRepositoryCustom {

    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Object[]> searchCashFlows(SbAcTxnCriteriaVO cfCriteriaVO) {
    	Query query;
    	StringBuffer mainQueryStringBuffer;
		String queryString;
		boolean toFormQueryForEar;
		
		mainQueryStringBuffer = new StringBuffer();
		
		mainQueryStringBuffer.append("SELECT CF.id AS cfId, baDV.id AS baId, baDV.value AS bankAccountOrInvestor, CF.flow_date, ttDV.id AS ttId, ttDV.value AS transactionType, CF.flow_amount, CF.narration, tcDV.id AS tcId, tcDV.value AS transactionCategory, CF.end_account_reference ");
		mainQueryStringBuffer.append("FROM cash_flow CF ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value baDV ON CF.bank_account_or_investor_fk = baDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ttDV ON CF.transaction_type_fk = ttDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tcDV ON CF.transaction_category_fk = tcDV.id ");
		mainQueryStringBuffer.append("WHERE true ");

		if (cfCriteriaVO.getFromId() != null) {
			mainQueryStringBuffer.append("AND CF.id >= ");
			mainQueryStringBuffer.append(cfCriteriaVO.getFromId());
			mainQueryStringBuffer.append(" ");
		}
		if (cfCriteriaVO.getToId() != null) {
			mainQueryStringBuffer.append("AND CF.id <= ");
			mainQueryStringBuffer.append(cfCriteriaVO.getToId());
			mainQueryStringBuffer.append(" ");
		}
		if (cfCriteriaVO.getFromDate() != null) {
			mainQueryStringBuffer.append("AND CF.flow_date >= '");
			mainQueryStringBuffer.append(cfCriteriaVO.getFromDate().toString());
			mainQueryStringBuffer.append("' ");
		}
		if (cfCriteriaVO.getToDate() != null) {
			mainQueryStringBuffer.append("AND CF.flow_date <= '");
			mainQueryStringBuffer.append(cfCriteriaVO.getToDate().toString());
			mainQueryStringBuffer.append("' ");
		}
		if (cfCriteriaVO.getFromAmount() != null) {
			mainQueryStringBuffer.append("AND CF.flow_amount >= ");
			mainQueryStringBuffer.append(cfCriteriaVO.getFromAmount());
			mainQueryStringBuffer.append(" ");
		}
		if (cfCriteriaVO.getToAmount() != null) {
			mainQueryStringBuffer.append("AND CF.flow_amount <= ");
			mainQueryStringBuffer.append(cfCriteriaVO.getToAmount());
			mainQueryStringBuffer.append(" ");
		}
		if (cfCriteriaVO.getNarrationOperatorIdValueVO() != null && UtilFuncs.toFormQueryForNullableField(cfCriteriaVO.getNarrationOperatorIdValueVO().getValue(), cfCriteriaVO.getNarration())) {
			mainQueryStringBuffer.append("AND ");
			mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("CF.narration", cfCriteriaVO.getNarrationOperatorIdValueVO().getValue(), cfCriteriaVO.getNarration())));
		}
		if (cfCriteriaVO.getBankAccountOrInvestorIdValueVO() != null) {
			mainQueryStringBuffer.append("AND CF.bank_account_or_investor_fk = ");
			mainQueryStringBuffer.append(cfCriteriaVO.getBankAccountOrInvestorIdValueVO().getId());
			mainQueryStringBuffer.append(" ");
		}
		if (cfCriteriaVO.getBookingDvId() != null) {
			mainQueryStringBuffer.append("AND CF.transaction_type_fk = ");
			mainQueryStringBuffer.append(cfCriteriaVO.getBookingDvId() == Constants.DVID_BOOKING_CREDIT ? Constants.DVID_TRANSACTION_TYPE_RECEIPT : Constants.DVID_TRANSACTION_TYPE_PAYMENT);
			mainQueryStringBuffer.append(" ");
		}
		
		if (cfCriteriaVO.getTransactionCategoryDvId() != null || cfCriteriaVO.getEndAccountReferenceOperator() != null) {
			if (cfCriteriaVO.getTransactionCategoryDvId() != null) {
				mainQueryStringBuffer.append("AND CF.transaction_category_fk = ");
				mainQueryStringBuffer.append(cfCriteriaVO.getTransactionCategoryDvId());
				mainQueryStringBuffer.append(" ");
			}
			toFormQueryForEar = UtilFuncs.toFormQueryForNullableField(cfCriteriaVO.getEndAccountReferenceOperator(), cfCriteriaVO.getEndAccountReference());
			if (toFormQueryForEar) {
				mainQueryStringBuffer.append("AND ");
				mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("CF.end_account_reference", cfCriteriaVO.getEndAccountReferenceOperator(), cfCriteriaVO.getEndAccountReference())));
			}
		}
		
		mainQueryStringBuffer.append("ORDER BY CF.flow_date, CF.id ");
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}

}
