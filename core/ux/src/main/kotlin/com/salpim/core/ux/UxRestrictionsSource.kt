package com.salpim.core.ux

import android.content.Context
import android.content.pm.PackageManager
import com.salpim.core.model.UxRestrictionFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 시스템 주행 제한의 진입점. 앱 코드는 [UxRestrictionFlags]만 본다. */
interface UxRestrictionsSource {
    val flags: StateFlow<UxRestrictionFlags>
    fun start()
    fun stop()

    companion object {
        /** 자동차 기기면 CarUxRestrictionsManager, 아니면(폰 에뮬레이터 e2e 등) 제한 없음 */
        fun create(context: Context): UxRestrictionsSource =
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
                CarUxRestrictionsSource(context.applicationContext)
            } else {
                StaticUxRestrictionsSource()
            }
    }
}

/** 고정값. 비자동차 기기와 테스트용. */
class StaticUxRestrictionsSource(initial: UxRestrictionFlags = UxRestrictionFlags.NONE) : UxRestrictionsSource {
    private val _flags = MutableStateFlow(initial)
    override val flags: StateFlow<UxRestrictionFlags> = _flags.asStateFlow()
    fun set(value: UxRestrictionFlags) {
        _flags.value = value
    }
    override fun start() = Unit
    override fun stop() = Unit
}
