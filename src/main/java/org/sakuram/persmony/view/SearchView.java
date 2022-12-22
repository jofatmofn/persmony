package org.sakuram.persmony.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.MoneyTransactionService;
import org.sakuram.persmony.service.SearchService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecFEVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SearchCriterionFEVO;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
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
		Map<String, FieldSpecFEVO> fieldSpecMap;
		Select<String> fieldNameSelect, operatorSelect;
		Grid<SearchCriterionFEVO> searchCriteriaGrid;
		Binder<SearchCriterionFEVO> searchCriteriaBinder;
		Editor<SearchCriterionFEVO> criterionEditor;
		Grid.Column<SearchCriterionFEVO> fieldNameColumn, operatorColumn, valuesCSVColumn;
		GridListDataView<SearchCriterionFEVO> searchCriteriaGridLDV;
		Button addButton, searchButton;
		TextField valuesDummyTextField;
		List<SearchCriterionFEVO> searchCriterionFEVOList;
		
		fieldSpecMap = new HashMap<String, FieldSpecFEVO>() {
			private static final long serialVersionUID = 1L;

			{
				put("is_closed", new FieldSpecFEVO("Is Closed?", FieldSpecFEVO.UiControl.CHECKBOX, null, null, null, null));
				put("product_end_date", new FieldSpecFEVO("Product End Date", null, true, null, null, null));
				put("investor_fk", new FieldSpecFEVO("Investor", FieldSpecFEVO.UiControl.SELECT, null, null, true, Constants.CATEGORY_INVESTOR));
			}
		};

		addButton = new Button("Add Row");
		add(addButton);
		searchCriteriaGrid = new Grid<>(SearchCriterionFEVO.class, false);
		searchCriterionFEVOList = new ArrayList<SearchCriterionFEVO>();
		searchCriteriaGridLDV = searchCriteriaGrid.setItems(searchCriterionFEVOList);
		add(searchCriteriaGrid);
		searchButton = new Button("Search");
		add(searchButton);
		
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add Row
		addButton.addClickListener(event -> {
			try {
				searchCriteriaGridLDV.addItem(new SearchCriterionFEVO());
				System.out.println("Length: " + searchCriteriaGridLDV.getItemCount());
			} finally {
				addButton.setEnabled(true);
			}
		});

		fieldNameColumn = searchCriteriaGrid.addColumn(SearchCriterionFEVO::getFieldName).setHeader("Field name");
		operatorColumn = searchCriteriaGrid.addColumn(SearchCriterionFEVO::getOperator).setHeader("Operator");
		valuesCSVColumn = searchCriteriaGrid.addColumn(SearchCriterionFEVO::getValuesCSV).setHeader("Values");
		searchCriteriaGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		
		searchCriteriaBinder = new Binder<>(SearchCriterionFEVO.class);
		criterionEditor = searchCriteriaGrid.getEditor();
		criterionEditor.setBinder(searchCriteriaBinder);

		fieldNameSelect = new Select<String>();
		operatorSelect = new Select<String>();
		valuesDummyTextField = new TextField();
		addCloseHandler(valuesDummyTextField, criterionEditor);
		searchCriteriaBinder.forField(valuesDummyTextField)
			.asRequired("Values must not be empty")
			.bind(SearchCriterionFEVO::getValuesCSV, SearchCriterionFEVO::setValuesCSV);
		valuesCSVColumn.setEditorComponent(valuesDummyTextField);
		valuesDummyTextField.setVisible(true);
		
		fieldNameSelect.setItems(fieldSpecMap.keySet());	// TODO: Label instead of field name.
		addCloseHandler(fieldNameSelect, criterionEditor);
		searchCriteriaBinder.forField(fieldNameSelect)
			.asRequired("Field name must not be empty")
			.bind(SearchCriterionFEVO::getFieldName, SearchCriterionFEVO::setFieldName);
		fieldNameColumn.setEditorComponent(fieldNameSelect);
		fieldNameSelect.addValueChangeListener(event -> {
			FieldSpecFEVO fieldSpecFEVO;
			List<IdValueVO> idValueVOList;
			Select<String> valueDvSelect = null;
			TextField valuesTextField = null;
			
			if (fieldNameSelect.getValue() == null) {
				return;
			}
			System.out.println("Value Changed to: " + fieldNameSelect.getValue());
			/* searchCriteriaBinder.removeBinding(valuesTextField);
			searchCriteriaBinder.removeBinding(valueDvSelect); */
			fieldSpecFEVO = fieldSpecMap.get(fieldNameSelect.getValue());
			if (fieldSpecFEVO.getIsDvSelect() == null || !fieldSpecFEVO.getIsDvSelect()) {
				valuesTextField = new TextField();
				addCloseHandler(valuesTextField, criterionEditor);
				searchCriteriaBinder.forField(valuesTextField)
					.asRequired("Values must not be empty")
					.bind(SearchCriterionFEVO::getValuesCSV, SearchCriterionFEVO::setValuesCSV);
				valuesCSVColumn.setEditorComponent(valuesTextField);
				System.out.println("Setting Editor to TextField");
			}
			if (fieldSpecFEVO.getIsSequencable() != null && fieldSpecFEVO.getIsSequencable()) {
				operatorSelect.setItems(Arrays.stream(FieldSpecFEVO.SeqOperator.values()).map(Enum::name).toArray(String[]::new));
			} else if (fieldSpecFEVO.getIsFreeText() != null && fieldSpecFEVO.getIsFreeText()) {
				operatorSelect.setItems(Arrays.stream(FieldSpecFEVO.TxtOperator.values()).map(Enum::name).toArray(String[]::new));
			} else if (fieldSpecFEVO.getIsDvSelect() != null && fieldSpecFEVO.getIsDvSelect()) {
				operatorSelect.setItems("IN");
				valueDvSelect = new Select<String>();
				idValueVOList = miscService.fetchDvsOfCategory(fieldSpecFEVO.getDvCategory());
				valueDvSelect.setItems(idValueVOList.stream().map(IdValueVO::getValue).collect(Collectors.toList()));
				valueDvSelect.setPlaceholder("Select " + fieldSpecFEVO.getLabel());
				addCloseHandler(valueDvSelect, criterionEditor);
				searchCriteriaBinder.forField(valueDvSelect)
					.asRequired("Values must not be empty")
					.bind(SearchCriterionFEVO::getValuesCSV, SearchCriterionFEVO::setValuesCSV);
				valuesCSVColumn.setEditorComponent(valueDvSelect);
				System.out.println("Setting Editor to DropDown");
			} else {
				operatorSelect.setItems("EQ");
			}

			criterionEditor.refresh();	// Required for the editor component of values to take effect immediately
			
		});

		addCloseHandler(operatorSelect, criterionEditor);
		searchCriteriaBinder.forField(operatorSelect)
			.asRequired("Operator must not be empty")
			.bind(SearchCriterionFEVO::getOperator, SearchCriterionFEVO::setOperator);
		operatorColumn.setEditorComponent(operatorSelect);

		searchCriteriaGrid.addItemDoubleClickListener(e -> {
			criterionEditor.editItem(e.getItem());
			System.out.println("Double Clicked for Edit: " + e.getItem().getFieldName());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable) {
		        ((Focusable) editorComponent).focus();
		    }
		});
		
		searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		searchButton.setDisableOnClick(true);
		// On click of Search
		searchButton.addClickListener(event -> {
			List<Object[]> recordList = null;
			try {
				// Validation
				// Back-end Call
				try {
	    			recordList = searchService.searchInvestments(searchCriterionFEVOList);
				} catch (Exception e) {
					showError(UtilFuncs.messageFromException(e));
					e.printStackTrace();
					return;
				}
				System.out.println("No. of investments fetched: " + recordList.size());
				
			/* } catch (Exception e) {
				showError("System Error!!! Contact Support.");
				return; */
			} finally {
				searchButton.setEnabled(true);
			}
		});
	}
	
    private static void addCloseHandler(Component criteriaField,
            Editor<SearchCriterionFEVO> editor) {
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
