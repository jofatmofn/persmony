package org.sakuram.persmony.view;

import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.PlanService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.CashFlowVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.view.SatCfCriteriaComponent.SatCfCriteriaConfigs;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
public class CashFlowSearchComponent extends Div {

	private static final long serialVersionUID = -3331256555348621264L;

	PlanService planService;
	MiscService miscService;
	
	SatCfCriteriaComponent satCfCriteriaComponent;
	
	DatePickerI18n isoDatePickerI18n;
	
	Grid<CashFlowVO> cashFlowsGrid;
	
	public CashFlowSearchComponent(PlanService planService, MiscService miscService, SatCfCriteriaComponent satCfCriteriaComponent, DatePickerI18n isoDatePickerI18n) {
		this.planService = planService;
		this.miscService = miscService;
		this.satCfCriteriaComponent = satCfCriteriaComponent;
		this.isoDatePickerI18n = isoDatePickerI18n;
	}
	
	public FormLayout showForm(Long bookingDvId) {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		Button searchButton, clearButton;
		SbAcTxnCriteriaVO cfCriteriaVO;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		cfCriteriaVO = new SbAcTxnCriteriaVO();
		formLayout.add(satCfCriteriaComponent.showForm(cfCriteriaVO, new SatCfCriteriaConfigs(false, bookingDvId)));
		
		hLayout = new HorizontalLayout();
		formLayout.add(hLayout);
		searchButton = new Button("Fetch");
		clearButton = new Button("Clear");
		hLayout.add(searchButton, clearButton);
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.setDisableOnClick(true);
		
		cashFlowsGrid = new Grid<>(CashFlowVO.class);
		cashFlowsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		cashFlowsGrid.setColumns(CashFlowVO.gridColumns());
		for (Column<CashFlowVO> column : cashFlowsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(cashFlowsGrid);
		
		// On click of Fetch
		searchButton.addClickListener(event -> {
			List<CashFlowVO> recordList = null;
			Notification notification;
			
			try {
				// Validation
				try {
					satCfCriteriaComponent.validateInput();
				} catch (AppException e) {
					ViewFuncs.showError(e.getMessage());
					return;
				}

				// Back-end Call
				try {
					recordList = planService.searchSavingsAccountTransactions(cfCriteriaVO);
					notification = Notification.show("No. of Cash Flows fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					cashFlowsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				searchButton.setEnabled(true);
			}
		});
		
		clearButton.addClickListener(event -> {
			satCfCriteriaComponent.clear();
		});
		
		return formLayout;
	}
	
	public Grid<CashFlowVO> getCashFlowsGrid() {
		return cashFlowsGrid;
	}
	
}
