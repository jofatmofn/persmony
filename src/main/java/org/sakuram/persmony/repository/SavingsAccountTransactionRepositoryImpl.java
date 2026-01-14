package org.sakuram.persmony.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

public class SavingsAccountTransactionRepositoryImpl implements SavingsAccountTransactionRepositoryCustom {
    @PersistenceContext
    public EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Object[]> searchSavingsAccountTransactions(SbAcTxnCriteriaVO sbAcTxnCriteriaVO) {
    	Query query;
    	StringBuffer mainQueryStringBuffer;
		String queryString;
		boolean toFormQueryForEar;
		
		mainQueryStringBuffer = new StringBuffer();
		
		mainQueryStringBuffer.append("SELECT SAT.id AS satId, baDV.id AS baId, baDV.value AS bankAccountOrInvestor, SAT.transaction_date, SAT.amount, bDV.id AS bId, bDV.value AS booking, SAT.value_date, SAT.reference, ");
		mainQueryStringBuffer.append("SAT.narration, SAT.balance, SAT.transaction_id, SAT.utr_number, SAT.remitter_branch, tcoDV.id AS tcId, tcoDV.value AS transactionCode, SAT.branch_code, ");
		mainQueryStringBuffer.append("SAT.transaction_time, ccDV.id AS ccId, ccDV.value AS costCenter, vtDV.id AS vtId, vtDV.value AS voucherType ");
		mainQueryStringBuffer.append("FROM savings_account_transaction SAT ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value baDV ON SAT.bank_account_or_investor_fk = baDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value bDV ON SAT.booking_fk = bDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value tcoDV ON SAT.transaction_code_fk = tcoDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value ccDV ON SAT.cost_center_fk = ccDV.id ");
		mainQueryStringBuffer.append("LEFT OUTER JOIN domain_value vtDV ON SAT.voucher_type_fk = vtDV.id ");
		mainQueryStringBuffer.append("WHERE true ");

		if (sbAcTxnCriteriaVO.getFromId() != null) {
			mainQueryStringBuffer.append("AND SAT.id >= ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getFromId());
			mainQueryStringBuffer.append(" ");
		}
		if (sbAcTxnCriteriaVO.getToId() != null) {
			mainQueryStringBuffer.append("AND SAT.id <= ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getToId());
			mainQueryStringBuffer.append(" ");
		}
		if (sbAcTxnCriteriaVO.getFromDate() != null) {
			mainQueryStringBuffer.append("AND SAT.transaction_date >= '"); // COALESCE(SAT.value_date, SAT.transaction_date)
			mainQueryStringBuffer.append(Constants.ANSI_DATE_FORMAT.format(sbAcTxnCriteriaVO.getFromDate()));
			mainQueryStringBuffer.append("' ");
		}
		if (sbAcTxnCriteriaVO.getToDate() != null) {
			mainQueryStringBuffer.append("AND SAT.transaction_date <= '"); // COALESCE(SAT.value_date, SAT.transaction_date)
			mainQueryStringBuffer.append(Constants.ANSI_DATE_FORMAT.format(sbAcTxnCriteriaVO.getToDate()));
			mainQueryStringBuffer.append("' ");
		}
		if (sbAcTxnCriteriaVO.getFromAmount() != null) {
			mainQueryStringBuffer.append("AND SAT.amount >= ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getFromAmount());
			mainQueryStringBuffer.append(" ");
		}
		if (sbAcTxnCriteriaVO.getToAmount() != null) {
			mainQueryStringBuffer.append("AND SAT.amount <= ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getToAmount());
			mainQueryStringBuffer.append(" ");
		}
		if (UtilFuncs.toFormQueryForNullableField(sbAcTxnCriteriaVO.getNarrationOperator(), sbAcTxnCriteriaVO.getNarration())) {
			mainQueryStringBuffer.append("AND ");
			mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("SAT.narration", sbAcTxnCriteriaVO.getNarrationOperator(), sbAcTxnCriteriaVO.getNarration())));
		}
		if (sbAcTxnCriteriaVO.getBankAccountOrInvestorDvId() != null) {
			mainQueryStringBuffer.append("AND SAT.bank_account_or_investor_fk = ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getBankAccountOrInvestorDvId());
			mainQueryStringBuffer.append(" ");
		}
		if (sbAcTxnCriteriaVO.getBookingDvId() != null) {
			mainQueryStringBuffer.append("AND SAT.booking_fk = ");
			mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getBookingDvId());
			mainQueryStringBuffer.append(" ");
		}
		
		if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() != null && sbAcTxnCriteriaVO.getTransactionCategoryDvId() == Constants.DVID_EMPTY_SELECT) {
			mainQueryStringBuffer.append("AND NOT EXISTS(SELECT 1 FROM sb_ac_txn_category SATC ");
			mainQueryStringBuffer.append("WHERE SATC.savings_account_transaction_fk = SAT.id) ");
			mainQueryStringBuffer.append("AND NOT EXISTS(SELECT 1 FROM realisation R ");
			mainQueryStringBuffer.append("WHERE R.savings_account_transaction_fk = SAT.id) ");
			mainQueryStringBuffer.append("AND NOT EXISTS(SELECT 1 FROM contract_join_sb_ac_txn CJSAT ");
			mainQueryStringBuffer.append("WHERE CJSAT.savings_account_transaction_fk = SAT.id) ");
			mainQueryStringBuffer.append("AND NOT EXISTS(SELECT 1 FROM contract_eq_join_sb_ac_txn CEJSAT ");
			mainQueryStringBuffer.append("WHERE CEJSAT.savings_account_transaction_fk = SAT.id) ");
		} else if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() != null || sbAcTxnCriteriaVO.getEndAccountReferenceOperator() != null) {
			toFormQueryForEar = UtilFuncs.toFormQueryForNullableField(sbAcTxnCriteriaVO.getEndAccountReferenceOperator(), sbAcTxnCriteriaVO.getEndAccountReference());
			mainQueryStringBuffer.append("AND (");
			
			mainQueryStringBuffer.append("EXISTS(SELECT 1 FROM realisation R JOIN investment_transaction IT ON R.investment_transaction_fk = IT.id ");
			mainQueryStringBuffer.append("WHERE R.savings_account_transaction_fk = SAT.id ");
			if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() == null || sbAcTxnCriteriaVO.getTransactionCategoryDvId() != Constants.DVID_TRANSACTION_CATEGORY_DTI) {
				mainQueryStringBuffer.append("AND false ");
			} else if (toFormQueryForEar) {
				if (NumberUtils.isDigits(sbAcTxnCriteriaVO.getEndAccountReference())) {
					mainQueryStringBuffer.append("AND IT.investment_fk = ");
					mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getEndAccountReference());
					mainQueryStringBuffer.append(" ");
				} else {
					mainQueryStringBuffer.append("AND false ");
				}
			}
			mainQueryStringBuffer.append(") ");
			
