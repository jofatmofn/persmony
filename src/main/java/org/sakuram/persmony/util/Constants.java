package org.sakuram.persmony.util;

import java.util.Map;

import org.sakuram.persmony.bean.DomainValue;

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
	public static final String CATEGORY_BANK_ACCOUNT = "BankAcc";
	public static final String CATEGORY_REALISATION_TYPE = "RlsnTyp";
	
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
	
	public static final byte ASSESSMENT_YEAR_START_MONTH = 4;
	public static final String CSV_DATE_FORMAT = "M/d/yyyy";
	
	public static final Character DYNAMIC_REALISATION_PERIODICITY_YEAR = 'Y';
	
	public static Map<Long, DomainValue> domainValueCache;
}
