package org.sakuram.persmony.view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("dem")
public class DebtEquityMutualView extends Div {

	private static final long serialVersionUID = 7040253088998928399L;

	DebtEquityMutualService debtEquityMutualService;
	MiscService miscService;
	
	public DebtEquityMutualView(DebtEquityMutualService debtEquityMutualService, MiscService miscService) {
		
		Span selectSpan;
		FormLayout formLayout;
		Select<Map.Entry<Integer,String>> functionSelect;
		List<Map.Entry<Integer, String>> menuItemsList;
		
		this.debtEquityMutualService = debtEquityMutualService;
		this.miscService = miscService;

		menuItemsList = new ArrayList<Map.Entry<Integer,String>>() {
			private static final long serialVersionUID = 1L;

			{
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Search Security"));
			}
		};
		selectSpan = new Span();
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		
		functionSelect = new Select<Map.Entry<Integer,String>>();
		functionSelect.setItems(menuItemsList);
		functionSelect.setItemLabelGenerator(operationItem -> {
			return operationItem.getValue();
		});
		functionSelect.setLabel("Function");
		functionSelect.setPlaceholder("Select Function");
		functionSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue().getKey()) {
	            case 1:
	            	handleSearchSecurity(formLayout);
	            	break;
	            }
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });

		selectSpan.add(functionSelect);
		add(selectSpan);
		add(formLayout);
	}
	
	private void handleSearchSecurity(FormLayout formLayout) {
		TextField isinTextField, companyNameTextField, securityNameTextField;
		Select<IdValueVO> isinOperatorSelect, companyNameOperatorSelect, securityNameOperatorSelect;
		Select<IdValueVO> securityTypeDvSelect;
		HorizontalLayout hLayout;
		Button fetchButton;
		Grid<IsinVO> isinsGrid;
		
		isinOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		isinTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "ISIN");
		hLayout.add(isinOperatorSelect, isinTextField);
		isinOperatorSelect.addValueChangeListener(event -> {
			if (isinOperatorSelect.getValue() == null) {
				isinTextField.setValue("");
			}
		});
		
		companyNameOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		companyNameTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Company Name");
		hLayout.add(companyNameOperatorSelect, companyNameTextField);
		companyNameOperatorSelect.addValueChangeListener(event -> {
			if (companyNameOperatorSelect.getValue() == null) {
				companyNameTextField.setValue("");
			}
		});
		
		securityNameOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		securityNameTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Security Name");
		hLayout.add(securityNameOperatorSelect, securityNameTextField);
		securityNameOperatorSelect.addValueChangeListener(event -> {
			if (securityNameOperatorSelect.getValue() == null) {
				securityNameTextField.setValue("");
			}
		});
		
		securityTypeDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_SECURITY_TYPE, null, true, false);
		formLayout.addFormItem(securityTypeDvSelect, "Security Type");
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		isinsGrid = new Grid<>(IsinVO.class);
		isinsGrid.setColumns("isin", "companyName", "securityName", "securityType");
		for (Column<IsinVO> column : isinsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(isinsGrid);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			IsinCriteriaVO isinCriteriaVO;
			List<IsinVO> recordList = null;
			Notification notification;

			try {
				// Validation
				if (!isinTextField.isEmpty() && isinOperatorSelect.getValue() == null) {
					ViewFuncs.showError("ISIN: Non-matching Operator and Value");
					return;
				}
				if (isinTextField.isEmpty() && isinOperatorSelect.getValue() != null) {
					ViewFuncs.showError("Specify Value for ISIN");
					return;
				}
				if (!companyNameTextField.isEmpty() && companyNameOperatorSelect.getValue() == null) {
					ViewFuncs.showError("Company Name: Non-matching Operator and Value");
					return;
				}
				if (companyNameTextField.isEmpty() && companyNameOperatorSelect.getValue() != null) {
					ViewFuncs.showError("Specify Value for Company Name");
					return;
				}
				if (!securityNameTextField.isEmpty() && securityNameOperatorSelect.getValue() == null) {
					ViewFuncs.showError("Security Name: Non-matching Operator and Value");
					return;
				}
				if (securityNameTextField.isEmpty() && securityNameOperatorSelect.getValue() != null) {
					ViewFuncs.showError("Specify Value for Security Name");
					return;
				}
				
				// Back-end Call
				isinCriteriaVO = new IsinCriteriaVO(
						isinTextField.getValue().equals("") ? null : isinTextField.getValue(),
						isinOperatorSelect.getValue() == null ? null : isinOperatorSelect.getValue().getValue(),
						companyNameTextField.getValue().equals("") ? null : companyNameTextField.getValue(),
						companyNameOperatorSelect.getValue() == null ? null : companyNameOperatorSelect.getValue().getValue(),
						securityNameTextField.getValue().equals("") ? null : securityNameTextField.getValue(),
						securityNameOperatorSelect.getValue() == null ? null : securityNameOperatorSelect.getValue().getValue(),
						securityTypeDvSelect.getValue() == null ? null : securityTypeDvSelect.getValue().getId()
						);
				try {
					recordList = debtEquityMutualService.searchSecurities(isinCriteriaVO);
					notification = Notification.show("No. of ISINs fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					isinsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});
	}
}
