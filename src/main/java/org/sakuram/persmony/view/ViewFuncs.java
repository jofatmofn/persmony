package org.sakuram.persmony.view;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IdValueVO;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.TextRenderer;

public class ViewFuncs {

	public static void showError(String message) {
		ConfirmDialog errorDialog;
		
		errorDialog = new ConfirmDialog();
		errorDialog.setHeader("Attention! Error!!");
		errorDialog.setText(message);
		errorDialog.setConfirmText("OK");
		errorDialog.open();	
	}
	
    public static Select<IdValueVO> newDvSelect(MiscService miscService, String dvCategory, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	Select<IdValueVO> selectDv;
    	selectDv = new Select<IdValueVO>();
    	configureSelect(selectDv, miscService.fetchDvsOfCategory(dvCategory), label, isNoSelectionAllowed, isNullAValue);
    	return selectDv;
    }
    
    public static void newDvSelect(Select<IdValueVO> selectDv, MiscService miscService, String dvCategory, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	configureSelect(selectDv, miscService.fetchDvsOfCategory(dvCategory), label, isNoSelectionAllowed, isNullAValue);
    }
    
    public static Select<IdValueVO> newDvSelect(List<IdValueVO> idValueVOList, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	Select<IdValueVO> selectDv;
    	selectDv = new Select<IdValueVO>();
    	configureSelect(selectDv, idValueVOList, label, isNoSelectionAllowed, isNullAValue);
    	return selectDv;
    }
    
    public static Select<IdValueVO> newSelect(List<String> valueList, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	List<IdValueVO> idValueVOList;
    	long seqNo;
    	Select<IdValueVO> selectDv;
    	
    	idValueVOList = new ArrayList<IdValueVO>(valueList.size());
    	seqNo = 0;
    	for (String value : valueList) {
    		idValueVOList.add(new IdValueVO(seqNo++, value));
    	}
    	selectDv = new Select<IdValueVO>();
    	configureSelect(selectDv, idValueVOList, label, isNoSelectionAllowed, isNullAValue);
    	return selectDv;
    }
    
    public static RadioButtonGroup<Boolean> newTriStateRBG() {
    	RadioButtonGroup<Boolean> triStateRBG;
    	triStateRBG = new RadioButtonGroup<Boolean>();
    	triStateRBG.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
    	triStateRBG.setItems(null, true, false);
    	triStateRBG.setValue(null);
    	triStateRBG.setRenderer(new TextRenderer<>((mn) -> {
    		if (mn == null) {
    			return "Both";
    		} else if (mn) {
    			return "Yes";
    		} else {
    			return "No";
    	    }
    	}));
    	return triStateRBG;
    }

    private static void configureSelect(Select<IdValueVO> dvSelect, List<IdValueVO> idValueVOList, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	// Following special values have to go to the end, so that the ordinal value from enums are not impacted
		if (isNullAValue) {
			idValueVOList.add(new IdValueVO(Constants.DVID_EMPTY_SELECT, "Empty"));
		}
		/* if (isNoSelectionAllowed) {
			idValueVOList.add(null); // This is the IdValueVO corresponding to "No Selection"; .getListDataView().addItem() cannot be used after this.
		} */
		dvSelect.setItemLabelGenerator(idValueVO -> {
			if (isNoSelectionAllowed && idValueVO == null) {
				return "No Selection";
			}
			return idValueVO.getValue();
		});
		dvSelect.setItems(idValueVOList);
		if (label != null) {
			dvSelect.setLabel(label);
			dvSelect.setPlaceholder("Select " + label);
		}
		dvSelect.setEmptySelectionAllowed(isNoSelectionAllowed);
		if (isNoSelectionAllowed) {
			dvSelect.setEmptySelectionCaption("No Selection");
		}
    }
    
    public static Hr newHorizontalLine() {
		Hr horizontalLine;
		horizontalLine = new Hr();
		horizontalLine.setWidthFull();
		horizontalLine.getStyle().set("background-color", "black");
		return horizontalLine;
    }
    
    public static Div newLine() {
    	Div spacer = new Div();
    	spacer.getStyle().set("grid-column", "1 / -1"); // span entire row
    	return spacer;
    }
}
