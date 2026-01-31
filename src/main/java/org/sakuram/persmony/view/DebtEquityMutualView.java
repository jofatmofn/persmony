package org.sakuram.persmony.view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.AccountingIsinActionEntryVO;
import org.sakuram.persmony.valueobject.ActionVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionCreateVO;
import org.sakuram.persmony.valueobject.IsinActionSpecVO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.LotVO;
import org.sakuram.persmony.valueobject.RealIsinActionEntryVO;
import org.sakuram.persmony.valueobject.TradeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
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
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("dem")
public class DebtEquityMutualView extends Div {

	private static final long serialVersionUID = 7040253088998928399L;

	DebtEquityMutualService debtEquityMutualService;
	MiscService miscService;
	DatePickerI18n isoDatePickerI18n;
	
	@Autowired
	private ApplicationContext context;
		
	public DebtEquityMutualView(DebtEquityMutualService debtEquityMutualService, MiscService miscService, DatePickerI18n isoDatePickerI18n) {
		
		Span selectSpan;
		FormLayout formLayout;
		Select<Map.Entry<Integer,String>> functionSelect;
		List<Map.Entry<Integer, String>> menuItemsList;
		
		this.debtEquityMutualService = debtEquityMutualService;
		this.miscService = miscService;
		this.isoDatePickerI18n = isoDatePickerI18n;

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
		HorizontalLayout hLayout, clientSideControlsLayout;
		Button historyButton, balancesButton;
		Grid<LotVO> lotsGrid;
		SecuritySearchComponent securitySearchComponent;
		Select<IdValueVO> dematAccountDvSelect;
		
		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		formLayout.addFormItem(securitySearchComponent.getLayout(), "ISIN");
		
        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, true, false);
        formLayout.addFormItem(dematAccountDvSelect, "Demat Account");
		
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
		
		clientSideControlsLayout = new HorizontalLayout();
		formLayout.add(clientSideControlsLayout);
		
		lotsGrid = new Grid<>(LotVO.class);
		formLayout.add(lotsGrid);
		
