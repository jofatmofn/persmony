package org.sakuram.persmony.valueobject;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class Report01VO {
	@CsvBindByName(column="Sl. No.")
	@CsvBindByPosition(position=0)
	long investmentId;
	
	@CsvBindByName(column="Investor")
	@CsvBindByPosition(position=1)
	String investor;
	
	@CsvBindByName(column="Product Provider")
	@CsvBindByPosition(position=2)
	String productProvider;
	
	@CsvBindByName(column="Product Name")
	@CsvBindByPosition(position=3)
	String productName;
	
	@CsvBindByName(column="Account No.")
	@CsvBindByPosition(position=4)
	String investmentIdWithProvider;
	
	@CsvBindByName(column="Is Closed?")
	@CsvBindByPosition(position=5)
	boolean isClosed;

}
