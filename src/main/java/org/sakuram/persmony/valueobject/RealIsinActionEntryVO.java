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
public class RealIsinActionEntryVO {
	IsinActionEntrySpecVO isinActionEntrySpecVO;
	LocalDate settlementDate;
	LocalDate holdingChangeDate;
	String isin;
	Double quantity;
	IdValueVO dematAccount;
	Double pricePerUnit;
	
	public boolean isEmpty() {
		return (settlementDate == null && holdingChangeDate == null && isin == null && quantity == null && dematAccount == null && pricePerUnit == null);
	}
	
	public void setEmpty() {
		settlementDate = null;
		holdingChangeDate = null;
		isin = null;
		quantity = null;
		dematAccount = null;
		pricePerUnit = null;
	}
}
