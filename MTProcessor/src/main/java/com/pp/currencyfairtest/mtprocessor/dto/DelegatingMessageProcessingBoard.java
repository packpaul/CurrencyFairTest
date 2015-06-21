package com.pp.currencyfairtest.mtprocessor.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="delegatingMPBoard")
public class DelegatingMessageProcessingBoard extends ProcessingBoard {
    public List<Message> messages;
}