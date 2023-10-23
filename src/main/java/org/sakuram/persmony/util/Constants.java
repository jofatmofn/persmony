package org.sakuram.persmony.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.valueobject.FieldSpecVO;

public class Constants {

	public static final String CATEGORY_INVESTOR = "Invstr";
	public static final String CATEGORY_PARTY = "Party"; /* Product Provider, Facilitator */
	public static final String CATEGORY_PRODUCT_TYPE = "PrdType";
	public static final String CATEGORY_ACCOUNTING_BASIS = "AcBasis";
	public static final String CATEGORY_TAXABILITY = "Taxblty";
	public static final String CATEGORY_NEW_INVESTMENT_REASON = "InvRson";
	public static final String CATEGORY_TRANSACTION_STATUS = "TxnStts";
	public static final String CATEGORY_TRANSACTION_TYPE = "TxnType";
	public static final String CATEGORY_DEMAT_ACCOUNT = "DematAc";
	public static final String CATEGORY_CLOSURE_TYPE = "ClsType";
	public static final String CATEGORY_ACCOUNT = "Account";
	public static final String CATEGORY_REALISATION_TYPE = "RlsnTyp";
	public static final String CATEGORY_BRANCH = "Branch";
	
	public static final long DVID_NEW_INVESTMENT_REASON_RENEWAL = 67;
	public static final long DVID_TRANSACTION_STATUS_PENDING = 69;
	public static final long DVID_TRANSACTION_STATUS_CANCELLED = 70;
	public static final long DVID_TRANSACTION_STATUS_COMPLETED = 71;
	public static final long DVID_TRANSACTION_TYPE_PAYMENT = 72;
	public static final long DVID_TRANSACTION_TYPE_RECEIPT = 73;
	public static final long DVID_TRANSACTION_TYPE_ACCRUAL = 74;
	public static final long DVID_CLOSURE_TYPE_MATURITY = 76;
	public static final long DVID_REALISATION_TYPE_SAVINGS_ACCOUNT = 101;
	public static final long DVID_REALISATION_TYPE_CASH = 102;
	public static final long DVID_REALISATION_TYPE_ANOTHER_REALISATION = 103;

	public static final int FLAG_POSITION_ACCOUNT_TYPE = 0;
	public static final int FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID = 1;
	public static final int FLAG_POSITION_ACCOUNT_ID = 2;
	public static final int FLAG_POSITION_ACCOUNT_INVESTOR_DVID = 3;
	public static final int FLAG_POSITION_ACCOUNT_STATUS = 4;
	public static final int FLAG_POSITION_FUNDS_ACCOUNT_PARTY_DVID = 1;
	
	public static final int FLAG_POSITION_BRANCH_BANK_DVID = 0;
	public static final int FLAG_POSITION_BRANCH_IFSC = 1;
	public static final int FLAG_POSITION_PARTY_ROLES = 0;
	public static final int FLAG_POSITION_REAL_INVESTORS = 0;

	public static final String ACCOUNT_TYPE_SAVINGS = "SB";
	public static final String ACCOUNT_TYPE_FUNDS = "Funds";
	
	public static final char ACCOUNT_STATUS_OPEN = 'O';
	public static final char ACCOUNT_STATUS_CLOSED = 'C';
	
	public static final byte ASSESSMENT_YEAR_START_MONTH = 4;
	public static final String CSV_DATE_FORMAT = "M/d/yyyy";

	public static final String DV_FLAGS_LEVEL1_SEPARATOR = ";";
	public static final String DV_FLAGS_LEVEL2_SEPARATOR = "-";
	
	public static final Character DYNAMIC_REALISATION_PERIODICITY_YEAR = 'Y';
	
	public static final Map<String, FieldSpecVO> SEARCH_FIELD_SPEC_MAP = new HashMap<String, FieldSpecVO>() {
		private static final long serialVersionUID = 1L;

		{
			put("I.id", new FieldSpecVO("Investment Id", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("closure_date", new FieldSpecVO("Closure Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("investment_id_with_provider", new FieldSpecVO("Investment Id With Provider", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("investor_id_with_provider", new FieldSpecVO("Investor Id With Provider", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("is_closed", new FieldSpecVO("Is Closed?", FieldSpecVO.DataType.BOOLEAN, false, false, false, null));
			put("product_end_date", new FieldSpecVO("Product End Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("product_id_of_provider", new FieldSpecVO("Provider's Product Id", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("product_name", new FieldSpecVO("Product Name", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("rate_of_interest", new FieldSpecVO("Rate of Interest%", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("worth", new FieldSpecVO("Worth", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("closure_type_fk", new FieldSpecVO("Closure Type", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_CLOSURE_TYPE));
			put("demat_account_fk", new FieldSpecVO("Demat Account", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_DEMAT_ACCOUNT));
			put("facilitator_fk", new FieldSpecVO("Facilitator", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_PARTY));
			put("investor_fk", new FieldSpecVO("Investor", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_INVESTOR));
			put("new_investment_reason_fk", new FieldSpecVO("New Investment Reason", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_NEW_INVESTMENT_REASON));
			// previous_investment_fk
			put("product_provider_fk", new FieldSpecVO("Product Provider", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_PARTY));
			put("product_type_fk", new FieldSpecVO("Product Type", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_PRODUCT_TYPE));
			put("taxability_fk", new FieldSpecVO("Taxability", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_TAXABILITY));
			put("is_accrual_applicable", new FieldSpecVO("Is Accrual Applicable?", FieldSpecVO.DataType.BOOLEAN, false, false, false, null));
			// put("dynamic_receipt_periodicity", new FieldSpecVO("Dynamic Receipt Periodicity", FieldSpecVO.DataType.OTHERS, false, false, false, null));
			put("provider_branch_fk", new FieldSpecVO("Provider Branch", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_BRANCH));
			put("accrued_interest", new FieldSpecVO("Accrued Interest", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("charges", new FieldSpecVO("Charges", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("clean_price", new FieldSpecVO("Clean Price", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			
			put("IT.id", new FieldSpecVO("Txn Id", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("IT.transaction_type_fk",new FieldSpecVO("Txn Type", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_TRANSACTION_TYPE));
			put("IT.due_date", new FieldSpecVO("Txn Due Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("IT.due_amount", new FieldSpecVO("Txn Due Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("IT.status_fk",new FieldSpecVO("Txn Status", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_TRANSACTION_STATUS));
			put("IT.returned_principal_amount", new FieldSpecVO("Txn Returned Principal Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("IT.interest_amount", new FieldSpecVO("Txn Interest Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("IT.tds_amount", new FieldSpecVO("Txn TDS Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("IT.taxability_fk",new FieldSpecVO("Txn Taxability", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_TAXABILITY));
			put("IT.assessment_year", new FieldSpecVO("Txn Assessment Year", FieldSpecVO.DataType.OTHERS, true, false, false, null));
		}
	};

	public static Map<Long, DomainValue> domainValueCache;
	public static Map<String, List<Long>> categoryDvIdCache;
}
