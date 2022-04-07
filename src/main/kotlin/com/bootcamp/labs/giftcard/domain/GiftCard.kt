package com.bootcamp.labs.giftcard.domain

import com.bootcamp.labs.giftcard.contract.command.IssueCardCommand
import com.bootcamp.labs.giftcard.contract.command.RedeemCardCommand
import com.bootcamp.labs.giftcard.contract.command.ReimburseCardCommand
import com.bootcamp.labs.giftcard.contract.event.CardIssuedEvent
import com.bootcamp.labs.giftcard.contract.event.CardRedeemedEvent
import com.bootcamp.labs.giftcard.contract.event.CardReimbursedEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class GiftCard {

    constructor(){}

    @AggregateIdentifier
    private var id: String = ""
    internal var amountOnCard = 0

    @AggregateMember
    private var transactions: GiftCardTransactions = GiftCardTransactions()

    @CommandHandler
    fun handle(issueCardCommand: IssueCardCommand){
        AggregateLifecycle.apply(CardIssuedEvent(issueCardCommand.id, issueCardCommand.amount))
    }

    @CommandHandler
    fun handle(redeemCardCommand: RedeemCardCommand) {
        require(redeemCardCommand.amount > 0) { "amount <= 0" }
        check(redeemCardCommand.amount <= amountOnCard) { "amount > remaining value" }
        check(redeemCardCommand.transactionId !in transactions.redeems) { "transaction id was used before" }
        AggregateLifecycle.apply(CardRedeemedEvent(id, redeemCardCommand.transactionId, redeemCardCommand.amount))
    }

    @CommandHandler
    fun handle(reimburseCardCommand: ReimburseCardCommand) {
        check(reimburseCardCommand.transactionId in transactions.redeems) {"transaction not found"}
        val reimbursingTransaction: CardRedeemedEvent = transactions.redeems[reimburseCardCommand.transactionId]!!
        check(reimburseCardCommand.transactionId !in transactions.reimburses) { "transaction was reimbursed before" }
        AggregateLifecycle.apply(CardReimbursedEvent(id, reimbursingTransaction.transactionId, reimbursingTransaction.amount))
    }


    @EventSourcingHandler
    fun on(cardIssuedEvent: CardIssuedEvent) {
        id = cardIssuedEvent.id
        amountOnCard = cardIssuedEvent.amount
    }


    @EventSourcingHandler
    fun on(cardRedeemedEvent: CardRedeemedEvent) {
        transactions.redeems[cardRedeemedEvent.transactionId] = cardRedeemedEvent
        amountOnCard -= cardRedeemedEvent.amount
    }

    @EventSourcingHandler
    fun on(cardReimbursedEvent: CardReimbursedEvent) {
        transactions.reimburses[cardReimbursedEvent.transactionId] = cardReimbursedEvent
        amountOnCard += cardReimbursedEvent.amount
    }
}