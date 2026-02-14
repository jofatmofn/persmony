package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class LotMatchVO {
	long fromLotId;
	long toLotId;
	double quantity;
}
