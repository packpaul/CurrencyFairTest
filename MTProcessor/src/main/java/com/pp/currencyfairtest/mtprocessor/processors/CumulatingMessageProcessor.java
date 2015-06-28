/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.processors;

import com.pp.currencyfairtest.mtprocessor.dto.Countries;
import com.pp.currencyfairtest.mtprocessor.dto.CumulatingMessageProcessingBoard;
import com.pp.currencyfairtest.mtprocessor.dto.CumulatingMessageProcessingBoard.CurrencySum;
import com.pp.currencyfairtest.mtprocessor.dto.Message;
import com.pp.currencyfairtest.mtprocessor.utils.Triple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class CumulatingMessageProcessor extends MessageProcessor implements DisposableBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CumulatingMessageProcessor.class);
    
    private static final String ALL_COUNTRIES = "*";
    
    /**
     * Tasks are guaranteed to execute sequentially in this executor
     */
    private final ExecutorService processingExecutor = Executors.newSingleThreadExecutor();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    private final Map<String, DataBag> countryToDataMap = new TreeMap<>();
    
    @Override
    public void destroy() throws Exception {
        processingExecutor.shutdown();
    }
    
    @Override
    public void post(final Message message) {
        processingExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    process(message);
                } catch(Throwable tr) {
                    LOGGER.error("Error on processing!", tr);
                }
            }
        });
    }
    
    /**
     * This method sums up all incoming currency amounts that is {@code Message.currencyTo}
     * is subtracted from the bank, and {@code Message.currencyFrom} is added to it.
     * The processed data is intended to show the revenue of the trading company along
     * separate currencies.
     * @param message 
     */
    void process(Message message) {
        LOGGER.debug("Processing message(consumeId={})", message.consumedId);
        
        final String country = message.originatingCountry;
        
        rwLock.writeLock().lock();
        try {
            sumUpCurrencies(country, message);
            sumUpCurrencies(ALL_COUNTRIES, message);
        } finally {
            rwLock.writeLock().unlock();
        }
        if (tradingHall != null) {
            tradingHall.updateTraders(endpoint);
        }
    }
    
    private void sumUpCurrencies(String country, Message message) {
        if (country == null) {
            throw new IllegalArgumentException("Country has to be provided!");
        }
        
        DataBag data = countryToDataMap.get(country);
        if (data == null) {
            data = new DataBag();
            countryToDataMap.put(country, data);
        }

        if (! data.add(message.currencyFrom, message.amountSell, message.consumedId)) {
            LOGGER.warn("{} currency wasn't added for the data is old!", message.currencyFrom);
        }
        if (! data.subtract(message.currencyTo, message.amountBuy, message.consumedId)) {
            LOGGER.warn("{} currency wasn't subtracted for the data is old!", message.currencyTo);
        }
    }

    @Override
    public CumulatingMessageProcessingBoard getBoard(Object input) {
        
        final String country;
        final Long snapshotId;
        
        if (input == null) {
            snapshotId = 0L;
            country = ALL_COUNTRIES;
        } else if (input instanceof Object[]) {
            Object[] args = (Object[]) input;
            snapshotId = (Long) args[0];
            country = (String) args[1];
        } else if (input instanceof Long) {
            snapshotId = (Long) input;
            country = ALL_COUNTRIES;
        } else {
            throw new IllegalArgumentException("Incorrect 'input' argument type!");
        }
        
        List<CurrencySum> currencySums = null;
        Long startingSnapshot = null;
        Long actualSnapshot = null;
        
        rwLock.readLock().lock();
lock:   try {
            DataBag data = countryToDataMap.get(country);
            if (data == null) {
                break lock;
            }
            
            Triple<List<CurrencySum>, Long, Long> sumsData =
                    data.createCurrencySumsData(snapshotId);
            startingSnapshot = sumsData.getSecond();
            actualSnapshot = sumsData.getThird();
            currencySums = sumsData.getFirst();
            
        } finally {
            rwLock.readLock().unlock();
        }
        
        CumulatingMessageProcessingBoard board = new CumulatingMessageProcessingBoard();
        board.startingSnapshot = (startingSnapshot != null) ? startingSnapshot : snapshotId;
        board.snapshot = (actualSnapshot != null) ? actualSnapshot : snapshotId;;
        board.country = country;
        board.currencySums = (currencySums != null) ? currencySums : Collections.<CurrencySum>emptyList();
        
        return board;
    }

    @Override
    public Countries getCountries() {
        Countries countries = new Countries();
        
        rwLock.readLock().lock();
        try {
            countries.items = new ArrayList(countryToDataMap.keySet());
        } finally {
            rwLock.readLock().unlock();
        }
        
        return countries;
    }

    private static class DataBag {
//        public volatile long snapShotId;
        
        private final Map<String, Double> currencyToSumAmountMap = new TreeMap<>();
        private final Map<String, Long> currencyToSnapshotId = new HashMap<>();
        
        boolean add(String currency, double amount, long snapshotId) {
            if (! checkAndSetSnapshotId(currency, snapshotId)) {
                return false;
            }
            Double sumAmount = currencyToSumAmountMap.get(currency);
            if (sumAmount == null) {
                currencyToSumAmountMap.put(currency, amount);
                return true;
            }
            sumAmount += amount;
            currencyToSumAmountMap.put(currency, sumAmount);
            
            return true;
        }
        
        boolean subtract(String currency, double amount, long snapshotId) {
            if (! checkAndSetSnapshotId(currency, snapshotId)) {
                return false;
            }
            Double sumAmount = currencyToSumAmountMap.get(currency);
            if (sumAmount == null) {
                currencyToSumAmountMap.put(currency, -amount);
                return true;
            }
            sumAmount -= amount;
            currencyToSumAmountMap.put(currency, sumAmount);
            
            return true;
        }
        
        private boolean checkAndSetSnapshotId(String currency, long snapshotId) {
            Long currencySnapshot = currencyToSnapshotId.get(currency);
            if ((currencySnapshot == null) || (currencySnapshot < snapshotId)) {
                currencyToSnapshotId.put(currency, snapshotId);
                return true;
            }
            
            return false;
        }
        
        /**
         * Creates a list of {@link CurrencySum}. Only data is returned that is
         * equal or greater than {@code snapshotId}
         * @param snapshotId
         * @return  a {@link Triple} of list, minimal snapshotId and maximal snapshotId.
         */
        Triple<List<CurrencySum>, Long, Long> createCurrencySumsData(long snapshotId) {

            List<CurrencySum> currencySumResult = new ArrayList<>(currencyToSumAmountMap.size());
            
            Long minSnapshotId = null;
            Long maxSnapshotId = null;
            
            for (Map.Entry<String, Double> currencySumAmount : currencyToSumAmountMap.entrySet()) {
                String currency = currencySumAmount.getKey();
                Long currencySnapshotId = currencyToSnapshotId.get(currency);
                if (currencySnapshotId < snapshotId) {
                    continue;
                }
                
                if ((minSnapshotId == null) || (currencySnapshotId < minSnapshotId)) {
                    minSnapshotId = currencySnapshotId;
                }
                if ((maxSnapshotId == null) || (currencySnapshotId > maxSnapshotId)) {
                    maxSnapshotId = currencySnapshotId;
                }
                
                CurrencySum result = new CurrencySum();
                result.currency = currencySumAmount.getKey();
                result.sum = currencySumAmount.getValue();
                currencySumResult.add(result);
            }
            
            return Triple.create(currencySumResult, minSnapshotId, maxSnapshotId);
        }
   }
    
}
