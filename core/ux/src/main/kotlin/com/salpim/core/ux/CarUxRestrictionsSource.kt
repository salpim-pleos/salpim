package com.salpim.core.ux

import android.car.Car
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.content.Context
import android.util.Log
import com.salpim.core.model.UxRestrictionFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AAOS 표준 CarUxRestrictionsManager 래퍼 (CLAUDE.md: onUxRestrictionsChanged/ActiveRestrictions 확인됨).
 * 연결 실패 시 안전 쪽(DO 필요)으로 둔다.
 */
internal class CarUxRestrictionsSource(private val context: Context) : UxRestrictionsSource {
    private val _flags = MutableStateFlow(UxRestrictionFlags.NONE)
    override val flags: StateFlow<UxRestrictionFlags> = _flags.asStateFlow()

    private var car: Car? = null
    private var manager: CarUxRestrictionsManager? = null

    private val listener = CarUxRestrictionsManager.OnUxRestrictionsChangedListener { r ->
        _flags.value = r.toFlags()
    }

    override fun start() {
        if (car != null) return
        try {
            val c = Car.createCar(context)
            val m = c.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE) as CarUxRestrictionsManager
            m.registerListener(listener)
            m.currentCarUxRestrictions?.let { _flags.value = it.toFlags() }
            car = c
            manager = m
        } catch (e: RuntimeException) {
            Log.w(TAG, "CarUxRestrictionsManager unavailable; applying restricted policy", e)
            _flags.value = UxRestrictionFlags(requiresDistractionOptimization = true)
        }
    }

    override fun stop() {
        manager?.unregisterListener()
        car?.disconnect()
        manager = null
        car = null
    }

    private fun CarUxRestrictions.toFlags(): UxRestrictionFlags {
        val active = activeRestrictions
        fun has(flag: Int) = active and flag != 0
        return UxRestrictionFlags(
            requiresDistractionOptimization = isRequiresDistractionOptimization,
            noKeyboard = has(CarUxRestrictions.UX_RESTRICTIONS_NO_KEYBOARD),
            noVideo = has(CarUxRestrictions.UX_RESTRICTIONS_NO_VIDEO),
            limitStringLength = has(CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH),
            limitContent = has(CarUxRestrictions.UX_RESTRICTIONS_LIMIT_CONTENT),
            maxStringLength = if (has(CarUxRestrictions.UX_RESTRICTIONS_LIMIT_STRING_LENGTH)) maxRestrictedStringLength else null,
        )
    }

    private companion object {
        const val TAG = "SalpimUx"
    }
}
