/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="cumulatingMPBoard")
public class CumulatingMessageProcessingBoard extends ProcessingBoard {
    public String country;
    public List<CurrencySum> currencySums;
    
    @XmlRootElement(name="currencySum")
    public static class CurrencySum {
        public String currency;
        public Double sum;
    }
}