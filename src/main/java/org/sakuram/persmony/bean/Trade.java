package org.sakuram.persmony.bean;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="trade")
public class Trade {

	@Id
	@SequenceGenerator(name="trade_seq_generator",sequenceName="trade_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="trade_seq_generator")
	@Column(name="id", nullable=false)
	private long id;

	@OneToOne
	@JoinColumn(name="isin_action_part_fk", nullable=false)
	private IsinActionPart isinActionPart;
	
	@Column(name="order_date", nullable=false)
	private Date orderDate;
	
	@Column(name="order_time", length=8, nullable=false)
	private String orderTime;
	
	@Column(name="order_no", length=31, nullable=false)
	private String orderNo;
	
	@Column(name="trade_date", nullable=false)
	private Date tradeDate;
	
	@Column(name="trade_time", length=8, nullable=false)
	private String tradeTime;
	
	@Column(name="trade_no", length=31, nullable=false)
	private String tradeNo;
	
	@Column(name="brokerage_per_unit", nullable=false, columnDefinition="NUMERIC", precision=13, scale=4)
	private Double brokeragePerUnit;
	
}
