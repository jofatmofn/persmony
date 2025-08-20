package org.sakuram.persmony.view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
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
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Security History"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "Create ISIN Action"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(3, "Match ISIN Actions"));
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
	            case 3:
	            	handleMatchIsinActions(formLayout);
	            	break;
	            case 4:
	            	debtEquityMutualService.determineBalancesMultiple("INE081A01012", new java.sql.Date(System.currentTimeMillis()), null);
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
		HorizontalLayout hLayout;
		Button historyButton, balancesButton;
		Grid<IsinActionVO> isinActionsGrid;
		SecuritySearchComponent securitySearchComponent;
		
		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		formLayout.addFormItem(securitySearchComponent.getLayout(), "ISIN");
		
		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		historyButton = new Button("History");
		hLayout.add(historyButton);
		historyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		historyButton.setDisableOnClick(true);
		
		balancesButton = new Button("Balances");
		hLayout.add(balancesButton);
		balancesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		balancesButton.setDisableOnClick(true);
		
		isinActionsGrid = new Grid<>(IsinActionVO.class);
		formLayout.add(isinActionsGrid);
		
		historyButton.addClickListener(event -> {
			try {
		        Dialog dialog;
				Select<IdValueVO> dematAccountDvSelect;
				Button proceedButton;
		        
				if (securitySearchComponent.getIsinTextField() == null || securitySearchComponent.getIsinTextField().isEmpty()) {
					return;
				}
		        dialog = new Dialog();
		        dialog.setModal(true); // Non-modal popover effect
		        dialog.setDraggable(true);
		        dialog.setCloseOnOutsideClick(true);
				dialog.setHeaderTitle("Demat Account");
		        
		        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), "Demat Account", true, false);

		        proceedButton = new Button("Proceed", e -> {
					List<IsinActionVO> isinActionVOList = null;
					
		            dialog.close();
					isinActionVOList = debtEquityMutualService.fetchIsinActions(securitySearchComponent.getIsinTextField().getValue(), new java.sql.Date(new java.util.Date().getTime()), dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(), true);
					Notification.show("No. of ISIN Actions fetched: " + isinActionVOList.size())
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					isinActionsGrid.setColumns("settlementDate", "isin", "securityName", "isinActionId", "tradeId", "actionType", "bookingType", "dematAccount", "transactionQuantity");
					for (Column<IsinActionVO> column : isinActionsGrid.getColumns()) {
						column.setResizable(true);
					}
					isinActionsGrid.setItems(isinActionVOList);
		        });

		        dialog.add(new VerticalLayout(dematAccountDvSelect, proceedButton));
		        dialog.open();

			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			} finally {
				historyButton.setEnabled(true);
			}
		});
		balancesButton.addClickListener(event -> {
			try {
				List<IsinActionVO> isinActionVOList = null;
				
				if (securitySearchComponent.getIsinTextField() == null || securitySearchComponent.getIsinTextField().isEmpty()) {
					return;
				}
				isinActionVOList = debtEquityMutualService.determineBalancesMultiple(securitySearchComponent.getIsinTextField().getValue(), new java.sql.Date(new java.util.Date().getTime()), null);
				Notification.show("No. of ISIN Actions with Balances: " + isinActionVOList.size())
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				isinActionsGrid.setColumns("settlementDate", "isin", "securityName", "isinActionId", "tradeId", "actionType", "bookingType", "dematAccount", "balance", "ppuBalance");
				for (Column<IsinActionVO> column : isinActionsGrid.getColumns()) {
					column.setResizable(true);
				}
				isinActionsGrid.setItems(isinActionVOList);
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			} finally {
				balancesButton.setEnabled(true);
			}
		});
		
	}
	
	private void handleMatchIsinActions(FormLayout formLayout) {
		
	}
}
