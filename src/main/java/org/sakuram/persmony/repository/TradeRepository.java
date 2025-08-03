package org.sakuram.persmony.repository;

import java.util.List;

import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long>{
	List<Trade> findByIsinActionPart_IsinAction(IsinAction isinAction);
}
