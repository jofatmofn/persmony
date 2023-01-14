package org.sakuram.persmony.valueobject;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleVO {
	private Date dueDate;
	private Double dueAmount;
	private Double returnedPrincipalAmount;
	private Double interestAmount;
	private Double tdsAmount;
	
	// Following methods handle the scenario that Vaadin bind expects java.util.Date while ScheduleVO has java.sql.Date
	public java.util.Date getDueDateUtil() {
		return dueDate == null ? null : new java.util.Date(dueDate.getTime());
	}
	
	public void setDueDateUtil(java.util.Date dt) {
		dueDate = (dt == null ? null : new Date(dt.getTime()));
	}
}
