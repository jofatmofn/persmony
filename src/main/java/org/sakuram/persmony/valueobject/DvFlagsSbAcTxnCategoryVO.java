package org.sakuram.persmony.valueobject;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DvFlagsSbAcTxnCategoryVO implements DvFlagsVO {
	public enum IOrC {
		BOOKING_DEPENDENT("B"),
		EXPENSE("X"),
		INCOME("C"),
		NONE("N");
		
        private static final Map<String, IOrC> FLAG_TO_ENUM = Stream.of(values())
                .collect(Collectors.toMap(IOrC::getFlag, Function.identity()));

        private final String flag;

        IOrC(String flag) {
            this.flag = flag;
        }
        
        public String getFlag() {
            return flag;
        }

        public static IOrC fromFlag(String flag) {
            return FLAG_TO_ENUM.get(flag);
        }
	}
	
	long id;
	String dvCategory;
	IOrC iOrC;
	
    public String getIorCString() {
        return iOrC.getFlag();
    }
    
    public void setIorCString(String flag) {
        this.iOrC= IOrC.fromFlag(flag);
        if (this.iOrC == null) {
            throw new IllegalArgumentException("Invalid flag: " + flag);
        }
    }
}
