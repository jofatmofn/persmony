package org.sakuram.persmony.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SbAcTxnCategoryVO {
	Long sbAcTxnCategoryId;
	IdValueVO transactionCategory;
	IdValueVO endAccountReference;
	Character groupId;
	Double amount;
}
