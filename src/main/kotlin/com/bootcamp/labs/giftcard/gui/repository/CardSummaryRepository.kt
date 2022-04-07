package com.bootcamp.labs.giftcard.gui.repository

import com.bootcamp.labs.giftcard.domain.CardSummary
import org.springframework.data.jpa.repository.JpaRepository


interface CardSummaryRepository : JpaRepository<CardSummary?, String?>