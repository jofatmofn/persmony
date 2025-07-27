package org.sakuram.persmony.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;

import org.sakuram.persmony.bean.Isin;
import org.sakuram.persmony.bean.IsinAction;
import org.sakuram.persmony.bean.IsinActionMatch;
import org.sakuram.persmony.bean.Trade;
import org.sakuram.persmony.repository.IsinActionMatchRepository;
import org.sakuram.persmony.repository.IsinActionRepository;
import org.sakuram.persmony.repository.IsinRepository;
import org.sakuram.persmony.util.AppException;
import org.sakuram.persmony.util.Constants;
import org.sakuram.persmony.valueobject.IsinActionVO;
import org.sakuram.persmony.valueobject.IsinCriteriaVO;
import org.sakuram.persmony.valueobject.IsinVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Service
@Transactional
public class DebtEquityMutualService {

	@Autowired
	IsinRepository isinRepository;
	
	@Autowired
	IsinActionRepository isinActionRepository;
	
	@Autowired
	IsinActionMatchRepository isinActionMatchRepository;
	
	public void determineBuyCost(String isin, double sellQuantity, Date sellDate, long dematAccount) {
		// Assumptions:
		//	1) No sell transaction prior to this, is pending to be processed
		//	2) There's no transaction on the same date
		List<IsinAction> matchingIsinActionList;
		IsinAction isinAction, previousIsinAction;
		int ind;
		double quantity, postMatchBalanceQuantity, toMatchQuantity, matchedQuantity;
		IsinActionMatch isinActionMatch;
		List<QuantityMatchedIsinActionVO> quantityMatchedIsinActionVOList;
		List<TransactionVO> transactionVOList;
		
		transactionVOList = new ArrayList<TransactionVO>();
		
		matchingIsinActionList = isinActionRepository.findMatchingIsinActions(isin, sellDate, dematAccount);

		if (matchingIsinActionList.size() > 0) {
			previousIsinAction = null;
			isinAction = null;
			quantity = 0;
			ind = 0;
			while (ind <= matchingIsinActionList.size()) {
				if (ind < matchingIsinActionList.size()) {
					isinAction = matchingIsinActionList.get(ind);
				} else {
					isinAction = null;
				}
				// Treat all the ISIN Actions corresponding to an Action together
				if (ind != 0 && (ind == matchingIsinActionList.size() || isinAction.getAction().getId() != previousIsinAction.getAction().getId() || previousIsinAction.getAction().getEntitledIsin() == null)) {
					System.out.println("<<< Summary: " + quantity + ">>>");
					if (previousIsinAction.getAction().getId() == Constants.ACTION_ID_GIFT_OR_TRANSFER && previousIsinAction.getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT) {
						processTransfer(previousIsinAction, transactionVOList);
					} else if (quantity > 0) {
						transactionVOList.add(new TransactionVO(previousIsinAction.getSettlementDate(), previousIsinAction.getId(), previousIsinAction.getAction().getActionType().getId(), quantity));
					}
					if (ind == matchingIsinActionList.size()) {
						break;
					}
					quantity = 0;
				}
				System.out.println(isinAction.getId() + "::" + isinAction.getDematAccount().getValue() + "::" + isinAction.getIsin().getIsin() + "::" +  Constants.ANSI_DATE_FORMAT.format(isinAction.getSettlementDate()) + "::" + isinAction.getAction().getActionType().getValue() + "::" + isinAction.getQuantity());
				quantity += isinAction.getQuantity() * (isinAction.getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT ? 1 : -1);
				previousIsinAction = isinAction;
				ind++;
			}
		}
		
		Collections.sort(transactionVOList);
		for(TransactionVO transactionVO : transactionVOList) {
			System.out.println(Constants.ANSI_DATE_FORMAT.format(transactionVO.getDate()) + "::" + transactionVO.getIsinActionId() + "::" + transactionVO.getActionType() + "::" + transactionVO.getQuantity());			
		}
		
		quantityMatchedIsinActionVOList = new ArrayList<QuantityMatchedIsinActionVO>();
		toMatchQuantity = sellQuantity;
		for(TransactionVO transactionVO : transactionVOList) {
			isinAction = isinActionRepository.findById(transactionVO.getIsinActionId()).
					orElseThrow(() -> new AppException("Missing ISIN Action " + transactionVO.getIsinActionId(), null));
			quantity = 0;
			for (IsinAction toIsinAction : isinAction.getToIsinActionMatchList().stream()
				    .map(IsinActionMatch::getToIsinAction)
				    .collect(Collectors.toSet())) {
				isinActionMatch = isinActionMatchRepository.findByFromIsinActionAndToIsinAction(isinAction, toIsinAction);
				if (isinActionMatch == null) {
					throw new AppException("Missing match from ISIN Action " + isinAction.getId() + " to " + toIsinAction.getId(), null);
				} else if (isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO &&
						toIsinAction.getAction().getId() != Constants.ACTION_ID_GIFT_OR_TRANSFER) {
					quantity += isinActionMatch.getQuantity();
				}
				System.out.println("From: " + isinActionMatch.getFromIsinAction().getId() + " To: " + isinActionMatch.getToIsinAction().getId() + " Action: " + isinActionMatch.getToIsinAction().getAction().getId() + " Quantity: " + isinActionMatch.getQuantity());
			}
			System.out.println("Current IA " + transactionVO.getIsinActionId() + "::Total: " + isinAction.getQuantity() + "::Matched: " + quantity);
			if (isinAction.getQuantity() > quantity) {	// Some balance is there
				postMatchBalanceQuantity = isinAction.getQuantity() - quantity;
				if (postMatchBalanceQuantity >= toMatchQuantity) {
					matchedQuantity = toMatchQuantity;
					System.out.println("Completed with " + isinAction.getId() + "::" + matchedQuantity);
					quantityMatchedIsinActionVOList.add(new QuantityMatchedIsinActionVO(isinAction.getId(), matchedQuantity));
					toMatchQuantity = 0;
					break;
				} else {
					matchedQuantity = postMatchBalanceQuantity;
					System.out.println("Progressing with " + isinAction.getId() + "::" + matchedQuantity);
					quantityMatchedIsinActionVOList.add(new QuantityMatchedIsinActionVO(isinAction.getId(), matchedQuantity));
					toMatchQuantity -= matchedQuantity;
				}
			}
			// System.out.println(Constants.ANSI_DATE_FORMAT.format(transactionVO.getDate()) + "::" + transactionVO.getActionType() + "::" + transactionVO.getQuantity());
		}
		if (toMatchQuantity > 0) {
			System.err.println("Insufficient Balance!!!");
		} else {
			for(QuantityMatchedIsinActionVO quantityMatchedIsinActionVO : quantityMatchedIsinActionVOList) {
				System.out.println(quantityMatchedIsinActionVO.getIsinActionId() + "::" + quantityMatchedIsinActionVO.getMatchedQuantity());
			}
		}
	}
	