			if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() == null || sbAcTxnCriteriaVO.getTransactionCategoryDvId() != Constants.DVID_TRANSACTION_CATEGORY_DTI) {
				mainQueryStringBuffer.append("OR EXISTS(SELECT 1 FROM contract C JOIN contract_join_sb_ac_txn CJSAT ON C.id = CJSAT.contract_fk ");
				mainQueryStringBuffer.append("JOIN isin_action IA ON IA.contract_fk = C.id ");
				mainQueryStringBuffer.append("JOIN isin ON IA.isin_fk = isin.isin ");
				mainQueryStringBuffer.append("WHERE CJSAT.savings_account_transaction_fk = SAT.id ");
				if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() != null) {
					mainQueryStringBuffer.append("AND isin.security_type_fk = ");
					mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getTransactionCategoryDvId());
					mainQueryStringBuffer.append(" ");
				}
				if (toFormQueryForEar) {
					mainQueryStringBuffer.append("AND ");
					mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("isin.isin", sbAcTxnCriteriaVO.getEndAccountReferenceOperator(), sbAcTxnCriteriaVO.getEndAccountReference())));
				}
				mainQueryStringBuffer.append(") ");

				mainQueryStringBuffer.append("OR EXISTS(SELECT 1 FROM contract_eq CE JOIN contract_eq_join_sb_ac_txn CEJSAT ON CE.id = CEJSAT.contract_eq_fk ");
				mainQueryStringBuffer.append("JOIN isin_action IA ON IA.contract_eq_fk = CE.id ");
				mainQueryStringBuffer.append("JOIN isin ON IA.isin_fk = isin.isin ");
				mainQueryStringBuffer.append("WHERE CEJSAT.savings_account_transaction_fk = SAT.id ");
				if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() != null) {
					mainQueryStringBuffer.append("AND isin.security_type_fk = ");
					mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getTransactionCategoryDvId());
					mainQueryStringBuffer.append(" ");
				}
				if (toFormQueryForEar) {
					mainQueryStringBuffer.append("AND ");
					mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("isin.isin", sbAcTxnCriteriaVO.getEndAccountReferenceOperator(), sbAcTxnCriteriaVO.getEndAccountReference())));
				}
				mainQueryStringBuffer.append(") ");

				mainQueryStringBuffer.append("OR EXISTS(SELECT 1 FROM sb_ac_txn_category SATC ");
				mainQueryStringBuffer.append("WHERE SATC.savings_account_transaction_fk = SAT.id ");
				if (sbAcTxnCriteriaVO.getTransactionCategoryDvId() != null) {
					mainQueryStringBuffer.append("AND SATC.transaction_category_fk = ");
					mainQueryStringBuffer.append(sbAcTxnCriteriaVO.getTransactionCategoryDvId());
					mainQueryStringBuffer.append(" ");
				}
				if (toFormQueryForEar) {
					mainQueryStringBuffer.append("AND ");
					mainQueryStringBuffer.append(UtilFuncs.sqlWhereClauseText(new SearchCriterionVO("SATC.end_account_reference", sbAcTxnCriteriaVO.getEndAccountReferenceOperator(), sbAcTxnCriteriaVO.getEndAccountReference())));
				}
				mainQueryStringBuffer.append(") ");
			}
			mainQueryStringBuffer.append(") ");
		}
		
		mainQueryStringBuffer.append("ORDER BY SAT.transaction_date, SAT.id ");
		queryString = mainQueryStringBuffer.toString();
    	LogManager.getLogger().debug(queryString);
    	query = entityManager.createNativeQuery(queryString);
    	return query.getResultList();
	}

}
