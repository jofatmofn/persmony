package org.sakuram.persmony.view;

import java.time.LocalDate;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.RealIsinActionEntryVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.datepicker.DatePicker;
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
	
	NumberField quantityNumberField, pricePerUnitNumberField;
	DatePicker settlementDateDatePicker, ownershipChangeDateDatePicker;
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
		
		bookingTextField = new TextField();
		settlementDateDatePicker = new DatePicker();
		ownershipChangeDateDatePicker = new DatePicker();
		securitySearchComponent = new SecuritySearchComponent(inputArgs.getDebtEquityMutualService(), inputArgs.getMiscService());
		quantityNumberField = new NumberField();
		pricePerUnitNumberField = new NumberField();
	    dematAccountSelect = ViewFuncs.newDvSelect(inputArgs.getMiscService().fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, false, false);
	    
	    addFormItem(bookingTextField, "Booking");
	    bookingTextField.setEnabled(false);
	    
		addFormItem(settlementDateDatePicker, "Settlement Date");
		
		switch(isinActionEntrySpecVO.getDateType()) {
		case ACQUISITION:
			addFormItem(ownershipChangeDateDatePicker, "Acquisition Date");
			break;
		case DISPOSAL:
			addFormItem(ownershipChangeDateDatePicker, "Disposal Date");
			break;
		default:
			break;
		}
		
		addFormItem(securitySearchComponent.getLayout(), "ISIN");
		if (isinActionEntrySpecVO.getIsinInputType() == IsinActionEntrySpecVO.IAIsinType.OTHER_ISIN) {
			securitySearchComponent.setEnabled(true);
		} else {
			securitySearchComponent.setEnabled(false);
			realIsinActionEntryVO.setIsin(inputArgs.getEntitledIsin());
		}

		addFormItem(quantityNumberField, "Quantity");
		quantityNumberField.setEnabled(false);
		switch(isinActionEntrySpecVO.getQuantityInputType()) {
		case INPUT:
			quantityNumberField.setEnabled(true);
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
		pricePerUnitNumberField.setEnabled(false);
		switch(isinActionEntrySpecVO.getPriceInputType()) {
		case INPUT:
			if (!inputArgs.isTradeApplicable) {
				pricePerUnitNumberField.setEnabled(true);
			}
			break;
		case NULL:
		case FACTOR:
			realIsinActionEntryVO.setPricePerUnit(null);
			break;
		case ZERO:
			realIsinActionEntryVO.setPricePerUnit(0D);
			break;
		}
		
		realIsinActionEntryVO.setDematAccount(inputArgs.getDematAccount());
		dematAccountSelect.setEnabled(false);
        addFormItem(dematAccountSelect, "Demat Account");
	    // Beware: Special Logic outside configuration
	    if (inputArgs.isDematInput) {
			dematAccountSelect.setEnabled(true);
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
		binder.forField(ownershipChangeDateDatePicker)
	    	.bind("ownershipChangeDate");
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
	}
}
