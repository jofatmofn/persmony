package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IsinCriteriaVO {
	String isin;
	String isinOperator;
	String companyName;
	String companyNameOperator;
	String securityName;
	String securityNameOperator;
	Long securityTypeDvId;
}