	public List<IsinActionVO> fetchIsinActions(String isinStr, Date priorToDate, Long dematAccount, boolean isTradeLevel) {
		List<IsinAction> isinActionList;
		List<IsinActionVO> isinActionVOList;
		IsinAction isinAction;
		Isin isin;
		Set<Long> processedIsinActionIdSet;
		int ind;
		
		isinActionList = new ArrayList<IsinAction>();
		isin = isinRepository.findById(isinStr).
				orElseThrow(() -> new AppException("Missing ISIN " + isinStr, null));
		addIsinActionList(isinActionList, isinActionRepository.findByIsinOrderBySettlementDateAsc(isin), priorToDate, dematAccount);
		
		processedIsinActionIdSet = new HashSet<Long>();
		ind = 0;
		while (ind < isinActionList.size()) {
			isinAction = isinActionList.get(ind);

			if (!processedIsinActionIdSet.contains(isinAction.getId())) {
				System.out.println("Now processing IA: " + isinAction.getId() + " with A: " + (isinAction.getAction() == null ? "" : isinAction.getAction().getId()));
				// (1) To
				addIsinActionList(isinActionList, Optional.ofNullable(isinAction.getToIsinActionMatchList())
						.orElse(Collections.emptyList())
						.stream()
					    .map(IsinActionMatch::getToIsinAction)
					    .collect(Collectors.toList()), priorToDate, dematAccount);
				// (2) From
				addIsinActionList(isinActionList, Optional.ofNullable(isinAction.getFromIsinActionMatchList())
						.orElse(Collections.emptyList())
						.stream()
					    .map(IsinActionMatch::getFromIsinAction)
					    .collect(Collectors.toList()), priorToDate, dematAccount);
				// (3) Same action
				if (isinAction.getAction() != null) {
					long actionId = isinAction.getAction().getId();	// Declaration and assignment together so that it is effectively final for the lambda below
					Specification<IsinAction> spec = (root, query, cb) -> {
					    List<Predicate> predicates = new ArrayList<>();

				        predicates.add(cb.equal(root.get("action").get("id"), actionId));
					    predicates.add(cb.equal(root.get("isInternal"), false));

					    return cb.and(predicates.toArray(new Predicate[0]));
					};
					addIsinActionList(isinActionList, isinActionRepository.findAll(spec), priorToDate, dematAccount);
				}
			}
			processedIsinActionIdSet.add(isinAction.getId());
			ind++;
			// if (ind == 10) break;
		}
		Collections.sort(isinActionList,
				Comparator.comparing(IsinAction::getSettlementDate)
				.thenComparing(IsinAction::getSettlementSequence, Comparator.nullsLast(Comparator.naturalOrder()))
				);
		isinActionVOList = new ArrayList<IsinActionVO>(isinActionList.size());
		for(IsinAction iA : isinActionList) {
			IsinActionVO isinActionVO = new IsinActionVO(
					iA.getSettlementDate(),
					iA.getIsin().getIsin(),
					iA.getIsin().getSecurityName(),
					iA.getId(),
					null,	// TODO: Replace with Trade id, applicable only for BUY in Secondary market
					(iA.getAction() == null ? iA.getActionType().getValue() : iA.getAction().getActionType().getValue()),
					iA.getQuantity(),
					null,
					null,
					iA.getQuantityBooking().getValue(),
					iA.getDematAccount().getValue()
					);
			if (iA.getTradeList() == null || iA.getTradeList().size() == 0 || !isTradeLevel) {
				isinActionVOList.add(isinActionVO);
			} else {
				for (Trade trade : iA.getTradeList()) {
					isinActionVOList.add(isinActionVO.toBuilder()
							.tradeId(trade.getId())
							.transactionQuantity(trade.getQuantity())
							.build());
				}
			}
		}
		return isinActionVOList;
	}
	
