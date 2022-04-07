package com.bootcamp.labs.giftcard

import com.bootcamp.labs.giftcard.contract.command.IssueCardCommand
import com.bootcamp.labs.giftcard.contract.command.RedeemCardCommand
import com.bootcamp.labs.giftcard.contract.command.ReimburseCardCommand
import com.bootcamp.labs.giftcard.contract.event.CardIssuedEvent
import com.bootcamp.labs.giftcard.contract.event.CardRedeemedEvent
import com.bootcamp.labs.giftcard.contract.event.CardReimbursedEvent
import com.bootcamp.labs.giftcard.domain.GiftCard
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.axonframework.test.aggregate.ResultValidator
import org.axonframework.test.aggregate.TestExecutor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GiftCardTest {

    private val CARD_ID = "cardId"
    private val TRANSACTION_ID = "transactionId"

    private lateinit var giftCard: FixtureConfiguration<GiftCard>

    @BeforeEach
    fun setUp() {
        giftCard = AggregateTestFixture(GiftCard::class.java)
    }

    @Test
    fun shouldIssueGiftCard() {
        giftCard.GIVEN(CardIssuedEvent(CARD_ID, 500))
            .WHEN(RedeemCardCommand(CARD_ID, TRANSACTION_ID, 100))
            .expectSuccessfulHandlerExecution()
            .expectEvents(CardRedeemedEvent(CARD_ID, TRANSACTION_ID, 100))
    }

    @Test
    fun shouldRedeemGiftCard() {
        giftCard.GIVEN(CardIssuedEvent(CARD_ID, 500))
            .WHEN(RedeemCardCommand(CARD_ID, TRANSACTION_ID, 100))
            .expectSuccessfulHandlerExecution()
            .expectState { state -> Assertions.assertEquals(state.amountOnCard, 400) }
    }

    @Test
    fun shouldNotRedeemWithNegativeAmount() {
        giftCard.GIVEN(CardIssuedEvent(CARD_ID, 500))
            .WHEN(RedeemCardCommand(CARD_ID, TRANSACTION_ID, -100))
            .expectExceptionMessage("amount <= 0")
    }

    @Test
    fun shouldNotRedeemWithRepetitiveTransactionId() {
        giftCard.givenNoPriorActivity().andGivenCommands(
            IssueCardCommand(CARD_ID, 500),
            RedeemCardCommand(CARD_ID, TRANSACTION_ID, 100)
        )
            .WHEN(RedeemCardCommand(CARD_ID, TRANSACTION_ID, 100))
            .expectException(IllegalStateException::class.java)
            .expectExceptionMessage("transaction id was used before")
    }

    @Test
    fun shouldNotRedeemWhenThereIsNotEnoughMoney() {
        giftCard.given(CardIssuedEvent(CARD_ID, 500))
            .WHEN(RedeemCardCommand(CARD_ID, TRANSACTION_ID, 600))
            .expectExceptionMessage("amount > remaining value")
    }

    @Test
    fun shouldReimburseGiftCard() {
        giftCard.GIVEN( CardIssuedEvent(CARD_ID, 500)
            ,CardRedeemedEvent(CARD_ID, TRANSACTION_ID, 100)
        )
            .WHEN(ReimburseCardCommand(CARD_ID, TRANSACTION_ID))
            .expectSuccessfulHandlerExecution()
            .expectState { state -> Assertions.assertEquals(state.amountOnCard, 500) }
    }

    @Test
    fun shouldNotReimburseWhenRedeemNotFound() {
        giftCard.GIVEN(
            CardIssuedEvent(CARD_ID, 500),
            CardRedeemedEvent(CARD_ID, TRANSACTION_ID, 100)
        )
            .WHEN(ReimburseCardCommand(CARD_ID, "wrongTransactionId"))
            .expectException(IllegalArgumentException::class.java)
            .expectExceptionMessage("transaction not found")
    }

    @Test
    fun shouldNotReimburseWhenCalledTwice() {
        giftCard.GIVEN(
            CardIssuedEvent(CARD_ID, 500),
            CardRedeemedEvent(CARD_ID, TRANSACTION_ID, 100),
            CardReimbursedEvent(CARD_ID, TRANSACTION_ID,100)
        )
            .WHEN(ReimburseCardCommand(CARD_ID, TRANSACTION_ID))
            .expectException(IllegalStateException::class.java)
            .expectExceptionMessage("transaction was reimbursed before")
    }


    fun <T> FixtureConfiguration<T>.GIVEN(vararg event: Any): TestExecutor<T> = this.given(*event)
    fun <T> TestExecutor<T>.WHEN(command: Any): ResultValidator<T> = this.`when`(command)
}