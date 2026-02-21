package org.sakuram.persmony.view;

import java.util.List;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionCriteriaVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class IsinActionSearchComponent extends Div {

	private static final long serialVersionUID = -2499855633667909144L;
	
	DebtEquityMutualService debtEquityMutualService;
	MiscService miscService;
	
	Grid<IsinActionVO> isinActionsGrid;
	
	public IsinActionSearchComponent(DebtEquityMutualService debtEquityMutualService, MiscService miscService) {
		this.debtEquityMutualService = debtEquityMutualService;
		this.miscService = miscService;
	}
	
	public FormLayout showForm() {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		IntegerField isinActionFromIdIntegerField, isinActionToIdIntegerField;
		DatePicker settlementFromDatePicker, settlementToDatePicker;
		RadioButtonGroup<String> isInternalRadioButtonGroup, bookingRadioButtonGroup;
		SecuritySearchComponent securitySearchComponent;
		Select<IdValueVO> dematAccountDvSelect, actionTypeDvSelect;
		Button fetchButton;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		isinActionFromIdIntegerField = new IntegerField("From");
		isinActionToIdIntegerField = new IntegerField("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "ISIN Action Id");
		hLayout.add(isinActionFromIdIntegerField, isinActionToIdIntegerField);
		
		settlementFromDatePicker = new DatePicker("From");
		settlementToDatePicker = new DatePicker("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Settlement Date");
		hLayout.add(settlementFromDatePicker, settlementToDatePicker);
		
		securitySearchComponent = new SecuritySearchComponent(debtEquityMutualService, miscService);
		formLayout.addFormItem(securitySearchComponent.getLayout(), "ISIN");
		
        dematAccountDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, true, false);
        formLayout.addFormItem(dematAccountDvSelect, "Demat Account");
		
        actionTypeDvSelect = ViewFuncs.newDvSelect(miscService.fetchDvsOfCategory(Constants.CATEGORY_SECURITY_ACTION, true, false), null, true, false);
        formLayout.addFormItem(actionTypeDvSelect, "Action");
		
		bookingRadioButtonGroup = new RadioButtonGroup<String>();
		bookingRadioButtonGroup.setItems("Both", "Credit Only", "Debit Only");
		bookingRadioButtonGroup.setValue("Both");
		formLayout.addFormItem(bookingRadioButtonGroup, "Booking");
		
		isInternalRadioButtonGroup = new RadioButtonGroup<String>();
		isInternalRadioButtonGroup.setItems("Both", "Real Only", "Accounting Only");
		isInternalRadioButtonGroup.setValue("Both");
		formLayout.addFormItem(isInternalRadioButtonGroup, "Entry Type");
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		isinActionsGrid = new Grid<>(IsinActionVO.class);
		isinActionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		isinActionsGrid.setColumns(IsinActionVO.gridColumns());
		for (Column<IsinActionVO> column : isinActionsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(isinActionsGrid);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			List<IsinActionVO> recordList = null;
			Notification notification;
			
			try {
				// Validation
				if (isinActionFromIdIntegerField.getValue() != null && isinActionToIdIntegerField.getValue() != null &&
						isinActionFromIdIntegerField.getValue() > isinActionToIdIntegerField.getValue()) {
					ViewFuncs.showError("From ISIN Action Id cannot be greater than the To ISIN Action Id");
					return;
				}
				if (settlementFromDatePicker.getValue() != null && settlementToDatePicker.getValue() != null &&
						settlementFromDatePicker.getValue().isAfter(settlementToDatePicker.getValue())) {
					ViewFuncs.showError("Settlement From Date cannot be after the To Date");
					return;
				}
				
				// Back-end Call
				try {
					recordList = debtEquityMutualService.searchIsinActions(
							new IsinActionCriteriaVO(
									(isinActionFromIdIntegerField.getValue() == null ? null : isinActionFromIdIntegerField.getValue().longValue()),
									(isinActionToIdIntegerField.getValue() == null ? null : isinActionToIdIntegerField.getValue().longValue()),
									settlementFromDatePicker.getValue(),
									settlementToDatePicker.getValue(),
									(securitySearchComponent.getIsinTextField().getValue().equals("") ? null : securitySearchComponent.getIsinTextField().getValue()),
									(dematAccountDvSelect.getValue() == null ? null : dematAccountDvSelect.getValue().getId()),
									(actionTypeDvSelect.getValue() == null ? null : actionTypeDvSelect.getValue().getId()),
									bookingRadioButtonGroup.getValue().equals("Both") ? null : (bookingRadioButtonGroup.getValue().equals("Credit Only") ? Constants.DVID_BOOKING_CREDIT : Constants.DVID_BOOKING_DEBIT),
									isInternalRadioButtonGroup.getValue().equals("Both") ? null : (isInternalRadioButtonGroup.getValue().equals("Real Only") ? false : true)
									)
							);
					notification = Notification.show("No. of ISIN ctions fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					isinActionsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});
		
		return formLayout;
	}
	
	public Grid<IsinActionVO> getIsinActionsGrid() {
		return isinActionsGrid;
	}
		
}