	public List<IsinActionVO> determineBalancesMultiple(String isinStr, Date priorToDate, Long dematAccount) {
		IsinAction isinAction;
		List<IsinActionVO> isinActionVOList;

		isinActionVOList = new ArrayList<IsinActionVO>();
		for(IsinActionVO isinActionVO : fetchIsinActions(isinStr, priorToDate, dematAccount, false)) {
			System.out.println("DetermineFifoMapping: " + isinActionVO.getIsinActionId());
			isinAction = isinActionRepository.findById(isinActionVO.getIsinActionId()).
					orElseThrow(() -> new AppException("Missing ISIN Action " + isinActionVO.getIsinActionId(), null));
			if ((isinAction.getActionType() == null || isinAction.getActionType().getId() != Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER) &&
					isinAction.getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT) {
				for (BalanceQuantityVO balanceQuantityVO : determineBalancesSingle(isinAction)) {
					if (balanceQuantityVO.getPpuBalanceQuantity() > 0) {
						isinActionVOList.add(isinActionVO.toBuilder()
								.tradeId(balanceQuantityVO.getTradeId())
								.balance(balanceQuantityVO.getBalanceQuantity())
								.ppuBalance(balanceQuantityVO.getPpuBalanceQuantity())
								.build());
						// System.out.println(balanceQuantityVO.getIsinActionId() + "::" + balanceQuantityVO.getPpuBalanceQuantity());
					}
				}
			}
		}
		return isinActionVOList;
	}
	
