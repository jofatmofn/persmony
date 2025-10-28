package org.sakuram.persmony.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sakuram.persmony.bean.Action;
import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.IsinActionMatch;
import org.sakuram.persmony.bean.IsinActionPart;
import org.sakuram.persmony.bean.Trade;
import org.sakuram.persmony.repository.ActionRepository;
import org.sakuram.persmony.repository.IsinActionMatchRepository;
import org.sakuram.persmony.repository.IsinActionPartRepository;
import org.sakuram.persmony.repository.IsinActionRepository;
import org.sakuram.persmony.repository.IsinRepository;
import org.sakuram.persmony.repository.TradeRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.AccountingIsinActionEntryVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionCreateVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;
import org.sakuram.persmony.valueobject.LotVO;
import org.sakuram.persmony.valueobject.RealIsinActionEntryVO;
import org.sakuram.persmony.valueobject.TradeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DebtEquityMutualService {

	@Autowired
	IsinRepository isinRepository;
	
	@Autowired
	IsinActionRepository isinActionRepository;
	
	@Autowired
	TradeRepository tradeRepository;
	
	@Autowired
	IsinActionMatchRepository isinActionMatchRepository;

	@Autowired
	ActionRepository actionRepository;
	
	@Autowired
	IsinActionPartRepository isinActionPartRepository;
	
	public List<LotVO> fetchLots(String isinStr, Date priorToDate, Long dematAccount, boolean isIsinIndependent, String orderBy) {
		List<IsinActionPart> isinActionPartList;
		List<IsinActionMatch> isinActionMatchList;
		List<LotVO> lotVOList;
		
		isinActionPartList = isinActionPartRepository.findMatchingIsinActionParts(isinStr, priorToDate, dematAccount, isIsinIndependent, orderBy);
		
		isinActionMatchList = isinActionPartList.stream()	// Given the children(IAP) list, get all the parent(IA)'s all the children(IAM)
			    .map(IsinActionPart::getIsinAction)
			    .filter(Objects::nonNull)
			    .flatMap(isinAction -> isinAction.getToIsinActionMatchList().stream())
			    .distinct()
			    .collect(Collectors.toList());
		lotVOList = new ArrayList<LotVO>(isinActionPartList.size());
		for(IsinActionPart isinActionPart : isinActionPartList) {
			IsinAction isinAction;
			double balance;
			
			isinAction = isinActionPart.getIsinAction();
			balance = isinActionPart.getQuantity() - isinActionMatchList
					.stream()
					.filter(isinActionMatch -> 
						isinActionMatch.getFromIsinAction().getId() == isinAction.getId() &&
						isinActionMatch.getFromIsinAction().getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT &&
						!isinActionMatch.getFromIsinAction().isInternal() &&
						(isinActionPart.getTrade() == null && isinActionMatch.getFromTrade() == null || isinActionPart.getTrade().getId() == isinActionMatch.getFromTrade().getId()))
					.mapToDouble(isinActionMatch -> isinActionMatch.getQuantity())
					.sum();
			lotVOList.add(new LotVO(
					new IsinActionVO(
						isinAction.getId(),
						isinAction.getSettlementDate(),
						isinAction.getIsin().getIsin(),
						isinAction.getIsin().getSecurityName(),
						new IdValueVO(isinAction.getEffectiveActionType().getId(), isinAction.getEffectiveActionType().getValue()),
						new IdValueVO(isinAction.getQuantityBooking().getId(), isinAction.getQuantityBooking().getValue()),
						new IdValueVO(isinAction.getDematAccount().getId(), isinAction.getDematAccount().getValue()),
						isinAction.isInternal()),
					(isinActionPart.getTrade() == null ? null : isinActionPart.getTrade().getId()),
					isinActionPart.getQuantity(),
					balance,
					isinActionPart.getAcquisitionDate(),
					isinActionPart.getPricePerUnit()
					)
					);
		}
		return lotVOList;
	}
	
	public List<IsinVO> searchSecurities(IsinCriteriaVO isinCriteriaVO) {
		List<Object[]> isinList;
		List<IsinVO> isinVOList;
		
		isinList = isinRepository.searchIsins(isinCriteriaVO);
		isinVOList = new ArrayList<IsinVO>(isinList.size());
		for(Object[] columns : isinList) {
			isinVOList.add(new IsinVO(columns));
		}
		return isinVOList;
	}
	
	public void createIsinActions(IsinActionCreateVO isinActionCreateVO) {
		Action action;
		Isin isin;
		IsinAction isinAction;
		IsinActionPart isinActionPart;
		
		Optional<RealIsinActionEntryVO> optionalRealIsinActionEntryVO;
		Short newSharesPerOld, oldSharesBase;
		
		newSharesPerOld = oldSharesBase = null;
		optionalRealIsinActionEntryVO = isinActionCreateVO.getRealIAEVOList().stream()
			.filter(rIAEVO -> rIAEVO.getNewSharesPerOld() != null && rIAEVO.getOldSharesBase() != null)
			.findAny();
		if (optionalRealIsinActionEntryVO.isPresent()) {
			newSharesPerOld = optionalRealIsinActionEntryVO.get().getNewSharesPerOld();
			oldSharesBase = optionalRealIsinActionEntryVO.get().getOldSharesBase();
		}
		
		// Action
		if (isinActionCreateVO.getAccountingIAEVOList().size() + isinActionCreateVO.getRealIAEVOList().size() > 1) {
			isin = isinRepository.findByIdCaseInsensitive(isinActionCreateVO.getEntitledIsin()).
					orElseThrow(() -> new AppException("Missing ISIN " + isinActionCreateVO.getEntitledIsin(), null));
			action = new Action();
			action.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionType().getId()));
			action.setEntitledIsin(isin);
			action.setFractionalEntitlementCash(null);	// TODO: Later enhancement
			action.setNewSharesPerOld(newSharesPerOld);
			action.setOldSharesBase(oldSharesBase);
			action.setRecordDate(isinActionCreateVO.getRecordDate());	// TODO: Record Date is not stored, if Action is not created
			actionRepository.save(action);
		} else {
			action = null;
		}
		
		// IsinAction - Accounting
		for (AccountingIsinActionEntryVO aIAEVO : isinActionCreateVO.getAccountingIAEVOList()) {
			isin = isinRepository.findByIdCaseInsensitive(aIAEVO.getIsin()).
					orElseThrow(() -> new AppException("Missing ISIN " + aIAEVO.getIsin(), null));
			
			isinAction = new IsinAction();
			if (action == null) {
				isinAction.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionType().getId()));
			} else {
				isinAction.setAction(action);
			}
			isinAction.setDematAccount(Constants.domainValueCache.get(isinActionCreateVO.getDematAccount().getId()));
			isinAction.setInternal(true);
			isinAction.setIsin(isin);
			isinAction.setQuantityBooking(Constants.domainValueCache.get(aIAEVO.getBookingType().getId()));
			isinAction.setSettlementDate(aIAEVO.getSettlementDate());
			isinActionRepository.save(isinAction);
		}
		
		// IsinAction - Real
		for (RealIsinActionEntryVO rIAEVO : isinActionCreateVO.getRealIAEVOList()) {
			List<LotVO> fifoLotVOList;
			
			isin = isinRepository.findByIdCaseInsensitive(rIAEVO.getIsin()).
					orElseThrow(() -> new AppException("Missing ISIN " + rIAEVO.getIsin(), null));
			
			isinAction = new IsinAction();
			if (action == null) {
				isinAction.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionType().getId()));
			} else {
				isinAction.setAction(action);
			}
			
	        if (rIAEVO.getIsinActionEntrySpecVO().getActionDvId() == Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER &&
	        		rIAEVO.getIsinActionEntrySpecVO().getEntrySpecName().equals(Constants.ACTION_TYPE_GIFT_OR_TRANSFER_ENTRY_SPEC_NAME_RECEIVE)) {
				isinAction.setDematAccount(Constants.domainValueCache.get(rIAEVO.getDematAccount().getId()));
	        } else {
				isinAction.setDematAccount(Constants.domainValueCache.get(isinActionCreateVO.getDematAccount().getId()));
	        }

	        isinAction.setSettlementSequence(null);	// TODO Future Enhancement
			isinAction.setInternal(false);
			isinAction.setIsin(isin);
			isinAction.setQuantityBooking(Constants.domainValueCache.get(rIAEVO.getIsinActionEntrySpecVO().getBookingTypeDvId()));
			isinAction.setSettlementDate(rIAEVO.getSettlementDate());
			isinActionRepository.save(isinAction);
			
			switch(rIAEVO.getIsinActionEntrySpecVO().getFifoMappingType()) {
			case USER_CHOICE:
			case PREVIOUS_USER_CHOICE:
				fifoLotVOList = isinActionCreateVO.getFifoLotVOList();
				break;
			case FULL_BALANCE:
				// Take care that this fresh fetching does not include lots inserted within this transaction
				fifoLotVOList = fetchLots(isinActionCreateVO.getEntitledIsin(), isinActionCreateVO.getRecordDate(), isinActionCreateVO.getDematAccount().getId(), false, "A")
					.stream()
					.filter(lotVO -> lotVO.getBalance() != null && lotVO.getBalance() > 0)
					.collect(Collectors.toList());
				break;
			default:	// NOT_APPLICABLE:
				fifoLotVOList = null;
				break;
			}
			
			switch(rIAEVO.getIsinActionEntrySpecVO().getLotCreationType()) {
			case TRADE:
				createLots(isinActionCreateVO.getTradeVOList(), fifoLotVOList, isinAction);
				break;
			case PROPAGATION:
				// TODO Rounding quantities should NOT be done for Mutual Funds
				if (fifoLotVOList != null) {
					double oldCostOfNewShares = 0;
					int computedQuantitySum = 0;
					
					isinActionPart = null;
					for (LotVO fifoLotVO : fifoLotVOList) {
						if (fifoLotVO.getBalance() <= 0) {
							continue;
						}
						// IsinActionPart
						isinActionPart = new IsinActionPart();
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotDateType()) {
						case OLD:
							isinActionPart.setAcquisitionDate(fifoLotVO.getAcquisitionDate());
							break;
						default:
							throw new AppException("Application not ready to handle the type of Lot Date " + rIAEVO.getIsinActionEntrySpecVO().getLotDateType().getFlag(), null);
						}
						isinActionPart.setIsinAction(isinAction);
						
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotQuantityType()) {
						case BALANCE:
							isinActionPart.setQuantity(fifoLotVO.getBalance());
							break;
						case FACTOR_OF_BALANCE:
							isinActionPart.setQuantity((double)(int)(newSharesPerOld / oldSharesBase * fifoLotVO.getBalance()));
							break;
						default:
							throw new AppException("Application not ready to handle the type of Lot Quantity " + rIAEVO.getIsinActionEntrySpecVO().getLotQuantityType().getFlag(), null);
						}
						computedQuantitySum += isinActionPart.getQuantity().intValue();
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotPriceType()) {
						case OLD:
							isinActionPart.setPricePerUnit(fifoLotVO.getPricePerUnit());
							break;
						case ZERO:
							isinActionPart.setPricePerUnit(0D);
							break;
						case COMPUTED:
							oldCostOfNewShares += (isinActionPart.getQuantity() * fifoLotVO.getPricePerUnit());
							isinActionPart.setPricePerUnit(oldSharesBase / newSharesPerOld * fifoLotVO.getPricePerUnit());
							break;
						default:
							throw new AppException("Application not ready to handle the type of Lot Price " + rIAEVO.getIsinActionEntrySpecVO().getLotPriceType().getFlag(), null);
						}
						isinActionPartRepository.save(isinActionPart);
					}
					// One additional lot for the sum of fractional entitlements
					if (rIAEVO.getQuantity() > computedQuantitySum) {
						IsinActionPart additionalIsinActionPart;
						additionalIsinActionPart = new IsinActionPart();
						additionalIsinActionPart.setAcquisitionDate(isinActionPart.getAcquisitionDate());
						additionalIsinActionPart.setIsinAction(isinAction);
						additionalIsinActionPart.setPricePerUnit(oldCostOfNewShares / rIAEVO.getQuantity());
						additionalIsinActionPart.setQuantity((double)(int)(rIAEVO.getQuantity() - computedQuantitySum));
						isinActionPartRepository.save(additionalIsinActionPart);
					}
					// rIAEVO.getTransactionQuantity()
				}
				break;
			default:	// ONE:
				List<TradeVO> tradeVOList;
				
				// IsinActionPart
				isinActionPart = new IsinActionPart();
				switch(rIAEVO.getIsinActionEntrySpecVO().getLotDateType()) {
				case INPUT:
					isinActionPart.setAcquisitionDate(rIAEVO.getSettlementDate());
					break;
				default:
					throw new AppException("Application not ready to handle the type of Lot Date " + rIAEVO.getIsinActionEntrySpecVO().getLotDateType().getFlag(), null);
				}
				isinActionPart.setIsinAction(isinAction);
				switch(rIAEVO.getIsinActionEntrySpecVO().getLotPriceType()) {
				case INPUT:
					isinActionPart.setPricePerUnit(rIAEVO.getPricePerUnit());
					break;
				case NULL:
					isinActionPart.setPricePerUnit(null);
					break;
				default:
					throw new AppException("Application not ready to handle the type of Lot Price " + rIAEVO.getIsinActionEntrySpecVO().getLotPriceType().getFlag(), null);
				}
				
				switch(rIAEVO.getIsinActionEntrySpecVO().getLotQuantityType()) {
				case INPUT:
				case FACTOR_OF_BALANCE:
					isinActionPart.setQuantity(rIAEVO.getQuantity());
					break;
				case BALANCE:
					isinActionPart.setQuantity(fifoLotVOList
							.stream()
							.mapToDouble(balanceIAVO -> balanceIAVO.getBalance())
							.sum()
					);
					break;
				default:
					throw new AppException("Application not ready to handle the type of Lot Quantity " + rIAEVO.getIsinActionEntrySpecVO().getLotDateType().getFlag(), null);
				}
				isinActionPartRepository.save(isinActionPart);
				
				tradeVOList = new ArrayList<TradeVO>();
				// TODO Enhancement to accept BrokeragePerUnit in the UI
				tradeVOList.add(new TradeVO(null, rIAEVO.getQuantity(), rIAEVO.getPricePerUnit(), null, null, null, null, null, null, null));
				createLots(tradeVOList, fifoLotVOList, isinAction);
				break;
			}
			
		}
		
	}

	private void createLots(List<TradeVO> tradeVOList, List<LotVO> fifoLotVOList, IsinAction isinAction) {
		int fifoInd;
		double currentFifoBalance, lotBalance;
		
		fifoInd = 0;
		currentFifoBalance = 0;
		lotBalance = 0;
		
		if (fifoLotVOList != null && fifoLotVOList.size() > 0) {
			fifoInd = getNextLotWithBalance(fifoLotVOList, fifoInd);
			currentFifoBalance = fifoLotVOList.get(fifoInd).getBalance();
		}

		for (TradeVO tradeVO : tradeVOList) {
			IsinActionPart isinActionPart;
			Trade trade;
			IsinActionMatch isinActionMatch;
			
			if (tradeVO.isEmpty()) {	// isinActionPart already inserted AND trade is not applicable
				trade = null;
			} else {
				// IsinActionPart
				isinActionPart = new IsinActionPart();
				isinActionPart.setAcquisitionDate(tradeVO.getTradeDate());
				isinActionPart.setIsinAction(isinAction);
				isinActionPart.setPricePerUnit(tradeVO.getPricePerUnit());
				isinActionPart.setQuantity(tradeVO.getQuantity());
				isinActionPartRepository.save(isinActionPart);
				
				// Trade
				trade = new Trade();
				trade.setBrokeragePerUnit(tradeVO.getBrokeragePerUnit());
				trade.setIsinActionPart(isinActionPart);
				trade.setOrderDate(tradeVO.getOrderDate());
				trade.setOrderNo(tradeVO.getOrderNo());
				trade.setOrderTime(tradeVO.getOrderTime());
				trade.setTradeDate(tradeVO.getTradeDate());
				trade.setTradeNo(tradeVO.getTradeNo());
				trade.setTradeTime(tradeVO.getTradeTime());
				tradeRepository.save(trade);
			}
			
			// IsinActionMatch
			if (fifoLotVOList != null) {
				lotBalance = tradeVO.getQuantity();
				while(lotBalance > 0) {
					IsinAction fromIsinAction;
					Trade fromTrade;
					
					fifoInd = getNextLotWithBalance(fifoLotVOList, fifoInd);
					currentFifoBalance = fifoLotVOList.get(fifoInd).getBalance();
					
					isinActionMatch = new IsinActionMatch();
					fromIsinAction = isinActionRepository.findById(fifoLotVOList.get(fifoInd).getIsinActionVO().getIsinActionId()).
							orElseThrow(() -> new AppException("Missing From Isin Action", null));
					isinActionMatch.setFromIsinAction(fromIsinAction);
					if (fifoLotVOList.get(fifoInd).getTradeId() != null) {
						fromTrade = tradeRepository.findById(fifoLotVOList.get(fifoInd).getTradeId()).
								orElseThrow(() -> new AppException("Missing From Isin Trade", null));
						isinActionMatch.setFromTrade(fromTrade);
					}
					isinActionMatch.setMatchReason(Constants.domainValueCache.get(Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO));
					isinActionMatch.setToIsinAction(isinAction);
					isinActionMatch.setToTrade(trade);
					if (currentFifoBalance > lotBalance) {
						isinActionMatch.setQuantity(lotBalance);
						currentFifoBalance -= lotBalance;
					} else {
						isinActionMatch.setQuantity(currentFifoBalance);
						currentFifoBalance = 0;
						fifoInd++;
					}
					lotBalance -= isinActionMatch.getQuantity();
					isinActionMatchRepository.save(isinActionMatch);
				}
			}
		}
		
		if (lotBalance > 0) {
			throw new AppException("Insufficient balance to map from Credits to Debits", null);
		}
	}

	public int getNextLotWithBalance(List<LotVO> fifoLotVOList, int fifoInd) {
		while (fifoLotVOList.get(fifoInd).getBalance() <= 0) {
			fifoInd++;
			if (fifoInd >= fifoLotVOList.size()) {
				throw new AppException("Insufficient balance to map from Credits to Debits", null);
			}
		}
		return fifoInd;
	}
	
	public void determineOneTimeNpsMatch() {
		Map<Long, Double> balanceQuantityMap;
		double unmatchedQuantity;
		Isin isin;
		
		for (String isinStr : Arrays.asList("NPS-HDFC-1-C", "NPS-HDFC-1-E", "NPS-HDFC-1-G", "NPS-HDFC-2-C", "NPS-HDFC-2-E", "NPS-HDFC-2-G")) {
			
			balanceQuantityMap = new LinkedHashMap<Long, Double>();
			
			isin = isinRepository.findByIdCaseInsensitive(isinStr).
					orElseThrow(() -> new AppException("Missing ISIN " + isinStr, null));
			for (IsinAction isinAction : isinActionRepository.findByIsinOrderBySettlementDateAsc(isin)) {
				if (isinAction.getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT) {
					balanceQuantityMap.put(isinAction.getId(), isinAction.getComputedQuantity());
				} else {
					unmatchedQuantity = isinAction.getComputedQuantity();
					for (Map.Entry<Long, Double> balanceQuantityEntry : balanceQuantityMap.entrySet()) {
						if (balanceQuantityEntry.getValue() > 0D) {
							if (balanceQuantityEntry.getValue() >= unmatchedQuantity) {
								System.out.println(String.format("INSERT INTO isin_action_match(from_isin_action_fk, to_isin_action_fk, quantity, match_reason_fk) VALUES(%d, %d, %f, 343);", balanceQuantityEntry.getKey(), isinAction.getId(), unmatchedQuantity));
								balanceQuantityEntry.setValue(balanceQuantityEntry.getValue() - unmatchedQuantity);
								unmatchedQuantity = 0;
								break;
							} else {
								System.out.println(String.format("INSERT INTO isin_action_match(from_isin_action_fk, to_isin_action_fk, quantity, match_reason_fk) VALUES(%d, %d, %f, 343);", balanceQuantityEntry.getKey(), isinAction.getId(), balanceQuantityEntry.getValue()));
								unmatchedQuantity -= balanceQuantityEntry.getValue();
								balanceQuantityEntry.setValue(0D);
							}
						}
					}
					if (unmatchedQuantity > 0D) {
						throw new AppException("Insufficient balance to handle the ISIN Action " + isinAction.getId() + ".", null);
					}
				}
			}
		}
	}
}
