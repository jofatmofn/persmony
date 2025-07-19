package org.sakuram.persmony.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.valueobject.FieldSpecVO;

public class Constants {

	public static final DateFormat ANSI_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final double EPSILON = 0.0000000009D;
	public static final double TOLERATED_DIFFERENCE_AMOUNT = 20D;	// No specific reason for this value
	
	public static final String CATEGORY_INVESTOR = "Invstr";
	public static final String CATEGORY_PRIMARY_INVESTOR = "PrimaryInvestor";
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
	public static final String CATEGORY_TAX_GROUP = "TaxGrp";
	public static final String CATEGORY_TRANSACTION_CATEGORY = "TxnCat";
	public static final String CATEGORY_TRANSACTION_CATEGORY_2 = "TxnCat2";
	public static final String CATEGORY_TRANSACTION_CODE = "TxnCod";
	public static final String CATEGORY_COST_CENTER = "CstCntr";
	public static final String CATEGORY_VOUCHER_TYPE = "VchrTyp";
	public static final String CATEGORY_NONE = "None";
	public static final String CATEGORY_BOOKING = "Booking";
	public static final String CATEGORY_SECURITY_TYPE = "ScrtTyp";
	
	public static final long DVID_NEW_INVESTMENT_REASON_RENEWAL = 67;
	public static final long DVID_NEW_INVESTMENT_REASON_TRANSFER_IN = 188;
	public static final long DVID_NEW_INVESTMENT_REASON_TRANSFER_BALANCE = 189;
	public static final long DVID_TRANSACTION_STATUS_PENDING = 69;
	public static final long DVID_TRANSACTION_STATUS_CANCELLED = 70;
	public static final long DVID_TRANSACTION_STATUS_COMPLETED = 71;
	public static final long DVID_TRANSACTION_TYPE_PAYMENT = 72;
	public static final long DVID_TRANSACTION_TYPE_RECEIPT = 73;
	public static final long DVID_TRANSACTION_TYPE_ACCRUAL = 74;
	public static final long DVID_CLOSURE_TYPE_MATURITY = 76;
	public static final long DVID_CLOSURE_TYPE_TRANSFER_OUT = 185;
	public static final long DVID_REALISATION_TYPE_SAVINGS_ACCOUNT = 101;
	public static final long DVID_REALISATION_TYPE_CASH = 102;
	public static final long DVID_REALISATION_TYPE_ANOTHER_REALISATION = 103;
	public static final long DVID_TRANSACTION_CATEGORY_OTHERS = 221;
	public static final long DVID_TRANSACTION_CATEGORY_DTI = 231;
	public static final long DVID_TRANSACTION_CATEGORY_SALARY = 215;
	public static final long DVID_TRANSACTION_CATEGORY_TAX_INCOME = 216;
	public static final long DVID_TRANSACTION_CATEGORY_EMPLOYMENT_TDS = 347;
	public static final long DVID_TRANSACTION_CATEGORY_INTERNSHIP = 348;
	public static final long DVID_TRANSACTION_CATEGORY_PROPERTY_RENT = 219;
	public static final long DVID_TRANSACTION_CATEGORY_TAX_RENTAL_INCOME = 282;
	public static final long DVID_TRANSACTION_CATEGORY_TAX_PROPERTY = 281;
	public static final long DVID_TRANSACTION_CATEGORY_TAX_WATER = 283;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY = 209;
	public static final long DVID_TRANSACTION_CATEGORY_MUTUAL_FUND = 211;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY_DIVIDEND = 233;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY_DIVIDEND_TDS = 272;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY_EXEMPT_DIVIDEND = 274;
	public static final long DVID_TRANSACTION_CATEGORY_MUTUAL_FUND_DIVIDEND = 234;
	public static final long DVID_TRANSACTION_CATEGORY_MUTUAL_FUND_DIVIDEND_TDS = 214;
	public static final long DVID_TRANSACTION_CATEGORY_SB_INTEREST = 218;
	public static final long DVID_TRANSACTION_CATEGORY_IT_REFUND_INTEREST = 304;
	public static final long DVID_TRANSACTION_CATEGORY_IT_REFUND_INTEREST_TDS = 305;
	public static final long DVID_TRANSACTION_CATEGORY_NPS_TIER_1 = 212;
	public static final long DVID_TRANSACTION_CATEGORY_NPS_TIER_2 = 213;
	public static final long DVID_TRANSACTION_CATEGORY_DONATION = 217;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY_BUYBACK = 307;
	public static final long DVID_TRANSACTION_CATEGORY_EQUITY_BUYBACK_TDS = 308;
	public static final long DVID_TRANSACTION_CATEGORY_GIFT = 275;
	public static final long DVID_TRANSACTION_CATEGORY_ON_BEHALF_GIFT = 312;
	public static final long DVID_TRANSACTION_CATEGORY_INCOME_TAX = 216;
	public static final long DVID_TRANSACTION_CATEGORY_NONE = 303;
	public static final long DVID_TRANSACTION_CATEGORY_IGNORE = 315;
	public static final long DVID_BOOKING_CREDIT = 222;
	public static final long DVID_BOOKING_DEBIT = 223;
	public static final long DVID_EMPTY_SELECT = -1L;
	public static final long DVID_TAX_GROUP_PO_BANK_DEPOSIT_INTEREST = 199;
	public static final long DVID_ISIN_ACTION_MATCH_REASON_DOUBLE_ENTRY = 342;
	public static final long DVID_ISIN_ACTION_MATCH_REASON_FIFO = 343;
	public static final long DVID_SECURITY_TYPE_EQUITY_SHARE = 344;
	public static final long DVID_SECURITY_TYPE_MUTUAL_FUND = 345;
	public static final long DVID_SECURITY_TYPE_DEBT_INSTRUMENT = 346;

