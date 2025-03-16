package org.sakuram.persmony.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.SearchService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.InvestmentDetailsVO;
import org.sakuram.persmony.valueobject.InvestmentTransactionVO;
import org.sakuram.persmony.valueobject.InvestmentVO;
import org.sakuram.persmony.valueobject.RealisationVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SearchCriterionVO;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("search")
public class SearchView extends Div {
	private static final long serialVersionUID = -9072230786957200591L;

	MoneyTransactionService moneyTransactionService;
	MiscService miscService;
	SearchService searchService;

	public SearchView(MoneyTransactionService moneyTransactionService, MiscService miscService, SearchService searchService) {
		Select<String> fieldNameSelect, operatorSelect;
		Grid<SearchCriterionVO> searchCriteriaGrid;
		Binder<SearchCriterionVO> searchCriteriaBinder;
		Editor<SearchCriterionVO> criterionEditor;
		Grid.Column<SearchCriterionVO> fieldNameColumn, operatorColumn, valuesCSVColumn;
		GridListDataView<SearchCriterionVO> searchCriteriaGridLDV;
		Button addButton, searchButton;
		TextField valuesDummyTextField;
		List<SearchCriterionVO> searchCriterionVOList;
		Grid<InvestmentVO> investmentsGrid;
		
		addButton = new Button("Add Row");
		add(addButton);
		searchCriteriaGrid = new Grid<>(SearchCriterionVO.class, false);
		searchCriterionVOList = new ArrayList<SearchCriterionVO>();
		searchCriteriaGridLDV = searchCriteriaGrid.setItems(searchCriterionVOList);
		add(searchCriteriaGrid);
		searchButton = new Button("Search");
		add(searchButton);
		investmentsGrid = new Grid<>(InvestmentVO.class);
		investmentsGrid.setColumns("investmentId", "investor", "productProvider", "providerBranch", "investmentIdWithProvider", "investorIdWithProvider", "productType", "productName", "productIdOfProvider", "rateOfInterest", "dematAccount", "units", "worth", "cleanPrice", "accruedInterest", "charges", "taxability", "isAccrualApplicable", "investmentStartDate", "investmentEndDate", "dynamicReceiptPeriodicity", "previousInvestment", "newInvestmentReason", "closed", "closureDate", "closureType");
		for (Column<InvestmentVO> column : investmentsGrid.getColumns()) {
			column.setResizable(true);
		}
		add(investmentsGrid);
		
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add Row
		addButton.addClickListener(event -> {
			try {
				searchCriteriaGridLDV.addItem(new SearchCriterionVO());
				System.out.println("Length: " + searchCriteriaGridLDV.getItemCount());
			} finally {
				addButton.setEnabled(true);
			}
		});

		fieldNameColumn = searchCriteriaGrid.addColumn(SearchCriterionVO::getFieldName).setHeader("Field name");
		operatorColumn = searchCriteriaGrid.addColumn(SearchCriterionVO::getOperator).setHeader("Operator");
		valuesCSVColumn = searchCriteriaGrid.addColumn(SearchCriterionVO::getValuesCSV).setHeader("Values");
		searchCriteriaGrid.addComponentColumn(searchCriterionVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e->{
				searchCriteriaGridLDV.removeItem(searchCriterionVO);
			});
			return delButton;
		}).setWidth("120px").setFlexGrow(0);
		searchCriteriaGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		
		searchCriteriaBinder = new Binder<>(SearchCriterionVO.class);
		criterionEditor = searchCriteriaGrid.getEditor();
		criterionEditor.setBinder(searchCriteriaBinder);

		fieldNameSelect = new Select<String>();
		operatorSelect = new Select<String>();
		valuesDummyTextField = new TextField();
		addCloseHandler(valuesDummyTextField, criterionEditor);
		searchCriteriaBinder.forField(valuesDummyTextField)
			.bind(SearchCriterionVO::getValuesCSV, SearchCriterionVO::setValuesCSV);
		valuesCSVColumn.setEditorComponent(valuesDummyTextField);
		valuesDummyTextField.setVisible(true);
		
		fieldNameSelect.setItems(Constants.SEARCH_FIELD_SPEC_MAP.keySet());	// TODO: Label instead of field name.
		addCloseHandler(fieldNameSelect, criterionEditor);
		searchCriteriaBinder.forField(fieldNameSelect)
			.bind(SearchCriterionVO::getFieldName, SearchCriterionVO::setFieldName);
		fieldNameColumn.setEditorComponent(fieldNameSelect);
		fieldNameSelect.addValueChangeListener(event -> {
			FieldSpecVO fieldSpecVO;
			List<IdValueVO> idValueVOList;
			Select<String> valueDvSelect = null;
			TextField valuesTextField = null;
			
			if (fieldNameSelect.getValue() == null) {
				return;
			}
			System.out.println("Value Changed to: " + fieldNameSelect.getValue());
			fieldSpecVO = Constants.SEARCH_FIELD_SPEC_MAP.get(fieldNameSelect.getValue());
			operatorSelect.setItems(new String[0]);
			if (fieldSpecVO.getIsDvSelect() == null || !fieldSpecVO.getIsDvSelect()) {
				valuesTextField = new TextField();
				addCloseHandler(valuesTextField, criterionEditor);
				searchCriteriaBinder.forField(valuesTextField)
					.bind(SearchCriterionVO::getValuesCSV, SearchCriterionVO::setValuesCSV);
				valuesCSVColumn.setEditorComponent(valuesTextField);
				System.out.println("Setting Editor to TextField");
			}
			
			if (fieldSpecVO.getIsSequencable() != null && fieldSpecVO.getIsSequencable()) {
				operatorSelect.setItems(Arrays.stream(FieldSpecVO.SeqOperator.values()).map(Enum::name).toArray(String[]::new));
			} else if (fieldSpecVO.getIsFreeText() != null && fieldSpecVO.getIsFreeText()) {
				operatorSelect.setItems(Arrays.stream(FieldSpecVO.TxtOperator.values()).map(Enum::name).toArray(String[]::new));
			} else if (fieldSpecVO.getIsDvSelect() != null && fieldSpecVO.getIsDvSelect()) {
				valueDvSelect = new Select<String>();
				idValueVOList = miscService.fetchDvsOfCategory(fieldSpecVO.getDvCategory(), false);
				valueDvSelect.setItems(idValueVOList.stream().map(IdValueVO::getValue).collect(Collectors.toList()));
				valueDvSelect.setPlaceholder("Select " + fieldSpecVO.getLabel());
				addCloseHandler(valueDvSelect, criterionEditor);
				searchCriteriaBinder.forField(valueDvSelect)
					.bind(SearchCriterionVO::getValuesCSV, SearchCriterionVO::setValuesCSV);
				valuesCSVColumn.setEditorComponent(valueDvSelect);
				System.out.println("Setting Editor to DropDown");
			} else {
			}

			criterionEditor.refresh();	// Required for the editor component of values to take effect immediately
			
		});

		addCloseHandler(operatorSelect, criterionEditor);
		searchCriteriaBinder.forField(operatorSelect)
			.bind(SearchCriterionVO::getOperator, SearchCriterionVO::setOperator);
		operatorColumn.setEditorComponent(operatorSelect);

		searchCriteriaGrid.addItemDoubleClickListener(e -> {
			criterionEditor.editItem(e.getItem());
			System.out.println("Double Clicked for Edit: " + e.getItem().getFieldName());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable<?>) {
		        ((Focusable<?>) editorComponent).focus();
		    }
		});
		
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.setDisableOnClick(true);
		// On click of Search
		searchButton.addClickListener(event -> {
			List<InvestmentVO> recordList = null;
			Notification notification;
			try {
				// Validation
				// Back-end Call
				try {
	    			recordList = searchService.searchInvestments(searchCriterionVOList);
	    			investmentsGrid.setItems(recordList);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
					return;
				}
				notification = Notification.show("No. of investments fetched: " + recordList.size());
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				
			} catch (Exception e) {
				showError("System Error!!! Contact Support.");
				return;
			} finally {
				searchButton.setEnabled(true);
			}
		});
		
		investmentsGrid.addItemDoubleClickListener(event -> {
			InvestmentDetailsVO investmentDetailsVO;
			Notification notification;
			Dialog dialog;
			VerticalLayout verticalLayout;
			Grid<InvestmentTransactionVO> investmentTransactionsGrid;
			Grid<RealisationVO> realisationGrid;
			Grid<SavingsAccountTransactionVO> savingsAccountTransactionGrid;
			Button closeButton;
			
			try {
				dialog = new Dialog();
				dialog.setHeaderTitle("Investment Details");
				closeButton = new Button(new Icon("lumo", "cross"),
				        (e) -> dialog.close());
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				dialog.getHeader().add(closeButton);
				verticalLayout = new VerticalLayout();
				verticalLayout.getStyle().set("width", "90rem");
				dialog.add(verticalLayout);
				
				investmentTransactionsGrid = new Grid<>(InvestmentTransactionVO.class);
				investmentTransactionsGrid.setColumns("investmentTransactionId", "transactionType", "dueDate", "assessmentYear", "dueAmount", "status", "settledAmount", "returnedPrincipalAmount", "interestAmount", "tdsAmount", "accrualTdsReference", "taxGroup");
				for (Column<InvestmentTransactionVO> column : investmentTransactionsGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add("Investment Transactions");
				verticalLayout.add(investmentTransactionsGrid);
				
				realisationGrid = new Grid<>(RealisationVO.class);
				realisationGrid.setColumns("realisationId", "investmentTransactionId", "realisationDate", "realisationType", "detailsReference", "amount", "returnedPrincipalAmount", "interestAmount", "tdsAmount", "tdsReference");
				for (Column<RealisationVO> column : realisationGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add("Realisations");
				verticalLayout.add(realisationGrid);
				
				savingsAccountTransactionGrid = new Grid<>(SavingsAccountTransactionVO.class);
				savingsAccountTransactionGrid.setColumns("savingsAccountTransactionId", "bankAccountOrInvestor.value", "transactionDate", "amount");
				for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionGrid.getColumns()) {
					column.setResizable(true);
				}
				verticalLayout.add("Savings Account Transactions");
				verticalLayout.add(savingsAccountTransactionGrid);
				
    			investmentDetailsVO = moneyTransactionService.fetchInvestmentDetails(event.getItem().getInvestmentId());
    			
    			investmentTransactionsGrid.setItems(investmentDetailsVO.getInvestmentTransactionVOList());
    			realisationGrid.setItems(investmentDetailsVO.getRealisationVOList());
    			savingsAccountTransactionGrid.setItems(investmentDetailsVO.getSavingsAccountTransactionVOList());
    			
    			dialog.open();
			} catch (Exception e) {
				showError(UtilFuncs.messageFromException(e));
				return;
			}
			notification = Notification.show("No. of investment transactions: " + investmentDetailsVO.getInvestmentTransactionVOList().size() +
					"\nNo. of realisations: " + investmentDetailsVO.getRealisationVOList().size() +
					"\nNo. of savings account transactions: " + investmentDetailsVO.getSavingsAccountTransactionVOList().size());
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});
		
	}
	
    private static void addCloseHandler(Component criteriaField,
            Editor<SearchCriterionVO> editor) {
    	criteriaField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

	private void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		errorDialog.open();
		
	}
}
