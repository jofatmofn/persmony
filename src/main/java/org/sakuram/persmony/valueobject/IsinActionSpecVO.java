package org.sakuram.persmony.valueobject;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
public class IsinActionSpecVO {
	long actionDvId;
	boolean toGroupIAs;
	boolean ratioApplicable;
	boolean costRetainedFractionApplicable;
	boolean recordDateApplicable;
	List<IsinActionEntrySpecVO> isinActionEntrySpecVOList;
	
	public void copyFrom(IsinActionSpecVO other) {
		this.actionDvId = other.actionDvId;
		this.toGroupIAs = other.toGroupIAs;
		this.ratioApplicable = other.ratioApplicable;
		this.costRetainedFractionApplicable = other.costRetainedFractionApplicable;
		this.recordDateApplicable = other.recordDateApplicable;
		if (other.isinActionEntrySpecVOList == null) {
			this.isinActionEntrySpecVOList = null;
		} else {
			if (this.isinActionEntrySpecVOList == null) {
				this.isinActionEntrySpecVOList = new ArrayList<IsinActionEntrySpecVO>();
			} else {
				this.isinActionEntrySpecVOList.clear();
			}
			this.isinActionEntrySpecVOList.addAll(other.isinActionEntrySpecVOList);
		}
	}
}
