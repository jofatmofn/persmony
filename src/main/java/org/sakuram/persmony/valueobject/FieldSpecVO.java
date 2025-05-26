package org.sakuram.persmony.valueobject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		CONTAINS,
		EMPTY
	}
	String label;
	DataType dataType;
	Boolean isSequencable;
	Boolean isFreeText;
	Boolean isDvSelect;
	String dvCategory;
	
	public static List<String> getTxtOperatorList() {
		return Stream.of(TxtOperator.values()).map(Enum::name).collect(Collectors.toList());
	}
	
	public static List<String> getSeqOperatorList() {
		return Stream.of(SeqOperator.values()).map(Enum::name).collect(Collectors.toList());
	}
	
	public static int SEQ_TEXT_OPERATOR_EMPTY = 6;
}
