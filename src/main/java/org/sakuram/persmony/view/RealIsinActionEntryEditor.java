package org.sakuram.persmony.view;

import java.time.LocalDate;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.RealIsinActionEntryVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SpringComponent
@Scope("prototype")	// multiple editors are required during parent bean DebtEquityMutualView construction, otherwise @UIScope will do.
public class RealIsinActionEntryEditor extends FormLayout {
	private static final long serialVersionUID = 1L;
	
	Checkbox entryApplicabilityCheckbox;
	NumberField quantityNumberField, pricePerUnitNumberField;
	DatePicker settlementDateDatePicker, holdingChangeDateDatePicker;
	SecuritySearchComponent securitySearchComponent;
	Select<IdValueVO> dematAccountSelect;
	TextField bookingTextField;

	RealIsinActionEntryVO realIsinActionEntryVO;
	
	private final Binder<RealIsinActionEntryVO> binder = new Binder<>(RealIsinActionEntryVO.class);

	public RealIsinActionEntryEditor(RealIsinActionEntryVO realIsinActionEntryVO, InputArgs inputArgs) {
		
		setResponsiveSteps(new ResponsiveStep("0", 1));
		this.realIsinActionEntryVO = realIsinActionEntryVO;
		
		IsinActionEntrySpecVO isinActionEntrySpecVO = realIsinActionEntryVO.getIsinActionEntrySpecVO();
		add(ViewFuncs.newHorizontalLine());
		addFormItem(new NativeLabel(realIsinActionEntryVO.getIsinActionEntrySpecVO().getEntrySpecName()), "Real Entry");
		
		entryApplicabilityCheckbox = new Checkbox();
		bookingTextField = new TextField();
		settlementDateDatePicker = new DatePicker();
        settlementDateDatePicker.setI18n(inputArgs.isoDatePickerI18n);
		holdingChangeDateDatePicker = new DatePicker();
        holdingChangeDateDatePicker.setI18n(inputArgs.isoDatePickerI18n);
		securitySearchComponent = new SecuritySearchComponent(inputArgs.getDebtEquityMutualService(), inputArgs.getMiscService());
		quantityNumberField = new NumberField();
		pricePerUnitNumberField = new NumberField();
	    dematAccountSelect = ViewFuncs.newDvSelect(inputArgs.getMiscService().fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, false, false);
	    
	    addFormItem(entryApplicabilityCheckbox, "Entry Applicable");
    	entryApplicabilityCheckbox.setEnabled(!isinActionEntrySpecVO.isMandatory());
    	entryApplicabilityCheckbox.addValueChangeListener(event -> {
    		if(entryApplicabilityCheckbox.getValue()) {
    			setFieldsReady(realIsinActionEntryVO, inputArgs);
    		} else {
    			realIsinActionEntryVO.setEmpty();
    			binder.refreshFields();
    			settlementDateDatePicker.setEnabled(false);
    			holdingChangeDateDatePicker.setEnabled(false);
    			securitySearchComponent.setEnabled(false);
    			quantityNumberField.setEnabled(false);
    			pricePerUnitNumberField.setEnabled(false);
    			dematAccountSelect.setEnabled(false);
    		}
		});
    	entryApplicabilityCheckbox.setValue(true);
	    
	    addFormItem(bookingTextField, "Booking");
	    bookingTextField.setEnabled(false);
	    
		addFormItem(settlementDateDatePicker, "Settlement Date");
		
		NativeLabel label = new NativeLabel("Date");
		addFormItem(holdingChangeDateDatePicker, label);
		switch(isinActionEntrySpecVO.getDateType()) {
		case ACQUISITION:
			label.setText("Acquisition Date");
			break;
		case DISPOSAL:
			label.setText("Disposal Date");
			break;
		default:
			break;
		}
		
		addFormItem(securitySearchComponent.getLayout(), "ISIN");
		realIsinActionEntryVO.setIsin(inputArgs.getEntitledIsin());

		addFormItem(quantityNumberField, "Quantity");
		switch(isinActionEntrySpecVO.getQuantityInputType()) {
		case INPUT:
			break;
		case BALANCE:
			realIsinActionEntryVO.setQuantity(inputArgs.getBalance());
			break;
		case PREVIOUS_INPUT:
			break;
		case ZERO:
			realIsinActionEntryVO.setQuantity(0D);
			break;
		}
		
		addFormItem(pricePerUnitNumberField, "Price Per Unit");
		pricePerUnitNumberField.setHelperText("Required if and only if trade is not applicable");
		switch(isinActionEntrySpecVO.getPriceInputType()) {
		case INPUT:
			break;
		case NULL:
		case FACTOR:
			realIsinActionEntryVO.setPricePerUnit(null);
			break;
		case ZERO:
			realIsinActionEntryVO.setPricePerUnit(0D);
			break;
		}
		
        addFormItem(dematAccountSelect, "Demat Account");
	    // Beware: Special Logic outside configuration
	    if (inputArgs.isDematInput) {
			realIsinActionEntryVO.setDematAccount(null);
	    } else {
			realIsinActionEntryVO.setDematAccount(inputArgs.getDematAccount());
	    }
	    
		binder.forField(bookingTextField)
		    .withConverter(
		        fieldValue -> null,
		        beanValue -> (beanValue != null && (Long) beanValue == Constants.DVID_BOOKING_DEBIT ? "Debit" : "Credit")
		    )
		    .bind(
		        bean -> bean.getIsinActionEntrySpecVO().getBookingTypeDvId(),
		        (bean, fieldVal) -> {}
		    );
		binder.forField(settlementDateDatePicker)
		    .bind("settlementDate");
		binder.forField(holdingChangeDateDatePicker)
	    	.bind("holdingChangeDate");
		binder.forField(securitySearchComponent)
	    	.withConverter(
	    			fieldValue -> fieldValue == null ? null : fieldValue.toUpperCase(),
	    			beanValue -> beanValue
		    )
			.bind("isin");
		binder.forField(quantityNumberField)	// Such binder.forField for individual fields can be avoided by naming the bean property and UI field the same
			.bind("quantity");
		binder.forField(pricePerUnitNumberField)
			.bind("pricePerUnit");
		binder.forField(dematAccountSelect)
			.bind("dematAccount");
	
		// binder.bindInstanceFields(this);
		binder.setBean(realIsinActionEntryVO);
		
	}
	
