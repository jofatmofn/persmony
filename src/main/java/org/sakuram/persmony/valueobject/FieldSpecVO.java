package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class FieldSpecVO {
	public enum DataType {
		BOOLEAN,
		DATE,
		OTHERS
	}
	public enum SeqOperator {
		EQ,
		NE,
		LT,
		LE,
		GT,
		GE,
		BETWEEN
	}
	public enum TxtOperator {
		EQ,
		NE,
		STARTS,
		ENDS,
		CONTAINS
	}
	String label;
	DataType dataType;
	Boolean isSequencable;
	Boolean isFreeText;
	Boolean isDvSelect;
	String dvCategory;
}
