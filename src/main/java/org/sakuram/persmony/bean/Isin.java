package org.sakuram.persmony.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

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
	@JoinColumn(name="security_type_fk", nullable=false)
	private DomainValue securityType;	// Maps to Transaction Category

	private Long stockId;	// TODO: FK to new Stock entity
	
	@PrePersist
	@PreUpdate
	void normalizeId() {
		if (isin != null) {
			isin = isin.toUpperCase();
		}
	}
}
