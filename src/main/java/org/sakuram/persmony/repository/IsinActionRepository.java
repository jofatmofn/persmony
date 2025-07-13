package org.sakuram.persmony.repository;

import java.sql.Date;
import java.util.List;

import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IsinActionRepository extends JpaRepository<IsinAction, Long>, JpaSpecificationExecutor<IsinAction> {

	@Query(nativeQuery=true, value=
			"SELECT IA.* "
			+ "FROM isin_action IA "
			+ "LEFT OUTER JOIN action A ON IA.action_fk = A.id "
			+ "WHERE IA.settlement_date < :sellDate "
			+ "AND (IA.isin_fk = :isin AND A.entitled_isin_fk IS NULL OR A.entitled_isin_fk = :isin) "
			+ "AND IA.demat_account_fk = :dematAccount "
			+ "ORDER BY IA.settlement_date"
			)
	public List<IsinAction> findMatchingIsinActions(@Param("isin") String isin, @Param("sellDate") Date sellDate, @Param("dematAccount") long dematAccount);
	
	public List<IsinAction> findByIsinOrderBySettlementDateAsc(Isin isin);

}
