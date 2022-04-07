package com.bootcamp.labs.giftcard.domain

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

@Entity
public class CardSummary {
    constructor(){}
    constructor(
        cardId: String,
        initialValue: Int,
        issuedAt: Instant
    ) {
        this.cardId = cardId
        this.initialValue = initialValue
        this.remainingValue = initialValue
        this.issuedAt = issuedAt
    }

    @Id
    lateinit var cardId: String
    var initialValue = 0
    lateinit var issuedAt: Instant
    var remainingValue = 0
}
