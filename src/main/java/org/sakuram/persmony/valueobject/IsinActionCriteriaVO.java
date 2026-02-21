package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class IsinActionCriteriaVO {
	Long fromId;
	Long toId;
	LocalDate fromDate;
	LocalDate toDate;
	String isin;
	Long dematAccountDvId;
	Long actionTypeDvId;
	Long quantityBookingDvId;
	Boolean isInternal;

}
