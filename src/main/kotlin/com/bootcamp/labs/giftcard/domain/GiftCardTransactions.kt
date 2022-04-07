package com.bootcamp.labs.giftcard.domain

import com.bootcamp.labs.giftcard.contract.event.CardRedeemedEvent
import com.bootcamp.labs.giftcard.contract.event.CardReimbursedEvent

data class GiftCardTransactions(var redeems: HashMap<String,CardRedeemedEvent>, var reimburses: HashMap<String,CardReimbursedEvent>) {
    constructor() : this(HashMap(),HashMap()) {
    }
}