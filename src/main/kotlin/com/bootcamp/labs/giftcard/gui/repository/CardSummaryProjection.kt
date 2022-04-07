package com.bootcamp.labs.giftcard.gui.repository

import com.bootcamp.labs.giftcard.contract.event.CardIssuedEvent
import com.bootcamp.labs.giftcard.contract.event.CardRedeemedEvent
import com.bootcamp.labs.giftcard.contract.event.CardReimbursedEvent
import com.bootcamp.labs.giftcard.contract.query.CountCardSummariesQuery
import com.bootcamp.labs.giftcard.contract.query.FindCardSummariesQuery
import com.bootcamp.labs.giftcard.domain.CardSummary
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.axonframework.queryhandling.QueryHandler
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.stream.Collectors

@Component
class CardSummaryProjection (val cardSummaryRepository: CardSummaryRepository){

    @EventHandler
    fun on(event: CardIssuedEvent, @Timestamp timestamp: Instant?) {
        cardSummaryRepository!!.save(CardSummary(event.id, event.amount, timestamp!!))
    }

    @EventHandler
    fun on(event: CardRedeemedEvent) {
        val cardSummary = cardSummaryRepository!!.findById(event.id)
        cardSummary.get().remainingValue = cardSummary.get().remainingValue - event.amount
        cardSummaryRepository!!.save(cardSummary.get())
    }

    @EventHandler
    fun on(event: CardReimbursedEvent) {
        val cardSummary = cardSummaryRepository!!.findById(event.id)
        cardSummary.get().remainingValue = cardSummary.get().remainingValue + event.amount
        cardSummaryRepository!!.save(cardSummary.get())
    }

    @QueryHandler
    fun handle(query: FindCardSummariesQuery): MutableList<CardSummary?>? {
        return cardSummaryRepository.findAll(PageRequest.of(query.offset, query.limit)).stream()
            .collect(Collectors.toList())
    }

    @QueryHandler
    fun handle(query: CountCardSummariesQuery): Long? {
        return cardSummaryRepository.count()
    }
}