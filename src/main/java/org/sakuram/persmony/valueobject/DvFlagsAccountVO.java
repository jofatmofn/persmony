package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DvFlagsAccountVO implements DvFlagsVO {
	long id;
	String accType;
	/* Bank Savings Account */
	Long branchDvId;
	String accId;
	long investorDvId;
	char openOrClosed;
	/* Fund Account */
	long partyDvId;
}