	public static final long ACTION_ID_GIFT_OR_TRANSFER = 5;
	
	public static final int FLAG_POSITION_ACCOUNT_TYPE = 0;
	public static final int FLAG_POSITION_SAVINGS_ACCOUNT_BRANCH_DVID = 1;
	public static final int FLAG_POSITION_ACCOUNT_ID = 2;
	public static final int FLAG_POSITION_ACCOUNT_INVESTOR_DVID = 3;
	public static final int FLAG_POSITION_ACCOUNT_STATUS = 4;
	public static final int FLAG_POSITION_ACCOUNT_PARTY_DVID = 1;
	
	public static final int FLAG_POSITION_BRANCH_BANK_DVID = 0;
	public static final int FLAG_POSITION_BRANCH_IFSC = 1;
	public static final int FLAG_POSITION_PARTY_ROLES = 0;
	public static final int FLAG_POSITION_REAL_INVESTORS = 0;
	public static final int FLAG_POSITION_DV_CATEGORY = 0;
	public static final int FLAG_POSITION_INCOME_OR_EXPENSE = 1;

	public static final String ACCOUNT_TYPE_SAVINGS = "SB";
	public static final String ACCOUNT_TYPE_FUNDS = "Funds";
	public static final String ACCOUNT_TYPE_DEMAT = "Demat";
	public static final String ACCOUNT_TYPE_PPF = "PPF";

	public static final String END_ACCOUNT_REFERENCE_HEALTH_INSURANCE = "Health Insurance";
	
	public static final char ACCOUNT_STATUS_OPEN = 'O';
	public static final char ACCOUNT_STATUS_CLOSED = 'C';
	
	public static final byte ASSESSMENT_YEAR_START_MONTH = 4;
	public static final String CSV_DATE_FORMAT = "M/d/yyyy";

	public static final String DV_FLAGS_LEVEL1_SEPARATOR = ";";
	public static final String DV_FLAGS_LEVEL2_SEPARATOR = "-";
	
	public static final Character DYNAMIC_REALISATION_PERIODICITY_YEAR = 'Y';
	
	public static final long NON_SATC_ID = -1;

	public static final Object[][] TAX_PERCENTAGE_ARRAY = {
			{"Jun-15", 0.15},
			{"Sep-15", 0.3},
			{"Dec-15", 0.3},
			{"Mar-15", 0.25},
	};
	
	public static final List<Long> DVID_TAX_GROUP_EXEMPTED_LIST = Arrays.asList(196L, 198L);
	public static final List<Long> DVID_TAX_GROUP_OTHER_LIST = Arrays.asList(197L, 200L);
	
