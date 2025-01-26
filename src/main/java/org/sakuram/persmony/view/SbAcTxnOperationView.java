package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.SbAcTxnService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("sat")
public class SbAcTxnOperationView extends Div {

	private static final long serialVersionUID = -335915836819765125L;

	SbAcTxnService sbAcTxnService;
	MiscService miscService;
	
	public SbAcTxnOperationView(SbAcTxnService sbAcTxnService, MiscService miscService) {
		Span selectSpan;
		FormLayout formLayout;
		Select<Map.Entry<Integer,String>> operationSelect;
		List<Map.Entry<Integer, String>> operationItemsList;
		
		this.sbAcTxnService = sbAcTxnService;
		this.miscService = miscService;

		operationItemsList = new ArrayList<Map.Entry<Integer,String>>() {
			private static final long serialVersionUID = 1L;

			{
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Load"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "Categorise"));
			}
		};
		selectSpan = new Span();
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1));
		
		operationSelect = new Select<Map.Entry<Integer,String>>();
		operationSelect.setItems(operationItemsList);
		operationSelect.setItemLabelGenerator(operationItem -> {
			return operationItem.getValue();
		});
		operationSelect.setLabel("Operation");
		operationSelect.setPlaceholder("Select Operation");
		operationSelect.addValueChangeListener(event -> {
			formLayout.remove(formLayout.getChildren().collect(Collectors.toList()));
			try {
	            switch(event.getValue().getKey()) {
	            case 1:
	            	// handleSbAcTxnLoad(formLayout);
	            	break;
	            case 2:
	            	handleSbAcTxnCategorise(formLayout);
	            	break;
	            }
			} catch (Exception e) {
				ViewFuncs.showError("System Error!!! Contact Support.");
				e.printStackTrace();
				return;
			}
        });

		selectSpan.add(operationSelect);
		add(selectSpan);
		add(formLayout);
	}
	
	private void handleSbAcTxnCategorise(FormLayout formLayout) {
		DatePicker sbAcTxnFromDatePicker, sbAcTxnToDatePicker;
		NumberField sbAcTxnFromAmoutNumberField, sbAcTxnToAmoutNumberField;
		TextField narrationTextField, endAccountReferenceTextField;
		Select<IdValueVO> bankAccountDvSelect, transactionCategoryDvSelect;
		RadioButtonGroup<String> bookingRadioButtonGroup;
		HorizontalLayout hLayout;
		Select<IdValueVO> narrationOperatorSelect, endAccountReferenceOperatorSelect;
		Button fetchButton;
		Grid<SavingsAccountTransactionVO> savingsAccountTransactionsGrid;
		
		sbAcTxnFromDatePicker = new DatePicker("From");
		sbAcTxnToDatePicker = new DatePicker("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Period");
		hLayout.add(sbAcTxnFromDatePicker, sbAcTxnToDatePicker);
		
		sbAcTxnFromAmoutNumberField = new NumberField("From");
		sbAcTxnToAmoutNumberField = new NumberField("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Amount");
		hLayout.add(sbAcTxnFromAmoutNumberField, sbAcTxnToAmoutNumberField);

		narrationOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		narrationTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Narration");
		hLayout.add(narrationOperatorSelect, narrationTextField);
		
		bankAccountDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT, null, true, false);
		formLayout.addFormItem(bankAccountDvSelect, "Account");
		
		bookingRadioButtonGroup = new RadioButtonGroup<String>();
		bookingRadioButtonGroup.setItems("Both", "Credit Only", "Debit Only");
		bookingRadioButtonGroup.setValue("Both");
		formLayout.addFormItem(bookingRadioButtonGroup, "Booking");
		
		transactionCategoryDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CATEGORY, null, true, true);
		formLayout.addFormItem(transactionCategoryDvSelect, "Transaction Category");
		
		endAccountReferenceOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		endAccountReferenceTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "End Account Reference");
		hLayout.add(endAccountReferenceOperatorSelect, endAccountReferenceTextField);
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		savingsAccountTransactionsGrid = new Grid<>(SavingsAccountTransactionVO.class);
		savingsAccountTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "bankAccount.value", "transactionDate", "narration", "booking.value", "amount", "balance", "valueDate", "reference", "transactionId", "utrNumber", "remitterBranch", "transactionCode.value", "branchCode", "transactionTime", "costCenter.value", "voucherType.value", "transactionCategory.value", "endAccountReference");
		// savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "bankAccount.value", "transactionDate", "narration", "booking.value", "amount", "balance", "valueDate", "reference", "transactionId", "utrNumber", "remitterBranch", "branchCode", "transactionTime", "transactionCategory == null ? \"\" : transactionCategory.value", "end_account_reference");
		for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionsGrid.getColumns()) {
			column.setResizable(true);
		}
		formLayout.add(savingsAccountTransactionsGrid);
		
		// On click of Fetch
		fetchButton.addClickListener(event -> {
			SbAcTxnCriteriaVO sbAcTxnCriteriaVO;
			List<SavingsAccountTransactionVO> recordList = null;
			Notification notification;

			try {
				// Validation
				if (sbAcTxnFromDatePicker.getValue() != null && sbAcTxnToDatePicker.getValue() != null &&
						Date.valueOf(sbAcTxnFromDatePicker.getValue()).after(Date.valueOf(sbAcTxnToDatePicker.getValue()))) {
					ViewFuncs.showError("From Date cannot be after the To Date");
					return;
				}
				if (sbAcTxnFromAmoutNumberField.getValue() != null && sbAcTxnToAmoutNumberField.getValue() != null &&
						sbAcTxnFromAmoutNumberField.getValue() > sbAcTxnToAmoutNumberField.getValue()) {
					ViewFuncs.showError("From Amount cannot be greater than the To Amount");
					return;
				}
				if (!narrationTextField.isEmpty() && narrationOperatorSelect.getValue() == null) {
					ViewFuncs.showError("Specify Operator for Narration");
					return;
				}
				if (narrationTextField.isEmpty() && narrationOperatorSelect.getValue() != null) {
					ViewFuncs.showError("Specify Value for Narration");
					return;
				}
				if (transactionCategoryDvSelect.getValue() != null && transactionCategoryDvSelect.getValue().equals("Empty") && !endAccountReferenceTextField.isEmpty()) {
					ViewFuncs.showError("End Account Reference can be specified only when Transaction Category is not 'Empty'");
					return;
				}
				if (!endAccountReferenceTextField.isEmpty() && endAccountReferenceOperatorSelect.getValue() == null) {
					ViewFuncs.showError("Specify Operator for End Account Reference");
					return;
				}
				if (endAccountReferenceTextField.isEmpty() && endAccountReferenceOperatorSelect.getValue() != null) {
					ViewFuncs.showError("Specify Value for End Account Reference");
					return;
				}
				
				// Back-end Call
				sbAcTxnCriteriaVO = new SbAcTxnCriteriaVO(
						sbAcTxnFromDatePicker.getValue() == null ? null : Date.valueOf(sbAcTxnFromDatePicker.getValue()),
						sbAcTxnToDatePicker.getValue() == null ? null : Date.valueOf(sbAcTxnToDatePicker.getValue()),
						sbAcTxnFromAmoutNumberField.getValue() == null ? null : (double)sbAcTxnFromAmoutNumberField.getValue().doubleValue(),
						sbAcTxnToAmoutNumberField.getValue() == null ? null : (double)sbAcTxnToAmoutNumberField.getValue().doubleValue(),
						narrationTextField.getValue().equals("") ? null : narrationTextField.getValue(),
						narrationOperatorSelect.getValue() == null ? null : narrationOperatorSelect.getValue().getValue(),
						bankAccountDvSelect.getValue() == null ? null : bankAccountDvSelect.getValue().getId(),
						bookingRadioButtonGroup.getValue().equals("Both") ? null : (bookingRadioButtonGroup.getValue().equals("Credit Only") ? 222L : 223),
						transactionCategoryDvSelect.getValue() == null ? null : transactionCategoryDvSelect.getValue().getId(),
						endAccountReferenceTextField.getValue().equals("") ? null : endAccountReferenceTextField.getValue(),
						endAccountReferenceOperatorSelect.getValue() == null ? null : endAccountReferenceOperatorSelect.getValue().getValue()
						);
				try {
					recordList = sbAcTxnService.searchSavingsAccountTransactions(sbAcTxnCriteriaVO);
					notification = Notification.show("No. of Savings Account Transactions fetched: " + recordList.size());
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					savingsAccountTransactionsGrid.setItems(recordList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				fetchButton.setEnabled(true);
			}
		});

		savingsAccountTransactionsGrid.addItemDoubleClickListener(event -> {
			acceptSbAcTxnCategory(event.getItem().getSavingsAccountTransactionId(), event.getItem().getAmount());
		});
		
	}
	
	private void acceptSbAcTxnCategory(long savingsAccountTransactionId, Double sbAcTxnAmount) {
		Dialog dialog;
		VerticalLayout verticalLayout;
		HorizontalLayout hLayout;
		Button addButton, closeButton, returnButton, saveButton;
		Grid<SbAcTxnCategoryVO> sbAcTxnCategoryGrid;
		Binder<SbAcTxnCategoryVO> sbAcTxnCategoryBinder;
		Editor<SbAcTxnCategoryVO> sbAcTxnCategoryEditor;
		GridListDataView<SbAcTxnCategoryVO> sbAcTxnCategoryGridLDV;
		List<SbAcTxnCategoryVO> sbAcTxnCategoryVOList;
		Select<IdValueVO> transactionCategoryDvSelect;
		TextField endAccountReferenceTextField;
		NumberField amountNumberField;
		Grid.Column<SbAcTxnCategoryVO> transactionCategoryColumn, endAccountReferenceColumn, amountColumn;
		
		sbAcTxnCategoryVOList = sbAcTxnService.fetchSbAcTxnCategories(savingsAccountTransactionId);
		
		dialog = new Dialog();
		dialog.setHeaderTitle("Categories");
		closeButton = new Button(new Icon("lumo", "cross"),
		        (e) -> {
		        	dialog.close();
		        });
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		dialog.getHeader().add(closeButton);
		verticalLayout = new VerticalLayout();
		verticalLayout.getStyle().set("width", "75rem");
		dialog.add(verticalLayout);
		
		sbAcTxnCategoryGrid = new Grid<>(SbAcTxnCategoryVO.class, false);
		sbAcTxnCategoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		sbAcTxnCategoryGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		sbAcTxnCategoryGridLDV = sbAcTxnCategoryGrid.setItems(sbAcTxnCategoryVOList);
		
		verticalLayout.add(sbAcTxnCategoryGrid);
		
		hLayout = new HorizontalLayout();
		verticalLayout.add(hLayout);
		addButton = new Button("Add Row");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setDisableOnClick(true);
		// On click of Add Row
		addButton.addClickListener(event -> {
			try {
				sbAcTxnCategoryGridLDV.addItem(new SbAcTxnCategoryVO());
			} finally {
				addButton.setEnabled(true);
			}
		});
		hLayout.add(addButton);

		saveButton = new Button("Save");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(saveButton);
		saveButton.setDisableOnClick(true);
		saveButton.addClickListener(event -> {
			try {
				Double totalAmount;
				
				for (int i = 0; i < sbAcTxnCategoryVOList.size(); i++) {
					SbAcTxnCategoryVO sbAcTxnCategoryVO = sbAcTxnCategoryVOList.get(i);
					if (sbAcTxnCategoryVO.getSbAcTxnCategoryId() == null && sbAcTxnCategoryVO.getTransactionCategory() == null &&
							sbAcTxnCategoryVO.getEndAccountReference() == null && sbAcTxnCategoryVO.getAmount() == null) {
						sbAcTxnCategoryGridLDV.removeItem(sbAcTxnCategoryVO);
						i--;
					}
				}
				totalAmount = 0D;
				// Validations
				for (SbAcTxnCategoryVO sbAcTxnCategoryVO : sbAcTxnCategoryVOList) {
					if (sbAcTxnCategoryVO.getTransactionCategory() == null || sbAcTxnCategoryVO.getAmount() == null || sbAcTxnCategoryVO.getAmount() == 0) {
						ViewFuncs.showError("Transaction Category and Amount cannot be empty");
						return;
					}
					totalAmount += sbAcTxnCategoryVO.getAmount();
				}
				if (!totalAmount.equals(sbAcTxnAmount)) {
					ViewFuncs.showError("Total of Category-wise amounts should match the SB A/c Txn. Amount " + sbAcTxnAmount);
					return;
				}
				try {
					sbAcTxnService.saveSbAcTxnCategories(savingsAccountTransactionId, sbAcTxnCategoryVOList);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				saveButton.setEnabled(true);
			}
			dialog.close();
		});

		returnButton = new Button("Return");
		returnButton.addClickListener(event -> {
			dialog.close();
		});
		returnButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		hLayout.add(returnButton);

		sbAcTxnCategoryGrid.addColumn("sbAcTxnCategoryId").setHeader("SAT Category Id");
		transactionCategoryColumn = sbAcTxnCategoryGrid.addColumn("transactionCategory.value").setHeader("Transaction Category");
		endAccountReferenceColumn = sbAcTxnCategoryGrid.addColumn("endAccountReference").setHeader("End Account Reference");
		amountColumn = sbAcTxnCategoryGrid.addColumn("amount").setHeader("Amount");
		sbAcTxnCategoryGrid.addComponentColumn(sbAcTxnCategoryVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e->{
				sbAcTxnCategoryGridLDV.removeItem(sbAcTxnCategoryVO);
			});
			return delButton;
		}).setWidth("120px").setFlexGrow(0);
		
		sbAcTxnCategoryBinder = new Binder<>(SbAcTxnCategoryVO.class);
		sbAcTxnCategoryEditor = sbAcTxnCategoryGrid.getEditor();
		sbAcTxnCategoryEditor.setBinder(sbAcTxnCategoryBinder);
		
		transactionCategoryDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CATEGORY, null, false, false);
		addCloseHandler(transactionCategoryDvSelect, sbAcTxnCategoryEditor);
		sbAcTxnCategoryBinder.forField(transactionCategoryDvSelect)
			.bind(SbAcTxnCategoryVO::getTransactionCategory, SbAcTxnCategoryVO::setTransactionCategory);
		transactionCategoryColumn.setEditorComponent(transactionCategoryDvSelect);
		
		amountNumberField = new NumberField();
		addCloseHandler(amountNumberField, sbAcTxnCategoryEditor);
		sbAcTxnCategoryBinder.forField(amountNumberField)
			.bind(SbAcTxnCategoryVO::getAmount, SbAcTxnCategoryVO::setAmount);
		amountColumn.setEditorComponent(amountNumberField);
		
		endAccountReferenceTextField = new TextField();
		addCloseHandler(endAccountReferenceTextField, sbAcTxnCategoryEditor);
		sbAcTxnCategoryBinder.forField(endAccountReferenceTextField)
			.bind(SbAcTxnCategoryVO::getEndAccountReference, SbAcTxnCategoryVO::setEndAccountReference);
		endAccountReferenceColumn.setEditorComponent(endAccountReferenceTextField);
		
		sbAcTxnCategoryGrid.addItemDoubleClickListener(e -> {
			sbAcTxnCategoryEditor.editItem(e.getItem());
		    Component editorComponent = e.getColumn().getEditorComponent();
		    if (editorComponent instanceof Focusable<?>) {
		        ((Focusable<?>) editorComponent).focus();
		    }
		});
		
		dialog.open();
	}
	
    private static void addCloseHandler(Component sbAcTxnCategoryField,
            Editor<SbAcTxnCategoryVO> editor) {
    	sbAcTxnCategoryField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

}
