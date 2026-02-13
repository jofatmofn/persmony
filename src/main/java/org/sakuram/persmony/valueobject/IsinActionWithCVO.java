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
public class IsinActionWithCVO {
	IsinActionVO isinActionVO;
	List<LotVO> lotVOList;
	ContractVO contractVO;	// Remarks: Strictly speaking the ContractVO should not be part of WithC
}
