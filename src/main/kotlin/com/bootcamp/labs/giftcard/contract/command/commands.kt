package com.bootcamp.labs.giftcard.contract.command

import org.axonframework.commandhandling.RoutingKey
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class IssueCardCommand(@RoutingKey var id: String, var amount: Int) {

}

data class RedeemCardCommand(@TargetAggregateIdentifier var id: String, var transactionId: String, var amount: Int) {

}

data class ReimburseCardCommand(@TargetAggregateIdentifier var id: String, var transactionId: String) {

}

