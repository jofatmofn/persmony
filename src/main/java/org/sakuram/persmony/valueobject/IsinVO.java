package org.sakuram.persmony.valueobject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class IsinVO {
	String isin;
	String companyName;
	String securityName;
	String securityType;
	
	public IsinVO(Object[] columns) {
		int colPos = 0;
		this.isin = (String) columns[colPos++];
		this.companyName = (String) columns[colPos++];
		this.securityName = (String) columns[colPos++];
		this.securityType = (String) columns[colPos++];
	}
}
