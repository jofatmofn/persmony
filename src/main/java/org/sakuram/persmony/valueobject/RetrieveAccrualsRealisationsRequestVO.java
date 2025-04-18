package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class RetrieveAccrualsRealisationsRequestVO {
	int fyStartYear;
	Long investorDvId;
	Long productProviderDvId;
	Boolean inForm26as;
	Boolean inAis;
	Boolean withInterest;
	Boolean withTds;
}
