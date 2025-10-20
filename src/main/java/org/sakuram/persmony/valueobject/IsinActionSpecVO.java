package org.sakuram.persmony.valueobject;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder(toBuilder=true)
public class IsinActionSpecVO {
	List<IsinActionEntrySpecVO> isinActionEntrySpecVOList;
}
