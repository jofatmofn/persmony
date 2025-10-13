package org.sakuram.persmony.view;

import java.sql.Date;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionSpecVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.sakuram.persmony.valueobject.TradeVO;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(4, "Test WIP"));
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
	            	handleCreateIsinActions(formLayout);
	            	break;
	            case 3:
	            	handleMatchIsinActions(formLayout);
	            	break;
	            case 4:
	            	// debtEquityMutualService.determineBalancesMultiple("INE081A01012", new java.sql.Date(System.currentTimeMillis()), null);
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
					isinActionVOList = debtEquityMutualService.fetchIsinActions(securitySearchComponent.getIsinTextField().getValue(), new java.sql.Date(new java.util.Date().getTime()), dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(), true, true);
					Notification.show("No. of ISIN Actions fetched: " + isinActionVOList.size())
						.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					isinActionsGrid.setColumns("internal", "settlementDate", "isin", "securityName", "isinActionId", "tradeId", "actionType", "bookingType", "dematAccount", "transactionQuantity");
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
				isinActionVOList = debtEquityMutualService.determineBalancesMultiple(securitySearchComponent.getIsinTextField().getValue(), new java.sql.Date(new java.util.Date().getTime()), null, true);
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
	
	private void handleCreateIsinActions(FormLayout parentFormLayout) {
		SecuritySearchComponent securitySearchComponent;
		Select<IdValueVO> dematAccountDvSelect, actionDvSelect;
		DatePicker recordDateDatePicker;
		List<IsinActionSpecVO> isinActionSpecVOList;
		List<IsinActionVO> isinActionVOList;
		
        actionDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_SECURITY_ACTION, false, true), null, false, false);
        parentFormLayout.addFormItem(actionDvSelect, "Action");
        
		FormLayout childFormLayout;
		childFormLayout = new FormLayout();
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		isinActionSpecVOList = new ArrayList<IsinActionSpecVO>();
		isinActionVOList = new ArrayList<IsinActionVO>();

		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		parentFormLayout.addFormItem(securitySearchComponent.getLayout(), "Entitled ISIN");
		
        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, false, false);
        parentFormLayout.addFormItem(dematAccountDvSelect, "Demat Account");
        
        recordDateDatePicker = new DatePicker();
        recordDateDatePicker.setValue(LocalDate.now());
		parentFormLayout.addFormItem(recordDateDatePicker, "Record Date");
		
		parentFormLayout.add(childFormLayout);
		
        actionDvSelect.addValueChangeListener(event -> {
        	childFormLayout.remove(childFormLayout.getChildren().collect(Collectors.toList()));
    		isinActionSpecVOList.clear();
        	if (Constants.ISIN_ACTION_SPEC_MAP.containsKey(actionDvSelect.getValue().getId())) {
        		isinActionSpecVOList.addAll(Constants.ISIN_ACTION_SPEC_MAP.get(actionDvSelect.getValue().getId()));
				if (securitySearchComponent.getIsinTextField() != null && !securitySearchComponent.getIsinTextField().isEmpty() &&
						dematAccountDvSelect.getValue() != null &&
						recordDateDatePicker.getValue() != null) {
	        		handleCreateIsinActions2(childFormLayout, securitySearchComponent.getIsinTextField().getValue(), dematAccountDvSelect.getValue().getValue(), isinActionSpecVOList, isinActionVOList);
				}
        	}
        });
        
		ValueChangeListener<ValueChangeEvent<?>> fetchFifoMappingLogic = e -> {
			isinActionVOList.clear();
			if (securitySearchComponent.getIsinTextField() != null && !securitySearchComponent.getIsinTextField().isEmpty() &&
					dematAccountDvSelect.getValue() != null &&
					recordDateDatePicker.getValue() != null) {
				if (isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getFifoMappingType() == IsinActionSpecVO.IAFifoMappingType.USER_CHOICE) ||
						isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getLotCreationType() == IsinActionSpecVO.IALotCreationType.PROPAGATION) ||
						isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getQuantityType() == IsinActionSpecVO.IAQuantityType.FULL_BALANCE)) {
					isinActionVOList.addAll(debtEquityMutualService.determineBalancesMultiple(securitySearchComponent.getIsinTextField().getValue(), Date.valueOf(recordDateDatePicker.getValue()), dematAccountDvSelect.getValue().getId(), false));
				}
	    		handleCreateIsinActions2(childFormLayout, securitySearchComponent.getIsinTextField().getValue(), dematAccountDvSelect.getValue().getValue(), isinActionSpecVOList, isinActionVOList);
			}
		};
		securitySearchComponent.getIsinTextField().addValueChangeListener(fetchFifoMappingLogic);
		dematAccountDvSelect.addValueChangeListener(fetchFifoMappingLogic);
		recordDateDatePicker.addValueChangeListener(fetchFifoMappingLogic);
		
	}
		
	private void handleCreateIsinActions2(FormLayout childFormLayout, String isin, String dematAccountName, List<IsinActionSpecVO> isinActionSpecVOList, List<IsinActionVO> balanceIAVOList) {
		HorizontalLayout buttonsHorizontalLayout;
		List<TradeVO> tradeVOList;
		AtomicReference<Double> quantityAR;
		
		childFormLayout.remove(childFormLayout.getChildren().collect(Collectors.toList()));
		tradeVOList = new ArrayList<TradeVO>();
		quantityAR = new AtomicReference<Double>();
		quantityAR.set(null);

		for (IsinActionSpecVO isinActionSpecVO : isinActionSpecVOList) {
			NumberField quantityNumberField;
			
			childFormLayout.add(ViewFuncs.newHorizontalLine());
			childFormLayout.addFormItem(new Label(isinActionSpecVO.getActionName()), "Real Entry");
			childFormLayout.addFormItem(new Label((isinActionSpecVO.getBookingTypeDvId() == Constants.DVID_BOOKING_DEBIT ? "Debit" : "Credit")), "Booking");
			quantityNumberField = new NumberField();
			childFormLayout.addFormItem(quantityNumberField, "Quantity");
			quantityNumberField.setEnabled(false);
			quantityNumberField.addValueChangeListener(event -> {
				quantityAR.set(quantityNumberField.getValue());
			});
			switch(isinActionSpecVO.getQuantityType()) {
			case INPUT:
			case FACTOR_OF_BALANCE:
				quantityNumberField.setEnabled(true);
				break;
			case FULL_BALANCE:
				quantityNumberField.setValue(
						balanceIAVOList
							.stream()
							.mapToDouble(balanceIAVO -> balanceIAVO.getBalance())
							.sum()
				);
				break;
			case PREVIOUS_INPUT:
				quantityNumberField.setValue(quantityAR.get());
				break;
			case ZERO:
				quantityNumberField.setValue(0D);
				break;
			}
			
			switch(isinActionSpecVO.getLotCreationType()) {
			case TRADE:
				break;
			default:
				break;
			}
		}
		
		childFormLayout.add(ViewFuncs.newHorizontalLine());
		buttonsHorizontalLayout = new HorizontalLayout();
		childFormLayout.add(buttonsHorizontalLayout);
		
		if (isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getFifoMappingType() == IsinActionSpecVO.IAFifoMappingType.USER_CHOICE) ||
				isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getLotCreationType() == IsinActionSpecVO.IALotCreationType.PROPAGATION)) {
			Button fifoMapButton;
			
	        fifoMapButton = new Button("FIFO Map");
	        buttonsHorizontalLayout.add(fifoMapButton);
			fifoMapButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			fifoMapButton.setDisableOnClick(true);
			
			fifoMapButton.addClickListener(event -> {
				try {
					if (quantityAR.get() == null) {
						ViewFuncs.showError("ACTION level quantity is required to map FIFO details");
						return;
					}
					acceptFifoMap(new String[] {isin, dematAccountName}, balanceIAVOList, quantityAR.get());
				} catch (Exception e) {
					ViewFuncs.showError("System Error!!! Contact Support.");
					e.printStackTrace();
					return;
				} finally {
					fifoMapButton.setEnabled(true);
				}
			});
		}
		
		if (isinActionSpecVOList.stream().anyMatch(isinActionSpecVO -> isinActionSpecVO.getLotCreationType() == IsinActionSpecVO.IALotCreationType.TRADE)) {
			Button tradeButton;
	        tradeButton = new Button("Trades");
	        buttonsHorizontalLayout.add(tradeButton);
			tradeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			tradeButton.setDisableOnClick(true);
			
			tradeButton.addClickListener(event -> {
				try {
					if (quantityAR.get() == null) {
						ViewFuncs.showError("ACTION level quantity is required to key-in TRADE details");
						return;
					}
					acceptTrades(tradeVOList, quantityAR.get());
				} catch (Exception e) {
					ViewFuncs.showError("System Error!!! Contact Support.");
					e.printStackTrace();
					return;
				} finally {
					tradeButton.setEnabled(true);
				}
			});
		}
		
		Button accountingEntriesButton;
        accountingEntriesButton = new Button("Accounting Entries");
        buttonsHorizontalLayout.add(accountingEntriesButton);
		accountingEntriesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		accountingEntriesButton.setDisableOnClick(true);
		
		accountingEntriesButton.addClickListener(event -> {
			try {
				// acceptFifoMap(new String[] {isin, dematAccountName}, balanceIAVOList);
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			} finally {
				accountingEntriesButton.setEnabled(true);
			}
		});
		
		Button saveButton;
        saveButton = new Button("Save");
        buttonsHorizontalLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		
		saveButton.addClickListener(event -> {
			try {
				// acceptFifoMap(new String[] {isin, dematAccountName}, balanceIAVOList);
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			} finally {
				saveButton.setEnabled(true);
			}
		});
		
	}
	
	private void handleMatchIsinActions(FormLayout formLayout) {
		
	}
	
	private void acceptFifoMap(String[] labels, List<IsinActionVO> balanceIAVOList, double iAQuantity) {
		Dialog dialog;
		VerticalLayout verticalLayout;
		HorizontalLayout hLayout;
		Button closeButton, saveButton, cancelButton;
		Grid<IsinActionVO> quantityPriceGrid;
		Binder<IsinActionVO> quantityPriceBinder;
		Editor<IsinActionVO> quantityPriceEditor;
		List<IsinActionVO> beforeChangeIsinActionVOList;
		NumberField quantityNumberField;

		beforeChangeIsinActionVOList = new ArrayList<IsinActionVO>(balanceIAVOList.size());
		beforeChangeIsinActionVOList.addAll(balanceIAVOList);

		for (IsinActionVO isinActionVO: balanceIAVOList) {
			System.out.println(isinActionVO.getIsinActionId() + "::" + isinActionVO.getTradeId() + "::" + isinActionVO.getActionType() + "::" + isinActionVO.getSettlementDate().toString() + "::" + isinActionVO.getTransactionQuantity() + "::" + isinActionVO.getBalance());
		}
		dialog = new Dialog();
		dialog.setHeaderTitle("FIFO Map");
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		verticalLayout = new VerticalLayout();
		verticalLayout.getStyle().set("width", "75rem");
		dialog.add(verticalLayout);
		
		;
		verticalLayout.add(new Label("ISIN: " + labels[0]));
		verticalLayout.add(new Label("Demat A/c: " + labels[1]));
		
		quantityPriceGrid = new Grid<>(IsinActionVO.class, false);
		quantityPriceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		quantityPriceGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		quantityPriceGrid.setItems(balanceIAVOList);
		quantityPriceGrid.setColumns("settlementDate", "actionType", "transactionQuantity", "balance");
		quantityPriceGrid.getColumnByKey("balance").setHeader("Mapped Quantity");
		for (Column<IsinActionVO> column : quantityPriceGrid.getColumns()) {
			column.setResizable(true);
		}
		verticalLayout.add(quantityPriceGrid);

		quantityPriceBinder = new Binder<>(IsinActionVO.class);
		quantityPriceEditor = quantityPriceGrid.getEditor();
		quantityPriceEditor.setBinder(quantityPriceBinder);
		
		quantityNumberField = new NumberField();
		quantityNumberField.getElement().addEventListener("keydown", e -> quantityPriceEditor.cancel())
        	.setFilter("event.key === 'Escape' || event.key === 'Esc'");
		quantityPriceBinder.forField(quantityNumberField)
			.bind(IsinActionVO::getBalance, IsinActionVO::setBalance);
		quantityPriceGrid.getColumnByKey("balance").setEditorComponent(quantityNumberField);
		
		hLayout = new HorizontalLayout();
		verticalLayout.add(hLayout);
		
		saveButton = new Button("Save");
		hLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		saveButton.addClickListener(event -> {
			try {
				// Validations
				double totalMappedQuantity;
				totalMappedQuantity = 0;
				for (IsinActionVO balanceIAVO : balanceIAVOList) {
					totalMappedQuantity += (balanceIAVO.getBalance() == null ? 0 : balanceIAVO.getBalance());
				}
				if (Math.abs(iAQuantity - totalMappedQuantity) > Constants.EPSILON) {
					ViewFuncs.showError("Total of Mapped Quantities does not match Action Quantity");
					return;
				}
			} finally {
				saveButton.setEnabled(true);
			}
			
			dialog.close();
		});
		cancelButton = new Button("Cancel");
		hLayout.add(cancelButton);
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton.addClickListener(event -> {
			balanceIAVOList.clear();
			balanceIAVOList.addAll(beforeChangeIsinActionVOList);
			dialog.close();
		});

		quantityPriceGrid.addItemDoubleClickListener(e -> {
		    quantityPriceEditor.editItem(e.getItem());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable<?>) {
		        ((Focusable<?>) editorComponent).focus();
		    }
		});
		
		dialog.open();
	}
	
	private void acceptTrades(List<TradeVO> tradeVOList, double iAQuantity) {
		Dialog dialog;
		FormLayout formLayout;
		HorizontalLayout hLayout;
		Button addButton, toGridButton, closeButton, cancelButton, returnButton;
		Grid<TradeVO> tradeGrid;
		GridListDataView<TradeVO> tradeGridLDV;
		TextField orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField;
		NumberField tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField;
		DatePicker orderDateDatePicker, tradeDateDatePicker;
		List<TradeVO> beforeChangeTradeVOList;
		AtomicReference<Long> editRowTradeIdAR, deleteRowTradeIdAR;

		editRowTradeIdAR = new AtomicReference<Long>();
		editRowTradeIdAR.set(null);
		deleteRowTradeIdAR = new AtomicReference<Long>();
		deleteRowTradeIdAR.set(null);
		beforeChangeTradeVOList = new ArrayList<TradeVO>(tradeVOList.size());
		beforeChangeTradeVOList.addAll(tradeVOList);

		dialog = new Dialog();
		dialog.setHeaderTitle("Trades");
		dialog.setCloseOnOutsideClick(false);
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
					tradeVOList.clear();
					tradeVOList.addAll(beforeChangeTradeVOList);
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		// verticalLayout = new VerticalLayout();
		formLayout = new FormLayout();
		formLayout.getStyle().set("width", "75rem");
		formLayout.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1)
		);
		dialog.add(formLayout);
		
		tradeGrid = new Grid<>(TradeVO.class);
		tradeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		tradeGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		tradeGridLDV = tradeGrid.setItems(tradeVOList);		
		formLayout.add(tradeGrid);
		formLayout.add(ViewFuncs.newLine());
		
		tradeIdNumberField = new NumberField();
		tradeIdNumberField.setEnabled(false);
		formLayout.addFormItem(tradeIdNumberField, "Trade Id");
		formLayout.add(ViewFuncs.newLine());
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Order");
		orderNoTextField = new TextField("No");
		orderDateDatePicker = new DatePicker("Date");
		orderTimeTextField = new TextField("Time");
		hLayout.add(orderNoTextField, orderDateDatePicker, orderTimeTextField);
		formLayout.add(ViewFuncs.newLine());

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Trade");
		tradeNoTextField = new TextField("No");
		tradeDateDatePicker = new DatePicker("Date");
		tradeTimeTextField = new TextField("Time");
		hLayout.add(tradeNoTextField, tradeDateDatePicker, tradeTimeTextField);
		formLayout.add(ViewFuncs.newLine());

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Measure");
		quantityNumberField = new NumberField("Quantity");
		pricePerUnitNumberField = new NumberField("Price per unit");
		brokeragePerUnitNumberField = new NumberField("Brokerage per unit");
		hLayout.add(quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField);
		formLayout.add(ViewFuncs.newLine());

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "");
		addButton = new Button("New Row");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of New Row
		addButton.addClickListener(event -> {
			try {
				TradeVO tradeVO;
				tradeVO = new TradeVO();
				tradeVO.setTradeId((long) (tradeVOList.size() == 0 ? -1 : (tradeVOList.get(tradeVOList.size() - 1).getTradeId() - 1))); 
				tradeGridLDV.addItem(tradeVO);
				tradeGrid.select(tradeVO);
				editRowTradeIdAR.set(tradeVO.getTradeId());

				vO2Fields(tradeVO, orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
			} finally {
				addButton.setEnabled(true);
			}
		});

		toGridButton = new Button("Save to Grid");
		toGridButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		toGridButton.setDisableOnClick(true);
		// On click of To Grid
		toGridButton.addClickListener(event -> {
			try {
				TradeVO tradeVO;
				if (editRowTradeIdAR.get() == null) {
					tradeVO = new TradeVO();
					tradeVO.setTradeId((long) (tradeVOList.size() == 0 ? -1 : (tradeVOList.get(tradeVOList.size() - 1).getTradeId() - 1)));
					tradeGridLDV.addItem(tradeVO); // tradeVOList is updated automatically
					tradeGrid.select(tradeVO);
					tradeIdNumberField.setValue(tradeVO.getTradeId().doubleValue());
				} else {
					tradeVO = tradeVOList
							.stream()
							.filter(tradeVObj -> tradeVObj.getTradeId().equals(editRowTradeIdAR.get()))
							.findFirst()
							.get();
				}
				editRowTradeIdAR.set(tradeVO.getTradeId());
				tradeGrid.select(tradeVO);
				tradeVO.setOrderNo(orderNoTextField.isEmpty() ? null : orderNoTextField.getValue());
				tradeVO.setOrderTime(orderTimeTextField.isEmpty() ? null : orderTimeTextField.getValue());
				tradeVO.setTradeNo(tradeNoTextField.isEmpty() ? null : tradeNoTextField.getValue());
				tradeVO.setTradeTime(tradeTimeTextField.isEmpty() ? null : tradeTimeTextField.getValue());
				tradeVO.setQuantity(quantityNumberField.getValue());
				tradeVO.setPricePerUnit(pricePerUnitNumberField.getValue());
				tradeVO.setBrokeragePerUnit(brokeragePerUnitNumberField.getValue());
				tradeVO.setOrderDate(orderDateDatePicker.getValue() == null ? null : Date.valueOf(orderDateDatePicker.getValue()));
				tradeVO.setTradeDate(tradeDateDatePicker.getValue() == null ? null : Date.valueOf(tradeDateDatePicker.getValue()));
				tradeGridLDV.refreshItem(tradeVO);
			} finally {
				toGridButton.setEnabled(true);
			}
		});
		hLayout.add(addButton, toGridButton);
		formLayout.add(ViewFuncs.newLine());
		
		formLayout.add(ViewFuncs.newHorizontalLine());
		formLayout.add(ViewFuncs.newLine());
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "");
		returnButton = new Button("Return");
		returnButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		returnButton.setDisableOnClick(true);
		returnButton.addClickListener(event -> {
			try {
				double totalTradeQuantity;
				
				editRowTradeIdAR.set(null);
				deleteRowTradeIdAR.set(null);
				vO2Fields(new TradeVO(), orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
				
				// Validations
				totalTradeQuantity = 0;
				for (int i = 0; i < tradeVOList.size(); i++) {
					TradeVO tradeVO = tradeVOList.get(i);
					if (tradeVO.getBrokeragePerUnit() == null && tradeVO.getOrderDate() == null &&
							tradeVO.getOrderNo() == null && tradeVO.getOrderTime() == null && tradeVO.getPricePerUnit() == null &&
							tradeVO.getQuantity() == null && tradeVO.getTradeDate() == null && tradeVO.getTradeNo() == null &&
							tradeVO.getTradeTime() == null) {
						tradeGridLDV.removeItem(tradeVO);
						i--;
						continue;
					}
					if (tradeVO.getBrokeragePerUnit() == null || tradeVO.getOrderDate() == null ||
							tradeVO.getOrderNo() == null || tradeVO.getOrderTime() == null || tradeVO.getPricePerUnit() == null ||
							tradeVO.getQuantity() == null || tradeVO.getTradeDate() == null || tradeVO.getTradeNo() == null ||
							tradeVO.getTradeTime() == null) {
						ViewFuncs.showError("Row " + (i+1) + " missing mandatory values");
						return;
					}
					totalTradeQuantity += tradeVO.getQuantity();
				}
				if (Math.abs(iAQuantity - totalTradeQuantity) > Constants.EPSILON) {
					ViewFuncs.showError("Total of Trade Quantities does not match Action Quantity");
					return;
				}
			} finally {
				returnButton.setEnabled(true);
			}
			dialog.close();
		});

		cancelButton = new Button("Cancel");
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton.addClickListener(event -> {
			tradeVOList.clear();
			tradeVOList.addAll(beforeChangeTradeVOList);
			dialog.close();
		});
		hLayout.add(returnButton, cancelButton);
		formLayout.add(ViewFuncs.newLine());

		tradeGrid.setColumns("tradeId", "orderDate", "orderTime", "orderNo", "tradeDate", "tradeTime", "tradeNo", "quantity", "pricePerUnit", "brokeragePerUnit");
		for (Column<TradeVO> column : tradeGrid.getColumns()) {
			column.setResizable(true);
		}

		tradeGrid.addItemClickListener(event -> {
			TradeVO tradeVO;

			tradeVO = event.getItem();
			System.out.println("Clicked Row " + tradeVO.getTradeId());
			if (deleteRowTradeIdAR == null || deleteRowTradeIdAR.get() != tradeVO.getTradeId()) {
				editRowTradeIdAR.set(tradeVO.getTradeId());
				vO2Fields(tradeVO, orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
			} else {
				editRowTradeIdAR.set(null);
			}
			deleteRowTradeIdAR.set(null);
		});

		tradeGrid.addComponentColumn(tradeVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e-> {
				tradeGridLDV.removeItem(tradeVO);
				editRowTradeIdAR.set(null);
				deleteRowTradeIdAR.set(tradeVO.getTradeId());
				
				vO2Fields(new TradeVO(), orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
			});
			return delButton;
		}).setWidth("120px").setFlexGrow(0);

		dialog.open();
	}
	
	private void vO2Fields(TradeVO tradeVO, TextField orderNoTextField, TextField orderTimeTextField, TextField tradeNoTextField, TextField tradeTimeTextField,
			NumberField tradeIdNumberField, NumberField quantityNumberField, NumberField pricePerUnitNumberField, NumberField brokeragePerUnitNumberField,
			DatePicker orderDateDatePicker, DatePicker tradeDateDatePicker) {
		if (tradeVO.getTradeId() == null) {
			tradeIdNumberField.clear();
		} else {
			tradeIdNumberField.setValue(tradeVO.getTradeId().doubleValue());
		}
		if (tradeVO.getOrderNo() == null) {
			orderNoTextField.clear();
		} else {
			orderNoTextField.setValue(tradeVO.getOrderNo());
		}
		if (tradeVO.getOrderTime() == null) {
			orderTimeTextField.clear();
		} else {
			orderTimeTextField.setValue(tradeVO.getOrderTime());
		}
		if (tradeVO.getTradeNo() == null) {
			tradeNoTextField.clear();
		} else {
			tradeNoTextField.setValue(tradeVO.getTradeNo());
		}
		if (tradeVO.getTradeTime() == null) {
			tradeTimeTextField.clear();
		} else {
			tradeTimeTextField.setValue(tradeVO.getTradeTime());
		}
		if (tradeVO.getQuantity() == null) {
			quantityNumberField.clear();
		} else {
			quantityNumberField.setValue(tradeVO.getQuantity());
		}
		if (tradeVO.getPricePerUnit() == null) {
			pricePerUnitNumberField.clear();
		} else {
			pricePerUnitNumberField.setValue(tradeVO.getPricePerUnit());
		}
		if (tradeVO.getBrokeragePerUnit() == null) {
			brokeragePerUnitNumberField.clear();
		} else {
			brokeragePerUnitNumberField.setValue(tradeVO.getBrokeragePerUnit());
		}
		if (tradeVO.getOrderDate() == null) {
			orderDateDatePicker.clear();
		} else {
			orderDateDatePicker.setValue(tradeVO.getOrderDate().toLocalDate());
		}
		if (tradeVO.getTradeDate() == null) {
			tradeDateDatePicker.clear();
		} else {
			tradeDateDatePicker.setValue(tradeVO.getTradeDate().toLocalDate());
		}
	}
}
