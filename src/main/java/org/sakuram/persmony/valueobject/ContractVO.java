package org.sakuram.persmony.valueobject;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ContractVO {
	long contractId;
	double netAmount;
	Double stampDuty;
	
	// Contract
	Double brokerage;
	Double clearingCharge;
	LocalDate contractDate;
	String contractNo;
	Double exchangeTransactionCharge;
	Double gst;
	Double sebiTurnoverFee;
	String settlementNo;
	Double stt;
	// ContractEq
	LocalDate allotmentDate;
	
	public ContractVO(long contractId, double netAmount, Double stampDuty, double brokerage, double clearingCharge, LocalDate contractDate, String contractNo, double exchangeTransactionCharge, double gst, double sebiTurnoverFee, String settlementNo, double stt) {
		this.contractId = contractId;
		this.netAmount = netAmount;
		this.stampDuty = stampDuty;
		this.brokerage = brokerage;
		this.clearingCharge = clearingCharge;
		this.contractDate = contractDate;
		this.contractNo = contractNo;
		this.exchangeTransactionCharge = exchangeTransactionCharge;
		this.gst = gst;
		this.sebiTurnoverFee = sebiTurnoverFee;
		this.settlementNo = settlementNo;
		this.stt = stt;
	}
	
	public ContractVO(long contractId, double netAmount, Double stampDuty, LocalDate allotmentDate) {
		this.contractId = contractId;
		this.netAmount = netAmount;
		this.stampDuty = stampDuty;
		this.allotmentDate = allotmentDate;
	}
}
