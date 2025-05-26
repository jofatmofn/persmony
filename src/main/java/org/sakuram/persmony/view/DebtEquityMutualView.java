package org.sakuram.persmony.view;

import java.sql.Date;
import java.text.ParseException;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.util.Constants;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("dem")
public class DebtEquityMutualView extends Div {

	private static final long serialVersionUID = 7040253088998928399L;

	DebtEquityMutualService debtEquityMutualService;
	
	public DebtEquityMutualView(DebtEquityMutualService debtEquityMutualService) {
		this.debtEquityMutualService = debtEquityMutualService;
		
    	try {
			debtEquityMutualService.determineBuyCost("INE002A01018", 120D, new Date(Constants.ANSI_DATE_FORMAT.parse("2025-05-02").getTime()), 206);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// debtEquityMutualService.determineOneTimeNpsMatch();

	}
}