	private List<BalanceQuantityVO> determineBalancesSingle(IsinAction fromIsinAction) {
		List<BalanceQuantityVO> balanceQuantityVOList;
		BalanceQuantityVO balanceQuantityVO;
		
		System.out.println("DetermineBalances: " + fromIsinAction.getId());
		balanceQuantityVOList = new ArrayList<BalanceQuantityVO>();
		if (fromIsinAction.getTradeList() == null || fromIsinAction.getTradeList().size() == 0) {
			balanceQuantityVO = new BalanceQuantityVO(fromIsinAction.getId(), null);
			balanceQuantityVOList.add(balanceQuantityVO);
			balanceQuantityVO.setBalanceQuantity(fromIsinAction.getQuantity());
			balanceQuantityVO.setPpuBalanceQuantity(fromIsinAction.getQuantity());
			System.out.println("From: " + fromIsinAction.getId());
			for(IsinActionMatch isinActionMatch : Optional.ofNullable(fromIsinAction.getToIsinActionMatchList())
					.orElse(Collections.emptyList())) {
				System.out.println("\tTo: " + isinActionMatch.getToIsinAction().getId());
				if (fromIsinAction.getDematAccount().equals(isinActionMatch.getToIsinAction().getDematAccount())) {
					balanceQuantityVO.setBalanceQuantity(balanceQuantityVO.getBalanceQuantity() - isinActionMatch.getQuantity());
				}
				if (isinActionMatch.getToIsinAction().getActionType() == null || isinActionMatch.getToIsinAction().getActionType().getId() != Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER) {
					balanceQuantityVO.setPpuBalanceQuantity(balanceQuantityVO.getPpuBalanceQuantity() - isinActionMatch.getQuantity());
				}
			}
		} else {
			for (Trade trade : fromIsinAction.getTradeList()) {
				balanceQuantityVO = new BalanceQuantityVO(fromIsinAction.getId(), trade.getId());
				balanceQuantityVOList.add(balanceQuantityVO);
				balanceQuantityVO.setBalanceQuantity(trade.getQuantity());
				balanceQuantityVO.setPpuBalanceQuantity(trade.getQuantity());
				System.out.println("Trade: " + trade.getId());
				for(IsinActionMatch isinActionMatch : Optional.ofNullable(fromIsinAction.getToIsinActionMatchList())
						.orElse(Collections.emptyList())) {
					if (isinActionMatch.getFromTrade().getId() == trade.getId()) {
						System.out.println("Trade: " + isinActionMatch.getFromTrade().getId());
						if (fromIsinAction.getDematAccount().equals(isinActionMatch.getToIsinAction().getDematAccount())) {
							balanceQuantityVO.setBalanceQuantity(balanceQuantityVO.getBalanceQuantity() - isinActionMatch.getQuantity());
						}
						if (isinActionMatch.getToIsinAction().getActionType() == null || isinActionMatch.getToIsinAction().getActionType().getId() != Constants.DVID_ISIN_ACTION_TYPE_GIFT_OR_TRANSFER) {
							balanceQuantityVO.setPpuBalanceQuantity(balanceQuantityVO.getPpuBalanceQuantity() - isinActionMatch.getQuantity());
						}
					}
				}				
			}
		}
		return balanceQuantityVOList;
	}
	
