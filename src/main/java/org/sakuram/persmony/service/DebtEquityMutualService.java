package org.sakuram.persmony.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sakuram.persmony.bean.Action;
import org.sakuram.persmony.bean.Contract;
import org.sakuram.persmony.bean.ContractEq;
import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.IsinActionMatch;
import org.sakuram.persmony.bean.IsinActionPart;
import org.sakuram.persmony.bean.Trade;
import org.sakuram.persmony.repository.ActionRepository;
import org.sakuram.persmony.repository.ContractEqRepository;
import org.sakuram.persmony.repository.IsinActionMatchRepository;
import org.sakuram.persmony.repository.IsinActionPartRepository;
import org.sakuram.persmony.repository.IsinActionRepository;
import org.sakuram.persmony.repository.IsinRepository;
import org.sakuram.persmony.repository.TradeRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.AccountingIsinActionEntryVO;
import org.sakuram.persmony.valueobject.ActionVO;
import org.sakuram.persmony.valueobject.ContractVO;
import org.sakuram.persmony.valueobject.IdValueVO;
import org.sakuram.persmony.valueobject.IsinActionCreateVO;
import org.sakuram.persmony.valueobject.IsinActionEntrySpecVO;
import org.sakuram.persmony.valueobject.IsinActionSpecVO;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.sakuram.persmony.valueobject.IsinActionWithCVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;
import org.sakuram.persmony.valueobject.LotMatchVO;
import org.sakuram.persmony.valueobject.LotVO;
import org.sakuram.persmony.valueobject.LotWithCVO;
import org.sakuram.persmony.valueobject.LotWithPVO;
import org.sakuram.persmony.valueobject.NpsActionVO;
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
	
	@Autowired
	ContractEqRepository contractEqRepository;
	
	public List<LotWithPVO> fetchLots(String isinStr, LocalDate priorToDate, Long dematAccount, boolean isIsinIndependent, String orderBy) {
		List<IsinActionPart> isinActionPartList;
		List<LotWithPVO> lotWithPVOList;
		
		isinActionPartList = isinActionPartRepository.findMatchingIsinActionParts(isinStr, priorToDate, dematAccount, isIsinIndependent, orderBy);
		
		lotWithPVOList = new ArrayList<LotWithPVO>(isinActionPartList.size());
		for(IsinActionPart isinActionPart : isinActionPartList) {
			lotWithPVOList.add(isinActionPartToWithPVo(isinActionPart, priorToDate));
		}
		return lotWithPVOList;
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
		
		IsinActionSpecVO isinActionSpecVO;
		
		isinActionSpecVO = Constants.ISIN_ACTION_SPEC_MAP.get(isinActionCreateVO.getActionVO().getActionType().getId());
		
		// Action
		if (isinActionCreateVO.getActionId() != null) {
			action = actionRepository.findById(isinActionCreateVO.getActionId()).
					orElseThrow(() -> new AppException("Missing Action " + isinActionCreateVO.getActionId(), null));
		} else if (isinActionCreateVO.getAccountingIAEVOList().size() + isinActionCreateVO.getRealIAEVOList().size() > 1 &&
				isinActionCreateVO.getActionVO().getActionType().getId() != Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER ||
				isinActionSpecVO.isToGroupIAs()) {
			isin = isinRepository.findByIdCaseInsensitive(isinActionCreateVO.getActionVO().getEntitledIsin()).
					orElseThrow(() -> new AppException("Missing ISIN " + isinActionCreateVO.getActionVO().getEntitledIsin(), null));
			action = new Action();
			action.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionVO().getActionType().getId()));
			action.setEntitledIsin(isin);
			action.setFractionalEntitlementCash(null);	// TODO: Later enhancement; FractionalEntitlementCash belongs to Demat level action
			action.setNewSharesPerOld(isinActionCreateVO.getActionVO().getNewSharesPerOld());
			action.setOldSharesBase(isinActionCreateVO.getActionVO().getOldSharesBase());
			action.setCostRetainedFraction(isinActionCreateVO.getActionVO().getCostRetainedFraction());
			action.setRecordDate(isinActionCreateVO.getActionVO().getRecordDate());
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
				// isinAction.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionVO().getActionType().getId()));
				throw new AppException("Unhandled scenario - Internal entry without Action", null);
			} else {
				isinAction.setAction(action);
			}
			isinAction.setDematAccount(Constants.domainValueCache.get(isinActionCreateVO.getDematAccount().getId()));
			isinAction.setInternal(true);
			isinAction.setIsin(isin);
			isinAction.setQuantityBooking(Constants.domainValueCache.get(aIAEVO.getBookingType().getId()));
			isinAction.setSettlementDate(aIAEVO.getSettlementDate());
			isinActionRepository.save(isinAction);
			// IsinActionPart
			isinActionPart = new IsinActionPart();
			isinActionPart.setIsinAction(isinAction);
			isinActionPart.setQuantity(aIAEVO.getTransactionQuantity());
			isinActionPartRepository.save(isinActionPart);
		}
		
		// IsinAction - Real
		for (RealIsinActionEntryVO rIAEVO : isinActionCreateVO.getRealIAEVOList()) {
			List<LotWithPVO> fifoLotWithPVOList;
			
			System.out.println("Now Processing: " + rIAEVO.getIsinActionEntrySpecVO().toString());
			if (rIAEVO.isEmpty()) {
				continue;
			}
			
			isin = isinRepository.findByIdCaseInsensitive(rIAEVO.getIsin()).
					orElseThrow(() -> new AppException("Missing ISIN " + rIAEVO.getIsin(), null));
			
			isinAction = new IsinAction();
			if (action == null) {
				isinAction.setActionType(Constants.domainValueCache.get(isinActionCreateVO.getActionVO().getActionType().getId()));
			} else {
				isinAction.setAction(action);
			}
			
	        if (isinActionCreateVO.getActionVO().getActionType().getId() == Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER &&
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
				fifoLotWithPVOList = isinActionCreateVO.getFifoLotWithPVOList();
				break;
			case FULL_BALANCE:
				// Take care that this fresh fetching does not include lots inserted within this transaction
				fifoLotWithPVOList = fetchLots(isinActionCreateVO.getActionVO().getEntitledIsin(), isinActionCreateVO.getActionVO().getRecordDate(), isinActionCreateVO.getDematAccount().getId(), false, "A")
					.stream()
					.filter(lotWithPVO -> lotWithPVO.getLotVO().getBalance() != null && lotWithPVO.getLotVO().getBalance() > 0)
					.collect(Collectors.toList());
				break;
			default:	// NOT_APPLICABLE:
				fifoLotWithPVOList = null;
				break;
			}
			
			IsinActionEntrySpecVO.IALotCreationType effectiveLotCreationType;
			effectiveLotCreationType = rIAEVO.getIsinActionEntrySpecVO().getLotCreationType();
			if (rIAEVO.getIsinActionEntrySpecVO().getLotCreationType() == IsinActionEntrySpecVO.IALotCreationType.TRADE &&
					isinActionCreateVO.getTradeVOList().size() == 0) {
				effectiveLotCreationType = IsinActionEntrySpecVO.IALotCreationType.ONE;
			}
			switch(effectiveLotCreationType) {
			case TRADE:
				createLots(isinActionCreateVO.getTradeVOList(), null, fifoLotWithPVOList, isinAction, rIAEVO.getHoldingChangeDate());
				break;
			case PROPAGATION:
				// TODO Rounding quantities should NOT be done for Mutual Funds
				if (fifoLotWithPVOList != null) {
					double oldCostOfNewShares = 0;
					int computedQuantitySum = 0;
					
					isinActionPart = null;
					for (LotWithPVO fifoLotWithPVO : fifoLotWithPVOList) {
						if (fifoLotWithPVO.getLotVO().getBalance() <= 0) {
							continue;
						}
						// IsinActionPart
						isinActionPart = new IsinActionPart();
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotDateType()) {
						case OLD:
							isinActionPart.setHoldingChangeDate(fifoLotWithPVO.getLotVO().getHoldingChangeDate());
							break;
						default:
							throw new AppException("Application not ready to handle the type of Lot Date " + rIAEVO.getIsinActionEntrySpecVO().getLotDateType().getFlag(), null);
						}
						isinActionPart.setIsinAction(isinAction);
						
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotQuantityType()) {
						case BALANCE:
							isinActionPart.setQuantity(fifoLotWithPVO.getLotVO().getBalance());
							break;
						case FACTOR_OF_BALANCE:
							isinActionPart.setQuantity((double)(int)(isinActionCreateVO.getActionVO().getNewSharesPerOld().doubleValue() / isinActionCreateVO.getActionVO().getOldSharesBase().doubleValue() * fifoLotWithPVO.getLotVO().getBalance()));
							break;
						default:
							throw new AppException("Application not ready to handle the type of Lot Quantity " + rIAEVO.getIsinActionEntrySpecVO().getLotQuantityType().getFlag(), null);
						}
						computedQuantitySum += isinActionPart.getQuantity().intValue();
						switch(rIAEVO.getIsinActionEntrySpecVO().getLotPriceType()) {
						case OLD:
							isinActionPart.setPricePerUnit(fifoLotWithPVO.getLotVO().getPricePerUnit());
							break;
						case ZERO:
							isinActionPart.setPricePerUnit(0D);
							break;
						case COMPUTED:
							oldCostOfNewShares += (isinActionPart.getQuantity() * fifoLotWithPVO.getLotVO().getPricePerUnit());
							// TODO Handling payout to shareholder
							isinActionPart.setPricePerUnit(isinActionCreateVO.getActionVO().getOldSharesBase().doubleValue() / isinActionCreateVO.getActionVO().getNewSharesPerOld().doubleValue() * fifoLotWithPVO.getLotVO().getPricePerUnit());
							break;
						case SPLIT:
							// As per Spec for Demerger RESULTING ENTITY
							double q2 = isinActionCreateVO.getActionVO().getNewSharesPerOld().doubleValue() / isinActionCreateVO.getActionVO().getOldSharesBase().doubleValue() * fifoLotWithPVO.getLotVO().getBalance();
							isinActionPart.setPricePerUnit(fifoLotWithPVO.getLotVO().getBalance() * fifoLotWithPVO.getLotVO().getPricePerUnit() * (1 - isinActionCreateVO.getActionVO().getCostRetainedFraction()) / (double)(int)q2);
							// Additional not in Spec, exclusive to Demerger REMAINING ENTITY
							IsinActionPart remainingEntityIAPOld = isinActionPartRepository.findById(fifoLotWithPVO.getLotVO().getIsinActionPartId()).
									orElseThrow(() -> new AppException("Missing ISIN Action Part " + fifoLotWithPVO.getLotVO().getIsinActionPartId(), null));
							IsinActionPart remainingEntityIAPNew = new IsinActionPart();
							remainingEntityIAPNew.copyFrom(remainingEntityIAPOld);
							remainingEntityIAPNew.setId(null);
							remainingEntityIAPNew.setPricePerUnit(isinActionCreateVO.getActionVO().getCostRetainedFraction() * fifoLotWithPVO.getLotVO().getPricePerUnit());
							isinActionPartRepository.save(remainingEntityIAPNew);
							
							remainingEntityIAPOld.setOverwritingAction(action);
							isinActionPartRepository.save(remainingEntityIAPOld);
							break;
						case PLUS:
							isinActionPart.setPricePerUnit(Objects.requireNonNullElse(fifoLotWithPVO.getLotVO().getPricePerUnit(), 0).doubleValue() + rIAEVO.getPricePerUnit());
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
						additionalIsinActionPart.setHoldingChangeDate(isinActionPart.getHoldingChangeDate());
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
					isinActionPart.setHoldingChangeDate(rIAEVO.getHoldingChangeDate());
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
				case ZERO:
					isinActionPart.setPricePerUnit(0D);
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
					isinActionPart.setQuantity(fifoLotWithPVOList
							.stream()
							.mapToDouble(balanceIAVO -> balanceIAVO.getLotVO().getBalance())
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
				createLots(tradeVOList, isinActionPart, fifoLotWithPVOList, isinAction, rIAEVO.getHoldingChangeDate());
				break;
			}
			
		}
		
	}

	private void createLots(List<TradeVO> tradeVOList, IsinActionPart isinActionPartInserted, List<LotWithPVO> fifoLotWithPVOList, IsinAction isinAction, LocalDate holdingChangeDate) {
		int fifoInd;
		double currentFifoBalance, lotBalance;
		
		fifoInd = 0;
		currentFifoBalance = 0;
		lotBalance = 0;
		
		if (fifoLotWithPVOList != null && fifoLotWithPVOList.size() > 0) {
			fifoInd = getNextLotWithBalance(fifoLotWithPVOList, fifoInd);
			currentFifoBalance = fifoLotWithPVOList.get(fifoInd).getLotVO().getBalance();
		}

		for (TradeVO tradeVO : tradeVOList) {
			IsinActionPart isinActionPart;
			Trade trade;
			IsinActionMatch isinActionMatch;
			
			if (tradeVO.isEmpty()) {	// isinActionPart already inserted AND trade is not applicable
				trade = null;
				isinActionPart = isinActionPartInserted;
			} else {
				// IsinActionPart
				isinActionPart = new IsinActionPart();
				isinActionPart.setHoldingChangeDate(holdingChangeDate);
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
			if (fifoLotWithPVOList != null) {
				lotBalance = tradeVO.getQuantity();
				while(lotBalance > 0) {
					IsinActionPart fromIsinActionPart;
					
					fifoInd = getNextLotWithBalance(fifoLotWithPVOList, fifoInd);
					currentFifoBalance = fifoLotWithPVOList.get(fifoInd).getLotVO().getBalance();
					
					isinActionMatch = new IsinActionMatch();
					fromIsinActionPart = isinActionPartRepository.findById(fifoLotWithPVOList.get(fifoInd).getLotVO().getIsinActionPartId()).
							orElseThrow(() -> new AppException("Missing From Isin Action", null));
					isinActionMatch.setFromIsinActionPart(fromIsinActionPart);
					isinActionMatch.setMatchReason(Constants.domainValueCache.get(Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO));
					isinActionMatch.setToIsinActionPart(isinActionPart);
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

	public ActionVO fetchAction(long actionId) {
		Action action;
		
		action = actionRepository.findById(actionId).
				orElseThrow(() -> new AppException("Missing Action " + actionId, null));
		
		return new ActionVO(
				new IdValueVO(action.getActionType().getId(), action.getActionType().getValue()),
				(action.getEntitledIsin() == null ? null : action.getEntitledIsin().getIsin()),
				action.getRecordDate(),
				action.getNewSharesPerOld(),
				action.getOldSharesBase(),
				action.getCostRetainedFraction()
				);
	}
	
	public int getNextLotWithBalance(List<LotWithPVO> fifoLotWithPVOList, int fifoInd) {
		while (fifoLotWithPVOList.get(fifoInd).getLotVO().getBalance() <= 0) {
			fifoInd++;
			if (fifoInd >= fifoLotWithPVOList.size()) {
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
	
	public void createNpsAction(NpsActionVO npsActionVO) {
		Action action;
		Isin isin;
		IsinAction templateIsinAction, isinAction;
		ContractEq contractEq;
		IsinActionPart isinActionPart;
		String isinStrStart;
		
		// Action
		action = new Action();
		action.setActionType(Constants.domainValueCache.get(npsActionVO.getActionTypeDvId()));
		actionRepository.save(action);
		
		// ContractEq
		contractEq = new ContractEq();
		contractEq.setNetAmount(BigDecimal.valueOf(npsActionVO.getAmount()));
		contractEq.setStampDuty(BigDecimal.valueOf(npsActionVO.getPaymentCharge()));
		contractEqRepository.save(contractEq);
		
		templateIsinAction = new IsinAction();
		templateIsinAction.setInternal(false);
		templateIsinAction.setSettlementDate(npsActionVO.getSettlementDate());
		templateIsinAction.setAction(action);
		templateIsinAction.setContractEq(contractEq);
		templateIsinAction.setDematAccount(Constants.domainValueCache.get(npsActionVO.getNpsAccountDvId()));
		isinStrStart = "NPS-HDFC-" + npsActionVO.getTier() + "-";  // TODO Hardcoded fund administrator To be taken from DB based on the given NPS Account
		
		// IsinAction E
		isin = isinRepository.findByIdCaseInsensitive(isinStrStart + "E").
				orElseThrow(() -> new AppException("Missing ISIN " + isinStrStart + "E", null));
		isinAction = new IsinAction(templateIsinAction);
		isinAction.setIsin(isin);
		isinAction.setQuantityBooking(Constants.domainValueCache.get(npsActionVO.getEUnits() > 0 ? Constants.DVID_BOOKING_CREDIT : Constants.DVID_BOOKING_DEBIT));
		isinActionRepository.save(isinAction);
		// IsinActionPart E
		isinActionPart = new IsinActionPart();
		isinActionPart.setPricePerUnit(npsActionVO.getENav());
		isinActionPart.setQuantity(Math.abs(npsActionVO.getEUnits()));
		isinActionPart.setIsinAction(isinAction);
		isinActionPartRepository.save(isinActionPart);
		
		// IsinAction C
		isin = isinRepository.findByIdCaseInsensitive(isinStrStart + "C").
				orElseThrow(() -> new AppException("Missing ISIN " + isinStrStart + "C", null));
		isinAction = new IsinAction(templateIsinAction);
		isinAction.setIsin(isin);
		isinAction.setQuantityBooking(Constants.domainValueCache.get(npsActionVO.getCUnits() > 0 ? Constants.DVID_BOOKING_CREDIT : Constants.DVID_BOOKING_DEBIT));
		isinActionRepository.save(isinAction);
		// IsinActionPart C
		isinActionPart = new IsinActionPart();
		isinActionPart.setPricePerUnit(npsActionVO.getCNav());
		isinActionPart.setQuantity(Math.abs(npsActionVO.getCUnits()));
		isinActionPart.setIsinAction(isinAction);
		isinActionPartRepository.save(isinActionPart);
		
		// IsinAction G
		isin = isinRepository.findByIdCaseInsensitive(isinStrStart + "G").
				orElseThrow(() -> new AppException("Missing ISIN " + isinStrStart + "G", null));
		isinAction = new IsinAction(templateIsinAction);
		isinAction.setIsin(isin);
		isinAction.setQuantityBooking(Constants.domainValueCache.get(npsActionVO.getGUnits() > 0 ? Constants.DVID_BOOKING_CREDIT : Constants.DVID_BOOKING_DEBIT));
		isinActionRepository.save(isinAction);
		// IsinActionPart G
		isinActionPart = new IsinActionPart();
		isinActionPart.setPricePerUnit(npsActionVO.getGNav());
		isinActionPart.setQuantity(Math.abs(npsActionVO.getGUnits()));
		isinActionPart.setIsinAction(isinAction);
		isinActionPartRepository.save(isinActionPart);
		
		// TODO Mapping between ContractEq and SavingsAccountTransaction
	}
	
	public IsinActionWithCVO fetchIsinAction(long isinActionId) {
		IsinAction isinAction;
		isinAction = isinActionRepository.findById(isinActionId).
				orElseThrow(() -> new AppException("Missing Action " + isinActionId, null));
		return isinActionToWithCVo(isinAction);
		
	}
	
	public LotWithCVO fetchLot(long isinActionPartId) {
		IsinActionPart isinActionPart;
		isinActionPart = isinActionPartRepository.findById(isinActionPartId).
				orElseThrow(() -> new AppException("Missing Action " + isinActionPartId, null));
		return lotToWithCVo(isinActionPart);
		
	}
	
	private IsinActionVO isinActionToVo(IsinAction isinAction) {
		return new IsinActionVO(
				isinAction.getId(),
				isinAction.getSettlementDate(),
				isinAction.getIsin().getIsin(),
				isinAction.getIsin().getSecurityName(),
				new IdValueVO(isinAction.getEffectiveActionType().getId(), isinAction.getEffectiveActionType().getValue()),
				new IdValueVO(isinAction.getQuantityBooking().getId(), isinAction.getQuantityBooking().getValue()),
				new IdValueVO(isinAction.getDematAccount().getId(), isinAction.getDematAccount().getValue()),
				isinAction.isInternal());
	}
	
	private LotVO isinActionPartToVo(IsinActionPart isinActionPart) {
		return isinActionPartToVo(isinActionPart, null);
	}
	
	private LotVO isinActionPartToVo(IsinActionPart isinActionPart, LocalDate priorToDate) {
		return new LotVO(
				isinActionPart.getId(),
				isinActionPart.getQuantity(),
				isinActionPart.getQuantity() - isinActionPart.getOutQuantity(priorToDate),
				isinActionPart.getHoldingChangeDate(),
				isinActionPart.getPricePerUnit()
				);
	}
	
	private ContractVO contractToVo(Contract contract) {
		return new ContractVO(
				contract.getId(),
				contract.getNetAmount().doubleValue(),
				contract.getStampDuty().doubleValue(),
				contract.getBrokerage().doubleValue(),
				contract.getClearingCharge().doubleValue(),
				contract.getContractDate(),
				contract.getContractNo(),
				contract.getExchangeTransactionCharge().doubleValue(),
				contract.getGst().doubleValue(),
				contract.getSebiTurnoverFee().doubleValue(),
				contract.getSettlementNo(),
				contract.getStt().doubleValue()
				);
	}
	
	private ContractVO contractEqToVo(ContractEq contractEq) {
		return new ContractVO(
				contractEq.getId(),
				contractEq.getNetAmount().doubleValue(),
				(contractEq.getStampDuty() == null ? null : contractEq.getStampDuty().doubleValue()),
				contractEq.getAllotmentDate()
				);
	}
	
	private LotMatchVO isinActionMatchToVo(IsinActionMatch isinActionMatch) {
		return new LotMatchVO(
				isinActionMatch.getFromIsinActionPart().getId(),
				isinActionMatch.getToIsinActionPart().getId(),
				isinActionMatch.getQuantity()
				);
	}
	
	private TradeVO tradeToVo(Trade trade) {
		return new TradeVO(
				trade.getId(),
				null,
				null,
				trade.getBrokeragePerUnit(),
				trade.getOrderDate(),
				trade.getOrderTime(),
				trade.getOrderNo(),
				trade.getTradeDate(),
				trade.getTradeTime(),
				trade.getTradeNo()
				);
	}
	
	private LotWithPVO isinActionPartToWithPVo(IsinActionPart isinActionPart, LocalDate priorToDate) {
		return new LotWithPVO(
				isinActionToVo(isinActionPart.getIsinAction()),
				isinActionPartToVo(isinActionPart, priorToDate)
				);
	}

	private IsinActionWithCVO isinActionToWithCVo(IsinAction isinAction) {
		IsinActionWithCVO isinActionWithCVO;
		List<LotVO> lotVOList;
		
		isinActionWithCVO = new IsinActionWithCVO();
		isinActionWithCVO.setIsinActionVO(isinActionToVo(isinAction));
		lotVOList = new ArrayList<LotVO>();
		isinActionWithCVO.setLotVOList(lotVOList);
		for(IsinActionPart isinActionPart : isinAction.getIsinActionPartList()) {
			lotVOList.add(isinActionPartToVo(isinActionPart));
		}
		
		if (isinAction.getContract() != null) {
			isinActionWithCVO.setContractVO(contractToVo(isinAction.getContract()));
		} else if (isinAction.getContractEq() != null) {
			isinActionWithCVO.setContractVO(contractEqToVo(isinAction.getContractEq()));
		}

		return isinActionWithCVO;
	}
	
	private LotWithCVO lotToWithCVo(IsinActionPart isinActionPart) {
		LotWithCVO lotWithCVO;
		List<IsinActionMatch> isinActionMatchList;
		List<LotMatchVO> lotMatchVOList;
		
		lotWithCVO = new LotWithCVO();
		lotWithCVO.setLotVO(isinActionPartToVo(isinActionPart));
		isinActionMatchList = isinActionPart.getFromIsinActionMatchList();
		if (isinActionMatchList.size() > 0) {
			lotMatchVOList = new ArrayList<LotMatchVO>();
			lotWithCVO.setReceivedFromLotMatchVOList(lotMatchVOList);
			for(IsinActionMatch isinActionMatch : isinActionMatchList) {
				lotMatchVOList.add(isinActionMatchToVo(isinActionMatch));
			}
		}
		isinActionMatchList = isinActionPart.getToIsinActionMatchList();
		if (isinActionMatchList.size() > 0) {
			lotMatchVOList = new ArrayList<LotMatchVO>();
			lotWithCVO.setSentToLotMatchVOList(lotMatchVOList);
			for(IsinActionMatch isinActionMatch : isinActionMatchList) {
				lotMatchVOList.add(isinActionMatchToVo(isinActionMatch));
			}
		}
		
		if (isinActionPart.getTrade() != null) {
			lotWithCVO.setTradeVO(tradeToVo(isinActionPart.getTrade()));
		}
		return lotWithCVO;
	}
}