		historyButton.addClickListener(event -> {
			Checkbox includeInternalCheckbox, includeRelatedIsinsCheckbox;
			AtomicBoolean includeInternal, includeRelatedIsins;
			
			try {
				if (securitySearchComponent.getIsinTextField() == null || securitySearchComponent.getIsinTextField().isEmpty()) {
					return;
				}
				
				includeInternal = new AtomicBoolean(true);
				includeRelatedIsins = new AtomicBoolean(true);
				clientSideControlsLayout.remove(clientSideControlsLayout.getChildren().collect(Collectors.toList()));
				includeInternalCheckbox = new Checkbox("Include Internal Entries");
				includeInternalCheckbox.setValue(true);
				includeRelatedIsinsCheckbox = new Checkbox("Include Related ISINs");
				includeRelatedIsinsCheckbox.setValue(true);
				clientSideControlsLayout.add(includeInternalCheckbox, includeRelatedIsinsCheckbox);
				
				List<LotVO> lotVOList = debtEquityMutualService.fetchLots(securitySearchComponent.getIsinTextField().getValue(), null, dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(), true, "S");
				Notification.show("No. of Lots fetched: " + lotVOList.size())
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				lotsGrid.setColumns("isinActionVO.internal", "isinActionVO.settlementDate", "holdingChangeDate", "isinActionVO.isin", "isinActionVO.securityName", "isinActionVO.isinActionId", "isinActionPartId", "tradeId", "isinActionVO.actionType.value", "isinActionVO.bookingType.value", "isinActionVO.dematAccount.value", "pricePerUnit", "transactionQuantity");
				lotsGrid.getColumnByKey("holdingChangeDate").setHeader("Acq/Disp. Date");
				lotsGrid.getColumnByKey("isinActionPartId").setHeader("Lot Id");
				lotsGrid.getColumnByKey("isinActionVO.actionType.value").setHeader("Action");
				lotsGrid.getColumnByKey("isinActionVO.bookingType.value").setHeader("Booking");
				lotsGrid.getColumnByKey("isinActionVO.dematAccount.value").setHeader("Demat A/c");
				for (Column<LotVO> column : lotsGrid.getColumns()) {
					column.setResizable(true);
				}
				lotsGrid.setItems(lotVOList);

				ValueChangeListener<ValueChangeEvent<?>> filterLogic = e -> {
					lotsGrid.setItems(lotVOList
							.stream()
							.filter(lotVO -> 
									includeInternal.get() && includeRelatedIsins.get() || 
									!includeInternal.get() && !includeRelatedIsins.get() && !lotVO.getIsinActionVO().isInternal() && lotVO.getIsinActionVO().getIsin().equals(securitySearchComponent.getIsinTextField().getValue()) ||
									!includeInternal.get() && includeRelatedIsins.get() && !lotVO.getIsinActionVO().isInternal() ||
									includeInternal.get() && !includeRelatedIsins.get() && lotVO.getIsinActionVO().getIsin().equals(securitySearchComponent.getIsinTextField().getValue()))
							.collect(Collectors.toList()));
				};
				includeInternalCheckbox.addValueChangeListener(e -> {
					includeInternal.set(includeInternalCheckbox.getValue());
					filterLogic.valueChanged(e);
				});
				includeRelatedIsinsCheckbox.addValueChangeListener(e -> {
					includeRelatedIsins.set(includeRelatedIsinsCheckbox.getValue()); 
					filterLogic.valueChanged(e);
				});
				
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			} finally {
				historyButton.setEnabled(true);
			}
		});
		balancesButton.addClickListener(event -> {
			Checkbox includeNilBalanceCheckbox;
			
			try {
				if (securitySearchComponent.getIsinTextField() == null || securitySearchComponent.getIsinTextField().isEmpty()) {
					return;
				}
		        
				clientSideControlsLayout.remove(clientSideControlsLayout.getChildren().collect(Collectors.toList()));
				includeNilBalanceCheckbox = new Checkbox("Include Nil Balance Lots");
				includeNilBalanceCheckbox.setValue(true);
				clientSideControlsLayout.add(includeNilBalanceCheckbox);
				
				List<LotVO> lotVOList = debtEquityMutualService.fetchLots(securitySearchComponent.getIsinTextField().getValue(), null, dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId(), true, "A")
						.stream()
						.filter(lotVO -> lotVO.getIsinActionVO().getBookingType().getId() == Constants.DVID_BOOKING_CREDIT && !lotVO.getIsinActionVO().isInternal())
						.collect(Collectors.toList());
				Notification.show("No. of Credit Lots: " + lotVOList.size())
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				lotsGrid.setColumns("isinActionVO.settlementDate", "holdingChangeDate", "isinActionVO.isin", "isinActionVO.securityName", "isinActionVO.isinActionId", "isinActionPartId", "tradeId", "isinActionVO.actionType.value", "isinActionVO.dematAccount.value", "pricePerUnit", "transactionQuantity", "balance");
				lotsGrid.getColumnByKey("holdingChangeDate").setHeader("Acq/Disp. Date");
				lotsGrid.getColumnByKey("isinActionPartId").setHeader("Lot Id");
				lotsGrid.getColumnByKey("isinActionVO.actionType.value").setHeader("Action");
				lotsGrid.getColumnByKey("isinActionVO.dematAccount.value").setHeader("Demat A/c");

				for (Column<LotVO> column : lotsGrid.getColumns()) {
					column.setResizable(true);
				}
				lotsGrid.setItems(lotVOList);

				includeNilBalanceCheckbox.addValueChangeListener(e -> {
					if (includeNilBalanceCheckbox.getValue()) {
						lotsGrid.setItems(lotVOList);
					} else {
						lotsGrid.setItems(lotVOList
								.stream()
								.filter(lotVO -> lotVO.getBalance() > 0)
								.collect(Collectors.toList()));
					}
				});
				
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
		IntegerField actionIdIntegerField;	// TODO LongField
		IntegerField newSharesPerOldIntegerField, oldSharesBaseIntegerField, logicTriggerIntegerField;
		NumberField costRetainedFractionNumberField;
		HorizontalLayout hLayout;
		List<LotVO> fifoLotVOList;
		IsinActionCreateVO isinActionCreateVO;
		IsinActionSpecVO isinActionSpecVO;
		Binder<IsinActionCreateVO> binder = new Binder<>(IsinActionCreateVO.class);
		
		isinActionSpecVO = new IsinActionSpecVO();
		isinActionCreateVO = new IsinActionCreateVO();
		isinActionCreateVO.setActionVO(new ActionVO());
		fifoLotVOList = new ArrayList<LotVO>();
		isinActionCreateVO.setFifoLotVOList(fifoLotVOList);
		
		logicTriggerIntegerField = new IntegerField();
		logicTriggerIntegerField.setVisible(false);
		logicTriggerIntegerField.setValue(1);

		actionIdIntegerField = new IntegerField();
        parentFormLayout.addFormItem(actionIdIntegerField, "Action Id");
		
        actionDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_SECURITY_ACTION, false, true), null, false, false);
        parentFormLayout.addFormItem(actionDvSelect, "Action");
        
		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		parentFormLayout.addFormItem(securitySearchComponent.getLayout(), "Entitled ISIN");
		
        recordDateDatePicker = new DatePicker();
        recordDateDatePicker.setI18n(isoDatePickerI18n);
		parentFormLayout.addFormItem(recordDateDatePicker, "Record Date");
		
		newSharesPerOldIntegerField = new IntegerField();
		oldSharesBaseIntegerField = new IntegerField();
		newSharesPerOldIntegerField.setMin(0);
		newSharesPerOldIntegerField.setMax(100);
		newSharesPerOldIntegerField.setEnabled(isinActionSpecVO.isRatioApplicable());
		oldSharesBaseIntegerField.setMin(0);
		oldSharesBaseIntegerField.setMax(100);
		oldSharesBaseIntegerField.setEnabled(isinActionSpecVO.isRatioApplicable());
		hLayout = new HorizontalLayout();
		hLayout.add(newSharesPerOldIntegerField, new NativeLabel(":"), oldSharesBaseIntegerField);
		parentFormLayout.addFormItem(hLayout, "Quantity Ratio (New:Old)");

		costRetainedFractionNumberField = new NumberField();
		costRetainedFractionNumberField.setMin(0);
		costRetainedFractionNumberField.setMax(1);
		parentFormLayout.addFormItem(costRetainedFractionNumberField, "Fraction of Cost Retained");
		
        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, false, false);
        parentFormLayout.addFormItem(dematAccountDvSelect, "Demat Account");
        
		FormLayout childFormLayout;
		childFormLayout = new FormLayout();
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		parentFormLayout.add(childFormLayout);
		
