package org.sakuram.persmony.valueobject;

import java.util.Objects;

import org.sakuram.persmony.bean.DomainValue;
import org.sakuram.persmony.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IdValueVO {
	Long id;
	String value;

	public IdValueVO(long id) {
		this.id = id;
    	this.value = Constants.domainValueCache.get(id).getValue();
	}
	
	public IdValueVO(DomainValue domainValue) {
		if (domainValue != null) {
			id = domainValue.getId();
			value = domainValue.getValue();
		}
	}
	
	// https://stackoverflow.com/questions/75407887/setting-value-for-select-element-is-not-showing-in-ui
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdValueVO other = (IdValueVO) obj;
		return Objects.equals(id, other.id);
	}
}
