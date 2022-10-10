package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DvFlagsBankAccVO implements DvFlagsVO {
	long id;
	String accType;
	Long branchDvId;
	String accId;
	long investorDvId;
	char openOrClosed;
}
