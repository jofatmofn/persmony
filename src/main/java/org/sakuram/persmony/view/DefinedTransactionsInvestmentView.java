package org.sakuram.persmony.view;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value="dti", layout=PersMonyLayout.class)
@PageTitle("Defined Transaction Investments")
public class DefinedTransactionsInvestmentView extends Div {

	private static final long serialVersionUID = -363821312812588185L;
	
	DTInvestmentSearchComponent dtInvestmentSearchComponent;
	DTInvestmentTransactionSearchComponent dtInvestmentTransactionSearchComponent;
	DTIOperationComponent dtiOperationComponent;
	
	public DefinedTransactionsInvestmentView(DTInvestmentSearchComponent dtInvestmentSearchComponent, DTInvestmentTransactionSearchComponent dtInvestmentTransactionSearchComponent, DTIOperationComponent dtiOperationComponent) {
		Div content;
		Tabs tabs;
		Map<Tab, Component> tabContent = new HashMap<Tab, Component>(3);
		Component investmentsSearchView, investmentTransactionsSearchView, investmentOperationsView;
		Tab investmentsSearchTab, investmentTransactionsSearchTab, investmentOperationsTab;
		
		this.dtInvestmentSearchComponent = dtInvestmentSearchComponent;
		this.dtInvestmentTransactionSearchComponent = dtInvestmentTransactionSearchComponent;
		this.dtiOperationComponent = dtiOperationComponent;
		
		setSizeFull();
		
		content = new Div();
		
		investmentsSearchTab = new Tab("Investments Search");
		investmentsSearchView = dtInvestmentSearchComponent.showForm();
		tabContent.put(investmentsSearchTab, investmentsSearchView);
		investmentTransactionsSearchTab = new Tab("Investment Transactions Search");
		investmentTransactionsSearchView = dtInvestmentTransactionSearchComponent.showForm();
		tabContent.put(investmentTransactionsSearchTab, investmentTransactionsSearchView);
		investmentOperationsTab = new Tab("Investment Operations");
		investmentOperationsView = dtiOperationComponent.showForm();
		tabContent.put(investmentOperationsTab, investmentOperationsView);
		
		tabs = new Tabs(investmentsSearchTab, investmentTransactionsSearchTab, investmentOperationsTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> {
        	content.removeAll();
            content.add(tabContent.get(e.getSelectedTab()));
        });

        add(tabs, content);

        content.add(investmentsSearchView);
	}
	
}
