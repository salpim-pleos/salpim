package com.salpim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salpim.app.ui.SalpimRoot
import com.salpim.app.ui.SalpimTheme
import com.salpim.feature.service.PlaceCategory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SalpimApp).container
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = container.engine.start()
            override fun onStop(owner: LifecycleOwner) = container.engine.stop()
        })
        setContent {
            SalpimTheme {
                val vm: MainViewModel = viewModel(factory = MainViewModel.Factory(container))
                val ui by vm.ui.collectAsStateWithLifecycle()
                SalpimRoot(
                    ui = ui,
                    onSelectWarning = vm::selectWarning,
                    onFindServiceCenters = { vm.findNearby(PlaceCategory.BLUE_HANDS) },
                    onRouteTo = vm::routeTo,
                    onPlayScenario = vm::playScenario,
                    onClearHistory = vm::clearHistory,
                )
            }
        }
    }
}
