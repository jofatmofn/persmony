package org.sakuram.persmony.view;

import java.util.List;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.valueobject.LotMatchVO;
import org.sakuram.persmony.valueobject.LotWithCVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class LotShowComponent {
	DebtEquityMutualService debtEquityMutualService;
	
	public LotShowComponent(DebtEquityMutualService debtEquityMutualService) {
		this.debtEquityMutualService = debtEquityMutualService;
	}
	
	public FormLayout showForm(long isinActionPartId) {
		FormLayout formLayout;
		LotWithCVO lotWithCVO;
		H4 section;

		lotWithCVO = debtEquityMutualService.fetchLot(isinActionPartId);
    	
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 3));
		
		section = new H4("Lot");
		formLayout.add(section);
		formLayout.setColspan(section, 3);
		
		IntegerField lotIdIntegerField = new IntegerField();
		lotIdIntegerField.setReadOnly(true);
		lotIdIntegerField.setValue(lotWithCVO.getLotVO().getIsinActionPartId().intValue());
		formLayout.addFormItem(lotIdIntegerField, "Lot Id");

		DatePicker holdingChangeDateDatePicker = new DatePicker();
		holdingChangeDateDatePicker.setReadOnly(true);
		holdingChangeDateDatePicker.setValue(lotWithCVO.getLotVO().getHoldingChangeDate());
		formLayout.addFormItem(holdingChangeDateDatePicker, "Acq/Disp. Date");
		
		NumberField pricePerUnitNumberField = new NumberField();
		pricePerUnitNumberField.setReadOnly(true);
		pricePerUnitNumberField.setValue(lotWithCVO.getLotVO().getPricePerUnit());
		formLayout.addFormItem(pricePerUnitNumberField, "Price Per Unit");

		NumberField transactionQuantityNumberField = new NumberField();
		transactionQuantityNumberField.setReadOnly(true);
		transactionQuantityNumberField.setValue(lotWithCVO.getLotVO().getTransactionQuantity());
		formLayout.addFormItem(transactionQuantityNumberField, "Txn. Qty.");

		NumberField balanceNumberField = new NumberField();
		balanceNumberField.setReadOnly(true);
		balanceNumberField.setValue(lotWithCVO.getLotVO().getBalance());
		formLayout.addFormItem(balanceNumberField, "Balance");

		if (lotWithCVO.getTradeVO() != null) {
			section = new H4("Trade");
			formLayout.add(section);
			formLayout.setColspan(section, 3);
			
			IntegerField tradeIdIntegerField = new IntegerField();
			tradeIdIntegerField.setReadOnly(true);
			tradeIdIntegerField.setValue(lotWithCVO.getTradeVO().getTradeId().intValue());
			formLayout.addFormItem(tradeIdIntegerField, "Trade Id");

			NumberField brokeragePerUnitNumberField = new NumberField();
			brokeragePerUnitNumberField.setReadOnly(true);
			brokeragePerUnitNumberField.setValue(lotWithCVO.getTradeVO().getBrokeragePerUnit());
			FormItem brokeragePerUnitFormItem = formLayout.addFormItem(brokeragePerUnitNumberField, "Brokerage Per Unit");
			formLayout.setColspan(brokeragePerUnitFormItem, 2);

			DatePicker orderDateDatePicker = new DatePicker();
			orderDateDatePicker.setReadOnly(true);
			orderDateDatePicker.setValue(lotWithCVO.getTradeVO().getOrderDate());
			formLayout.addFormItem(orderDateDatePicker, "Order Date");
			
			TextField orderTimeTextField = new TextField();
			orderTimeTextField.setReadOnly(true);
			orderTimeTextField.setValue(lotWithCVO.getTradeVO().getOrderTime());
			formLayout.addFormItem(orderTimeTextField, "Order Time");
			
			TextField orderNoTextField = new TextField();
			orderNoTextField.setReadOnly(true);
			orderNoTextField.setValue(lotWithCVO.getTradeVO().getOrderNo());
			formLayout.addFormItem(orderNoTextField, "Order No.");
			
			DatePicker tradeDateDatePicker = new DatePicker();
			tradeDateDatePicker.setReadOnly(true);
			tradeDateDatePicker.setValue(lotWithCVO.getTradeVO().getTradeDate());
			formLayout.addFormItem(tradeDateDatePicker, "Trade Date");
			
			TextField tradeTimeTextField = new TextField();
			tradeTimeTextField.setReadOnly(true);
			tradeTimeTextField.setValue(lotWithCVO.getTradeVO().getTradeTime());
			formLayout.addFormItem(tradeTimeTextField, "Trade Time");
			
			TextField tradeNoTextField = new TextField();
			tradeNoTextField.setReadOnly(true);
			tradeNoTextField.setValue(lotWithCVO.getTradeVO().getTradeNo());
			formLayout.addFormItem(tradeNoTextField, "Trade No.");
			
		}
		
		if (lotWithCVO.getReceivedFromLotMatchVOList() != null) {
			showFifoMatches(formLayout, lotWithCVO.getReceivedFromLotMatchVOList(), "Received From Other Lots", "fromLotId");
		}
		if (lotWithCVO.getSentToLotMatchVOList() != null) {
			showFifoMatches(formLayout, lotWithCVO.getSentToLotMatchVOList(), "Sent To Other Lots", "toLotId");
		}
		return formLayout;
	}

	private void showFifoMatches(FormLayout formLayout, List<LotMatchVO> lotMatchVOList, String sectionTitle, String lotIdColumn) {
		H4 section;
		Grid<LotMatchVO> lotMatchGrid;
		
		section = new H4(sectionTitle);
		formLayout.add(section);
		formLayout.setColspan(section, 3);
		
		lotMatchGrid = new Grid<>(LotMatchVO.class);
		lotMatchGrid.setColumns(lotIdColumn, "quantity");
		formLayout.add(lotMatchGrid);
		for (Column<LotMatchVO> column : lotMatchGrid.getColumns()) {
			column.setResizable(true);
		}
		lotMatchGrid.setItems(lotMatchVOList);
		lotMatchGrid.addItemClickListener(e -> {
			Dialog dialog;
			Button closeButton;

			dialog = new Dialog();
			dialog.setHeaderTitle("DEM Details");
			closeButton = new Button(new Icon("lumo", "cross"),
			        (eventCloseButton) -> {
			        	dialog.close();
			        });
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			dialog.getHeader().add(closeButton);
			
	    	dialog.add(this.showForm(lotIdColumn.equals("fromLotId") ? e.getItem().getFromLotId() : e.getItem().getToLotId()));
		    
		    dialog.open();
		});
		formLayout.add(lotMatchGrid);
		formLayout.setColspan(lotMatchGrid, 3);
	}
}
