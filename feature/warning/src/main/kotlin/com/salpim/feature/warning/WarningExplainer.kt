package com.salpim.feature.warning

import android.content.Context
import com.salpim.core.model.Explanation
import com.salpim.core.model.Urgency
import com.salpim.core.model.WarningCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class KnowledgeBaseFile(val version: Int, val entries: List<KbEntry>)

@Serializable
internal data class KbEntry(
    val code: String,
    val title: String,
    val oneLine: String,
    val detail: String,
    val causes: List<String>,
    val actions: List<String>,
    val urgency: Urgency,
    val needsService: Boolean,
)

/**
 * 경고 코드 → 쉬운 설명. 지식 베이스(JSON asset)만으로 오프라인 동작한다. LLM/네트워크 사용 안 함.
 */
class WarningExplainer internal constructor(private val entries: Map<String, KbEntry>) {
    val knownCodes: Set<WarningCode> get() = entries.keys.mapTo(mutableSetOf()) { WarningCode(it) }

    fun explain(code: WarningCode): Explanation {
        val e = entries[code.value] ?: return fallback(code)
        return Explanation(
            code = code,
            title = e.title,
            oneLine = e.oneLine,
            detail = e.detail,
            causes = e.causes,
            actions = e.actions,
            urgency = e.urgency,
            needsService = e.needsService,
            known = true,
        )
    }

    /** 긴급도 높은 순으로 정렬해 설명 */
    fun explainAll(codes: Collection<WarningCode>): List<Explanation> =
        codes.map(::explain).sortedBy { it.urgency.ordinal }

    private fun fallback(code: WarningCode) = Explanation(
        code = code,
        title = "알 수 없는 경고",
        oneLine = "확인되지 않은 경고예요. 점검을 권장해요",
        detail = "앱이 아직 설명을 갖고 있지 않은 경고 신호예요 (코드: ${code.value}). 차량 매뉴얼을 확인하거나 정비소 점검을 권장해요.",
        causes = emptyList(),
        actions = listOf("정차 후 차량 매뉴얼에서 경고등 의미를 확인하세요", "가까운 시일 내 정비소 점검을 권장해요"),
        urgency = Urgency.SERVICE_SOON,
        needsService = true,
        known = false,
    )

    companion object {
        const val ASSET_NAME = "warnings_kb.json"
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(text: String): WarningExplainer {
            val file = json.decodeFromString(KnowledgeBaseFile.serializer(), text)
            return WarningExplainer(file.entries.associateBy { it.code })
        }

        fun fromAssets(context: Context): WarningExplainer =
            context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { fromJson(it.readText()) }
    }
}
