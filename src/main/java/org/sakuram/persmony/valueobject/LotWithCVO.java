package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LotWithCVO {
	LotVO lotVO;
	TradeVO tradeVO;
	List<LotMatchVO> receivedFromLotMatchVOList;
	List<LotMatchVO> sentToLotMatchVOList;
}
