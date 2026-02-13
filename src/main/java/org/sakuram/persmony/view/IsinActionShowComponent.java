package org.sakuram.persmony.view;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.valueobject.IsinActionWithCVO;
import org.sakuram.persmony.valueobject.LotVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class IsinActionShowComponent {

	DebtEquityMutualService debtEquityMutualService;
	
	public IsinActionShowComponent(DebtEquityMutualService debtEquityMutualService) {
		this.debtEquityMutualService = debtEquityMutualService;
	}
	
	public FormLayout showForm(long isinActionId) {
		FormLayout formLayout, childFormLayout;
		IsinActionWithCVO isinActionWithCVO;
		Grid<LotVO> lotsGrid;

		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		childFormLayout = new FormLayout();
		formLayout.add(childFormLayout);
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		childFormLayout.add(new H4("Isin Action"));
		
    	isinActionWithCVO = debtEquityMutualService.fetchIsinAction(isinActionId);
		Notification.show("No. of Lots fetched: " + isinActionWithCVO.getLotVOList().size())
			.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    	
		childFormLayout = new FormLayout();
		formLayout.add(childFormLayout);
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 3));
		
		IntegerField isinActionIdIntegerField = new IntegerField();
		isinActionIdIntegerField.setReadOnly(true);
		isinActionIdIntegerField.setValue((int)isinActionWithCVO.getIsinActionVO().getIsinActionId());
		childFormLayout.addFormItem(isinActionIdIntegerField, "Isin Action Id");

		DatePicker settlementDateDatePicker = new DatePicker();
		settlementDateDatePicker.setReadOnly(true);
		settlementDateDatePicker.setValue(isinActionWithCVO.getIsinActionVO().getSettlementDate());
		childFormLayout.addFormItem(settlementDateDatePicker, "Settlement Date");
		
		TextField isinTextField = new TextField();
		isinTextField.setReadOnly(true);
		isinTextField.setValue(isinActionWithCVO.getIsinActionVO().getIsin() == null ? "" : isinActionWithCVO.getIsinActionVO().getIsin());
		childFormLayout.addFormItem(isinTextField, "ISIN");
		
		TextField securityNameTextField = new TextField();
		securityNameTextField.setReadOnly(true);
		securityNameTextField.setValue(isinActionWithCVO.getIsinActionVO().getSecurityName() == null ? "" : isinActionWithCVO.getIsinActionVO().getSecurityName());
		childFormLayout.addFormItem(securityNameTextField, "Security Name");
		
		TextField actionTypeTextField = new TextField();
		actionTypeTextField.setReadOnly(true);
		actionTypeTextField.setValue(isinActionWithCVO.getIsinActionVO().getActionType().getValue() == null ? "" : isinActionWithCVO.getIsinActionVO().getActionType().getValue());
		childFormLayout.addFormItem(actionTypeTextField, "Action");
		
		TextField bookingTypeTextField = new TextField();
		bookingTypeTextField.setReadOnly(true);
		bookingTypeTextField.setValue(isinActionWithCVO.getIsinActionVO().getBookingType().getValue() == null ? "" : isinActionWithCVO.getIsinActionVO().getBookingType().getValue());
		childFormLayout.addFormItem(bookingTypeTextField, "Booking");
		
		TextField dematAccountTextField = new TextField();
		dematAccountTextField.setReadOnly(true);
		dematAccountTextField.setValue(isinActionWithCVO.getIsinActionVO().getDematAccount().getValue() == null ? "" : isinActionWithCVO.getIsinActionVO().getDematAccount().getValue());
		childFormLayout.addFormItem(dematAccountTextField, "Demat A/c");
		
		Checkbox isInternalCheckbox = new Checkbox();
		isInternalCheckbox.setReadOnly(true);
		isInternalCheckbox.setValue(isinActionWithCVO.getIsinActionVO().isInternal());
		childFormLayout.addFormItem(isInternalCheckbox, "Accounting?");


		if (isinActionWithCVO.getContractVO() != null) {
			H4 section;
			if (isinActionWithCVO.getContractVO().getContractNo() != null) {
				section = new H4("Contract");
			} else {
				section = new H4("Contract Equivalent");
			}
			childFormLayout.add(section);
			childFormLayout.setColspan(section, 3);
			
			IntegerField contractIdIntegerField = new IntegerField();
			contractIdIntegerField.setReadOnly(true);
			contractIdIntegerField.setValue((int)isinActionWithCVO.getContractVO().getContractId());
			childFormLayout.addFormItem(contractIdIntegerField, "Contract Id");

			NumberField netAmountNumberField = new NumberField();
			netAmountNumberField.setReadOnly(true);
			netAmountNumberField.setValue(isinActionWithCVO.getContractVO().getNetAmount());
			childFormLayout.addFormItem(netAmountNumberField, "Net Amount");

			NumberField stampDutyNumberField = new NumberField();
			stampDutyNumberField.setReadOnly(true);
			stampDutyNumberField.setValue(isinActionWithCVO.getContractVO().getStampDuty());
			childFormLayout.addFormItem(stampDutyNumberField, "Stamp Duty");

			if (isinActionWithCVO.getContractVO().getContractNo() == null) {
				DatePicker allotmentDateDatePicker = new DatePicker();
				allotmentDateDatePicker.setReadOnly(true);
				allotmentDateDatePicker.setValue(isinActionWithCVO.getContractVO().getAllotmentDate());
				childFormLayout.addFormItem(allotmentDateDatePicker, "Allotment Date");
				
			} else {
				NumberField brokerageNumberField = new NumberField();
				brokerageNumberField.setReadOnly(true);
				brokerageNumberField.setValue(isinActionWithCVO.getContractVO().getBrokerage());
				childFormLayout.addFormItem(brokerageNumberField, "Brokerage");

				NumberField clearingChargeNumberField = new NumberField();
				clearingChargeNumberField.setReadOnly(true);
				clearingChargeNumberField.setValue(isinActionWithCVO.getContractVO().getClearingCharge());
				childFormLayout.addFormItem(clearingChargeNumberField, "Stamp Duty");

				DatePicker contractDateDatePicker = new DatePicker();
				contractDateDatePicker.setReadOnly(true);
				contractDateDatePicker.setValue(isinActionWithCVO.getContractVO().getContractDate());
				childFormLayout.addFormItem(contractDateDatePicker, "Contract Date");
				
				TextField contractNoTextField = new TextField();
				contractNoTextField.setReadOnly(true);
				contractNoTextField.setValue(isinActionWithCVO.getContractVO().getContractNo());
				childFormLayout.addFormItem(contractNoTextField, "Contract No.");
				
				NumberField exchangeTransactionChargeNumberField = new NumberField();
				exchangeTransactionChargeNumberField.setReadOnly(true);
				exchangeTransactionChargeNumberField.setValue(isinActionWithCVO.getContractVO().getExchangeTransactionCharge());
				childFormLayout.addFormItem(exchangeTransactionChargeNumberField, "Xchange Txn. Charge");

				NumberField gstNumberField = new NumberField();
				gstNumberField.setReadOnly(true);
				gstNumberField.setValue(isinActionWithCVO.getContractVO().getGst());
				childFormLayout.addFormItem(gstNumberField, "GST");

				NumberField sebiTurnoverFeeNumberField = new NumberField();
				sebiTurnoverFeeNumberField.setReadOnly(true);
				sebiTurnoverFeeNumberField.setValue(isinActionWithCVO.getContractVO().getSebiTurnoverFee());
				childFormLayout.addFormItem(sebiTurnoverFeeNumberField, "SEBI Turnover Fee");

				TextField settlementNoTextField = new TextField();
				settlementNoTextField.setReadOnly(true);
				settlementNoTextField.setValue(isinActionWithCVO.getContractVO().getSettlementNo());
				childFormLayout.addFormItem(settlementNoTextField, "Settlement No.");
				
				NumberField sttNumberField = new NumberField();
				sttNumberField.setReadOnly(true);
				sttNumberField.setValue(isinActionWithCVO.getContractVO().getGst());
				childFormLayout.addFormItem(sttNumberField, "STT");

			}
		}
		
		childFormLayout = new FormLayout();
		formLayout.add(childFormLayout);
		childFormLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		childFormLayout.add(new H4("Lots"));
		
		lotsGrid = new Grid<>(LotVO.class);
		childFormLayout.add(lotsGrid);
		lotsGrid.setColumns(LotVO.gridColumns());
		lotsGrid.getColumnByKey("holdingChangeDate").setHeader("Acq/Disp. Date");
		lotsGrid.getColumnByKey("isinActionPartId").setHeader("Lot Id");
		for (Column<LotVO> column : lotsGrid.getColumns()) {
			column.setResizable(true);
		}
		lotsGrid.setItems(isinActionWithCVO.getLotVOList());
		
		return formLayout;
	}
}
