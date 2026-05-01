package org.sakuram.persmony.view;

import java.util.concurrent.atomic.AtomicBoolean;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.FieldSpecVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;

import lombok.Getter;
import lombok.Setter;

@SpringComponent
@Scope("prototype")
public class TxnCatEarCriteriaComponent {
	MiscService miscService;
	
	TxnCatEarCriteriaVO txnCatEarCriteriaVO;
	private final Binder<TxnCatEarCriteriaVO> binder = new Binder<>(TxnCatEarCriteriaVO.class);
	
	public TxnCatEarCriteriaComponent(MiscService miscService) {
		this.miscService = miscService;
	}
	
	public FormLayout showForm(TxnCatEarCriteriaVO txnCatEarCriteriaVO) {
		FormLayout formLayout;
		HorizontalLayout hLayout;
		TextField endAccountReferenceTextField;
		Select<IdValueVO> transactionCategoryDvSelect, endAccountReferenceDvSelect, endAccountReferenceOperatorSelect;
		AtomicBoolean isEarTextEnabled, isEarSelectEnabled;

		this.txnCatEarCriteriaVO = txnCatEarCriteriaVO;
		
		formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1));
		
		transactionCategoryDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORY_TRANSACTION_CATEGORY + "+" + Constants.CATEGORY_TRANSACTION_CATEGORY_2, null, true, true);
		formLayout.addFormItem(transactionCategoryDvSelect, "Transaction Category");
		
		endAccountReferenceOperatorSelect = ViewFuncs.newSelect(FieldSpecVO.getTxtOperatorList(), "Operator", true, false);
		endAccountReferenceTextField = new TextField("Specify Value");
		endAccountReferenceDvSelect = ViewFuncs.newDvSelect(miscService, Constants.CATEGORIES_USED_BY_CATEGORY_TRANSACTION_CATEGORY, null, true, false);
		endAccountReferenceDvSelect.setLabel("Select Value");
		isEarTextEnabled = new AtomicBoolean(true);
		isEarSelectEnabled = new AtomicBoolean(true);
		hLayout = new HorizontalLayout();
		formLayout.addFormItem(hLayout, "End Account Reference");
		hLayout.add(endAccountReferenceOperatorSelect, endAccountReferenceTextField, endAccountReferenceDvSelect);

		binder.forField(endAccountReferenceTextField)
				.bind("endAccountReference");
		binder.forField(transactionCategoryDvSelect)
				.bind("transactionCategoryIdValueVO");
		binder.forField(endAccountReferenceDvSelect)
				.bind("endAccountReferenceIdValueVO");
		binder.forField(endAccountReferenceOperatorSelect)
				.bind("endAccountReferenceOperatorIdValueVO");

		ValueChangeListener<ValueChangeEvent<?>> earLogic = e -> {
			String dvCategory;
			HasValue<?, ?> source;
			
			source = e.getHasValue();
			if (source == transactionCategoryDvSelect) {
				if (transactionCategoryDvSelect.getValue() == null) {
					isEarTextEnabled.set(true);
					isEarSelectEnabled.set(true);
					endAccountReferenceDvSelect.clear();
					ViewFuncs.newDvSelect(endAccountReferenceDvSelect, miscService, Constants.CATEGORIES_USED_BY_CATEGORY_TRANSACTION_CATEGORY, null, true, false);
				} else if(transactionCategoryDvSelect.getValue().getId() == Constants.DVID_EMPTY_SELECT) {
					isEarTextEnabled.set(false);
					isEarSelectEnabled.set(false);
					endAccountReferenceTextField.setValue("");
					endAccountReferenceOperatorSelect.setValue(null);
					endAccountReferenceDvSelect.clear();
				} else {
					dvCategory = Constants.TXN_CAT_TO_DV_CAT_MAP.get(transactionCategoryDvSelect.getValue().getId());
					if (dvCategory == null || dvCategory.equals("")) {
						isEarTextEnabled.set(true);
						isEarSelectEnabled.set(false);
						endAccountReferenceDvSelect.clear();
					} else if (dvCategory.equals(Constants.CATEGORY_NONE)) {
						isEarTextEnabled.set(false);
						isEarSelectEnabled.set(false);
						endAccountReferenceTextField.setValue("");
						endAccountReferenceOperatorSelect.setValue(null);
						endAccountReferenceDvSelect.clear();
					} else {
						isEarTextEnabled.set(false);
						isEarSelectEnabled.set(true);
						endAccountReferenceOperatorSelect.setValue(null);
						endAccountReferenceTextField.setValue("");
						endAccountReferenceDvSelect.clear();
						ViewFuncs.newDvSelect(endAccountReferenceDvSelect, miscService, dvCategory, null, true, false);
					}
				}
				endAccountReferenceOperatorSelect.setEnabled(isEarTextEnabled.get());
				endAccountReferenceTextField.setEnabled(isEarTextEnabled.get());
				endAccountReferenceDvSelect.setEnabled(isEarSelectEnabled.get());
				
			} else if (source == endAccountReferenceOperatorSelect) {
				if (endAccountReferenceOperatorSelect.getValue() == null) {
					endAccountReferenceTextField.setValue("");
				}
			} else if (source == endAccountReferenceTextField) {
				if (endAccountReferenceTextField.getValue().isEmpty()) {
					endAccountReferenceOperatorSelect.setValue(null);
					endAccountReferenceDvSelect.setEnabled(isEarSelectEnabled.get());
				} else {
					endAccountReferenceDvSelect.setValue(null);
					endAccountReferenceDvSelect.setEnabled(false);
				}
			} else if (source == endAccountReferenceDvSelect) {
				if (endAccountReferenceDvSelect.getValue() == null) {
					if (isEarTextEnabled.get()) {
						endAccountReferenceOperatorSelect.setValue(null);
					} else {
						endAccountReferenceOperatorSelect.setValue(null);
					}
					endAccountReferenceOperatorSelect.setEnabled(isEarTextEnabled.get());
					endAccountReferenceTextField.setEnabled(isEarTextEnabled.get());
				} else {
					endAccountReferenceOperatorSelect.setValue(null);
					endAccountReferenceOperatorSelect.setEnabled(false);
					endAccountReferenceTextField.setValue("");
					endAccountReferenceTextField.setEnabled(false);
				}
				
			}
		};
		transactionCategoryDvSelect.addValueChangeListener(e -> {
			earLogic.valueChanged(e);
		});
		endAccountReferenceOperatorSelect.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				earLogic.valueChanged(e);
			}
		});
		endAccountReferenceTextField.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				earLogic.valueChanged(e);
			}
		});
		endAccountReferenceDvSelect.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				earLogic.valueChanged(e);
			}
		});
		
		binder.setBean(txnCatEarCriteriaVO);
		
		return formLayout;
	}
	
	public void validateInput() {
		if (txnCatEarCriteriaVO.getTransactionCategoryIdValueVO() != null && txnCatEarCriteriaVO.getTransactionCategoryIdValueVO().getValue().equals("Empty") && !txnCatEarCriteriaVO.getEndAccountReference().isEmpty()) {
			throw new AppException("End Account Reference can be specified only when Transaction Category is not 'Empty'", null);
		}
		if (!txnCatEarCriteriaVO.getEndAccountReference().equals("") && txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO() == null) {
			throw new AppException("End Account Reference: Non-matching Operator and Value", null);
		}
		if (txnCatEarCriteriaVO.getEndAccountReference().equals("") && txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO() != null && txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO().getId() != FieldSpecVO.TxtOperator.EQ.ordinal() && txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO().getId() != FieldSpecVO.TxtOperator.NE.ordinal()) {
			throw new AppException("Specify Value for End Account Reference " + txnCatEarCriteriaVO.getEndAccountReferenceOperatorIdValueVO().getValue(), null);
		}
		
	}
	
	public void clear() {
		txnCatEarCriteriaVO.setTransactionCategoryIdValueVO(null);
		txnCatEarCriteriaVO.setEndAccountReferenceOperatorIdValueVO(null);
		txnCatEarCriteriaVO.setEndAccountReference("");
		txnCatEarCriteriaVO.setEndAccountReferenceIdValueVO(null);
		binder.refreshFields();
	}
	
    @Getter @Setter
	public static class TxnCatEarCriteriaVO {
		String endAccountReference;
		IdValueVO transactionCategoryIdValueVO, endAccountReferenceIdValueVO, endAccountReferenceOperatorIdValueVO;
	}
}
