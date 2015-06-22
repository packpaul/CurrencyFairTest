/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.api;

import com.pp.currencyfairtest.mtprocessor.dto.Message;

public interface MessageConsumable {
    
    void post(Message message);
    
}
