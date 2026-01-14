package org.sakuram.persmony.bean;

import java.sql.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
