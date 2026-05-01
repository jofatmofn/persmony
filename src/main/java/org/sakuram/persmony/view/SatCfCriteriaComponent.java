package org.sakuram.persmony.view;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.SbAcTxnCriteriaVO;
import org.sakuram.persmony.view.TxnCatEarCriteriaComponent.TxnCatEarCriteriaVO;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class SatCfCriteriaComponent {
	MiscService miscService;

	TxnCatEarCriteriaComponent txnCatEarCriteriaComponent;
	DatePickerI18n isoDatePickerI18n;
	
	SbAcTxnCriteriaVO satCfCriteriaVO;
	SatCfCriteriaConfigs satCfCriteriaConfigs;
	TxnCatEarCriteriaVO txnCatEarCriteriaVO;
	private final Binder<SbAcTxnCriteriaVO> binder = new Binder<>(SbAcTxnCriteriaVO.class);
	
	public SatCfCriteriaComponent(MiscService miscService, TxnCatEarCriteriaComponent txnCatEarCriteriaComponent, DatePickerI18n isoDatePickerI18n) {
		this.miscService = miscService;
		this.txnCatEarCriteriaComponent = txnCatEarCriteriaComponent;
		this.isoDatePickerI18n = isoDatePickerI18n;
	}
	
	public FormLayout showForm(SbAcTxnCriteriaVO satCfCriteriaVO) {
		return showForm(satCfCriteriaVO, new SatCfCriteriaConfigs(true, null));
	}
	
	public FormLayout showForm(SbAcTxnCriteriaVO satCfCriteriaVO, SatCfCriteriaConfigs satCfCriteriaConfigs) {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		DatePicker sbAcTxnFromDatePicker, sbAcTxnToDatePicker;
		IntegerField sbAcTxnFromIdIntegerField, sbAcTxnToIdIntegerField;	// TODO: LongField
		NumberField sbAcTxnFromAmoutNumberField, sbAcTxnToAmoutNumberField;
		TextField narrationTextField;
		Select<IdValueVO> bankAccountOrInvestorDvSelect, narrationOperatorSelect;
		RadioButtonGroup<String> bookingRadioButtonGroup;
		
		this.satCfCriteriaVO = satCfCriteriaVO;
		this.satCfCriteriaConfigs = satCfCriteriaConfigs;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		sbAcTxnFromIdIntegerField = new IntegerField("From");
		sbAcTxnToIdIntegerField = new IntegerField("To");
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "Txn Id");
		hLayout.add(sbAcTxnFromIdIntegerField, sbAcTxnToIdIntegerField);
		
		sbAcTxnFromDatePicker = new DatePicker("From");
		sbAcTxnFromDatePicker.setI18n(isoDatePickerI18n);
		sbAcTxnToDatePicker = new DatePicker("To");
		sbAcTxnToDatePicker.setI18n(isoDatePickerI18n);
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
		formLayout.addFormItem(bookingRadioButtonGroup, "Booking");
		bookingRadioButtonGroup.setReadOnly(!satCfCriteriaConfigs.isBookingInput);
		satCfCriteriaVO.setBookingDvId(satCfCriteriaConfigs.bookingDvId);
		
		txnCatEarCriteriaVO = new TxnCatEarCriteriaComponent.TxnCatEarCriteriaVO();
		formLayout.add(txnCatEarCriteriaComponent.showForm(txnCatEarCriteriaVO));
		
		binder.forField(sbAcTxnFromIdIntegerField).bind("fromId");
		binder.forField(sbAcTxnToIdIntegerField).bind("toId");
		binder.forField(sbAcTxnFromDatePicker).bind("fromDate");
		binder.forField(sbAcTxnToDatePicker).bind("toDate");
		binder.forField(sbAcTxnFromAmoutNumberField).bind("fromAmount");
		binder.forField(sbAcTxnToAmoutNumberField).bind("toAmount");
		binder.forField(narrationTextField).bind("narration");
		binder.forField(narrationOperatorSelect).bind("narrationOperatorIdValueVO");
		binder.forField(bankAccountOrInvestorDvSelect).bind("bankAccountOrInvestorIdValueVO");
		binder.forField(bookingRadioButtonGroup)
				.withConverter(
						(fieldValue -> fieldValue.equals("Both") ? null : fieldValue.equals("Credit Only") ? Constants.DVID_BOOKING_CREDIT : Constants.DVID_BOOKING_DEBIT),
						(beanValue -> beanValue == null ? "Both" : beanValue == Constants.DVID_BOOKING_CREDIT ? "Credit Only" : "Debit Only")
						)
				.bind("bookingDvId");
		
		binder.setBean(satCfCriteriaVO);
		
		return formLayout;
	}
	
	public void validateInput() {
		if (satCfCriteriaVO.getFromId() != null && satCfCriteriaVO.getToId() != null && 
				satCfCriteriaVO.getFromId() > satCfCriteriaVO.getToId()) {
			throw new AppException("From SB Ac Txn Id cannot be greater than the To SB Ac Txn Id", null);
		}
		if (satCfCriteriaVO.getFromDate() != null && satCfCriteriaVO.getToDate() != null && 
				satCfCriteriaVO.getFromDate().isAfter(satCfCriteriaVO.getToDate())) {
			throw new AppException("From Date cannot be after the To Date", null);
		}
		if (satCfCriteriaVO.getFromAmount() != null && satCfCriteriaVO.getToAmount() != null && 
				satCfCriteriaVO.getFromAmount() > satCfCriteriaVO.getToAmount()) {
			throw new AppException("From Amount cannot be greater than the To Amount", null);
		}
		if (!satCfCriteriaVO.getNarration().equals("") && satCfCriteriaVO.getNarrationOperatorIdValueVO() == null) {
			throw new AppException("Narration: Non-matching Operator and Value", null);
		}
		if (satCfCriteriaVO.getNarration().equals("") && satCfCriteriaVO.getNarrationOperatorIdValueVO() != null && satCfCriteriaVO.getNarrationOperatorIdValueVO().getId() != FieldSpecVO.TxtOperator.EQ.ordinal() && satCfCriteriaVO.getNarrationOperatorIdValueVO().getId() != FieldSpecVO.TxtOperator.NE.ordinal()) {
			throw new AppException("Specify Value for Narration", null);
		}
		txnCatEarCriteriaComponent.validateInput();
		
		satCfCriteriaVO.setTransactionCategoryDvId(txnCatEarCriteriaVO.getTransactionCategoryIdValueVO() == null ? null : txnCatEarCriteriaVO.getTransactionCategoryIdValueVO().getId());
		satCfCriteriaVO.setEndAccountReference(txnCatEarCriteriaVO.getEndAccountReferenceIdValueVO() == null ? (txnCatEarCriteriaVO.getEndAccountReference().equals("") ? null : txnCatEarCriteriaVO.getEndAccountReference()) : txnCatEarCriteriaVO.getEndAccountReferenceIdValueVO().getId().toString());
		satCfCriteriaVO.setEndAccountReferenceOperator(txnCatEarCriteriaVO.getEndAccountReferenceIdValueVO() == null ? (txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO() == null ? null : txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO().getValue()) : FieldSpecVO.TxtOperator.EQ.name());
		
	}
	
	public void clear() {
		satCfCriteriaVO.setFromId(null);
		satCfCriteriaVO.setToId(null);
		satCfCriteriaVO.setFromDate(null);
		satCfCriteriaVO.setToDate(null);
		satCfCriteriaVO.setFromAmount(null);
		satCfCriteriaVO.setToAmount(null);
		satCfCriteriaVO.setNarrationOperatorIdValueVO(null);
		satCfCriteriaVO.setNarration("");
		satCfCriteriaVO.setBankAccountOrInvestorIdValueVO(null);
		if (satCfCriteriaConfigs.isBookingInput) {
			satCfCriteriaVO.setBookingDvId(null);
		}
		binder.refreshFields();
		txnCatEarCriteriaComponent.clear();
	}
	
	static class SatCfCriteriaConfigs {
		Boolean isBookingInput;
		Long bookingDvId;
		
		public SatCfCriteriaConfigs(boolean isBookingInput, Long bookingDvId) {
			this.isBookingInput = isBookingInput;
			this.bookingDvId = bookingDvId;
		}
	}
	
}