	private void setFieldsReady(RealIsinActionEntryVO realIsinActionEntryVO, InputArgs inputArgs) {
		IsinActionEntrySpecVO isinActionEntrySpecVO = realIsinActionEntryVO.getIsinActionEntrySpecVO();
		settlementDateDatePicker.setEnabled(true);
		if (isinActionEntrySpecVO.getDateType() == IsinActionEntrySpecVO.IADateType.ACQUISITION || isinActionEntrySpecVO.getDateType() == IsinActionEntrySpecVO.IADateType.DISPOSAL) {
			holdingChangeDateDatePicker.setEnabled(true);
		} else {
			holdingChangeDateDatePicker.setEnabled(false);
		}
		if (isinActionEntrySpecVO.getIsinInputType() == IsinActionEntrySpecVO.IAIsinType.OTHER_ISIN) {
			securitySearchComponent.setEnabled(true);
		} else {
			securitySearchComponent.setEnabled(false);
		}
		if (isinActionEntrySpecVO.getQuantityInputType() == IsinActionEntrySpecVO.IAQuantityType.INPUT) {
			quantityNumberField.setEnabled(true);
		} else {
			quantityNumberField.setEnabled(false);
		}
		if (isinActionEntrySpecVO.getPriceInputType() == IsinActionEntrySpecVO.IAPriceType.INPUT) {
			pricePerUnitNumberField.setEnabled(true);
		} else {
			pricePerUnitNumberField.setEnabled(false);
		}
	    if (inputArgs.isDematInput) {
			dematAccountSelect.setEnabled(true);
	    } else {
			dematAccountSelect.setEnabled(false);
	    }
	}
	
    /* public void setRealIsinActionEntryVO(RealIsinActionEntryVO isinActionEntryVO) {
        binder.setBean(isinActionEntryVO);
    }

    public RealIsinActionEntryVO getRealIsinActionEntryVO() {
        return binder.getBean();
    } */

    public boolean isValid() {
        return binder.validate().isOk();
    }

    public void saveToBean(RealIsinActionEntryVO isinActionEntryVO) {
        binder.writeBeanIfValid(isinActionEntryVO);
    }

    public Double getQuantity() {
    	return quantityNumberField.getValue();
    }
    
    public NumberField getQuantityNumberField() {
    	return quantityNumberField;
    }
    
    public void setQuantityNumberField(Double quantity) {
    	quantityNumberField.setValue(quantity);
    }
    
    @Getter
	@AllArgsConstructor
    public static class InputArgs {
		String entitledIsin;
		IdValueVO dematAccount;
		LocalDate recordDate;
		Double balance;
		boolean isTradeApplicable;
		boolean isDematInput;
		DebtEquityMutualService debtEquityMutualService;
		MiscService miscService;
		DatePickerI18n isoDatePickerI18n;
	}
}
