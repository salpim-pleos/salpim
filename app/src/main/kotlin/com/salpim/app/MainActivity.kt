package com.salpim.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salpim.app.ui.MainActions
import com.salpim.app.ui.SalpimRoot
import com.salpim.core.designsystem.SalpimTheme

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
                SalpimRoot(ui = ui, actions = rememberMicPermissionActions(vm))
            }
        }
    }

    /** 마이크를 누르면 권한부터 확인한다. 허용되면 바로 듣기 시작, 거부되면 말로 안내한다 */
    @Composable
    private fun rememberMicPermissionActions(vm: MainViewModel): MainActions {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) vm.onMic() else vm.onHint(MIC_DENIED_UTTERANCE)
        }
        return remember(vm) {
            object : MainActions by vm {
                override fun onMic() {
                    val granted = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (granted) vm.onMic() else launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private companion object {
        /** 권한이 없으면 "알아듣지 못함" 흐름으로 예시 문장을 안내한다 */
        const val MIC_DENIED_UTTERANCE = ""
    }
}
