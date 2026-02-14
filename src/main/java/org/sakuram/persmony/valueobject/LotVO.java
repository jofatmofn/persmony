package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LotVO {
	Long isinActionPartId;
	Double transactionQuantity;
	Double balance;
	LocalDate holdingChangeDate;
	Double pricePerUnit;

	public static String[] gridColumns() {
		return new String[] {"isinActionPartId", "holdingChangeDate", "pricePerUnit", "transactionQuantity"};
	}
	
}
