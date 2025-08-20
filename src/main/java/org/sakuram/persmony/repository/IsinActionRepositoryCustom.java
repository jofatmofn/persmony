package org.sakuram.persmony.repository;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.IsinAction;

public interface IsinActionRepositoryCustom {
	public List<IsinAction> findIsinIndependentIsinActions(String isin, Date sellDate, Long dematAccount);
}
