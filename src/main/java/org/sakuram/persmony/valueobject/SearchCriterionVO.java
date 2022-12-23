package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriterionVO {
	String fieldName;
	String operator;
	String valuesCSV;
}
