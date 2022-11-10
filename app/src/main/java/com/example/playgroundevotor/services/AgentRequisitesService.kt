package com.example.playgroundevotor.services

import com.example.playgroundevotor.SelectAgentActivity
import ru.evotor.framework.core.IntegrationService
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEvent
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEventProcessor
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.core.action.processor.ActionProcessor
import ru.evotor.framework.receipt.ReceiptApi

class AgentRequisitesService : IntegrationService() {

    override fun createProcessors(): MutableMap<String, ActionProcessor> {
        val map = mutableMapOf<String, ActionProcessor>()
        map[BeforePositionsEditedEvent.NAME_SELL_RECEIPT] = eventProcessor
        map[BeforePositionsEditedEvent.NAME_PAYBACK_RECEIPT] = eventProcessor
        map[BeforePositionsEditedEvent.NAME_BUY_RECEIPT] = eventProcessor
        map[BeforePositionsEditedEvent.NAME_BUYBACK_RECEIPT] = eventProcessor
        return map
    }

    private val eventProcessor = object : BeforePositionsEditedEventProcessor() {

        override fun call(action: String, event: BeforePositionsEditedEvent, callback: Callback) {

            try {
                val receipt = ReceiptApi.getReceipt(this@AgentRequisitesService, event.receiptUuid) ?: throw RuntimeException("Чек с идентификатором ${event.receiptUuid} не найден.")
                val changes = event.changes.filter {
                    it is PositionAdd // применять реквизиты можно только на добавленные позиции
                            && it.position.agentRequisites == null // если на них еще не применены реквизиты
                }

                if (changes.isEmpty()) {
                    callback.skip()
                    return
                }

                if (changes.size > 1) {
                    callback.skip()
                    return
                }

                val position = (changes.single() as PositionAdd).position


                callback.startActivity(
                    SelectAgentActivity.start(this@AgentRequisitesService, position.uuid))

            } catch (e : Exception) {
                callback.skip()
            }
        }
    }
}