		binder.forField(actionIdIntegerField)
			.withConverter(
	    			fieldValue -> fieldValue == null ? null : fieldValue.longValue(),
	    			beanValue -> beanValue == null ? null : beanValue.intValue()
			)
			.bind("actionId");
		binder.forField(actionDvSelect)
			.bind(
				vo -> vo.getActionVO().getActionType(),
				(vo, beanValue) -> vo.getActionVO().setActionType(beanValue)
			);
		binder.forField(securitySearchComponent)
			.withConverter(
    			fieldValue -> fieldValue == null ? null : fieldValue.toUpperCase(),
    			beanValue -> beanValue
			)
			.bind(
					vo -> vo.getActionVO().getEntitledIsin(),
					(vo, beanValue) -> vo.getActionVO().setEntitledIsin(beanValue)
			);
		binder.forField(recordDateDatePicker)
			.bind(
				vo -> vo.getActionVO().getRecordDate(),
				(vo, beanValue) -> vo.getActionVO().setRecordDate(beanValue)
			);
		binder.forField(newSharesPerOldIntegerField)
			.withConverter(
    			fieldValue -> fieldValue == null ? null : fieldValue.shortValue(),
    			beanValue -> beanValue == null ? null : beanValue.intValue()
			)
			.bind(
				vo -> vo.getActionVO().getNewSharesPerOld(),
				(vo, beanValue) -> vo.getActionVO().setNewSharesPerOld(beanValue)
			);
		binder.forField(oldSharesBaseIntegerField)
			.withConverter(
				fieldValue -> fieldValue == null ? null : fieldValue.shortValue(),
    			beanValue -> beanValue == null ? null : beanValue.intValue()
			)
			.bind(
					vo -> vo.getActionVO().getOldSharesBase(),
					(vo, beanValue) -> vo.getActionVO().setOldSharesBase(beanValue)
			);
		binder.forField(costRetainedFractionNumberField)
			.bind(
				vo -> vo.getActionVO().getCostRetainedFraction(),
				(vo, beanValue) -> vo.getActionVO().setCostRetainedFraction(beanValue)
			);
		binder.forField(dematAccountDvSelect)
			.bind("dematAccount");
		binder.setBean(isinActionCreateVO);

        actionIdIntegerField.addValueChangeListener(event -> {
        	System.out.println("actionIdIntegerField.addValueChangeListener");
        	if (actionIdIntegerField.getValue() == null) {
        		isinActionCreateVO.getActionVO().setActionType(new IdValueVO());
        		actionDvSelect.setEnabled(true);
        		isinActionCreateVO.getActionVO().setEntitledIsin("");
        		securitySearchComponent.setEnabled(true);
        		isinActionCreateVO.getActionVO().setRecordDate(null);
        		recordDateDatePicker.setEnabled(true);
        		isinActionCreateVO.getActionVO().setNewSharesPerOld(null);
        		newSharesPerOldIntegerField.setEnabled(true);
        		isinActionCreateVO.getActionVO().setOldSharesBase(null);
        		oldSharesBaseIntegerField.setEnabled(true);
        		isinActionCreateVO.getActionVO().setCostRetainedFraction(null);
        		costRetainedFractionNumberField.setEnabled(true);
        	} else {
        		isinActionCreateVO.setActionVO(debtEquityMutualService.fetchAction(actionIdIntegerField.getValue().longValue()));
        		actionDvSelect.setEnabled(false);
        		securitySearchComponent.setEnabled(false);
        		recordDateDatePicker.setEnabled(false);
        		newSharesPerOldIntegerField.setEnabled(false);
        		oldSharesBaseIntegerField.setEnabled(false);
        		costRetainedFractionNumberField.setEnabled(false);
        	}
    		binder.refreshFields();
        });

        // dematAccountDvSelect.addValueChangeListener(event -> {
        	// isinActionCreateVO.setDematAccount(dematAccountDvSelect.getValue());        	
        // });
        
        actionDvSelect.addValueChangeListener(event -> {
        	childFormLayout.remove(childFormLayout.getChildren().collect(Collectors.toList()));
        	if (actionDvSelect.getValue() != null && Constants.ISIN_ACTION_SPEC_MAP.containsKey(actionDvSelect.getValue().getId())) {
        		IsinActionSpecVO localIsinActionSpecVO = Constants.ISIN_ACTION_SPEC_MAP.get(actionDvSelect.getValue().getId());
        		isinActionSpecVO.copyFrom(localIsinActionSpecVO);
        		if (isinActionSpecVO.isRecordDateApplicable()) {
            		recordDateDatePicker.setEnabled(true);
        		} else {
        			isinActionCreateVO.getActionVO().setRecordDate(null);
            		recordDateDatePicker.setEnabled(false);
        		}
        		if (isinActionSpecVO.isRatioApplicable()) {
        			newSharesPerOldIntegerField.setEnabled(true);
        			oldSharesBaseIntegerField.setEnabled(true);
        		} else {
            		isinActionCreateVO.getActionVO().setNewSharesPerOld(null);
        			newSharesPerOldIntegerField.setEnabled(false);
            		isinActionCreateVO.getActionVO().setOldSharesBase(null);
        			oldSharesBaseIntegerField.setEnabled(false);
        		}
        		if (isinActionSpecVO.isCostRetainedFractionApplicable()) {
        			costRetainedFractionNumberField.setEnabled(true);
        		} else {
            		isinActionCreateVO.getActionVO().setCostRetainedFraction(null);
            		costRetainedFractionNumberField.setEnabled(false);
        		}
        		binder.refreshFields();
				logicTriggerIntegerField.setValue(logicTriggerIntegerField.getValue() == 0 ? 1 : 0);
        	} else {
        		isinActionSpecVO.copyFrom(new IsinActionSpecVO());
        	}
        });
        
