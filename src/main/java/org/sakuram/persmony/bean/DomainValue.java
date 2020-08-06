package org.sakuram.persmony.bean;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@EnableAutoConfiguration
@ComponentScan
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY)
@Table(name="domain_value")
public class DomainValue {

	@Id	 
	@Column(name="id", nullable=false)
	private long id;
	
	@Column(name="category", nullable=false, length=7)
	private String category;
	
	@Column(name="value", nullable=false, length=127)
	private String value;
	
	@Column(name="flags_csv", nullable=true, length=1023)
	private String flagsCsv;

}
