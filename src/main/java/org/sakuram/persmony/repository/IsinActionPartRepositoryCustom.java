package org.sakuram.persmony.repository;

import java.time.LocalDate;
import java.util.List;

import org.sakuram.persmony.bean.IsinActionPart;

public interface IsinActionPartRepositoryCustom {
	public List<IsinActionPart> findMatchingIsinActionParts(String isin, LocalDate sellDate, Long dematAccount, boolean isIsinIndependent, String orderBy);
}
