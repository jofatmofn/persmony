package org.sakuram.persmony.repository;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.IsinActionPart;

public interface IsinActionPartRepositoryCustom {
	public List<IsinActionPart> findMatchingIsinActionParts(String isin, Date sellDate, Long dematAccount, boolean isIsinIndependent, String orderBy);
}
