package org.sakuram.persmony.view;

import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.ReportService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.DomainValueVO;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;

@Route("itran")
public class InvestmentTransactionView extends Div {
	private static final long serialVersionUID = 6529685098267757690L;
	
	ReportService reportService;
	MiscService miscService;

	public InvestmentTransactionView(ReportService reportService, MiscService miscService) {
		FormLayout formLayout;
		Select<String> transactionTypeSelect;
		
		this.reportService = reportService;
		this.miscService = miscService;
		
		formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
        
		transactionTypeSelect = new Select<String>();
		transactionTypeSelect.setItems("SingleRealisationWithBank", "TxnSingleRealisationWithBank", "SingleLastRealisationWithBank", "Invest", "Renewal");
		transactionTypeSelect.setPlaceholder("Select Transaction Type");
		transactionTypeSelect.addValueChangeListener(event -> {
            switch(event.getValue()) {
            case "SingleRealisationWithBank":
            	handleSingleRealisationWithBank(formLayout);
            	break;
            case "TxnSingleRealisationWithBank":
            	handleTxnSingleRealisationWithBank(formLayout);
            	break;
            case "SingleLastRealisationWithBank":
            	handleSingleLastRealisationWithBank(formLayout);
            	break;
            case "Invest":
            	handleInvest(formLayout);
            	break;
            case "Renewal":
            	handleRenewal(formLayout);
            	break;
            }
        });

		formLayout.addFormItem(transactionTypeSelect, "Transaction Type");
		add(formLayout);
	}
	
	private void handleSingleRealisationWithBank(FormLayout formLayout) {
		IntegerField investmentTransactionIdIntegerField;
		Select<DomainValueVO> bankAccountDvSelect;
		List<DomainValueVO> domainValueVOList;
		
		investmentTransactionIdIntegerField = new IntegerField();
		formLayout.addFormItem(investmentTransactionIdIntegerField, "Investment Transaction Id");
		
		bankAccountDvSelect = new Select<DomainValueVO>();
		formLayout.addFormItem(bankAccountDvSelect, "Bank Account");
		domainValueVOList = miscService.fetchDvOfCategory(Constants.CATEGORY_BANK_ACCOUNT);
		bankAccountDvSelect.setItemLabelGenerator(domainValueVO -> {
			return domainValueVO.getValue();
		});
		bankAccountDvSelect.setItems(domainValueVOList);
		bankAccountDvSelect.setPlaceholder("Select Bank Account");
	}
	
	private void handleTxnSingleRealisationWithBank(FormLayout formLayout) {
		
	}
	
	private void handleSingleLastRealisationWithBank(FormLayout formLayout) {
		
	}
	
	private void handleInvest(FormLayout formLayout) {
		
	}
	
	private void handleRenewal(FormLayout formLayout) {
		
	}
}
