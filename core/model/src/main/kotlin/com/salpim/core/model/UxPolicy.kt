package com.salpim.core.model

/**
 * 시스템 주행 제한(CarUxRestrictions)을 앱 내부 표현으로 옮긴 값.
 * Android 타입에 의존하지 않도록 core/ux 에서 변환해 넘긴다.
 */
data class UxRestrictionFlags(
    val requiresDistractionOptimization: Boolean = false,
    val noKeyboard: Boolean = false,
    val noVideo: Boolean = false,
    val limitStringLength: Boolean = false,
    val limitContent: Boolean = false,
    val maxStringLength: Int? = null,
) {
    companion object {
        val NONE = UxRestrictionFlags()
    }
}

/** 화면이 렌더링 방식을 결정할 때 보는 정책. */
data class UxPolicy(
    val detailAllowed: Boolean,
    val textInputAllowed: Boolean,
    val scrollingAllowed: Boolean,
    val videoAllowed: Boolean,
    /** null이면 제한 없음 */
    val maxStringLength: Int?,
    val maxGlanceItems: Int,
) {
    val restricted: Boolean get() = !detailAllowed

    companion object {
        const val DRIVING_MAX_ITEMS = 3

        /** 시스템 기본값을 모를 때 쓰는 보수적 한 줄 길이 [설계] */
        const val DEFAULT_MAX_STRING = 40

        /**
         * DRIVING 이거나 시스템 제한이 활성이면 제한 정책.
         * 시스템 제한이 우선이며, 앱이 DO를 선언해도 직접 제한한다 (안전/UX 규칙 2).
         */
        fun resolve(state: DrivingState, flags: UxRestrictionFlags): UxPolicy {
            val restricted = state == DrivingState.DRIVING || flags.requiresDistractionOptimization
            if (!restricted) {
                return UxPolicy(
                    detailAllowed = true,
                    textInputAllowed = !flags.noKeyboard,
                    scrollingAllowed = !flags.limitContent,
                    videoAllowed = !flags.noVideo,
                    maxStringLength = if (flags.limitStringLength) flags.maxStringLength else null,
                    maxGlanceItems = Int.MAX_VALUE,
                )
            }
            return UxPolicy(
                detailAllowed = false,
                textInputAllowed = false,
                scrollingAllowed = false,
                videoAllowed = false,
                maxStringLength = flags.maxStringLength?.coerceAtMost(DEFAULT_MAX_STRING) ?: DEFAULT_MAX_STRING,
                maxGlanceItems = DRIVING_MAX_ITEMS,
            )
        }
    }
}

fun String.ellipsize(max: Int?): String =
    if (max == null || length <= max) this else take((max - 1).coerceAtLeast(0)) + "…"
