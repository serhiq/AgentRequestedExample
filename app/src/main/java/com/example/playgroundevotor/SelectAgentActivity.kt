package com.example.playgroundevotor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button

import ru.evotor.framework.core.IntegrationAppCompatActivity
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEvent
import ru.evotor.framework.core.action.event.receipt.before_positions_edited.BeforePositionsEditedEventResult
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionAdd
import ru.evotor.framework.core.action.event.receipt.changes.position.PositionEdit
import ru.evotor.framework.receipt.Position
import ru.evotor.framework.receipt.position.AgentRequisites
import kotlin.collections.ArrayList


class SelectAgentActivity : IntegrationAppCompatActivity()  {
    private val positionUuid
        get() = intent.extras?.getString(ARG_POSITION_UUID, null) ?: throw RuntimeException("Не указан идентификатор позиции.")


    private val sourceEvent
        get() = BeforePositionsEditedEvent.create(sourceBundle) ?: throw java.lang.RuntimeException("Отсутствует исходное событие BeforePositionsEditedEvent.")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_agent)

        findViewById<Button>(R.id.cancel_button).setOnClickListener {
            setIntegrationResult(BeforePositionsEditedEventResult(sourceEvent.changes, null, null))
            finish()
        }

        findViewById<Button>(R.id.apply_btn).setOnClickListener {    applyAgent() }

        setFinishOnTouchOutside(false)
    }

    private fun applyAgent() {
        val agent = AgentRequisites.createForAgent(principalInn = "7707083893", principalPhones = listOf("+77777777514"), principalName = "АО СЛВ")

        val sourcePosition = sourceEvent.changes
            .filterIsInstance(PositionAdd::class.java)
            .last { it.position.uuid == positionUuid }
            .position


        val resultPosition =  Position.Builder.copyFrom(sourcePosition)
            .setAgentRequisites(agent)
            .build()


        val changes = ArrayList(sourceEvent.changes).apply {
            add(PositionEdit(resultPosition))
        }
        setIntegrationResult(BeforePositionsEditedEventResult(changes, null, null))
        finish()

    }

    companion object {
        private const val ARG_POSITION_UUID = "ARG_POSITION_UUID"

        fun start(context: Context, positionUuid: String) : Intent {
            return Intent(context, SelectAgentActivity::class.java).apply {
                putExtra(ARG_POSITION_UUID, positionUuid)
            }
        }
    }
}