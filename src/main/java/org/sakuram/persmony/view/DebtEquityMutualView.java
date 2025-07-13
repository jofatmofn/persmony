package org.sakuram.persmony.view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "Test WIP"));
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
	            case 2:
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
		GridContextMenu<IsinVO> isinsGridContextMenu;
		Grid<IsinActionVO> isinActionsGrid;
		
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
		formLayout.add(new Label(" "));
		
		isinActionsGrid = new Grid<>(IsinActionVO.class);
		isinActionsGrid.setColumns("settlementDate", "isin", "securityName", "isinActionId", "actionType", "quantity", "bookingType", "dematAccount");
		for (Column<IsinActionVO> column : isinActionsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(isinActionsGrid);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			IsinCriteriaVO isinCriteriaVO;
			List<IsinVO> isinVOList = null;
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
					isinVOList = debtEquityMutualService.searchSecurities(isinCriteriaVO);
					notification = Notification.show("No. of ISINs fetched: " + isinVOList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					isinsGrid.setItems(isinVOList);
					isinActionsGrid.setItems(new ArrayList<IsinActionVO>());
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});
		
		isinsGridContextMenu = isinsGrid.addContextMenu();
		isinsGridContextMenu.addItem("History", event -> {
			Optional<IsinVO> isinVO;
			
			isinVO = event.getItem();
			if (isinVO.isPresent()) {
				try {
				        Dialog dialog;
						Select<IdValueVO> dematAccountDvSelect;
						Button proceedButton;
				        
				        dialog = new Dialog();
				        dialog.setModal(true); // Non-modal popover effect
				        dialog.setDraggable(true);
				        dialog.setCloseOnOutsideClick(true);
						dialog.setHeaderTitle("Demat Account");
				        
				        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), "Demat Account", true, false);

				        proceedButton = new Button("Proceed", e -> {
							List<IsinActionVO> isinActionVOList = null;
							Notification notification;
							
				            dialog.close();
							isinActionVOList = debtEquityMutualService.fetchIsinActions(isinVO.get().getIsin(), new java.sql.Date(new java.util.Date().getTime()), dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId());
							notification = Notification.show("No. of ISIN Actions fetched: " + isinActionVOList.size());
							notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
							isinActionsGrid.setItems(isinActionVOList);
				        });

				        dialog.add(new VerticalLayout(dematAccountDvSelect, proceedButton));
				        dialog.open();

				} catch (Exception e) {
					ViewFuncs.showError("System Error!!! Contact Support.");
					e.printStackTrace();
					return;
				}
			}
		});
		
	}
}
