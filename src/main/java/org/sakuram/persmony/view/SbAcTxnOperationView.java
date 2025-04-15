package org.sakuram.persmony.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.service.SbAcTxnService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.util.UtilFuncs;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SavingsAccountTransactionVO;
import org.sakuram.persmony.valueobject.SbAcTxnCategoryVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.valueobject.SbAcTxnImportStatsVO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
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
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
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
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import lombok.Getter;
import lombok.Setter;

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
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(1, "Import"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(2, "View/Categorise"));
				add(new AbstractMap.SimpleImmutableEntry<Integer, String>(3, "Create"));
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
	            	handleSbAcTxnImport(formLayout);
	            	break;
	            case 2:
	            	handleSbAcTxnCategorise(formLayout);
	            	break;
	            case 3:
	            	handleSbAcTxnCreate(formLayout);
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
	
	private void handleSbAcTxnCreate(FormLayout formLayout) {
		Select<IdValueVO> bankAccountOrInvestorDvSelect, bookingDvSelect, transactionCodeDvSelect, costCenterDvSelect, voucherTypeDvSelect;
		DatePicker transactionDateDatePicker, valueDateDatePicker;
		NumberField amountNumberField, balanceNumberField;
		TextField referenceTextField, narrationTextField, transactionIdTextField, utrNumberTextField, remitterBranchTextField, transactionTimeTextField;
		IntegerField branchCodeIntegerField;
		HorizontalLayout hLayout;
		Button saveButton;
		
		// UI Elements
		bankAccountOrInvestorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT + "+" + Constants.CATEGORY_PRIMARY_INVESTOR, null, false, false);
		formLayout.addFormItem(bankAccountOrInvestorDvSelect, "Account");
		
		transactionDateDatePicker = new DatePicker("Transaction");
		valueDateDatePicker = new DatePicker("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Date");
		hLayout.add(transactionDateDatePicker, valueDateDatePicker);
		
		amountNumberField = new NumberField("Txn. Amount");
		balanceNumberField = new NumberField("Balance");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Amount");
		hLayout.add(amountNumberField, balanceNumberField);

		bookingDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_BOOKING, null, false, false);
		formLayout.addFormItem(bookingDvSelect, "Booking");
		
		narrationTextField = new TextField();
		formLayout.addFormItem(narrationTextField, "Narration");
		
		referenceTextField = new TextField();
		formLayout.addFormItem(referenceTextField, "Reference");
		
		transactionIdTextField = new TextField();
		formLayout.addFormItem(transactionIdTextField, "Transaction Id");
		
		utrNumberTextField = new TextField();
		formLayout.addFormItem(utrNumberTextField, "UTR Number");
		
		remitterBranchTextField = new TextField();
		formLayout.addFormItem(remitterBranchTextField, "Remitter Branch");
		
		transactionCodeDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CODE, null, true, false);
		formLayout.addFormItem(transactionCodeDvSelect, "Transaction Code");
		
		branchCodeIntegerField = new IntegerField();
		formLayout.addFormItem(branchCodeIntegerField, "Branch Code");
		
		transactionTimeTextField = new TextField();
		formLayout.addFormItem(transactionTimeTextField, "Transaction Time");
		
		costCenterDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_COST_CENTER, null, true, false);
		formLayout.addFormItem(costCenterDvSelect, "Cost Center");
		
		voucherTypeDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_VOUCHER_TYPE, null, true, false);
		formLayout.addFormItem(voucherTypeDvSelect, "Voucher Type");
		
		saveButton = new Button("Save");
		formLayout.add(saveButton);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.setDisableOnClick(true);
		// On click of Save
		saveButton.addClickListener(event -> {
			Notification notification;
			SavingsAccountTransactionVO savingsAccountTransactionVO;
			
			try {
				// Validation
				if (transactionDateDatePicker.getValue() == null) {
					ViewFuncs.showError("Transaction Date cannot be Empty");
					return;
				}
				if (bookingDvSelect.getValue() == null) {
					ViewFuncs.showError("Booking cannot be Empty");
					return;
				}
				if (amountNumberField.getValue() == null || amountNumberField.getValue() == 0) {
					ViewFuncs.showError("Transaction Amount cannot be Empty");
					return;
				}
				if (narrationTextField.getValue() == null || narrationTextField.getValue().trim().equals("")) {
					ViewFuncs.showError("Narration cannot be Empty");
					return;
				}
				if (bankAccountOrInvestorDvSelect.getValue() != null && balanceNumberField.getValue() == null ||
						bankAccountOrInvestorDvSelect.getValue() == null && balanceNumberField.getValue() != null) {
					ViewFuncs.showError("Balance is applicable (only) for a banking Transaction");
					return;
				}
				
				// Back-end Call
				savingsAccountTransactionVO =  new SavingsAccountTransactionVO(
						Constants.DVID_EMPTY_SELECT,
						bankAccountOrInvestorDvSelect.getValue() == null ? null : new IdValueVO(bankAccountOrInvestorDvSelect.getValue().getId(), null),
						Date.valueOf(transactionDateDatePicker.getValue()),
						(double)amountNumberField.getValue().doubleValue(),
						new IdValueVO(bookingDvSelect.getValue().getId(), null),
						valueDateDatePicker.getValue() == null ? null : Date.valueOf(valueDateDatePicker.getValue()),
						(referenceTextField.getValue() == null || referenceTextField.getValue().equals("") ? null : referenceTextField.getValue()),
						narrationTextField.getValue(),
						(balanceNumberField.getValue() == null ? null : (double)balanceNumberField.getValue().doubleValue()),
						(transactionIdTextField.getValue() == null || transactionIdTextField.getValue().equals("") ? null : transactionIdTextField.getValue()),
						(utrNumberTextField.getValue() == null || utrNumberTextField.getValue().equals("") ? null : utrNumberTextField.getValue()),
						(remitterBranchTextField.getValue() == null || remitterBranchTextField.getValue().equals("") ? null : remitterBranchTextField.getValue()),
						transactionCodeDvSelect.getValue() == null ? null : new IdValueVO(transactionCodeDvSelect.getValue().getId(), null),
						branchCodeIntegerField.getValue() == null ? null : (int)branchCodeIntegerField.getValue().intValue(),
						(transactionTimeTextField.getValue() == null || transactionTimeTextField.getValue().equals("") ? null : transactionTimeTextField.getValue()),
						costCenterDvSelect.getValue() == null ? null : new IdValueVO(costCenterDvSelect.getValue().getId(), null),
						voucherTypeDvSelect.getValue() == null ? null : new IdValueVO(voucherTypeDvSelect.getValue().getId(), null)
				);
				try {
					sbAcTxnService.createSavingsAccountTransaction(savingsAccountTransactionVO);
					notification = Notification.show("Savings Account Transaction Created Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				saveButton.setEnabled(true);
			}
		});
	}
	
	private void handleSbAcTxnImport(FormLayout formLayout) {
		Select<IdValueVO> bankAccountDvSelect;
		FileBuffer fileBuffer;
		Upload upload;
		Button importButton;
		ScopeLocalDummy01 uploadedContents;
		Grid<SavingsAccountTransactionVO> savingsAccountTransactionsGrid;
		
		uploadedContents = new ScopeLocalDummy01();
		
		savingsAccountTransactionsGrid = new Grid<>(SavingsAccountTransactionVO.class);
		savingsAccountTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		savingsAccountTransactionsGrid.setAllRowsVisible(true);
		savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "transactionDate", "narration", "booking.value", "amount", "balance");
		for (Column<SavingsAccountTransactionVO> column : savingsAccountTransactionsGrid.getColumns()) {
			column.setResizable(true);
		}
		
		bankAccountDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT, null, false, false);
		formLayout.addFormItem(bankAccountDvSelect, "Account");
		bankAccountDvSelect.addValueChangeListener(event -> {
			List<SavingsAccountTransactionVO> recordList;
			recordList = new ArrayList<SavingsAccountTransactionVO>(1);
			try {
				recordList.add(sbAcTxnService.fetchLastSavingsAccountTransaction(bankAccountDvSelect.getValue().getId()));
			} catch (Exception e) {
				ViewFuncs.showError(UtilFuncs.messageFromException(e));
				return;
			} finally {
				savingsAccountTransactionsGrid.setItems(recordList);
			}
		});

		formLayout.addFormItem(savingsAccountTransactionsGrid, "Last Transaction");
		
		fileBuffer = new FileBuffer();
		upload = new Upload(fileBuffer);
		formLayout.addFormItem(upload, "Upload CSV");
		upload.setAcceptedFileTypes("text/csv", ".csv");
		upload.addFileRejectedListener(event -> {
			uploadedContents.setMultipartFile(null);
			ViewFuncs.showError(event.getErrorMessage());
		});
		upload.addSucceededListener(event -> {
			DiskFileItem diskFileItem;
			InputStream inputStream;
			OutputStream outputStream;
			int ret;
			
		    try {
		    	diskFileItem = new org.apache.commons.fileupload.disk.DiskFileItem(
		    			"file",
	    				"text/csv",
	    				true,
	    				event.getFileName(),
	    				(int) fileBuffer.getInputStream().available(),
	    				fileBuffer.getFileData().getFile().getParentFile());
		    	// stackoverflow 42253005 commonsmultipartfile-size-is-0
		    	inputStream = fileBuffer.getInputStream();
		    	outputStream = diskFileItem.getOutputStream(); // stackoverflow 4120635 java-lang-nullpointerexception-while-creating-diskfileitem
		    	ret = inputStream.read();
		    	while ( ret != -1 )
		    	{
		    		outputStream.write(ret);
		    	    ret = inputStream.read();
		    	}
		    	outputStream.flush();
		    	uploadedContents.setMultipartFile(new CommonsMultipartFile(diskFileItem));
		    } catch (IOException e) {
				ViewFuncs.showError("File upload failed: " + e.getMessage());
		    }
		});
		
		importButton = new Button("Import CSV");
		formLayout.add(importButton);
		importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		importButton.setDisableOnClick(true);

		// On click of Fetch
		importButton.addClickListener(event -> {
			Notification notification;
			SbAcTxnImportStatsVO sbAcTxnImportStatsVO;

			try {
				// Validation
				if (bankAccountDvSelect.getValue() == null) {
					ViewFuncs.showError("Select a bank account");
					return;
				}
				if (uploadedContents.getMultipartFile() == null) {
					ViewFuncs.showError("Upload a valid statement");
					return;
				}
				
				try {
					sbAcTxnImportStatsVO = sbAcTxnService.importSavingsAccountTransactions(bankAccountDvSelect.getValue().getId(), uploadedContents.getMultipartFile());
					notification = Notification.show("Savings Account Transactions Imported.\nCredits: Count - " + sbAcTxnImportStatsVO.getCreditCount() +
							" Amount - " + sbAcTxnImportStatsVO.getCreditTotal() +
							"\n.Debits: Count - " + sbAcTxnImportStatsVO.getDebitCount() +
							" Amount - " + sbAcTxnImportStatsVO.getDebitTotal() + ".");
					notification.setDuration(15000);
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
					uploadedContents.setMultipartFile(null);
				} catch (Exception e) {
					ViewFuncs.showError(UtilFuncs.messageFromException(e));
					return;
				}
			} finally {
				importButton.setEnabled(true);
			}
		});

	}
	
	private void handleSbAcTxnCategorise(FormLayout formLayout) {
		DatePicker sbAcTxnFromDatePicker, sbAcTxnToDatePicker;
		IntegerField sbAcTxnFromIdIntegerField, sbAcTxnToIdIntegerField;	// TODO: LongField
		NumberField sbAcTxnFromAmoutNumberField, sbAcTxnToAmoutNumberField;
		TextField narrationTextField, endAccountReferenceTextField;
		Select<IdValueVO> bankAccountOrInvestorDvSelect, transactionCategoryDvSelect;
		RadioButtonGroup<String> bookingRadioButtonGroup;
		HorizontalLayout hLayout;
		Select<IdValueVO> narrationOperatorSelect, endAccountReferenceOperatorSelect;
		Button fetchButton;
		Grid<SavingsAccountTransactionVO> savingsAccountTransactionsGrid;
		GridContextMenu<SavingsAccountTransactionVO> sATGridContextMenu;
		Map<Long, String> txnCatToDvCatMap;
		
		try {
			txnCatToDvCatMap = miscService.fetchDvCategoriesOfTxnCategories();
		} catch (Exception e) {
			ViewFuncs.showError(UtilFuncs.messageFromException(e));
			return;
		}
		
		sbAcTxnFromIdIntegerField = new IntegerField("From");
		sbAcTxnToIdIntegerField = new IntegerField("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "SB Ac Txn Id");
		hLayout.add(sbAcTxnFromIdIntegerField, sbAcTxnToIdIntegerField);
		
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
		narrationOperatorSelect.addValueChangeListener(event -> {
			if (narrationOperatorSelect.getValue() == null) {
				narrationTextField.setValue("");
			}
		});
		
		bankAccountOrInvestorDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_ACCOUNT + "+" + Constants.CATEGORY_PRIMARY_INVESTOR, null, true, false);
		formLayout.addFormItem(bankAccountOrInvestorDvSelect, "Account");
		
		bookingRadioButtonGroup = new RadioButtonGroup<String>();
		bookingRadioButtonGroup.setItems("Both", "Credit Only", "Debit Only");
		bookingRadioButtonGroup.setValue("Both");
		formLayout.addFormItem(bookingRadioButtonGroup, "Booking");
		
		transactionCategoryDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CATEGORY, null, true, true);
		transactionCategoryDvSelect.getListDataView().addItem(new IdValueVO(Constants.DVID_TRANSACTION_CATEGORY_DTI, Constants.domainValueCache.get(Constants.DVID_TRANSACTION_CATEGORY_DTI).getValue()));
		formLayout.addFormItem(transactionCategoryDvSelect, "Transaction Category");
		
		endAccountReferenceOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		endAccountReferenceTextField = new TextField("Value");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "End Account Reference");
		hLayout.add(endAccountReferenceOperatorSelect, endAccountReferenceTextField);
		endAccountReferenceOperatorSelect.addValueChangeListener(event -> {
			if (endAccountReferenceOperatorSelect.getValue() == null) {
				endAccountReferenceTextField.setValue("");
			}
		});
		
		fetchButton = new Button("Fetch");
		formLayout.add(fetchButton);
		fetchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		fetchButton.setDisableOnClick(true);
		
		savingsAccountTransactionsGrid = new Grid<>(SavingsAccountTransactionVO.class);
		savingsAccountTransactionsGrid.setNestedNullBehavior(NestedNullBehavior.ALLOW_NULLS);
		savingsAccountTransactionsGrid.setColumns("savingsAccountTransactionId", "bankAccountOrInvestor.value", "transactionDate", "narration", "booking.value", "amount", "balance", "valueDate", "reference", "transactionId", "utrNumber", "remitterBranch", "transactionCode.value", "branchCode", "transactionTime", "costCenter.value", "voucherType.value");
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
				if (sbAcTxnFromIdIntegerField.getValue() != null && sbAcTxnToIdIntegerField.getValue() != null &&
						sbAcTxnFromIdIntegerField.getValue() > sbAcTxnToIdIntegerField.getValue()) {
					ViewFuncs.showError("From SB Ac Txn Id cannot be greater than the To SB Ac Txn Id");
					return;
				}
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
				if (transactionCategoryDvSelect.getValue() != null && transactionCategoryDvSelect.getValue().getValue().equals("Empty") && !endAccountReferenceTextField.isEmpty()) {
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
						sbAcTxnFromIdIntegerField.getValue() == null ? null : (long)sbAcTxnFromIdIntegerField.getValue(),
						sbAcTxnToIdIntegerField.getValue() == null ? null : (long)sbAcTxnToIdIntegerField.getValue(),
						sbAcTxnFromDatePicker.getValue() == null ? null : Date.valueOf(sbAcTxnFromDatePicker.getValue()),
						sbAcTxnToDatePicker.getValue() == null ? null : Date.valueOf(sbAcTxnToDatePicker.getValue()),
						sbAcTxnFromAmoutNumberField.getValue() == null ? null : (double)sbAcTxnFromAmoutNumberField.getValue().doubleValue(),
						sbAcTxnToAmoutNumberField.getValue() == null ? null : (double)sbAcTxnToAmoutNumberField.getValue().doubleValue(),
						narrationTextField.getValue().equals("") ? null : narrationTextField.getValue(),
						narrationOperatorSelect.getValue() == null ? null : narrationOperatorSelect.getValue().getValue(),
						bankAccountOrInvestorDvSelect.getValue() == null ? null : bankAccountOrInvestorDvSelect.getValue().getId(),
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
			acceptSbAcTxnCategory(event.getItem().getSavingsAccountTransactionId(), event.getItem().getAmount(), txnCatToDvCatMap);
			savingsAccountTransactionsGrid.select(event.getItem());
		});
		
		sATGridContextMenu = savingsAccountTransactionsGrid.addContextMenu();
		sATGridContextMenu.addItem("Categorise", event -> {	// Same as Double Click
			Optional<SavingsAccountTransactionVO> savingsAccountTransactionVO;
			
			savingsAccountTransactionVO = event.getItem();
			if (savingsAccountTransactionVO.isPresent()) {
				acceptSbAcTxnCategory(savingsAccountTransactionVO.get().getSavingsAccountTransactionId(), savingsAccountTransactionVO.get().getAmount(), txnCatToDvCatMap);
				savingsAccountTransactionsGrid.select(savingsAccountTransactionVO.get());
			}
		});
		sATGridContextMenu.addItem("No Category", event -> {
			Optional<SavingsAccountTransactionVO> savingsAccountTransactionVO;
			List<SbAcTxnCategoryVO> sbAcTxnCategoryVOList;
			Notification notification;
			
			savingsAccountTransactionVO = event.getItem();
			if (savingsAccountTransactionVO.isPresent()) {
				sbAcTxnCategoryVOList = new ArrayList<SbAcTxnCategoryVO>(1);
				sbAcTxnCategoryVOList.add(new SbAcTxnCategoryVO(
						null,
						new IdValueVO(Constants.DVID_TRANSACTION_CATEGORY_NONE, null),
						null,
						null,
						savingsAccountTransactionVO.get().getAmount()
						));
				sbAcTxnService.saveSbAcTxnCategories(savingsAccountTransactionVO.get().getSavingsAccountTransactionId(), sbAcTxnCategoryVOList);
				savingsAccountTransactionsGrid.select(savingsAccountTransactionVO.get());
				notification = Notification.show("Categorised Successfully.");
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			}
		});
	}
	
	private void acceptSbAcTxnCategory(long savingsAccountTransactionId, Double sbAcTxnAmount, Map<Long, String> txnCatToDvCatMap) {
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
		TextField groupIdTextField;
		NumberField amountNumberField;
		Grid.Column<SbAcTxnCategoryVO> transactionCategoryColumn, endAccountReferenceColumn, amountColumn, groupIdColumn;
		Label txnAmountLabel;
		
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
		
		txnAmountLabel = new Label("SB A/c Txn. Amount: " + sbAcTxnAmount.doubleValue());
		verticalLayout.add(txnAmountLabel);

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
				Notification notification;
				Map<Character, Double> groupwiseTotalMap;
				
				for (int i = 0; i < sbAcTxnCategoryVOList.size(); i++) {
					SbAcTxnCategoryVO sbAcTxnCategoryVO = sbAcTxnCategoryVOList.get(i);
					if (sbAcTxnCategoryVO.getSbAcTxnCategoryId() == null && sbAcTxnCategoryVO.getTransactionCategory() == null &&
							sbAcTxnCategoryVO.getEndAccountReference() == null && sbAcTxnCategoryVO.getAmount() == null) {
						sbAcTxnCategoryGridLDV.removeItem(sbAcTxnCategoryVO);
						i--;
					}
				}
				// Validations
				groupwiseTotalMap = new HashMap<Character, Double>();
				for (SbAcTxnCategoryVO sbAcTxnCategoryVO : sbAcTxnCategoryVOList) {
					String dvCategory;
					if (sbAcTxnCategoryVO.getTransactionCategory() == null || sbAcTxnCategoryVO.getAmount() == null || sbAcTxnCategoryVO.getAmount() == 0) {
						ViewFuncs.showError("Transaction Category and Amount cannot be empty");
						return;
					}
					dvCategory = txnCatToDvCatMap.get(sbAcTxnCategoryVO.getTransactionCategory().getId());
					if ((dvCategory == null || !dvCategory.equals(Constants.CATEGORY_NONE)) &&
							(sbAcTxnCategoryVO.getEndAccountReference() == null || sbAcTxnCategoryVO.getEndAccountReference().getValue() == null)) {
						ViewFuncs.showError("End Account Reference cannot be empty");
						return;
					}
					if (sbAcTxnCategoryVOList.stream().anyMatch(
							o -> o != sbAcTxnCategoryVO &&
							!Objects.equals(o.getGroupId(), sbAcTxnCategoryVO.getGroupId()) &&
							sbAcTxnCategoryVO.getTransactionCategory().getValue().equals(o.getTransactionCategory().getValue()))) {
						ViewFuncs.showError("Same transaction category cannot be used in multiple groups");
						return;
					}
					if (groupwiseTotalMap.containsKey(sbAcTxnCategoryVO.getGroupId())) {
						groupwiseTotalMap.put(sbAcTxnCategoryVO.getGroupId(), groupwiseTotalMap.get(sbAcTxnCategoryVO.getGroupId()) + sbAcTxnCategoryVO.getAmount());
					} else {
						groupwiseTotalMap.put(sbAcTxnCategoryVO.getGroupId(), sbAcTxnCategoryVO.getAmount());
					}
				}
				for (Map.Entry<Character, Double> groupTotalEntry : groupwiseTotalMap.entrySet()) {
					if (groupTotalEntry.getValue().doubleValue() > sbAcTxnAmount.doubleValue()) {
						ViewFuncs.showError("Group <" + groupTotalEntry.getKey() + ">: Total of Category-wise amounts (" + groupTotalEntry.getValue().doubleValue() + ") exceeds the SB A/c Txn. Amount.");
						return;
					}
				}

				try {
					sbAcTxnService.saveSbAcTxnCategories(savingsAccountTransactionId, sbAcTxnCategoryVOList);
					notification = Notification.show("Categorised Successfully.");
					notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
		transactionCategoryColumn.setResizable(true);
		endAccountReferenceColumn = sbAcTxnCategoryGrid.addColumn(sbAcTxnCategoryVO -> {
			String dvCategory;
			dvCategory = txnCatToDvCatMap.get(sbAcTxnCategoryVO.getTransactionCategory().getId());
			if (dvCategory != null && dvCategory.equals(Constants.CATEGORY_NONE)) return "";
			else return sbAcTxnCategoryVO.getEndAccountReference().getValue() ;
			}).setHeader("End Account Reference");
		endAccountReferenceColumn.setResizable(true);
		amountColumn = sbAcTxnCategoryGrid.addColumn("amount").setHeader("Amount");
		groupIdColumn = sbAcTxnCategoryGrid.addColumn("groupId").setHeader("Group");
		sbAcTxnCategoryGrid.addComponentColumn(sbAcTxnCategoryVO -> {
			Button delButton = new Button();
			delButton.setIcon(new Icon(VaadinIcon.TRASH));
			delButton.addClickListener(e->{
				if (sbAcTxnCategoryVO.getTransactionCategory() == null || !sbAcTxnCategoryVO.getTransactionCategory().getId().equals(Constants.DVID_TRANSACTION_CATEGORY_DTI)) {
					sbAcTxnCategoryGridLDV.removeItem(sbAcTxnCategoryVO);
				}
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
		
		groupIdTextField = new TextField();
		groupIdTextField.setMaxLength(1);
		addCloseHandler(groupIdTextField, sbAcTxnCategoryEditor);
		sbAcTxnCategoryBinder.forField(groupIdTextField)
			.bind(sbAcTxnCategoryVO -> (sbAcTxnCategoryVO != null && sbAcTxnCategoryVO.getGroupId() != null) ? sbAcTxnCategoryVO.getGroupId().toString() : null,
					(sbAcTxnCategoryVO, groupId) -> sbAcTxnCategoryVO.setGroupId((groupId != null && !groupId.equals("")) ? groupId.charAt(0) : null));
		groupIdColumn.setEditorComponent(groupIdTextField);

		transactionCategoryDvSelect.addValueChangeListener(event -> {
			String dvCategory;
			Select<IdValueVO> endAccountReferenceDvSelect;
			TextField endAccountReferenceTextField;

			if (transactionCategoryDvSelect.getValue() == null) { // TODO: Remove the code repetition
				endAccountReferenceTextField = new TextField();
				endAccountReferenceColumn.setEditorComponent(endAccountReferenceTextField);
				addCloseHandler(endAccountReferenceTextField, sbAcTxnCategoryEditor);
				sbAcTxnCategoryBinder.forField(endAccountReferenceTextField)
					.bind(sbAcTxnCategoryVO -> (sbAcTxnCategoryVO != null && sbAcTxnCategoryVO.getEndAccountReference() != null && sbAcTxnCategoryVO.getEndAccountReference().getValue() != null) ? sbAcTxnCategoryVO.getEndAccountReference().getValue() : "", (sbAcTxnCategoryVO, endAccountReference) -> sbAcTxnCategoryVO.setEndAccountReference(new IdValueVO(null, endAccountReference)));
				endAccountReferenceTextField.setEnabled(false);
				if (event.isFromClient()) { // After double click on the grid, the event is propagated to select's value change
					endAccountReferenceTextField.setValue(""); // Components must be attached to the UI before updates are properly reflected
				}
			} else {
				dvCategory = txnCatToDvCatMap.get(transactionCategoryDvSelect.getValue().getId());
				if (dvCategory == null || dvCategory.equals("") || dvCategory.equals(Constants.CATEGORY_NONE)) {
					endAccountReferenceTextField = new TextField();
					endAccountReferenceColumn.setEditorComponent(endAccountReferenceTextField);
					addCloseHandler(endAccountReferenceTextField, sbAcTxnCategoryEditor);
					sbAcTxnCategoryBinder.forField(endAccountReferenceTextField)
						.bind(sbAcTxnCategoryVO -> (sbAcTxnCategoryVO != null && sbAcTxnCategoryVO.getEndAccountReference() != null && sbAcTxnCategoryVO.getEndAccountReference().getValue() != null) ? sbAcTxnCategoryVO.getEndAccountReference().getValue() : "", (sbAcTxnCategoryVO, endAccountReference) -> sbAcTxnCategoryVO.setEndAccountReference(new IdValueVO(null, endAccountReference)));
					if (dvCategory != null && dvCategory.equals(Constants.CATEGORY_NONE)) {
						endAccountReferenceTextField.setEnabled(false);
					} else {
						endAccountReferenceTextField.setEnabled(true);
					}
					if (event.isFromClient()) { // After double click on the grid, the event is propagated to select's value change
						endAccountReferenceTextField.setValue("");
					}
				} else {
					endAccountReferenceDvSelect = ViewFuncs.newDvSelect(miscService, dvCategory, null, false, false);
					endAccountReferenceColumn.setEditorComponent(endAccountReferenceDvSelect);
					addCloseHandler(endAccountReferenceDvSelect, sbAcTxnCategoryEditor);
					sbAcTxnCategoryBinder.forField(endAccountReferenceDvSelect)
						.bind(SbAcTxnCategoryVO::getEndAccountReference, SbAcTxnCategoryVO::setEndAccountReference);
					endAccountReferenceDvSelect.setEnabled(true);
				}
			}
			sbAcTxnCategoryEditor.refresh();
		});
		
		sbAcTxnCategoryGrid.addItemDoubleClickListener(e -> {
			if (e.getItem().getSbAcTxnCategoryId() == null || e.getItem().getSbAcTxnCategoryId() != Constants.NON_SATC_ID) {
			    sbAcTxnCategoryEditor.editItem(e.getItem());
			    Component editorComponent = e.getColumn().getEditorComponent();
			    if (editorComponent instanceof Focusable<?>) {
			        ((Focusable<?>) editorComponent).focus();
			    }
			}
		});
		
		dialog.open();
	}
	
    private static void addCloseHandler(Component sbAcTxnCategoryField,
            Editor<SbAcTxnCategoryVO> editor) {
    	sbAcTxnCategoryField.getElement().addEventListener("keydown", e -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");
    }

    @Getter @Setter
    private class ScopeLocalDummy01 {
    	MultipartFile multipartFile;

    }
}
