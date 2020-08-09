package org.sakuram.persmony.valueobject;

import java.sql.Date;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class Report01VO {
	
	/* Investment Details */
	
	@CsvBindByName(column="Sl. No.")
	@CsvBindByPosition(position=0)
	Long investmentId;
	
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
	Boolean isClosed;

	/* Receipt Details */
	
	@CsvBindByName(column="Receipt Date")
	@CsvBindByPosition(position=6)
	Date receiptDate;

	@CsvBindByName(column="Receipt Amount")
	@CsvBindByPosition(position=7)
	Float receiptAmout;

	@CsvBindByName(column="Receipt Status")
	@CsvBindByPosition(position=8)
	String receiptStatus;

}
