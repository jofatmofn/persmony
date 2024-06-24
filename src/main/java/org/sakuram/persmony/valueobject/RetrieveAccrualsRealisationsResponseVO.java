package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RetrieveAccrualsRealisationsResponseVO {
	List<DueRealisationVO> dueRealisationVOList;
	
	public void copyTo(RetrieveAccrualsRealisationsResponseVO retrieveAccrualsRealisationsResponseVO) {	// TODO: Do this in LOMBOK way
		retrieveAccrualsRealisationsResponseVO.dueRealisationVOList = this.getDueRealisationVOList();
	}
}
