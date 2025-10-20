package org.sakuram.persmony.view;

import java.sql.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.sakuram.persmony.service.DebtEquityMutualService;
import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.RealIsinActionEntryVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SpringComponent
@Scope("prototype")	// multiple editors are required during parent bean DebtEquityMutualView construction, otherwise @UIScope will do.
public class RealIsinActionEntryEditor extends FormLayout {
	private static final long serialVersionUID = 1L;
	
	NumberField quantity, pricePerUnit;
	DatePicker settlementDate;
	SecuritySearchComponent securitySearchComponent;
	RatioComponent ratioComponent;
	Select<IdValueVO> toDematAccount;

	RealIsinActionEntryVO realIsinActionEntryVO;
	
	private final Binder<RealIsinActionEntryVO> binder = new Binder<>(RealIsinActionEntryVO.class);

	public RealIsinActionEntryEditor(RealIsinActionEntryVO realisinActionEntryVO, InputArgs inputArgs) {
		
		setResponsiveSteps(new ResponsiveStep("0", 1));
		this.realIsinActionEntryVO = realisinActionEntryVO;
		
		IsinActionEntrySpecVO isinActionEntrySpecVO = realisinActionEntryVO.getIsinActionEntrySpecVO();
		add(ViewFuncs.newHorizontalLine());
		addFormItem(new Label(realisinActionEntryVO.getIsinActionEntrySpecVO().getEntrySpecName()), "Real Entry");
		addFormItem(new Label((realisinActionEntryVO.getIsinActionEntrySpecVO().getBookingTypeDvId() == Constants.DVID_BOOKING_DEBIT ? "Debit" : "Credit")), "Booking");
		
		settlementDate = new DatePicker();
		ratioComponent = new RatioComponent();
		quantity = new NumberField();
		pricePerUnit = new NumberField();
	    toDematAccount = ViewFuncs.newDvSelect(inputArgs.getMiscService().fetchDvsOfCategory(Constants.CATEGORY_DEMAT_ACCOUNT, true, false), null, false, false);
	    
		binder.forField(settlementDate)
	    	.withConverter(
		        localDate -> (localDate == null ? null : java.sql.Date.valueOf(localDate)), // convert to bean type
		        sqlDate -> (sqlDate == null ? null : sqlDate.toLocalDate())                 // convert to field type
		    )
		    .bind("settlementDate");
		binder.forField(securitySearchComponent)
		binder.bindInstanceFields(this);
		binder.setBean(realisinActionEntryVO);
		
		addFormItem(settlementDate, "Settlement Date");
		if (realisinActionEntryVO.getIsinActionEntrySpecVO().getSettlementDateInputType() == IsinActionEntrySpecVO.IASettlementDateType.OTHER_DATE) {
			settlementDate.setEnabled(true);
		} else {
			settlementDate.setEnabled(false);
			// settlementDate.setValue(inputArgs.getRecordDate().toLocalDate());
			realisinActionEntryVO.setSettlementDate(inputArgs.getRecordDate());
		}
		
		securitySearchComponent = new SecuritySearchComponent(inputArgs.getDebtEquityMutualService(), inputArgs.getMiscService());
		addFormItem(securitySearchComponent.getLayout(), "ISIN");
		if (isinActionEntrySpecVO.getIsinInputType() == IsinActionEntrySpecVO.IAIsinType.OTHER_ISIN) {
			securitySearchComponent.setEnabled(true);
		} else {
			securitySearchComponent.setEnabled(false);
			// securitySearchComponent.getIsinTextField().setValue(inputArgs.getEntitledIsin());
			realisinActionEntryVO.setIsin(inputArgs.getEntitledIsin());
		}

		addFormItem(ratioComponent.getLayout(), "Ratio (New:Old)");
		if (isinActionEntrySpecVO.isFactorOfExistingQuantity()) {
			ratioComponent.setEnabled(true);
		} else {
			ratioComponent.setEnabled(false);
		}
		
		addFormItem(quantity, "Quantity");
		quantity.setEnabled(false);
		quantity.addValueChangeListener(event -> {
			inputArgs.getQuantityAR().set(quantity.getValue());
		});
		switch(isinActionEntrySpecVO.getQuantityInputType()) {
		case INPUT:
			quantity.setEnabled(true);
			break;
		case BALANCE:
			quantity.setValue(inputArgs.getBalance());
			break;
		case PREVIOUS_INPUT:
			quantity.setValue(inputArgs.getQuantityAR().get());
			break;
		case ZERO:
			quantity.setValue(0D);
			break;
		}
		
		addFormItem(pricePerUnit, "Price Per Unit");
		if (isinActionEntrySpecVO.getPriceInputType() == IsinActionEntrySpecVO.IAPriceType.INPUT) {
			pricePerUnit.setEnabled(true);
		} else {
			pricePerUnit.setEnabled(false);
		}
		
	    toDematAccount.setValue(inputArgs.getDematAccount());
	    // Beware: Special Logic outside configuration
	    if (isinActionEntrySpecVO.getActionDvId() == Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER &&
	    		isinActionEntrySpecVO.getEntrySpecName().equals(Constants.ACTION_TYPE_GIFT_OR_TRANSFER_ENTRY_SPEC_NAME_RECEIVE)) {
	        addFormItem(toDematAccount, "To Demat Account");
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

    @Getter
	@AllArgsConstructor
    public static class InputArgs {
		String entitledIsin;
		IdValueVO dematAccount;
		Date recordDate;
		AtomicReference<Double> quantityAR;
		Double balance;
		DebtEquityMutualService debtEquityMutualService;
		MiscService miscService;
	}
}
