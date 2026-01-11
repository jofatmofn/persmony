package org.sakuram.persmony.valueobject;

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
	// Existing Action
	Long actionId;
	// New Action
	ActionVO actionVO;
	
	IdValueVO dematAccount;
	List<RealIsinActionEntryVO> realIAEVOList;

	List<LotVO> fifoLotVOList;
	List<TradeVO> tradeVOList;
	List<AccountingIsinActionEntryVO> accountingIAEVOList;
	
}