	private void addIsinActionList(List<IsinAction> targetList, List<IsinAction> sourceList, Date priorToDate, Long dematAccount) {		
		if (sourceList != null) {
			targetList.addAll(
					sourceList.stream()
					.filter(isinAction -> !targetList.contains(isinAction) &&
							isinAction.getSettlementDate().before(priorToDate) && 
							(dematAccount == null || isinAction.getDematAccount().getId() == dematAccount) &&
							!isinAction.isInternal())
					.collect(Collectors.toList())
					);
		}
	}
	
	private void processTransfer(IsinAction toIsinAction, List<TransactionVO> transactionVOList) {
		IsinAction matchingFromIsinAction;
		
		matchingFromIsinAction = null;
		for (IsinActionMatch isinActionMatch : toIsinAction.getFromIsinActionMatchList()) {
			if (isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_DOUBLE_ENTRY) {
				matchingFromIsinAction = isinActionMatch.getFromIsinAction();
				break;
			}
		}
		if (matchingFromIsinAction == null) {
			throw new AppException("Missing match for \"TO\" ISIN Action " + toIsinAction.getId(), null);
		}
		for (IsinActionMatch isinActionMatch : matchingFromIsinAction.getFromIsinActionMatchList()) {
			if (isinActionMatch.getMatchReason().getId() == Constants.DVID_ISIN_ACTION_MATCH_REASON_FIFO) {
				if (isinActionMatch.getFromIsinAction().getAction().getId() == Constants.ACTION_ID_GIFT_OR_TRANSFER && isinActionMatch.getFromIsinAction().getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT) {
					processTransfer(isinActionMatch.getFromIsinAction(), transactionVOList);
				} else {
					transactionVOList.add(new TransactionVO(isinActionMatch.getFromIsinAction().getSettlementDate(), isinActionMatch.getFromIsinAction().getId(), isinActionMatch.getFromIsinAction().getAction().getActionType().getId(), isinActionMatch.getQuantity()));
				}
			}
		}
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
	
	public void determineOneTimeNpsMatch() {
		Map<Long, Double> balanceQuantityMap;
		double unmatchedQuantity;
		Isin isin;
		
		for (String isinStr : Arrays.asList("NPS-HDFC-1-C", "NPS-HDFC-1-E", "NPS-HDFC-1-G", "NPS-HDFC-2-C", "NPS-HDFC-2-E", "NPS-HDFC-2-G")) {
			
			balanceQuantityMap = new LinkedHashMap<Long, Double>();
			
			isin = isinRepository.findById(isinStr).
					orElseThrow(() -> new AppException("Missing ISIN " + isinStr, null));
			for (IsinAction isinAction : isinActionRepository.findByIsinOrderBySettlementDateAsc(isin)) {
				if (isinAction.getQuantityBooking().getId() == Constants.DVID_BOOKING_CREDIT) {
					balanceQuantityMap.put(isinAction.getId(), isinAction.getQuantity());
				} else {
					unmatchedQuantity = isinAction.getQuantity();
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
	@Getter @Setter
	@AllArgsConstructor
	protected class TransactionVO implements Comparable<TransactionVO> {
		Date date;
		long isinActionId;
		long actionType;
		double quantity;
		
        @Override
        public int compareTo(TransactionVO other) {
            return this.date.compareTo(other.date);
        }
	}
	
	@Getter @Setter
	@AllArgsConstructor
	protected class QuantityMatchedIsinActionVO {
		long isinActionId;
		double matchedQuantity;
	}
	
	@Getter @Setter
	protected class BalanceQuantityVO {
		long isinActionId;
		Long tradeId;
		double balanceQuantity;
		double ppuBalanceQuantity;
		
		BalanceQuantityVO(long isinActionId, Long tradeId) {
			this.isinActionId = isinActionId;
			this.tradeId = tradeId;
		}
	}
}
