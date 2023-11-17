package org.sakuram.persmony.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.bean.Investment;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
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
    	StringBuffer mainQueryStringBuffer, subQueryStringBuffer, stringBuffer;
		String queryString;
		boolean isFirstTime;
		FieldSpecVO fieldSpecVO;
		String valuesArr[];
		
		isFirstTime = true;
		mainQueryStringBuffer = new StringBuffer();
		subQueryStringBuffer = new StringBuffer();
		
		mainQueryStringBuffer.append("SELECT iDV.value AS investor, ppDV.value AS productProvider, daDV.value AS dematAccount, fDV.value AS facilitator, I.investor_id_with_provider, I.product_id_of_provider, I.investment_id_with_provider, I.product_name, ptDV.value AS productType, I.worth, I.clean_price, I.accrued_interest, I.charges, I.rate_of_interest, tDV.value AS taxability, I.previous_investment_fk, nirDV.value AS newInvestmentReason, I.product_end_date, I.is_closed, ctDV.value AS closureType, I.closure_date, I.is_accrual_applicable, I.dynamic_receipt_periodicity, pbDV.value AS providerBranch, I.id ");
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
				stringBuffer = subQueryStringBuffer;
				stringBuffer.append("AND ");
			} else {
				stringBuffer = mainQueryStringBuffer;
				stringBuffer.append(isFirstTime ? "WHERE " : "AND ");
				isFirstTime = false;
			}
			searchCriterionVO.setValuesCSV(searchCriterionVO.getValuesCSV().replaceAll("[^A-Za-z0-9\\-., &]", "").trim().toLowerCase());
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
				if (searchCriterionVO.getOperator() == null) {
					throw new AppException("Operator not specified for " + searchCriterionVO.getFieldName(), null);
				}
				stringBuffer.append(searchCriterionVO.getFieldName());
				stringBuffer.append(" ");
				if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.BETWEEN.name()) {
					valuesArr = searchCriterionVO.getValuesCSV().split("\\s*,\\s*");
					if (valuesArr.length != 2) {
						throw new AppException("With BETWEEN as Operator, two values are to be specified for " + searchCriterionVO.getFieldName() + ". Current Input: " + searchCriterionVO.getValuesCSV() + "; Count: " + valuesArr.length, null);	
					}
					if (fieldSpecVO.getDataType() == FieldSpecVO.DataType.DATE) {
						valuesArr[0] = "'" + valuesArr[0] + "'";
						valuesArr[1] = "'" + valuesArr[1] + "'";
					} else {
						try {
							Double.valueOf(valuesArr[0]);
							Double.valueOf(valuesArr[1]);
						} catch (NumberFormatException e) {
							throw new AppException("Numeric value expected for " + searchCriterionVO.getFieldName(), null);
						}
					}
					stringBuffer.append("BETWEEN ");
					stringBuffer.append(valuesArr[0]);
					stringBuffer.append(" AND ");
					stringBuffer.append(valuesArr[1]);
					continue;
				} else {
					if (fieldSpecVO.getDataType() == FieldSpecVO.DataType.OTHERS) {
						try {
							Double.valueOf(searchCriterionVO.getValuesCSV());
						} catch (NumberFormatException e) {
							throw new AppException("Numeric value expected for " + searchCriterionVO.getFieldName(), null);
						}
					}
					if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.EQ.name()) {
						stringBuffer.append("= ");
					} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.NE.name()) {
						stringBuffer.append("<> ");
					} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.LT.name()) {
						stringBuffer.append("< ");
					} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.LE.name()) {
						stringBuffer.append("<= ");
					} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.GT.name()) {
						stringBuffer.append("> ");
					} else if (searchCriterionVO.getOperator() == FieldSpecVO.SeqOperator.GE.name()) {
						stringBuffer.append(">= ");
					}
					if (fieldSpecVO.getDataType() == FieldSpecVO.DataType.DATE) {
						stringBuffer.append("'");
						stringBuffer.append(searchCriterionVO.getValuesCSV());
						stringBuffer.append("' ");
					} else {
						stringBuffer.append(searchCriterionVO.getValuesCSV());
						stringBuffer.append(" ");
					}
				}
			} else if (fieldSpecVO.getIsFreeText() != null && fieldSpecVO.getIsFreeText()) {
				stringBuffer.append("LOWER(");
				stringBuffer.append(searchCriterionVO.getFieldName());
				stringBuffer.append(") ");
				if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.EQ.name()) {
					stringBuffer.append("= ");
					stringBuffer.append("'");
					stringBuffer.append(searchCriterionVO.getValuesCSV());
					stringBuffer.append("' ");
				} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.NE.name()) {
					stringBuffer.append("<> ");
					stringBuffer.append("'");
					stringBuffer.append(searchCriterionVO.getValuesCSV());
					stringBuffer.append("' ");
				} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.STARTS.name()) {
					stringBuffer.append("LIKE '");
					stringBuffer.append(searchCriterionVO.getValuesCSV());
					stringBuffer.append("%' ");
				} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.ENDS.name()) {
					stringBuffer.append("LIKE '%");
					stringBuffer.append(searchCriterionVO.getValuesCSV());
					stringBuffer.append("' ");
				} else if (searchCriterionVO.getOperator() == FieldSpecVO.TxtOperator.CONTAINS.name()) {
					stringBuffer.append("LIKE '%");
					stringBuffer.append(searchCriterionVO.getValuesCSV());
					stringBuffer.append("%' ");
				}
			} else if (fieldSpecVO.getIsDvSelect() != null && fieldSpecVO.getIsDvSelect()) {
				stringBuffer.append("LOWER(");
				switch(fieldSpecVO.getDvCategory()) {
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
				case Constants.CATEGORY_TRANSACTION_TYPE:
					stringBuffer.append("ttDV");
					break;
				case Constants.CATEGORY_TRANSACTION_STATUS:
					stringBuffer.append("tsDV");
					break;
				}
				stringBuffer.append(".value) = '");
				stringBuffer.append(searchCriterionVO.getValuesCSV());
				stringBuffer.append("' ");
			}
		}
		if (subQueryStringBuffer.length() > 0) {
			mainQueryStringBuffer.append(isFirstTime ? "WHERE " : "AND ");
			mainQueryStringBuffer.append("EXISTS(SELECT 1 FROM investment_transaction IT ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ttDV ON IT.transaction_type_fk = ttDV.id ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tsDV ON IT.status_fk = tsDV.id ");
			mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tDV ON IT.taxability_fk = tDV.id ");
			mainQueryStringBuffer.append("WHERE IT.investment_fk = I.id ");
			mainQueryStringBuffer.append(subQueryStringBuffer);
			mainQueryStringBuffer.append(")");
		}
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}
}
