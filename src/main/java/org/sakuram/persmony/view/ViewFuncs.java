package org.sakuram.persmony.view;

import java.util.ArrayList;
import java.util.List;

import org.sakuram.persmony.service.MiscService;
import org.sakuram.persmony.valueobject.IdValueVO;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.select.Select;

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
    	createSelect(selectDv, miscService.fetchDvsOfCategory(dvCategory), label, isNoSelectionAllowed, isNullAValue);
    	return selectDv;
    }
    
    public static void newDvSelect(Select<IdValueVO> selectDv, MiscService miscService, String dvCategory, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	createSelect(selectDv, miscService.fetchDvsOfCategory(dvCategory), label, isNoSelectionAllowed, isNullAValue);
    }
    
    public static Select<IdValueVO> newSelect(List<String> valueList, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
    	List<IdValueVO> idValueVOList;
    	long seqNo;
    	Select<IdValueVO> selectDv;
    	
    	idValueVOList = new ArrayList<IdValueVO>(valueList.size());
    	seqNo = 0;
    	for (String value : valueList) {
    		idValueVOList.add(new IdValueVO(++seqNo, value));
    	}
    	selectDv = new Select<IdValueVO>();
    	createSelect(selectDv, idValueVOList, label, isNoSelectionAllowed, isNullAValue);
    	return selectDv;
    }
    
    private static void createSelect(Select<IdValueVO> dvSelect, List<IdValueVO> idValueVOList, String label, boolean isNoSelectionAllowed, boolean isNullAValue) {
		if (isNullAValue) {
			idValueVOList.add(0, new IdValueVO(-1L, "Empty"));
		}
		dvSelect.setItemLabelGenerator(idValueVO -> {
			if (isNoSelectionAllowed && idValueVO == null) {	// Required if no selection is allowed
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
}
