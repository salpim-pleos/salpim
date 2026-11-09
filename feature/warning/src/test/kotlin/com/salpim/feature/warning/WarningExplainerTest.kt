package com.salpim.feature.warning

import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import com.salpim.core.model.WarningCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WarningExplainerTest {
    // Gradle 단위 테스트의 작업 디렉터리는 모듈 루트
    private val explainer = WarningExplainer.fromJson(
        File("src/main/assets/${WarningExplainer.ASSET_NAME}").readText(Charsets.UTF_8),
    )

    @Test
    fun everyKnowledgeBaseEntryIsComplete() {
        assertTrue(explainer.knownCodes.isNotEmpty())
        for (code in explainer.knownCodes) {
            val e = explainer.explain(code)
            assertTrue("$code known", e.known)
            assertTrue("$code title", e.title.isNotBlank())
            assertTrue("$code oneLine", e.oneLine.isNotBlank())
            assertTrue("$code detail", e.detail.isNotBlank())
            assertTrue("$code actions", e.actions.isNotEmpty() && e.actions.all { it.isNotBlank() })
            assertTrue("$code oneLine too long for driving", e.oneLine.length <= 40)
        }
    }

    @Test
    fun allAppWarningCodesAreCovered() {
        val declared = listOf(
            WarningCodes.TIRE_PRESSURE_LOW,
            WarningCodes.BRAKE_SYSTEM,
            WarningCodes.ENGINE_OIL_PRESSURE,
            WarningCodes.COOLANT_TEMP_HIGH,
            WarningCodes.CHARGING_SYSTEM,
            WarningCodes.AIRBAG,
            WarningCodes.LOW_FUEL,
            WarningCodes.EV_BATTERY_LOW,
        )
        assertEquals(declared.toSet(), explainer.knownCodes)
    }

    @Test
    fun noAssertiveDiagnosisWording() {
        val banned = listOf("고장입니다", "진단 결과", "고장 판정", "확실히")
        for (code in explainer.knownCodes) {
            val e = explainer.explain(code)
            val text = (listOf(e.title, e.oneLine, e.detail) + e.causes + e.actions).joinToString(" ")
            banned.forEach { assertFalse("$code contains '$it'", it in text) }
        }
    }

    @Test
    fun unknownCodeFallsBackSafely() {
        val e = explainer.explain(WarningCode("SOMETHING_NEW"))
        assertFalse(e.known)
        assertEquals(Urgency.SERVICE_SOON, e.urgency)
        assertTrue(e.needsService)
        assertTrue(e.oneLine.contains("점검"))
    }

    @Test
    fun explainAllSortsByUrgency() {
        val list = explainer.explainAll(
            listOf(WarningCodes.LOW_FUEL, WarningCodes.BRAKE_SYSTEM, WarningCodes.TIRE_PRESSURE_LOW),
        )
        assertEquals(listOf(Urgency.IMMEDIATE_STOP, Urgency.SERVICE_SOON, Urgency.INFO), list.map { it.urgency })
    }
}
