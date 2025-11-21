package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IsinActionRepository extends JpaRepository<IsinAction, Long>, JpaSpecificationExecutor<IsinAction>, IsinActionRepositoryCustom {

	public List<IsinAction> findByIsinOrderBySettlementDateAsc(Isin isin);

	public List<IsinAction> findByIsinInOrderBySettlementDateAscSettlementSequenceAsc(List<Isin> isinList);
}