	// For performance, hard-coded. Is it worth reading from DB
	public static final Map<Long, Long> INVESTOR_TO_PRIMARY_MAP = Map.ofEntries(
		    Map.entry(1L, 1L),
		    Map.entry(2L, 2L),
		    Map.entry(3L, 3L),
		    Map.entry(4L, 4L),
		    Map.entry(107L, 1L),
		    Map.entry(175L, 2L)
	);
	public static final Map<Long, long[]> PRIMARY_TO_INVESTOR_LIST_MAP = Map.ofEntries(
		    Map.entry(1L, new long[] {1L, 107L}),
		    Map.entry(2L, new long[] {2L, 175L}),
		    Map.entry(3L, new long[] {3L}),
		    Map.entry(4L, new long[] {4L})
	);

	public static final Map<String, Long> DESC_TO_ID_MAP = Map.ofEntries(	// TODO: Should be read from DB
		    Map.entry(CATEGORY_TRANSACTION_CODE + ":CLR", 224L),
		    Map.entry(CATEGORY_TRANSACTION_CODE + ":CSH", 225L),
		    Map.entry(CATEGORY_TRANSACTION_CODE + ":TRF", 226L),
		    Map.entry(CATEGORY_COST_CENTER + ":NSE-EQ - Z", 236L),
		    Map.entry(CATEGORY_VOUCHER_TYPE + ":Bank Payments", 227L),
		    Map.entry(CATEGORY_VOUCHER_TYPE + ":Bank Receipts", 228L),
		    Map.entry(CATEGORY_VOUCHER_TYPE + ":Book Voucher", 229L),
		    Map.entry(CATEGORY_VOUCHER_TYPE + ":Journal Entry", 230L)
	);

	public static final Map<String, FieldSpecVO> SEARCH_FIELD_SPEC_MAP = new HashMap<String, FieldSpecVO>() {
		private static final long serialVersionUID = 1L;

		{
			put("I.id", new FieldSpecVO("Investment Id", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("closure_date", new FieldSpecVO("Closure Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("investment_id_with_provider", new FieldSpecVO("Investment Id With Provider", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("investor_id_with_provider", new FieldSpecVO("Investor Id With Provider", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("is_closed", new FieldSpecVO("Is Closed?", FieldSpecVO.DataType.BOOLEAN, false, false, false, null));
			put("investment_start_date", new FieldSpecVO("Investment Start Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("investment_end_date", new FieldSpecVO("Investment End Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("product_id_of_provider", new FieldSpecVO("Provider's Product Id", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("product_name", new FieldSpecVO("Product Name", FieldSpecVO.DataType.OTHERS, false, true, false, null));
			put("rate_of_interest", new FieldSpecVO("Rate of Interest%", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("units", new FieldSpecVO("No. of Units", FieldSpecVO.DataType.OTHERS, true, false, false, null));
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
			put("IT.tax_group_fk",new FieldSpecVO("Txn Tax Group", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_TAXABILITY));
			put("IT.assessment_year", new FieldSpecVO("Txn Assessment Year", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			
			put("R.id", new FieldSpecVO("Realisation Id", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("R.amount", new FieldSpecVO("Realisation Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("R.realisation_date", new FieldSpecVO("Realisation Date", FieldSpecVO.DataType.DATE, true, false, false, null));
			put("R.realisation_type_fk", new FieldSpecVO("Realisation Type", FieldSpecVO.DataType.OTHERS, false, false, true, Constants.CATEGORY_REALISATION_TYPE));
			put("R.interest_amount", new FieldSpecVO("Realisation Interest Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("R.returned_principal_amount", new FieldSpecVO("Realisation Returned Principal Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("R.tds_amount", new FieldSpecVO("Realisation TDS Amount", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			put("R.savings_account_transaction_fk", new FieldSpecVO("SAT Id", FieldSpecVO.DataType.OTHERS, true, false, false, null));
			// label, dataType, isSequencable, isFreeText, isDvSelect, dvCategory
		}
	};

	public static Map<Long, DomainValue> domainValueCache;
	public static Map<String, List<Long>> categoryDvIdCache;
}
