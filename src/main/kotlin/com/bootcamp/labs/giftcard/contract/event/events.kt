package com.bootcamp.labs.giftcard.contract.event

data class CardIssuedEvent(var id: String, var amount: Int) {
}

data class CardRedeemedEvent(var id: String, var transactionId: String, var amount: Int) {
}

data class CardReimbursedEvent(var id: String, var transactionId: String, var amount: Int)  {
}