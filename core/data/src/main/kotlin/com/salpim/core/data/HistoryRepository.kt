package com.salpim.core.data

import com.salpim.core.model.DrivingEvent
import com.salpim.core.model.WarningCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WarningRecord(val code: WarningCode, val firstSeenMs: Long)

/** 경고 이력과 주행 이벤트. 사용자가 앱 안에서 전부 삭제할 수 있어야 한다. */
interface HistoryRepository {
    val warnings: StateFlow<List<WarningRecord>>
    val events: StateFlow<List<DrivingEvent>>
    fun recordWarning(code: WarningCode, atMs: Long)
    fun recordEvent(event: DrivingEvent)
    fun clearAll()
}

class InMemoryHistoryRepository(private val maxItems: Int = 500) : HistoryRepository {
    private val _warnings = MutableStateFlow<List<WarningRecord>>(emptyList())
    private val _events = MutableStateFlow<List<DrivingEvent>>(emptyList())
    override val warnings: StateFlow<List<WarningRecord>> = _warnings.asStateFlow()
    override val events: StateFlow<List<DrivingEvent>> = _events.asStateFlow()

    override fun recordWarning(code: WarningCode, atMs: Long) {
        _warnings.update { list ->
            if (list.any { it.code == code }) list else (list + WarningRecord(code, atMs)).takeLast(maxItems)
        }
    }

    override fun recordEvent(event: DrivingEvent) {
        _events.update { (it + event).takeLast(maxItems) }
    }

    override fun clearAll() {
        _warnings.value = emptyList()
        _events.value = emptyList()
    }
}
