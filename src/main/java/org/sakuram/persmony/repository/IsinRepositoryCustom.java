package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.valueobject.IsinCriteriaVO;

public interface IsinRepositoryCustom {
	public List<Object[]> searchIsins(IsinCriteriaVO securityCriteriaVO);
}
