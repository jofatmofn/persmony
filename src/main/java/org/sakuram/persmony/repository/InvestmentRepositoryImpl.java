package org.sakuram.persmony.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

public class InvestmentRepositoryImpl implements InvestmentRepositoryCustom {

    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<Investment> executeDynamicQuery(String queryString) {
    	LogManager.getLogger().debug(queryString);
    	Query query;
    	query = entityManager.createNativeQuery(queryString, Investment.class);
    	return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> searchInvestments(List<SearchCriterionVO> searchCriterionVOList) {
    	Query query;
    	StringBuffer mainQueryStringBuffer, itSubQueryStringBuffer, rSubQueryStringBuffer, stringBuffer;
		String queryString;
		boolean isFirstTime;
		FieldSpecVO fieldSpecVO;
		
		isFirstTime = true;
		mainQueryStringBuffer = new StringBuffer();
		itSubQueryStringBuffer = new StringBuffer();
		rSubQueryStringBuffer = new StringBuffer();
		
		mainQueryStringBuffer.append("SELECT iDV.value AS investor, ppDV.value AS productProvider, daDV.value AS dematAccount, fDV.value AS facilitator, I.investor_id_with_provider, I.product_id_of_provider, I.investment_id_with_provider, I.product_name, ptDV.value AS productType, I.units, I.worth, I.clean_price, I.accrued_interest, I.charges, I.rate_of_interest, tDV.value AS taxability, I.previous_investment_fk, nirDV.value AS newInvestmentReason, I.investment_start_date, I.investment_end_date, I.is_closed, ctDV.value AS closureType, I.closure_date, I.is_accrual_applicable, I.dynamic_receipt_periodicity, pbDV.value AS providerBranch, I.id ");
		mainQueryStringBuffer.append("FROM investment I ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value iDV ON I.investor_fk = iDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ppDV ON I.product_provider_fk = ppDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value daDV ON I.demat_account_fk = daDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value fDV ON I.facilitator_fk = fDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ptDV ON I.product_type_fk = ptDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tDV ON I.taxability_fk = tDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value nirDV ON I.new_investment_reason_fk = nirDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ctDV ON I.closure_type_fk = ctDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value pbDV ON I.provider_branch_fk = pbDV.id ");
		
		for (SearchCriterionVO searchCriterionVO : searchCriterionVOList) {
			if(searchCriterionVO.getFieldName() == null || searchCriterionVO.getValuesCSV() == null ||
					searchCriterionVO.getFieldName().equals("") || searchCriterionVO.getValuesCSV().equals("")) {
				continue;
			}
			if(searchCriterionVO.getFieldName().startsWith("IT.")) {
				stringBuffer = itSubQueryStringBuffer;
				stringBuffer.append("AND ");
			} else if(searchCriterionVO.getFieldName().startsWith("R.")) {
					stringBuffer = rSubQueryStringBuffer;
					stringBuffer.append("AND ");
			} else {
				stringBuffer = mainQueryStringBuffer;
				stringBuffer.append(isFirstTime ? "WHERE " : "AND ");
				isFirstTime = false;
			}
			searchCriterionVO.setValuesCSV(searchCriterionVO.getValuesCSV().replaceAll("[^A-Za-z0-9\\-., &/]", "").trim().toLowerCase());
			fieldSpecVO = Constants.SEARCH_FIELD_SPEC_MAP.get(searchCriterionVO.getFieldName());
			if (fieldSpecVO.getDataType() == FieldSpecVO.DataType.BOOLEAN) {
				switch(searchCriterionVO.getValuesCSV()) {
				case "true":
					break;
				case "false":
					stringBuffer.append("NOT ");
					break;
				default:
					throw new AppException("Invalid value specified for " + searchCriterionVO.getFieldName(), null);
				}
				stringBuffer.append(searchCriterionVO.getFieldName());
				stringBuffer.append(" ");
			} else if (fieldSpecVO.getIsSequencable() != null && fieldSpecVO.getIsSequencable()) {	// Date or Numbers
				stringBuffer.append(UtilFuncs.sqlWhereClauseSequencable(searchCriterionVO, fieldSpecVO.getDataType() == FieldSpecVO.DataType.DATE));
			} else if (fieldSpecVO.getIsFreeText() != null && fieldSpecVO.getIsFreeText()) {
				stringBuffer.append(UtilFuncs.sqlWhereClauseText(searchCriterionVO));
			} else if (fieldSpecVO.getIsDvSelect() != null && fieldSpecVO.getIsDvSelect()) {
				stringBuffer.append("LOWER(");
				switch(fieldSpecVO.getDvCategory()) {
				// FKs of Investment
				case Constants.CATEGORY_INVESTOR:
					stringBuffer.append("iDV");
					break;
				case Constants.CATEGORY_PARTY:
					if (fieldSpecVO.getLabel().equals("Product Provider")) {	// TODO: Clean this dependency on label
						stringBuffer.append("ppDV");
					} else {
						stringBuffer.append("fDV");
					}
					break;
				case Constants.CATEGORY_DEMAT_ACCOUNT:
					stringBuffer.append("daDV");
					break;
				case Constants.CATEGORY_PRODUCT_TYPE:
					stringBuffer.append("ptDV");
					break;
				case Constants.CATEGORY_TAXABILITY:
					stringBuffer.append("tDV");
					break;
				case Constants.CATEGORY_NEW_INVESTMENT_REASON:
					stringBuffer.append("nirDV");
					break;
				case Constants.CATEGORY_CLOSURE_TYPE:
					stringBuffer.append("ctDV");
					break;
				case Constants.CATEGORY_BRANCH:
					stringBuffer.append("pbDV");
					break;
				// FKs of Investment Transaction
				case Constants.CATEGORY_TRANSACTION_TYPE:
					stringBuffer.append("ttDV");
					break;
				case Constants.CATEGORY_TRANSACTION_STATUS:
					stringBuffer.append("tsDV");
					break;
				case Constants.CATEGORY_TAX_GROUP:
					stringBuffer.append("tgDV");
					break;
				// FKs of Realisation
				case Constants.CATEGORY_REALISATION_TYPE:
					stringBuffer.append("rtDV");
					break;
				}
				stringBuffer.append(".value) = '");
				stringBuffer.append(searchCriterionVO.getValuesCSV());
				stringBuffer.append("' ");
			}
		}
		if (itSubQueryStringBuffer.length() > 0) {
			mainQueryStringBuffer.append(isFirstTime ? "WHERE " : "AND ");
			isFirstTime = false;
			mainQueryStringBuffer.append("EXISTS(SELECT 1 FROM investment_transaction IT ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ttDV ON IT.transaction_type_fk = ttDV.id ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tsDV ON IT.status_fk = tsDV.id ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tgDV ON IT.tax_group_fk = tgDV.id ");
			mainQueryStringBuffer.append("WHERE IT.investment_fk = I.id ");
			mainQueryStringBuffer.append(itSubQueryStringBuffer);
			mainQueryStringBuffer.append(") ");
		}
		if (rSubQueryStringBuffer.length() > 0) {
			mainQueryStringBuffer.append(isFirstTime ? "WHERE " : "AND ");
			isFirstTime = false;
			mainQueryStringBuffer.append("EXISTS(SELECT 1 FROM realisation R ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value rtDV ON R.realisation_type_fk = rtDV.id ");
			mainQueryStringBuffer.append("JOIN investment_transaction IT ON R.investment_transaction_fk = IT.id ");
			mainQueryStringBuffer.append("WHERE IT.investment_fk = I.id ");
			mainQueryStringBuffer.append(rSubQueryStringBuffer);
			mainQueryStringBuffer.append(") ");
		}
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}
}
