package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class FieldSpecFEVO {
	public enum UiControl {
		TEXTFIELD,
		NUMBERFIELD,
		DATEPICKER,
		SELECT,
		RADIOBUTTONGROUP,
		CHECKBOX
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
	UiControl uiControl;
	Boolean isSequencable;
	Boolean isFreeText;
	Boolean isDvSelect;
	String dvCategory;
}
