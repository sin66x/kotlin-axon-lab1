package com.bootcamp.labs.giftcard

import com.bootcamp.labs.giftcard.contract.event.CardIssuedEvent
import com.bootcamp.labs.giftcard.contract.event.CardRedeemedEvent
import com.bootcamp.labs.giftcard.contract.event.CardReimbursedEvent
import com.bootcamp.labs.giftcard.contract.query.CountCardSummariesQuery
import com.bootcamp.labs.giftcard.contract.query.FindCardSummariesQuery
import com.bootcamp.labs.giftcard.domain.CardSummary
import com.bootcamp.labs.giftcard.gui.repository.CardSummaryProjection
import com.bootcamp.labs.giftcard.gui.repository.CardSummaryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.Instant
import java.util.*


@DataJpaTest
class GiftCardJpaTests {

    private val CARD_ID = UUID.randomUUID().toString()
    private val TRANSACTION_ID = UUID.randomUUID().toString()

    private val INIT_AMOUNT = 500
    private val REDEEM_AMOUNT = 100


    @Autowired
    private lateinit var cardSummaryRepository: CardSummaryRepository
    private lateinit var cardSummaryProjection: CardSummaryProjection

    @BeforeEach
    fun setup() {
        cardSummaryProjection = CardSummaryProjection(cardSummaryRepository)
    }

    @Test
    fun shouldIssueCard() {
        cardSummaryProjection.on(CardIssuedEvent(CARD_ID, INIT_AMOUNT), Instant.now())
        val cardSummary = cardSummaryRepository.findById(CARD_ID)
        Assertions.assertTrue(cardSummary.isPresent)
    }

    @Test
    fun shouldRedeemCard() {
        cardSummaryProjection.on(CardIssuedEvent(CARD_ID, INIT_AMOUNT), Instant.now())
        val cardSummary1 = cardSummaryRepository.findById(CARD_ID)
        cardSummaryProjection.on(CardRedeemedEvent(CARD_ID, TRANSACTION_ID, REDEEM_AMOUNT))
        val cardSummary = cardSummaryRepository.findById(CARD_ID)
        Assertions.assertEquals(cardSummary.get().remainingValue, INIT_AMOUNT - REDEEM_AMOUNT)
    }

    @Test
    fun shouldReimburse() {
        cardSummaryProjection.on(CardIssuedEvent(CARD_ID, INIT_AMOUNT), Instant.now())
        cardSummaryProjection.on(CardRedeemedEvent(CARD_ID, TRANSACTION_ID, REDEEM_AMOUNT))
        cardSummaryProjection.on(CardReimbursedEvent(CARD_ID, TRANSACTION_ID, REDEEM_AMOUNT))
        val cardSummary = cardSummaryRepository.findById(CARD_ID)
        Assertions.assertEquals(cardSummary.get().remainingValue, INIT_AMOUNT)
    }

    @Test
    fun shouldListCards() {
        cardSummaryProjection.on(CardIssuedEvent(CARD_ID, INIT_AMOUNT), Instant.now())
        val cardSummaries: MutableList<CardSummary?>? = cardSummaryProjection.handle(FindCardSummariesQuery(0, 100))
        cardSummaries!!
    }

    @Test
    fun shouldCountCards() {
        cardSummaryProjection.on(CardIssuedEvent(CARD_ID, INIT_AMOUNT), Instant.now())
        val count: Long = cardSummaryProjection.handle(CountCardSummariesQuery())!!
        Assertions.assertTrue(count > 0)
    }

}