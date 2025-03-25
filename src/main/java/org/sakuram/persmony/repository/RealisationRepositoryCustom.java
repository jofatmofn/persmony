package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.valueobject.RetrieveAccrualsRealisationsRequestVO;

public interface RealisationRepositoryCustom {
	public List<Object[]> retrieveAccrualsRealisations(RetrieveAccrualsRealisationsRequestVO retrieveAccrualsRealisationsRequestVO);

}
