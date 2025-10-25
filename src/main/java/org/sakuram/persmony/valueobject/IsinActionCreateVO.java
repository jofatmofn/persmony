package org.sakuram.persmony.valueobject;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IsinActionCreateVO {
	IdValueVO actionType;
	String entitledIsin;
	IdValueVO dematAccount;
	Date recordDate;
	List<RealIsinActionEntryVO> realIAEVOList;

	List<IsinActionVO> fifoIAVOList;
	List<TradeVO> tradeVOList;
	List<AccountingIsinActionEntryVO> accountingIAEVOList;
	
}
