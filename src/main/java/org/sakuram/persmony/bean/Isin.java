package org.sakuram.persmony.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name="isin")
public class Isin {

	@Id
	@Column(name="isin", length=15, nullable=false)
	private String isin;

	@Column(name="company_name", length=63, nullable=false)
	private String companyName;

	@Column(name="security_name", length=127, nullable=false)
	private String securityName;

	@ManyToOne
	@JoinColumn(name="security_type", nullable=false)
	private DomainValue securityType;	// Maps to Transaction Category
	
}