		ValueChangeListener<ValueChangeEvent<?>> fetchFifoMappingLogic = e -> {
			fifoLotVOList.clear();
			if (securitySearchComponent.getIsinTextField().getValue() != null && !securitySearchComponent.getIsinTextField().isEmpty() &&
					dematAccountDvSelect.getValue() != null && dematAccountDvSelect.getValue().getId() != null &&
					(recordDateDatePicker.getValue() != null || !isinActionSpecVO.isRecordDateApplicable())) {
				fifoLotVOList.addAll(debtEquityMutualService.fetchLots(securitySearchComponent.getIsinTextField().getValue(), recordDateDatePicker.getValue(), dematAccountDvSelect.getValue().getId(), false, "A")
						.stream()
						.filter(lotVO -> lotVO.getBalance() != null && lotVO.getBalance() > 0 && lotVO.getIsinActionVO().getBookingType().getId() == Constants.DVID_BOOKING_CREDIT &&
							(lotVO.getHoldingChangeDate() == null || recordDateDatePicker.getValue() == null || lotVO.getHoldingChangeDate().isBefore(recordDateDatePicker.getValue()) || lotVO.getHoldingChangeDate().isEqual(recordDateDatePicker.getValue())))
						.collect(Collectors.toList())
					);
				System.out.println(isinActionCreateVO);
	    		handleCreateIsinActions2(childFormLayout, isinActionSpecVO, isinActionCreateVO, logicTriggerIntegerField);
			}
		};
		securitySearchComponent.getIsinTextField().addValueChangeListener(fetchFifoMappingLogic);
		dematAccountDvSelect.addValueChangeListener(fetchFifoMappingLogic);
		recordDateDatePicker.addValueChangeListener(fetchFifoMappingLogic);
		logicTriggerIntegerField.addValueChangeListener(fetchFifoMappingLogic);
		
	}

	private void handleCreateIsinActions2(FormLayout childFormLayout, IsinActionSpecVO isinActionSpecVO, IsinActionCreateVO isinActionCreateVO, IntegerField logicTriggerIntegerField) {
		HorizontalLayout buttonsHorizontalLayout;
		NumberField balanceNumberField;
		List<RealIsinActionEntryEditor> realIsinActionEntryEditorList;
		List<TradeVO> tradeVOList;
		AtomicReference<Double> quantityAR;
		List<RealIsinActionEntryVO> realIAEVOList;
		List<AccountingIsinActionEntryVO> accountingIAEVOList;
		List<IsinActionEntrySpecVO> isinActionEntrySpecVOList;
		
		childFormLayout.remove(childFormLayout.getChildren().collect(Collectors.toList()));
		tradeVOList = new ArrayList<TradeVO>();
		quantityAR = new AtomicReference<Double>();
		quantityAR.set(0D);
		isinActionEntrySpecVOList = isinActionSpecVO.getIsinActionEntrySpecVOList();
		realIAEVOList = new ArrayList<RealIsinActionEntryVO>(isinActionEntrySpecVOList.size());
		realIsinActionEntryEditorList = new ArrayList<RealIsinActionEntryEditor>(isinActionEntrySpecVOList.size());
		accountingIAEVOList = new ArrayList<AccountingIsinActionEntryVO>();
		isinActionCreateVO.setTradeVOList(tradeVOList);
		isinActionCreateVO.setRealIAEVOList(realIAEVOList);
		isinActionCreateVO.setAccountingIAEVOList(accountingIAEVOList);
		
		childFormLayout.add(ViewFuncs.newHorizontalLine());
		balanceNumberField = new NumberField();
		childFormLayout.addFormItem(balanceNumberField, "Balance");
		balanceNumberField.setEnabled(false);
		balanceNumberField.setValue(
				isinActionCreateVO.getFifoLotVOList()
					.stream()
					.mapToDouble(balanceIAVO -> balanceIAVO.getBalance())
					.sum()
		);
		
		for (IsinActionEntrySpecVO isinActionEntrySpecVO : isinActionEntrySpecVOList) {
			RealIsinActionEntryVO realIsinActionEntryVO = new RealIsinActionEntryVO();
			realIAEVOList.add(realIsinActionEntryVO);
			realIsinActionEntryVO.setIsinActionEntrySpecVO(isinActionEntrySpecVO);
			RealIsinActionEntryEditor realIsinActionEntryEditor = context.getBean(RealIsinActionEntryEditor.class, realIsinActionEntryVO,
					new RealIsinActionEntryEditor.InputArgs(isinActionCreateVO.getActionVO().getEntitledIsin(), isinActionCreateVO.getDematAccount(), isinActionCreateVO.getActionVO().getRecordDate(), balanceNumberField.getValue(),
							isinActionEntrySpecVOList.stream().anyMatch(iActionEntrySpecVO -> iActionEntrySpecVO.getLotCreationType() == IsinActionEntrySpecVO.IALotCreationType.TRADE),
							isinActionSpecVO.getActionDvId() == Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER && isinActionEntrySpecVO.getEntrySpecName().equals(Constants.ACTION_TYPE_GIFT_OR_TRANSFER_ENTRY_SPEC_NAME_RECEIVE),
							debtEquityMutualService, miscService, isoDatePickerI18n));
			realIsinActionEntryEditorList.add(realIsinActionEntryEditor);
			childFormLayout.add(realIsinActionEntryEditor);
		}
		
		// Input provided by user propagated to read-only fields
		for (int i = 0; i < realIsinActionEntryEditorList.size(); i++) {
			RealIsinActionEntryEditor realIsinActionEntryEditor = realIsinActionEntryEditorList.get(i);
			if (realIAEVOList.get(i).getIsinActionEntrySpecVO().getQuantityInputType() == IsinActionEntrySpecVO.IAQuantityType.INPUT) {
				realIsinActionEntryEditor.getQuantityNumberField().addValueChangeListener(e -> {
					quantityAR.set(realIsinActionEntryEditor.getQuantityNumberField().getValue());
					if (e.isFromClient()) {
						Double newVal = e.getValue();
						for (int j = 0; j < realIsinActionEntryEditorList.size(); j++) {
							if (realIAEVOList.get(j).getIsinActionEntrySpecVO().getQuantityInputType() == IsinActionEntrySpecVO.IAQuantityType.PREVIOUS_INPUT) {
								realIsinActionEntryEditorList.get(j).setQuantityNumberField(newVal);
								realIAEVOList.get(j).setQuantity(newVal);	// It is read-only and hence this needs to be done explicitly
																			// Same as realIsinActionEntryEditorList.get(j).getBean().setQuantity(newVal);
							}
						}
					}
				});
			}
		}

		childFormLayout.add(ViewFuncs.newHorizontalLine());
		buttonsHorizontalLayout = new HorizontalLayout();
		childFormLayout.add(buttonsHorizontalLayout);
		
		if (isinActionEntrySpecVOList.stream().anyMatch(isinActionEntrySpecVO -> 
				isinActionEntrySpecVO.getFifoMappingType() == IsinActionEntrySpecVO.IAFifoMappingType.USER_CHOICE)) {
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
					acceptFifoMap(new String[] {isinActionCreateVO.getActionVO().getEntitledIsin(), isinActionCreateVO.getDematAccount().getValue()}, isinActionCreateVO.getFifoLotVOList(), quantityAR.get());
				} catch (Exception e) {
					ViewFuncs.showError("System Error!!! Contact Support.");
					e.printStackTrace();
					return;
				} finally {
					fifoMapButton.setEnabled(true);
				}
			});
		}
		
		if (isinActionEntrySpecVOList.stream().anyMatch(isinActionEntrySpecVO -> isinActionEntrySpecVO.getLotCreationType() == IsinActionEntrySpecVO.IALotCreationType.TRADE)) {
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
				acceptAccountingEntries(accountingIAEVOList);
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
				// Validations
				// ISIN should be present in the DB
				// FIFO sum, trades sum should match with ISIN quantity
				System.out.println(isinActionCreateVO);
				debtEquityMutualService.createIsinActions(isinActionCreateVO);
				Notification.show("ISIN Actions created successfully.")
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				logicTriggerIntegerField.setValue(logicTriggerIntegerField.getValue() == 0 ? 1 : 0);
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
	
	private void acceptFifoMap(String[] labels, List<LotVO> balanceLotVOList, double iAQuantity) {
		Dialog dialog;
		VerticalLayout verticalLayout;
		HorizontalLayout hLayout;
		Button closeButton, doneButton, cancelButton, zeroAllButton;
		Grid<LotVO> quantityPriceGrid;
		Binder<LotVO> quantityPriceBinder;
		Editor<LotVO> quantityPriceEditor;
		List<LotVO> beforeChangeLotVOList;
		NumberField quantityNumberField;

		beforeChangeLotVOList = new ArrayList<LotVO>(balanceLotVOList.size());
		for (LotVO lotVO : balanceLotVOList) {	// Replacement for .addAll, so that the items are cloned (note: Just shallow copy, not deep)
			beforeChangeLotVOList.add(new LotVO(lotVO));
		}

		dialog = new Dialog();
		dialog.setHeaderTitle("FIFO Map");
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
					for (LotVO lotVO : beforeChangeLotVOList) {
						balanceLotVOList.add(new LotVO(lotVO));
					}
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		verticalLayout = new VerticalLayout();
		verticalLayout.getStyle().set("width", "75rem");
		dialog.add(verticalLayout);
		
		;
		verticalLayout.add(new NativeLabel("ISIN: " + labels[0]));
		verticalLayout.add(new NativeLabel("Demat A/c: " + labels[1]));
		
		quantityPriceGrid = new Grid<>(LotVO.class, false);
		quantityPriceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		quantityPriceGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		quantityPriceGrid.setItems(balanceLotVOList);
		quantityPriceGrid.setColumns("holdingChangeDate", "isinActionVO.actionType.value", "transactionQuantity", "balance");
		quantityPriceGrid.getColumnByKey("balance").setHeader("Mapped Quantity");
		for (Column<LotVO> column : quantityPriceGrid.getColumns()) {
			column.setResizable(true);
		}
		verticalLayout.add(quantityPriceGrid);

		quantityPriceBinder = new Binder<>(LotVO.class);
		quantityPriceEditor = quantityPriceGrid.getEditor();
		quantityPriceEditor.setBinder(quantityPriceBinder);
		
		quantityNumberField = new NumberField();
		quantityNumberField.getElement().addEventListener("keydown", e -> quantityPriceEditor.cancel())
        	.setFilter("event.key === 'Escape' || event.key === 'Esc'");
		quantityPriceBinder.forField(quantityNumberField)
			.bind(LotVO::getBalance, LotVO::setBalance);
		quantityPriceGrid.getColumnByKey("balance").setEditorComponent(quantityNumberField);
		quantityNumberField.addValueChangeListener(e -> {
			if (quantityNumberField.getValue() == null) {
				quantityNumberField.setValue(0D);
			}
		});
		
		hLayout = new HorizontalLayout();
		verticalLayout.add(hLayout);
		
		doneButton = new Button("Done");
		hLayout.add(doneButton);
		doneButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		doneButton.setDisableOnClick(true);
		doneButton.addClickListener(event -> {
			try {
				// Validations
				double totalMappedQuantity;
				totalMappedQuantity = 0;
				for (LotVO balanceLotVO : balanceLotVOList) {
					totalMappedQuantity += (balanceLotVO.getBalance() == null ? 0 : balanceLotVO.getBalance());
				}
				if (Math.abs(iAQuantity - totalMappedQuantity) > Constants.EPSILON) {
					ViewFuncs.showError("Total of Mapped Quantities does not match Action Quantity");
					return;
				}
			} finally {
				doneButton.setEnabled(true);
			}
			
			dialog.close();
		});
		
		cancelButton = new Button("Cancel");
		hLayout.add(cancelButton);
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton.addClickListener(event -> {
			balanceLotVOList.clear();
			for (LotVO lotVO : beforeChangeLotVOList) {
				balanceLotVOList.add(new LotVO(lotVO));
			}
			dialog.close();
		});

		zeroAllButton = new Button("Zero All");
		hLayout.add(zeroAllButton);
		zeroAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		zeroAllButton.addClickListener(event -> {
			for (LotVO balanceLotVO : balanceLotVOList) {
				balanceLotVO.setBalance(0D);
			}
			quantityPriceGrid.getDataProvider().refreshAll();
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
		Button addButton, updateButton, clearButton, closeButton, cancelButton, doneButton;
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
		for (TradeVO tradeVO : tradeVOList) {
			beforeChangeTradeVOList.add(tradeVO);
		}

		dialog = new Dialog();
		dialog.setHeaderTitle("Trades");
		dialog.setCloseOnOutsideClick(false);
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
					tradeVOList.clear();
					for (TradeVO tradeVO : beforeChangeTradeVOList) {
						tradeVOList.add(tradeVO);
					}
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
        orderDateDatePicker.setI18n(isoDatePickerI18n);
		orderTimeTextField = new TextField("Time");
		hLayout.add(orderNoTextField, orderDateDatePicker, orderTimeTextField);
		formLayout.add(ViewFuncs.newLine());

		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Trade");
		tradeNoTextField = new TextField("No");
		tradeDateDatePicker = new DatePicker("Date");
        tradeDateDatePicker.setI18n(isoDatePickerI18n);
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
		addButton = new Button("Add");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add
		addButton.addClickListener(event -> {
			try {

				if (editRowTradeIdAR.get() != null) {
					ConfirmDialog confirmDialog = new ConfirmDialog();
					confirmDialog.setHeader("Confirm Add");
					confirmDialog.setText("Are you sure you want to ADD (and not UPDATE)?");
					confirmDialog.setConfirmButton("Confirm", confirmEvent -> {
						TradeVO tradeVO;
						tradeVO = new TradeVO();
						fields2VO(tradeVO, orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
								tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
								orderDateDatePicker, tradeDateDatePicker);
						tradeVO.setTradeId((long) (tradeVOList.size() == 0 ? -1 : (tradeVOList.get(tradeVOList.size() - 1).getTradeId() - 1)));
						tradeGridLDV.addItem(tradeVO);
						tradeGrid.select(tradeVO);
						editRowTradeIdAR.set(tradeVO.getTradeId());
						tradeIdNumberField.setValue(tradeVO.getTradeId().doubleValue());
					});
					confirmDialog.setCancelButton("Cancel", cancelEvent -> {
					});
					confirmDialog.open();
				} else {
					TradeVO tradeVO;
					tradeVO = new TradeVO();
					fields2VO(tradeVO, orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
							tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
							orderDateDatePicker, tradeDateDatePicker);
					tradeVO.setTradeId((long) (tradeVOList.size() == 0 ? -1 : (tradeVOList.get(tradeVOList.size() - 1).getTradeId() - 1)));
					tradeGridLDV.addItem(tradeVO);
					tradeGrid.select(tradeVO);
					editRowTradeIdAR.set(tradeVO.getTradeId());
					tradeIdNumberField.setValue(tradeVO.getTradeId().doubleValue());
				}
			} finally {
				addButton.setEnabled(true);
			}
		});

		updateButton = new Button("Update");
		updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		updateButton.setDisableOnClick(true);
		// On click of Update
		updateButton.addClickListener(event -> {
			try {
				TradeVO tradeVO;
				if (editRowTradeIdAR.get() == null) {
					ViewFuncs.showError("No row selected for Update");
					return;
				}
				tradeVO = tradeVOList
						.stream()
						.filter(tradeVObj -> tradeVObj.getTradeId().equals(editRowTradeIdAR.get()))
						.findFirst()
						.get();
				tradeGrid.select(tradeVO);
				fields2VO(tradeVO, orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
				tradeGridLDV.refreshItem(tradeVO);
			} finally {
				updateButton.setEnabled(true);
			}
		});
		
		clearButton = new Button("Clear");
		clearButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		clearButton.setDisableOnClick(true);
		// On click of Update
		clearButton.addClickListener(event -> {
			try {
				vO2Fields(new TradeVO(), orderNoTextField, orderTimeTextField, tradeNoTextField, tradeTimeTextField,
						tradeIdNumberField, quantityNumberField, pricePerUnitNumberField, brokeragePerUnitNumberField,
						orderDateDatePicker, tradeDateDatePicker);
			} finally {
				clearButton.setEnabled(true);
			}
		});
		
		hLayout.add(addButton, updateButton, clearButton);
		formLayout.add(ViewFuncs.newLine());
		
		formLayout.add(ViewFuncs.newHorizontalLine());
		formLayout.add(ViewFuncs.newLine());
		
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "");
		doneButton = new Button("Done");
		doneButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		doneButton.setDisableOnClick(true);
		doneButton.addClickListener(event -> {
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
					ViewFuncs.showError("Total of Trade Quantities " + totalTradeQuantity + " does not match Action Quantity " + iAQuantity);
					return;
				}
			} finally {
				doneButton.setEnabled(true);
			}
			dialog.close();
		});

		cancelButton = new Button("Cancel");
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton.addClickListener(event -> {
			tradeVOList.clear();
			for (TradeVO tradeVO : beforeChangeTradeVOList) {
				tradeVOList.add(tradeVO);
			}
			dialog.close();
		});
		hLayout.add(doneButton, cancelButton);
		formLayout.add(ViewFuncs.newLine());

		tradeGrid.setColumns("tradeId", "orderDate", "orderTime", "orderNo", "tradeDate", "tradeTime", "tradeNo", "quantity", "pricePerUnit", "brokeragePerUnit");
		for (Column<TradeVO> column : tradeGrid.getColumns()) {
			column.setResizable(true);
		}

		tradeGrid.addItemClickListener(event -> {
			TradeVO tradeVO;

			tradeVO = event.getItem();
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
			orderDateDatePicker.setValue(tradeVO.getOrderDate());
		}
		if (tradeVO.getTradeDate() == null) {
			tradeDateDatePicker.clear();
		} else {
			tradeDateDatePicker.setValue(tradeVO.getTradeDate());
		}
	}

	private void fields2VO(TradeVO tradeVO, TextField orderNoTextField, TextField orderTimeTextField, TextField tradeNoTextField, TextField tradeTimeTextField,
			NumberField tradeIdNumberField, NumberField quantityNumberField, NumberField pricePerUnitNumberField, NumberField brokeragePerUnitNumberField,
			DatePicker orderDateDatePicker, DatePicker tradeDateDatePicker) {
		
		tradeVO.setTradeId(tradeIdNumberField.getValue() == null ? null : tradeIdNumberField.getValue().longValue());
		tradeVO.setOrderNo(orderNoTextField.getValue());
		tradeVO.setOrderTime(orderTimeTextField.getValue());
		tradeVO.setTradeNo(tradeNoTextField.getValue());
		tradeVO.setTradeTime(tradeTimeTextField.getValue());
		tradeVO.setQuantity(quantityNumberField.getValue());
		tradeVO.setPricePerUnit(pricePerUnitNumberField.getValue());
		tradeVO.setBrokeragePerUnit(brokeragePerUnitNumberField.getValue());
		tradeVO.setOrderDate(orderDateDatePicker.getValue());
		tradeVO.setTradeDate(tradeDateDatePicker.getValue());
	}

	private void acceptAccountingEntries(List<AccountingIsinActionEntryVO> accountingIAEVOList) {
		Dialog dialog;
		FormLayout formLayout;
		HorizontalLayout hLayout;
		Button addButton, closeButton, cancelButton, doneButton;
		Grid<AccountingIsinActionEntryVO> accountingIAGrid;
		Binder<AccountingIsinActionEntryVO> accountingIABinder;
		Editor<AccountingIsinActionEntryVO> accountingIAEditor;
		GridListDataView<AccountingIsinActionEntryVO> accountingIAGridLDV;
		Select<IdValueVO> bookingTypeDvSelect;
		DatePicker settlementDateDatePicker;
		NumberField transactionQuantityField;
		Grid.Column<AccountingIsinActionEntryVO> settlementDateColumn, isinColumn, transactionQuantityColumn, bookingTypeColumn;
		List<AccountingIsinActionEntryVO> beforeChangeAccountingIAVOList;
		
		beforeChangeAccountingIAVOList = new ArrayList<AccountingIsinActionEntryVO>(accountingIAEVOList.size());
		for (AccountingIsinActionEntryVO accountingIsinActionEntryVO : accountingIAEVOList) {
			beforeChangeAccountingIAVOList.add(accountingIsinActionEntryVO);
		}
    	
		dialog = new Dialog();
		dialog.setHeaderTitle("Accounting Entries");
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
		        	accountingIAEVOList.clear();
		    		for (AccountingIsinActionEntryVO accountingIsinActionEntryVO : beforeChangeAccountingIAVOList) {
		    			accountingIAEVOList.add(accountingIsinActionEntryVO);
		    		}
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		formLayout = new FormLayout();
		formLayout.getStyle().set("width", "75rem");
		formLayout.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1)
		);
		dialog.add(formLayout);
		
		accountingIAGrid = new Grid<>(AccountingIsinActionEntryVO.class, false);
		accountingIAGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		accountingIAGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		accountingIAGridLDV = accountingIAGrid.setItems(accountingIAEVOList);
		
		formLayout.add(accountingIAGrid);
		
		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		addButton = new Button("Add Row");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add Row
		addButton.addClickListener(event -> {
			try {
				accountingIAGridLDV.addItem(new AccountingIsinActionEntryVO());
			} finally {
				addButton.setEnabled(true);
			}
		});
		hLayout.add(addButton);

		doneButton = new Button("Done");
		doneButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(doneButton);
		doneButton.setDisableOnClick(true);
		doneButton.addClickListener(event -> {
			try {
				for (int i = 0; i < accountingIAEVOList.size(); i++) {
					AccountingIsinActionEntryVO accountingIAEVO = accountingIAEVOList.get(i);
					if (accountingIAEVO.getSettlementDate() == null &&
							accountingIAEVO.getIsin().isEmpty() && accountingIAEVO.getTransactionQuantity() == null &&
							accountingIAEVO.getBookingType() == null) {
						accountingIAGridLDV.removeItem(accountingIAEVO);
						i--;
					}
				}
				// Validations
				for (AccountingIsinActionEntryVO accountingIAEVO : accountingIAEVOList) {
					if (accountingIAEVO.getSettlementDate() == null ||
							accountingIAEVO.getIsin().isEmpty() || accountingIAEVO.getTransactionQuantity() == null ||
							accountingIAEVO.getBookingType() == null) {
						ViewFuncs.showError("None of the fields can be empty");
						return;
					}
				}
			} finally {
				doneButton.setEnabled(true);
			}
			dialog.close();
		});

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> {
        	accountingIAEVOList.clear();
    		for (AccountingIsinActionEntryVO accountingIsinActionEntryVO : beforeChangeAccountingIAVOList) {
    			accountingIAEVOList.add(accountingIsinActionEntryVO);
    		}
			dialog.close();
		});
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(cancelButton);

		// accountingIAGrid.addColumn("isinActionId").setHeader("ISIN Action Id").setWidth("75px");
		settlementDateColumn = accountingIAGrid.addColumn("settlementDate").setHeader("Date");
		settlementDateColumn.setResizable(true);
		isinColumn = accountingIAGrid.addColumn("isin").setHeader("ISIN");
		isinColumn.setResizable(true)
			.setWidth("225px");
		transactionQuantityColumn = accountingIAGrid.addColumn("transactionQuantity").setHeader("Quantity");
		bookingTypeColumn = accountingIAGrid.addColumn("bookingType.value").setHeader("Debit/Credit").setWidth("75px");
		accountingIAGrid.addComponentColumn(sbAcTxnCategoryVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e->{
				accountingIAGridLDV.removeItem(sbAcTxnCategoryVO);
			});
			return delButton;
		}).setWidth("120px").setFlexGrow(0);
		
		accountingIABinder = new Binder<>(AccountingIsinActionEntryVO.class);
		accountingIAEditor = accountingIAGrid.getEditor();
		accountingIAEditor.setBinder(accountingIABinder);
		
		settlementDateDatePicker = new DatePicker();
        settlementDateDatePicker.setI18n(isoDatePickerI18n);
		addCloseHandler(settlementDateDatePicker, accountingIAEditor);
		accountingIABinder.forField(settlementDateDatePicker)
			.bind(AccountingIsinActionEntryVO::getSettlementDate, AccountingIsinActionEntryVO::setSettlementDate);
		settlementDateColumn.setEditorComponent(settlementDateDatePicker);
		
		SecuritySearchComponent securitySearchComponent;
		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		addCloseHandler(securitySearchComponent, accountingIAEditor);
		accountingIABinder.forField(securitySearchComponent)
			.bind(AccountingIsinActionEntryVO::getIsin, AccountingIsinActionEntryVO::setIsin);
		isinColumn.setEditorComponent(securitySearchComponent.getLayout());
	
		transactionQuantityField = new NumberField();
		addCloseHandler(transactionQuantityField, accountingIAEditor);
		accountingIABinder.forField(transactionQuantityField)
			.bind(AccountingIsinActionEntryVO::getTransactionQuantity, AccountingIsinActionEntryVO::setTransactionQuantity);
		transactionQuantityColumn.setEditorComponent(transactionQuantityField);
		
		bookingTypeDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_BOOKING, null, false, false);
		addCloseHandler(bookingTypeDvSelect, accountingIAEditor);
		accountingIABinder.forField(bookingTypeDvSelect)
			.bind(AccountingIsinActionEntryVO::getBookingType, AccountingIsinActionEntryVO::setBookingType);
		bookingTypeColumn.setEditorComponent(bookingTypeDvSelect);
		
		accountingIAGrid.addItemDoubleClickListener(e -> {
		    accountingIAEditor.editItem(e.getItem());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable<?>) {
		        ((Focusable<?>) editorComponent).focus();
		    }
		});
		
		dialog.open();
	}
	
    private static void addCloseHandler(Component accountingEntryIAField,
            Editor<AccountingIsinActionEntryVO> editor) {
    	accountingEntryIAField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

}
